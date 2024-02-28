package org.radarbase.auth.exception

/** Exception indicating a problem with the Identity Provider */
class IdpException: Throwable {
    var token: String? = null

    constructor(message: String) : super(message)
    constructor(message: String, token: String) : super(message)
    {
        this.token = token
    }

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(message: String, cause: Throwable, token: String) : super(message, cause)
    {
        this.token = token
    }
}
