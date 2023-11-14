package org.radarbase.auth.exception

/** Exception indicating a problem with the Identity Provider */
class IdpException: Throwable {
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
