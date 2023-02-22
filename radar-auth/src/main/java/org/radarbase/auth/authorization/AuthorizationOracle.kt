package org.radarbase.auth.authorization

import org.radarbase.auth.token.RadarToken
import java.util.*
import java.util.function.Consumer

interface AuthorizationOracle {
    /**
     * Whether [identity] has permission [permission], regarding given [entity]. An additional
     * [entityScope] can be provided to check whether the permission is also valid regarding that
     * scope. The permission is checked both for its
     * own entity scope and for the [EntityDetails.minimumEntityOrNull] entity scope.
     * @return true if identity has permission, false otheriwse
     */
    fun hasPermission(
        identity: RadarToken,
        permission: Permission,
        entityBuilder: Consumer<EntityDetails>,
    ): Boolean = hasPermission(identity, permission, EntityDetails().apply(entityBuilder::accept))

    /**
     * Whether [identity] has permission [permission], regarding given [entity]. An additional
     * [entityScope] can be provided to check whether the permission is also valid regarding that
     * scope. The permission is checked both for its
     * own entity scope and for the [EntityDetails.minimumEntityOrNull] entity scope.
     * @return true if identity has permission, false otheriwse
     */
    fun hasGlobalPermission(
        identity: RadarToken,
        permission: Permission,
    ): Boolean = hasPermission(identity, permission)

    /**
     * Whether [identity] has permission [permission], regarding given [entity]. An additional
     * [entityScope] can be provided to check whether the permission is also valid regarding that
     * scope. The permission is checked both for its
     * own entity scope and for the [EntityDetails.minimumEntityOrNull] entity scope.
     * @return true if identity has permission, false otheriwse
     */
    fun hasPermission(
        identity: RadarToken,
        permission: Permission,
        entity: EntityDetails = EntityDetails.global,
        entityScope: Permission.Entity = permission.entity,
    ): Boolean

    /**
     * Whether given [identity] would have the [permission] scope in any of its roles. This doesn't
     * check whether [identity] has access to a specific entity or global access.
     * @return true if identity has scope, false otherwise
     */
    fun hasScope(identity: RadarToken, permission: Permission): Boolean

    /**
     * Return a list of referents, per scope, that given [identity] has given [permission] on.
     * The GLOBAL scope does not have any referents, so that will always return an empty list.
     * The ORGANIZATION scope will give a list of organization names, and the PROJECT scope a list
     * of project names. If identity has no role with given permission, this will return an empty
     * map.
     */
    fun referentsByScope(
        identity: RadarToken,
        permission: Permission
    ): AuthorityReferenceSet

    fun RoleAuthority.mayBeGranted(permission: Permission): Boolean
}

