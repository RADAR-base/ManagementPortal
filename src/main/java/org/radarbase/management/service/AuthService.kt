package org.radarbase.management.service

import kotlinx.coroutines.runBlocking
import org.radarbase.auth.authorization.*
import org.radarbase.auth.token.RadarToken
import org.radarbase.management.security.NotAuthorizedException
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Consumer
import javax.annotation.Nullable

@Service
class AuthService(
    @Nullable
    private val token: RadarToken?,
    private val oracle: AuthorizationOracle,
) {
    /**
     * Check whether given [token] would have the [permission] scope in any of its roles. This doesn't
     * check whether [token] has access to a specific entity or global access.
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
     * Check whether [token] has permission [permission], regarding given entity from [builder].
     * The permission is checked both for its
     * own entity scope and for the [EntityDetails.minimumEntityOrNull] entity scope.
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

        // entitydetails builder is null means we require global permission
        val entity = if (builder != null) entityDetailsBuilder(builder) else EntityDetails.global

        val hasPermission = runBlocking {
            oracle.hasPermission(token, permission, entity, scope)
        }
        if (!hasPermission) {
            throw NotAuthorizedException(
                "User ${token.username} with client ${token.clientId} does not have permission $permission to scope " +
                        "$scope of $entity"
            )
        }
    }

    fun referentsByScope(permission: Permission): AuthorityReferenceSet {
        val token = token ?: return AuthorityReferenceSet()
        return oracle.referentsByScope(token, permission)
    }

    fun mayBeGranted(role: RoleAuthority, permission: Permission): Boolean = with(oracle) {
        role.mayBeGranted(permission)
    }

    fun mayBeGranted(authorities: Collection<RoleAuthority>, permission: Permission): Boolean {
        return authorities.any{ mayBeGranted(it, permission) }
    }
}
