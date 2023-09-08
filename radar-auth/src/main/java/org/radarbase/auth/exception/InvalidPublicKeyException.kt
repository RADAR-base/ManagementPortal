package org.radarbase.auth.exception

class InvalidPublicKeyException: TokenValidationException {
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
