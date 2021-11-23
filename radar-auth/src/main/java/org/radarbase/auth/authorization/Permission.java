package org.radarbase.auth.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Class to represent the different permissions in the RADAR platform. A permission has an entity
 * and an operation.
 */
public enum Permission {
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

    public enum Entity {
        // ManagementPortal entities
        SOURCETYPE,
        SOURCEDATA,
        SOURCE,
        SUBJECT,
        USER,
        ROLE,
        ORGANIZATION,
        PROJECT,
        OAUTHCLIENTS,
        AUDIT,
        AUTHORITY,

        // RMT measurements
        MEASUREMENT
    }

    public enum Operation {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }

    private final Entity entity;
    private final Operation operation;

    /**
     * Permission constructor. In general, the constants in this class should be preferred
     * for referencing permissions.
     * @param entity the entity that the permission refers to.
     * @param operation the operation on given entity that requires a permission.
     */
    Permission(Entity entity, Operation operation) {
        if (entity == null || operation == null) {
            throw new IllegalArgumentException("Entity and operation can not be null");
        }
        this.entity = entity;
        this.operation = operation;
    }

    public Entity getEntity() {
        return entity;
    }

    public Operation getOperation() {
        return operation;
    }

    /**
     * Check if a given authority has this permission associated with it.
     * @param authority the authority name
     * @return true if the given authority has this permission associated with it, false otherwise
     */
    public boolean isAuthorityAllowed(AuthoritiesConstants authority) {
        return Permissions.allowedAuthorities(this).contains(authority);
    }

    @Override
    public String toString() {
        return "Permission{entity=" + entity + ", operation=" + operation + '}';
    }

    /**
     * Stream all available permissions.
     */
    public static Stream<Permission> stream() {
        return Arrays.stream(values());
    }

    /** Returns all available scope names. */
    public static String[] scopes() {
        return stream()
                .map(Permission::scopeName)
                .toArray(String[]::new);
    }

    /** Return matching permission. */
    public static Permission of(Entity entity, Operation operation) {
        return stream()
                .filter(p -> p.getEntity() == entity && p.getOperation() == operation)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No permission found for given entity and operation"));
    }

    /**
     * Turn this permission into an OAuth scope name and return it.
     *
     * @return the OAuth scope representation of this permission
     */
    public String scopeName() {
        return entity + "." + operation;
    }
}
