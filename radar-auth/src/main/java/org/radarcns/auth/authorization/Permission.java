package org.radarcns.auth.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to represent the different permissions in the RADAR platform. A permission has an entity
 * and an operation.
 */
public class Permission {

    private static final Logger log = LoggerFactory.getLogger(Permission.class);

    public enum Entity {
        // ManagementPortal entities
        SOURCETYPE,
        SOURCEDATA,
        SOURCE,
        SUBJECT,
        USER,
        ROLE,
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

    public static final Permission SOURCETYPE_CREATE =
            new Permission(Entity.SOURCETYPE, Operation.CREATE);
    public static final Permission SOURCETYPE_READ =
            new Permission(Entity.SOURCETYPE, Operation.READ);
    public static final Permission SOURCETYPE_UPDATE =
            new Permission(Entity.SOURCETYPE, Operation.UPDATE);
    public static final Permission SOURCETYPE_DELETE =
            new Permission(Entity.SOURCETYPE, Operation.DELETE);
    public static final Permission SOURCEDATA_CREATE =
            new Permission(Entity.SOURCEDATA, Operation.CREATE);
    public static final Permission SOURCEDATA_READ =
            new Permission(Entity.SOURCEDATA, Operation.READ);
    public static final Permission SOURCEDATA_UPDATE =
            new Permission(Entity.SOURCEDATA, Operation.UPDATE);
    public static final Permission SOURCEDATA_DELETE =
            new Permission(Entity.SOURCEDATA, Operation.DELETE);
    public static final Permission SOURCE_CREATE = new Permission(Entity.SOURCE, Operation.CREATE);
    public static final Permission SOURCE_READ = new Permission(Entity.SOURCE, Operation.READ);
    public static final Permission SOURCE_UPDATE = new Permission(Entity.SOURCE, Operation.UPDATE);
    public static final Permission SOURCE_DELETE = new Permission(Entity.SOURCE, Operation.DELETE);
    public static final Permission SUBJECT_CREATE =
            new Permission(Entity.SUBJECT, Operation.CREATE);
    public static final Permission SUBJECT_READ = new Permission(Entity.SUBJECT, Operation.READ);
    public static final Permission SUBJECT_UPDATE =
            new Permission(Entity.SUBJECT, Operation.UPDATE);
    public static final Permission SUBJECT_DELETE =
            new Permission(Entity.SUBJECT, Operation.DELETE);
    public static final Permission USER_CREATE = new Permission(Entity.USER, Operation.CREATE);
    public static final Permission USER_READ = new Permission(Entity.USER, Operation.READ);
    public static final Permission USER_UPDATE = new Permission(Entity.USER, Operation.UPDATE);
    public static final Permission USER_DELETE = new Permission(Entity.USER, Operation.DELETE);
    public static final Permission ROLE_CREATE = new Permission(Entity.ROLE, Operation.CREATE);
    public static final Permission ROLE_READ = new Permission(Entity.ROLE, Operation.READ);
    public static final Permission ROLE_UPDATE = new Permission(Entity.ROLE, Operation.UPDATE);
    public static final Permission ROLE_DELETE = new Permission(Entity.ROLE, Operation.DELETE);
    public static final Permission PROJECT_CREATE = new
            Permission(Entity.PROJECT, Operation.CREATE);
    public static final Permission PROJECT_READ = new Permission(Entity.PROJECT, Operation.READ);
    public static final Permission PROJECT_UPDATE =
            new Permission(Entity.PROJECT, Operation.UPDATE);
    public static final Permission PROJECT_DELETE =
            new Permission(Entity.PROJECT, Operation.DELETE);
    public static final Permission OAUTHCLIENTS_CREATE =
            new Permission(Entity.OAUTHCLIENTS, Operation.CREATE);
    public static final Permission OAUTHCLIENTS_READ =
            new Permission(Entity.OAUTHCLIENTS, Operation.READ);
    public static final Permission OAUTHCLIENTS_UPDATE =
            new Permission(Entity.OAUTHCLIENTS, Operation.UPDATE);
    public static final Permission OAUTHCLIENTS_DELETE =
            new Permission(Entity.OAUTHCLIENTS, Operation.DELETE);
    public static final Permission AUDIT_READ = new Permission(Entity.AUDIT, Operation.READ);
    public static final Permission AUTHORITY_READ =
            new Permission(Entity.AUTHORITY, Operation.READ);
    public static final Permission MEASUREMENT_READ =
            new Permission(Entity.MEASUREMENT, Operation.READ);
    public static final Permission MEASUREMENT_CREATE =
            new Permission(Entity.MEASUREMENT, Operation.CREATE);

    private final Entity entity;
    private final Operation operation;

    private Permission(Entity entity, Operation operation) {
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
     * Get all currently defined permissions.
     * @return A list containing all currently defined permissions
     */
    public static List<Permission> allPermissions() {
        return Arrays.stream(Permission.class.getDeclaredFields())  // get declared fields
            .filter(f -> Modifier.isStatic(f.getModifiers()))       // that are static
            .filter(f -> f.getType() == Permission.class)           // and of type Permission
            .map(f -> {
                try {
                    return (Permission) f.get(null);
                } catch (IllegalAccessException ex) {
                    log.error("Could not get permissions through reflection. Fieldname: {}",
                            f.getName());
                    return null;
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Permission)) {
            return false;
        }

        Permission that = (Permission) other;

        if (entity != that.entity) {
            return false;
        }
        return operation == that.operation;
    }

    @Override
    public int hashCode() {
        int result = entity.hashCode();
        result = 31 * result + operation.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Permission{entity=" + entity + ", operation=" + operation + '}';
    }
}
