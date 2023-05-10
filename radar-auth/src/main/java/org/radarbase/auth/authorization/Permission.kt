package org.radarbase.auth.authorization

/**
 * Class to represent the different permissions in the RADAR platform. A permission has an entity
 * and an operation.
 */
enum class Permission(val entity: Entity, val operation: Operation) {
    SOURCETYPE_CREATE(Entity.SOURCETYPE, Operation.CREATE),
    SOURCETYPE_READ(Entity.SOURCETYPE, Operation.READ),
    SOURCETYPE_UPDATE(Entity.SOURCETYPE, Operation.UPDATE),
    SOURCETYPE_DELETE(Entity.SOURCETYPE, Operation.DELETE),

    SOURCEDATA_CREATE(Entity.SOURCEDATA, Operation.CREATE),
    SOURCEDATA_READ(Entity.SOURCEDATA, Operation.READ),
    SOURCEDATA_UPDATE(Entity.SOURCEDATA, Operation.UPDATE),
    SOURCEDATA_DELETE(Entity.SOURCEDATA, Operation.DELETE),

    SOURCE_CREATE(Entity.SOURCE, Operation.CREATE),
    SOURCE_READ(Entity.SOURCE, Operation.READ),
    SOURCE_UPDATE(Entity.SOURCE, Operation.UPDATE),
    SOURCE_DELETE(Entity.SOURCE, Operation.DELETE),

    SUBJECT_CREATE(Entity.SUBJECT, Operation.CREATE),
    SUBJECT_READ(Entity.SUBJECT, Operation.READ),
    SUBJECT_UPDATE(Entity.SUBJECT, Operation.UPDATE),
    SUBJECT_DELETE(Entity.SUBJECT, Operation.DELETE),

    USER_CREATE(Entity.USER, Operation.CREATE),
    USER_READ(Entity.USER, Operation.READ),
    USER_UPDATE(Entity.USER, Operation.UPDATE),
    USER_DELETE(Entity.USER, Operation.DELETE),

    ROLE_CREATE(Entity.ROLE, Operation.CREATE),
    ROLE_READ(Entity.ROLE, Operation.READ),
    ROLE_UPDATE(Entity.ROLE, Operation.UPDATE),
    ROLE_DELETE(Entity.ROLE, Operation.DELETE),

    PROJECT_CREATE(Entity.PROJECT, Operation.CREATE),
    PROJECT_READ(Entity.PROJECT, Operation.READ),
    PROJECT_UPDATE(Entity.PROJECT, Operation.UPDATE),
    PROJECT_DELETE(Entity.PROJECT, Operation.DELETE),

    ORGANIZATION_CREATE(Entity.ORGANIZATION, Operation.CREATE),
    ORGANIZATION_READ(Entity.ORGANIZATION, Operation.READ),
    ORGANIZATION_UPDATE(Entity.ORGANIZATION, Operation.UPDATE),
    ORGANIZATION_DELETE(Entity.ORGANIZATION, Operation.DELETE),

    OAUTHCLIENTS_CREATE(Entity.OAUTHCLIENTS, Operation.CREATE),
    OAUTHCLIENTS_READ(Entity.OAUTHCLIENTS, Operation.READ),
    OAUTHCLIENTS_UPDATE(Entity.OAUTHCLIENTS, Operation.UPDATE),
    OAUTHCLIENTS_DELETE(Entity.OAUTHCLIENTS, Operation.DELETE),

    AUDIT_READ(Entity.AUDIT, Operation.READ),

    AUTHORITY_READ(Entity.AUTHORITY, Operation.READ),

    MEASUREMENT_READ(Entity.MEASUREMENT, Operation.READ),
    MEASUREMENT_CREATE(Entity.MEASUREMENT, Operation.CREATE);

    enum class Entity {
        // ManagementPortal entities
        SOURCETYPE, SOURCEDATA, SOURCE, SUBJECT, USER, ROLE, ORGANIZATION, PROJECT, OAUTHCLIENTS,
        AUDIT, AUTHORITY,
        // RMT measurements
        MEASUREMENT
    }

    enum class Operation {
        CREATE, READ, UPDATE, DELETE
    }

    override fun toString(): String = "Permission{entity=$entity, operation=$operation}"

    /**
     * Turn this permission into an OAuth scope name and return it.
     *
     * @return the OAuth scope representation of this permission
     */
    fun scope(): String = "$entity.$operation"

    companion object {
        /** Returns all available scope names.  */
        @JvmStatic
        fun scopes(): Array<String> {
            return values()
                .map { obj: Permission -> obj.scope() }
                .toTypedArray()
        }

        /** Return matching permission.  */
        fun of(entity: Entity, operation: Operation): Permission =
            requireNotNull(
                values().firstOrNull { p -> p.entity == entity && p.operation == operation }
            ) { "No permission found for given entity and operation" }

        @JvmStatic
        fun ofScope(scope: String): Permission {
            return valueOf(scope.replace('.', '_'))
        }
    }
}
