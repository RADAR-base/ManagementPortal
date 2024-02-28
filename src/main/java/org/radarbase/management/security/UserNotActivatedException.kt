package org.radarbase.management.security

import org.springframework.security.core.AuthenticationException

/**
 * This exception is thrown in case of a not activated user trying to authenticate.
 */
class UserNotActivatedException : AuthenticationException {
    constructor(message: String?) : super(message)
    constructor(message: String?, t: Throwable?) : super(message, t)

    companion object {
        private const val serialVersionUID = 1L
    }
}
