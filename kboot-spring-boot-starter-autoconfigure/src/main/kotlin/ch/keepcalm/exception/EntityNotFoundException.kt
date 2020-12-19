package ch.keepcalm.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class EntityNotFoundException(message: String, val clazz: Class<*>, val id: Any? = null, cause: Throwable? = null) :
        RuntimeException(message, cause) {

    constructor(clazz: Class<*>, id: Any?, cause: Throwable? = null) :
            this("Entity ${clazz.simpleName} with args/value: $id not found", clazz, id, cause)
}
