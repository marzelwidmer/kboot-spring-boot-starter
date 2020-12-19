package ch.keepcalm.exception

class ServiceUnavailableException(val serviceName: String, cause: Throwable) :
        RuntimeException("Service $serviceName is unavailable", cause)
