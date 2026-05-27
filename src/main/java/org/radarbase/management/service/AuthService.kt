package org.radarbase.management.service

import kotlinx.coroutines.runBlocking
import org.radarbase.auth.authorization.*
import org.radarbase.auth.token.RadarToken
import org.radarbase.management.security.NotAuthorizedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Consumer

@Service
class AuthService(
    private val oracle: AuthorizationOracle,
) {
    /**
     * Lazily retrieves the RadarToken from the current security context.
     */
    private val token: RadarToken?
        get() = SecurityContextHolder.getContext()
            .authentication
            ?.credentials as? RadarToken

    /**
     * Check whether the current user has the given [permission] scope.
     * @throws NotAuthorizedException if identity does not have scope
     */
    @Throws(NotAuthorizedException::class)
    fun checkScope(permission: Permission) {
        val token = token ?: throw NotAuthorizedException("User without authentication does not have permission.")

        if (!oracle.hasScope(token, permission)) {
            throw NotAuthorizedException(
                "User ${token.username} with client ${token.clientId} does not have permission $permission"
            )
        }
    }

    /**
     * Check whether the current user has [permission] on a given entity built by [builder].
     * If builder is null, checks for global permission.
     * @throws NotAuthorizedException if identity does not have permission
     */
    @JvmOverloads
    @Throws(NotAuthorizedException::class)
    fun checkPermission(
        permission: Permission,
        builder: Consumer<EntityDetails>? = null,
        scope: Permission.Entity = permission.entity,
    ) {
        val token = token ?: throw NotAuthorizedException("User without authentication does not have permission.")

        val entity = builder?.let { entityDetailsBuilder(it) } ?: EntityDetails.global

        val hasPermission = runBlocking {
            oracle.hasPermission(token, permission, entity, scope)
        }

        if (!hasPermission) {
            throw NotAuthorizedException(
                "User ${token.username} with client ${token.clientId} does not have permission $permission to scope $scope of $entity"
            )
        }
    }

    /**
     * Returns the referents this token has access to for a given permission.
     */
    fun referentsByScope(permission: Permission): AuthorityReferenceSet {
        val token = token ?: return AuthorityReferenceSet()
        return oracle.referentsByScope(token, permission)
    }

    /**
     * Whether a role may be granted a given permission.
     */
    fun mayBeGranted(role: RoleAuthority, permission: Permission): Boolean = with(oracle) {
        role.mayBeGranted(permission)
    }
}
