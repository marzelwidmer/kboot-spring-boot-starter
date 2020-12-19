package ch.keepcalm.exception

class ServiceFailedException(val serviceName: String,
                             val serviceMethod: String,
                             val serviceStatus: Int,
                             val details: String,
                             val serviceArgs: Array<Any>, cause: Throwable) :
        RuntimeException("Service request $serviceName.$serviceMethod failed", cause)
