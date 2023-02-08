package org.radarbase.auth.authorization

import java.io.Serializable
import java.util.*
import java.util.stream.Collector
import java.util.stream.Collectors

/**
 * Constants for Spring Security authorities.
 */
enum class RoleAuthority(
    val scope: Scope,
    @JvmField val isPersonal: Boolean,
) : Serializable {
    SYS_ADMIN(Scope.GLOBAL, false),
    PROJECT_ADMIN(Scope.PROJECT, false),
    PROJECT_OWNER(Scope.PROJECT, false),
    PROJECT_AFFILIATE(Scope.PROJECT, false),
    PROJECT_ANALYST(Scope.PROJECT, false),
    PARTICIPANT(Scope.PROJECT, true),
    INACTIVE_PARTICIPANT(Scope.PROJECT, true),
    ORGANIZATION_ADMIN(Scope.ORGANIZATION, false);

    val authority: String = "ROLE_$name"

    enum class Scope {
        GLOBAL, ORGANIZATION, PROJECT
    }

    /**
     * Check if this role has may have given permission associated with it.
     * @param permission the permission to check
     * @return true if this role has given permission associated with it, false otherwise
     */
    fun mayBeGranted(permission: Permission) = this in AuthorizationOracle.allowedRoles(permission)

    companion object {
        const val SYS_ADMIN_AUTHORITY = "ROLE_SYS_ADMIN"

        /**
         * Find role authority based on authority name.
         * @param authority authority name
         * @return RoleAuthority
         * @throws IllegalArgumentException if no role authority exists with the given name.
         * @throws NullPointerException if given authority is null.
         */
        @JvmStatic
        fun valueOfAuthority(authority: String): RoleAuthority {
            val upperAuthority = authority.uppercase()
            require(upperAuthority.startsWith("ROLE_")) { "Cannot map role without 'ROLE_' prefix" }
            return valueOf(upperAuthority.substring(5))
        }

        /**
         * Find role authority based on authority name.
         * @param authority authority name
         * @return RoleAuthority or null if no role authority exists with the given name.
         */
        @JvmStatic
        fun valueOfAuthorityOrNull(authority: String): RoleAuthority? {
            return try {
                valueOfAuthority(authority)
            } catch (ex: IllegalArgumentException) {
                null
            }
        }
    }
}
