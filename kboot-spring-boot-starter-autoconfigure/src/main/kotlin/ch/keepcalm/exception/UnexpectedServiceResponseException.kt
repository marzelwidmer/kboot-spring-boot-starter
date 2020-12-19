package ch.keepcalm.exception

class UnexpectedServiceResponseException(val serviceName: String,
                                         val serviceMethod: String,
                                         val serviceArgs: Array<Any>,
                                         cause: Throwable) :
        RuntimeException("Service request $serviceName.$serviceMethod returned unexpected response", cause)
