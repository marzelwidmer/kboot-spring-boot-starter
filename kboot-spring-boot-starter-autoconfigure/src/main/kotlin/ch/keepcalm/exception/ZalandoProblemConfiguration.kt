package ch.keepcalm.exception

import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zalando.problem.ProblemModule
import org.zalando.problem.validation.ConstraintViolationProblemModule

@Configuration
class ZalandoProblemConfiguration {

    /*
    * Jackson Afterburner module to speed up serialization/deserialization.
    */
    @Bean
    @ConditionalOnClass(AfterburnerModule::class)
    fun afterburnerModule() = AfterburnerModule()

    /*
     * Module for serialization/deserialization of RFC7807 Problem.
     */
    @Bean
    @ConditionalOnClass(ProblemModule::class)
    fun problemModule() = ProblemModule()

    /*
     * Module for serialization/deserialization of ConstraintViolationProblem.
     */
    @Bean
    @ConditionalOnClass(ConstraintViolationProblemModule::class)
    fun constraintViolationProblemModule() = ConstraintViolationProblemModule()
}
