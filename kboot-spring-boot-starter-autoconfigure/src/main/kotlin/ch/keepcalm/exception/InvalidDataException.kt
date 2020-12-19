package ch.keepcalm.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidDataException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
