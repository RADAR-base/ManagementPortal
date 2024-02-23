package org.radarbase.management.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

/**
 * Utility class for Spring Security.
 */
object SecurityUtils {
    val currentUserLogin: String?
        /**
         * Get the login of the current user.
         *
         * @return the login of the current user if present
         */
        get() {
            val securityContext = SecurityContextHolder.getContext()
            return getUserName(securityContext.authentication)
        }

    /**
     * Get the user name contianed in an Authentication object.
     *
     * @param authentication context authentication
     * @return user name if present
     */
    fun getUserName(authentication: Authentication): String? = authentication
        .let { obj: Authentication -> obj.principal }
        .let { principal: Any? ->
            when (principal) {
                is UserDetails -> {
                    return (authentication.principal as UserDetails).username
                }

                is String -> {
                    return authentication.principal as String
                }

                is Authentication -> {
                    return principal.name
                }

                else -> {
                    return null
                }
            }
        }
}
