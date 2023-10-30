package org.radarbase.management.security.jwt

class SignatureException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, ex: Throwable?) : super(message, ex)
    constructor(ex: Throwable?) : super(ex)
}
