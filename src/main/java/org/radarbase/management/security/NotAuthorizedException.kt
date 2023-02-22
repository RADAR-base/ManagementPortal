package org.radarbase.management.security

import java.security.GeneralSecurityException

/**
 * Created by dverbeec on 27/09/2017.
 */
class NotAuthorizedException : GeneralSecurityException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
