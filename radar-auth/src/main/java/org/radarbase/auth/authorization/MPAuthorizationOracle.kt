package org.radarbase.auth.authorization

import org.radarbase.auth.token.RadarToken
import org.radarbase.kotlin.coroutines.forkAny
import java.util.*

class MPAuthorizationOracle(
    private val relationService: EntityRelationService
) : AuthorizationOracle {
    /**
     * Whether [identity] has permission [permission], regarding given [entity]. An additional
     * [entityScope] can be provided to check whether the permission is also valid regarding that
     * scope. The permission is checked both for its
     * own entity scope and for the [EntityDetails.minimumEntityOrNull] entity scope.
     * @return true if identity has permission, false otheriwse
     */
    override suspend fun hasPermission(
        identity: RadarToken,
        permission: Permission,
        entity: EntityDetails,
        entityScope: Permission.Entity,
    ): Boolean {
        if (permission.scope() !in identity.scopes) return false

        if (identity.isClientCredentials) return true

        return identity.roles.forkAny {
            it.hasPermission(identity, permission, entity, entityScope)
        }
    }

    /**
     * Whether given [identity] would have the [permission] scope in any of its roles. This doesn't
     * check whether [identity] has access to a specific entity or global access.
     * @return true if identity has scope, false otherwise
     */
    override fun hasScope(identity: RadarToken, permission: Permission): Boolean {
        if (permission.scope() !in identity.scopes) return false

        if (identity.isClientCredentials) return true

        return identity.roles.any { it.role.mayBeGranted(permission) }
    }

    /**
     * Return a list of referents, per scope, that given [identity] has given [permission] on.
     * The GLOBAL scope does not have any referents, so that will always return an empty list.
     * The ORGANIZATION scope will give a list of organization names, and the PROJECT scope a list
     * of project names. If identity has no role with given permission, this will return an empty
     * map.
     */
    override fun referentsByScope(
        identity: RadarToken,
        permission: Permission
    ): AuthorityReferenceSet {
        if (identity.isClientCredentials) {
            return AuthorityReferenceSet(global = true)
        }
        var global = false
        val organizations = mutableSetOf<String>()
        val projects = mutableSetOf<String>()
        val personalProjects = mutableSetOf<String>()

        identity.roles.forEach {
            if (it.role.mayBeGranted(permission)) {
                when (it.role.scope) {
                    RoleAuthority.Scope.GLOBAL -> global = true
                    RoleAuthority.Scope.ORGANIZATION -> organizations.add(it.referent!!)
                    RoleAuthority.Scope.PROJECT -> {
                        if (it.role.isPersonal) {
                            personalProjects.add(it.referent!!)
                        } else {
                            projects.add(it.referent!!)
                        }
                    }
                }
            }
        }

        return AuthorityReferenceSet(
            global = global,
            organizations = organizations,
            projects = projects,
            personalProjects = personalProjects,
        )
    }

    override fun RoleAuthority.mayBeGranted(permission: Permission) = this in allowedRoles(permission)

    override fun Collection<RoleAuthority>.mayBeGranted(permission: Permission): Boolean {
        val allowedRoles = allowedRoles(permission)
        return any { it in allowedRoles }
    }

    /**
     * Whether the current role from [identity] has [permission] over given [entity] in
     * [entityScope] in any way.
     */
    private suspend fun AuthorityReference.hasPermission(
        identity: RadarToken,
        permission: Permission,
        entity: EntityDetails,
        entityScope: Permission.Entity,
    ): Boolean {
        if (!role.mayBeGranted(permission)) return false
        if (role.scope == RoleAuthority.Scope.GLOBAL) return true
        // if no entity scope is available and the role scope is not global, no matching authority
        // can be found.
        val minEntityScope = entity.minimumEntityOrNull() ?: return false
        return hasAuthority(identity, permission, entity, entityScope) &&
                (entityScope == minEntityScope ||
                        hasAuthority(identity, permission, entity, minEntityScope))
    }

    /**
     * Whether the current role from [identity] has a specific authority with [permission]
     * over given [entity] in [entityScope]
     */
    private suspend fun AuthorityReference.hasAuthority(
        identity: RadarToken,
        permission: Permission,
        entity: EntityDetails,
        entityScope: Permission.Entity,
    ): Boolean = when (entityScope) {
        Permission.Entity.MEASUREMENT -> hasAuthority(identity, permission, entity,
            Permission.Entity.SOURCE
        )
        Permission.Entity.SOURCE -> (!role.isPersonal ||
                // no specific source is mentioned -> just check the subject
                entity.source == null ||
                entity.source in identity.sources) &&
                hasAuthority(identity, permission, entity, Permission.Entity.SUBJECT)
        Permission.Entity.SUBJECT -> (!role.isPersonal ||
                entity.subject == identity.subject) &&
                hasAuthority(identity, permission, entity, Permission.Entity.PROJECT)
        Permission.Entity.PROJECT -> when (role.scope) {
            RoleAuthority.Scope.PROJECT -> referent == entity.project
            RoleAuthority.Scope.ORGANIZATION -> entity.findOrganization() == referent
            else -> false
        }
        Permission.Entity.ORGANIZATION -> when (role.scope) {
            RoleAuthority.Scope.PROJECT -> referent == entity.project || entity.organizationContainsProject(referent!!)
            RoleAuthority.Scope.ORGANIZATION -> entity.findOrganization() == referent
            else -> false
        }
        Permission.Entity.USER -> entity.user == identity.username || !role.isPersonal
        else -> true
    }

    private suspend fun EntityDetails.findOrganization(): String? {
        organization?.let { return it }
        val p = project ?: return null
        return relationService.findOrganizationOfProject(p)
            .also { this.organization = it }
    }

    private suspend fun EntityDetails.organizationContainsProject(targetProject: String): Boolean {
        val org = findOrganization() ?: return false
        return relationService.organizationContainsProject(org, targetProject)
    }

    /**
     * Created by dverbeec on 22/09/2017.
     */
    companion object Permissions {
        /**
         * Get the permission matrix.
         *
         *
         * The permission matrix maps each [Permission] to a set of authorities that have that
         * permission.
         * @return An unmodifiable view of the permission matrix.
         */
        @JvmStatic
        val permissionMatrix: Map<Permission, Set<RoleAuthority>> = createPermissions()

        /**
         * Look up the allowed authorities for a given permission. Authorities are String constants that
         * appear in [RoleAuthority].
         * @param permission The permission to look up.
         * @return An unmodifiable view of the set of allowed authorities.
         */
        @JvmStatic
        fun allowedRoles(permission: Permission): Set<RoleAuthority> {
            return permissionMatrix[permission] ?: emptySet()
        }

        /**
         * Static permission matrix based on the currently agreed upon security rules.
         */
        private fun createPermissions(): Map<Permission, Set<RoleAuthority>> {
            val rolePermissions: MutableMap<RoleAuthority, Sequence<Permission>> =
                EnumMap(RoleAuthority::class.java)

            // System admin can do everything.
            rolePermissions[RoleAuthority.SYS_ADMIN] = Permission.values().asSequence()

            // Organization admin can do most things, but not view subjects or measurements
            rolePermissions[RoleAuthority.ORGANIZATION_ADMIN] = Permission.values().asSequence()
                .exclude(
                    Permission.ORGANIZATION_CREATE,
                    Permission.SOURCEDATA_CREATE,
                    Permission.SOURCETYPE_CREATE
                )
                .excludeEntities(
                    Permission.Entity.AUDIT,
                    Permission.Entity.AUTHORITY,
                    Permission.Entity.MEASUREMENT
                )

            // for all authorities except for SYS_ADMIN, the authority is scoped to a project, which
            // is checked elsewhere
            // Project Admin - has all currently defined permissions except creating new projects
            // Note: from radar-auth:0.5.7 we allow PROJECT_ADMIN to create measurements.
            // This can be done by uploading data through the web application.
            rolePermissions[RoleAuthority.PROJECT_ADMIN] = Permission.values().asSequence()
                .exclude(Permission.PROJECT_CREATE)
                .excludeEntities(Permission.Entity.AUDIT, Permission.Entity.AUTHORITY)
                .limitEntityOperations(Permission.Entity.ORGANIZATION, Permission.Operation.READ)

            /* Project Owner */
            // CRUD operations on subjects to allow enrollment
            rolePermissions[RoleAuthority.PROJECT_OWNER] = Permission.values().asSequence()
                .exclude(Permission.PROJECT_CREATE)
                .excludeEntities(
                    Permission.Entity.AUDIT,
                    Permission.Entity.AUTHORITY,
                    Permission.Entity.USER
                )
                .limitEntityOperations(Permission.Entity.ORGANIZATION, Permission.Operation.READ)

            /* Project affiliate */
            // Create, read and update participant (no delete)
            rolePermissions[RoleAuthority.PROJECT_AFFILIATE] = Permission.values().asSequence()
                .exclude(Permission.SUBJECT_DELETE)
                .excludeEntities(
                    Permission.Entity.AUDIT,
                    Permission.Entity.AUTHORITY,
                    Permission.Entity.USER
                )
                .limitEntityOperations(Permission.Entity.ORGANIZATION, Permission.Operation.READ)
                .limitEntityOperations(Permission.Entity.PROJECT, Permission.Operation.READ)

            /* Project analyst */
            // Can read everything except users, authorities and audits
            rolePermissions[RoleAuthority.PROJECT_ANALYST] = Permission.values().asSequence()
                .excludeEntities(
                    Permission.Entity.AUDIT,
                    Permission.Entity.AUTHORITY,
                    Permission.Entity.USER
                )
                // Can add metadata to sources, only read other things.
                .filter { p ->
                    p.operation == Permission.Operation.READ ||
                            p == Permission.SUBJECT_UPDATE
                }

            /* Participant */
            // Can update and read own data and can read and write own measurements
            rolePermissions[RoleAuthority.PARTICIPANT] = sequenceOf(
                Permission.SUBJECT_READ,
                Permission.SUBJECT_UPDATE,
                Permission.MEASUREMENT_CREATE,
                Permission.MEASUREMENT_READ
            )

            /* Inactive participant */
            // Doesn't have any permissions
            rolePermissions[RoleAuthority.INACTIVE_PARTICIPANT] = emptySequence()

            // invert map
            return rolePermissions.asSequence()
                .flatMap { (role, permissionSeq) ->
                    permissionSeq.map { p -> Pair(p, role) }
                }
                .groupingBy { (p, _) -> p }
                .foldTo(
                    EnumMap(Permission::class.java),
                    initialValueSelector = { _, (_, role) -> enumSetOf(role) },
                    operation = { _, set, (_, role) ->
                        set += role
                        set
                    }
                )
        }

        private fun Sequence<Permission>.limitEntityOperations(
            entity: Permission.Entity,
            vararg operations: Permission.Operation
        ): Sequence<Permission> {
            val operationSet = enumSetOf(*operations)
            return filter { p: Permission -> p.entity != entity || p.operation in operationSet }
        }

        private fun Sequence<Permission>.exclude(vararg permissions: Permission): Sequence<Permission> {
            val permissionSet = enumSetOf(*permissions)
            return filter { it !in permissionSet }
        }

        private fun Sequence<Permission>.excludeEntities(vararg entities: Permission.Entity): Sequence<Permission> {
            val entitySet = enumSetOf(*entities)
            return filter { it.entity !in entitySet }
        }

        private inline fun <reified T: Enum<T>> enumSetOf(vararg values: T): EnumSet<T> = EnumSet.noneOf(
            T::class.java
        ).apply {
            values.forEach { add(it) }
        }
    }
}
