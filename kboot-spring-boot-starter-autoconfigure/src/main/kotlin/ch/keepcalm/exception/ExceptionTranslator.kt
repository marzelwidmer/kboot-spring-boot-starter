package ch.keepcalm.exception

import brave.Tracer
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import feign.FeignException
import feign.RetryableException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.DefaultProblem
import org.zalando.problem.Problem
import org.zalando.problem.ProblemBuilder
import org.zalando.problem.Status
import org.zalando.problem.spring.web.advice.HttpStatusAdapter
import org.zalando.problem.spring.web.advice.ProblemHandling
import org.zalando.problem.spring.web.advice.io.MessageNotReadableAdviceTrait
import org.zalando.problem.spring.web.advice.validation.ConstraintViolationProblem
import java.net.URI
import java.util.*
import javax.annotation.Nonnull
import javax.annotation.Nullable
import javax.servlet.http.HttpServletRequest

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807)
 *
 * no support for ConcurrencyFailureException -> requires transaction management
 */
@ControllerAdvice
class ExceptionTranslator(private val tracer: Tracer) : ProblemHandling, MessageNotReadableAdviceTrait {
    companion object {
        private const val PROBLEM_BASE_URL = "http://keepcalm/problem"
        const val ERR_VALIDATION = "error.validation"

        @JvmField
        val DEFAULT_TYPE: URI = URI.create("$PROBLEM_BASE_URL/problem-with-message")
        @JvmField
        val CONSTRAINT_VIOLATION_TYPE: URI = URI.create("$PROBLEM_BASE_URL/constraint-violation")
        @JvmField
        val ENTITY_NOT_FOUND_TYPE: URI = URI.create("$PROBLEM_BASE_URL/entity-not-found")
        @JvmField
        val DOWNSTREAM_SERVICE_UNAVAILABLE: URI = URI.create("$PROBLEM_BASE_URL/downstream-service-unavailable")
        @JvmField
        val DOWNSTREAM_SERVICE_ERROR: URI = URI.create("$PROBLEM_BASE_URL/downstream-service-error")
        @JvmField
        val INVALID_DATA: URI = URI.create("$PROBLEM_BASE_URL/invalid-data")

        private const val PATH = "path"
    }

    /**
     * Post-process Problem payload to add the message key for front-end if needed.
     */
    override fun process(@Nullable entity: ResponseEntity<Problem>, request: NativeWebRequest):
            ResponseEntity<Problem> {

        if (entity.body == null) {
            return entity
        }
        val problem = entity.body
        if (!(problem is ConstraintViolationProblem || problem is DefaultProblem)) {
            return entity
        }
        val type: URI = if (Problem.DEFAULT_TYPE.equals(problem.type)) DEFAULT_TYPE else problem.type

        val builder = Problem.builder().withType(type)
                .withStatus(problem.status)
                .withTitle(problem.title)
                .with(PATH, request.getNativeRequest(HttpServletRequest::class.java)!!.requestURI)
                .withTraceId()

        return if (problem is ConstraintViolationProblem) {
            createConstraintViolationProblem(builder, problem, entity)
        } else {
            createDefaultProblem(builder, problem, entity)
        }
    }

    fun createDefaultProblem(builder: ProblemBuilder, problem: Problem?, entity: ResponseEntity<Problem>):
            ResponseEntity<Problem> {
        builder
                .withCause((problem as DefaultProblem).cause)
                .withDetail(problem.getDetail())
                .withInstance(problem.getInstance())
                .withTraceId()

        problem.parameters.forEach { builder.with(it.key, it.value) }

        if (!problem.getParameters().containsKey("message") && problem.getStatus() != null) {
            builder.with("message", "error.http.${problem.status?.statusCode}")
        }
        return ResponseEntity(builder.build(), entity.headers, entity.statusCode)
    }


    fun createConstraintViolationProblem(builder: ProblemBuilder,
                                         problem: ConstraintViolationProblem,
                                         entity: ResponseEntity<Problem>): ResponseEntity<Problem> =
            builder
                    .with("violations", problem.violations)
                    .with("message", ERR_VALIDATION)
                    .withTraceId()
                    .build()
                    .run {
                        return ResponseEntity(this, entity.headers, entity.statusCode)
                    }

    override fun handleMethodArgumentNotValid(
            ex: MethodArgumentNotValidException, @Nonnull request: NativeWebRequest): ResponseEntity<Problem> =
            ex.bindingResult.fieldErrors
                    .map { FieldErrorVM(it.objectName, it.field, it.defaultMessage) }
                    .run {
                        Problem.builder()
                                .withType(CONSTRAINT_VIOLATION_TYPE)
                                .withTitle("Method argument not valid")
                                .withStatus(defaultConstraintViolationStatus())
                                .with("message", ERR_VALIDATION)
                                .with("fieldErrors", this)
                                .withTraceId()
                                .build()
                    }.run {
                        create(ex, this, request)
                    }

    @ExceptionHandler(value = [
        EntityNotFoundException::class,
        NoSuchElementException::class,
        kotlin.NoSuchElementException::class])
    fun handleNoSuchElementException(ex: Exception, request: NativeWebRequest): ResponseEntity<Problem> =
            Problem.builder()
                    .withStatus(Status.NOT_FOUND)
                    .withType(ENTITY_NOT_FOUND_TYPE)
                    .withTraceId()
                    .build()
                    .run {
                        create(ex, this, request)
                    }

    @ExceptionHandler(value = [ServiceFailedException::class])
    fun handleServiceFailedException(ex: ServiceFailedException, request: NativeWebRequest): ResponseEntity<Problem> =
            prepare(ex, Status.SERVICE_UNAVAILABLE, DOWNSTREAM_SERVICE_ERROR)
                    .withTitle("Downstream service failed")
                    .withDetail(ex.message)
                    .with("service-name", ex.serviceName)
                    .with("service-method", ex.serviceMethod)
                    .with("service-args", ex.serviceArgs)
                    .with("service-status", ex.serviceStatus)
                    .with("service-exception", ex.details)
                    .withTraceId()
                    .build()
                    .run {
                        create(ex, this, request)
                    }

    @ExceptionHandler(value = [ServiceUnavailableException::class])
    fun handleServiceUnavailableException(ex: ServiceUnavailableException,
                                          request: NativeWebRequest): ResponseEntity<Problem> =
            prepare(ex, Status.SERVICE_UNAVAILABLE, DOWNSTREAM_SERVICE_UNAVAILABLE)
                    .withTitle("Downstream service is unavailable")
                    .with("service-name", ex.serviceName)
                    .withTraceId()
                    .build()
                    .run {
                        create(ex, this, request)
                    }

    @ExceptionHandler(value = [UnexpectedServiceResponseException::class])
    fun handleUnexpectedServiceResponseException(ex: UnexpectedServiceResponseException,
                                                 request: NativeWebRequest): ResponseEntity<Problem> =
            prepare(ex, Status.SERVICE_UNAVAILABLE, DOWNSTREAM_SERVICE_ERROR)
                    .withTitle("Downstream service returned unexpected or invalid content")
                    .with("service-name", ex.serviceName)
                    .with("service-method", ex.serviceMethod)
                    .with("service-args", ex.serviceArgs)
                    .withTraceId()
                    .build()
                    .run {
                        create(ex, this, request)
                    }

    override fun handleMessageNotReadableException(
            ex: HttpMessageNotReadableException,
            request: NativeWebRequest): ResponseEntity<Problem> {
        if (ex.cause is MissingKotlinParameterException) {
            val missingKotlinParameterException = ex.cause as MissingKotlinParameterException
            return prepare(ex, Status.BAD_REQUEST, INVALID_DATA)
                    .withTitle("Invalid Data")
                    .withDetail(ex.message)
                    .with("missing-property", missingKotlinParameterException.parameter.name ?: "")
                    .withTraceId()
                    .build()
                    .run {
                        create(ex, this, request)
                    }
        }
        return super<ProblemHandling>.handleMessageNotReadableException(ex, request)
    }

    @ExceptionHandler(
            value = [InvalidDataException::class, IllegalStateException::class, IllegalArgumentException::class])
    fun handleInvalidDataException(ex: Exception,
                                   request: NativeWebRequest): ResponseEntity<Problem> =
            prepare(ex, Status.BAD_REQUEST, INVALID_DATA)
                    .withTitle("Invalid Data")
                    .withDetail(ex.message)
                    .withTraceId()
                    .build()
                    .run {
                        create(ex, this, request)
                    }

    @ExceptionHandler(value = [FeignException::class])
    fun handleFeignException(ex: FeignException, request: NativeWebRequest): ResponseEntity<Problem> =
            prepare(ex, HttpStatusAdapter(HttpStatus.resolve(ex.status()) ?: HttpStatus.INTERNAL_SERVER_ERROR),
                    DOWNSTREAM_SERVICE_ERROR
            )
                    .withTitle("Error accessing backend service")
                    .withDetail(ex.message)
                    .withTraceId()
                    .build()
                    .run {
                        create(ex, this, request)
                    }

    @ExceptionHandler(value = [RetryableException::class])
    fun handleRetryableException(ex: RetryableException, request: NativeWebRequest): ResponseEntity<Problem> =
            prepare(ex, Status.SERVICE_UNAVAILABLE, DOWNSTREAM_SERVICE_UNAVAILABLE)
                    .withTitle("Error accessing backend service")
                    .withDetail(ex.message)
                    .withTraceId()
                    .build()
                    .run {
                        create(ex, this, request)
                    }

    @ExceptionHandler(value = [RestClientResponseException::class])
    fun handleRestClientResponseException(ex: RestClientResponseException,
                                          request: NativeWebRequest): ResponseEntity<Problem> =
            prepare(ex, HttpStatusAdapter(HttpStatus.resolve(ex.rawStatusCode) ?: HttpStatus.INTERNAL_SERVER_ERROR),
                    DOWNSTREAM_SERVICE_ERROR
            )
                    .withTitle("Error accessing backend service")
                    .withDetail(ex.message)
                    .withTraceId()
                    .build()
                    .run {
                        create(ex, this, request)
                    }

    data class FieldErrorVM(val objectName: String, val field: String, val message: String?)

    private fun ProblemBuilder.withTraceId() =
            this.apply {
                tracer.currentSpan()?.apply {
                    with("trace-id", context().traceIdString())
                }
            }
}
