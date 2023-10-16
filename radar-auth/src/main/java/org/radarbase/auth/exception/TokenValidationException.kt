package org.radarbase.auth.exception

/**
 * Created by dverbeec on 15/09/2017.
 */
open class TokenValidationException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
