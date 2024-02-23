package org.radarbase.management.security

/**
 * Application constants.
 */
object Constants {
    //Regex for acceptable logins
    const val ENTITY_ID_REGEX = "^[_'.@A-Za-z0-9- ]*$"
    const val TOKEN_NAME_REGEX = "^[A-Za-z0-9.-]*$"
    const val SYSTEM_ACCOUNT = "system"
    const val ANONYMOUS_USER = "anonymousUser"
}
