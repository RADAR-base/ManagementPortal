package org.radarbase.auth.exception

/**
 *  Thrown when the authentication assurance level of a token is not sufficient (i.e. if no multifactor authentication
 *  was performed)
 */
open class InsufficientAuthenticationLevelException : TokenValidationException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
