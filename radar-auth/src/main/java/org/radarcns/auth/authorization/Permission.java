package org.radarcns.auth.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dverbeec on 22/09/2017.
 */
public class Permission {

    private static final Logger log = LoggerFactory.getLogger(Permission.class);

    public enum ENTITY {
        // ManagementPortal entities
        DEVICETYPE,
        SENSORDATA,
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

    public enum OPERATION {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }

    public static final Permission DEVICETYPE_CREATE = new Permission(ENTITY.DEVICETYPE, OPERATION.CREATE);
    public static final Permission DEVICETYPE_READ = new Permission(ENTITY.DEVICETYPE, OPERATION.READ);
    public static final Permission DEVICETYPE_UPDATE = new Permission(ENTITY.DEVICETYPE, OPERATION.UPDATE);
    public static final Permission DEVICETYPE_DELETE = new Permission(ENTITY.DEVICETYPE, OPERATION.DELETE);
    public static final Permission SENSORDATA_CREATE = new Permission(ENTITY.SENSORDATA, OPERATION.CREATE);
    public static final Permission SENSORDATA_READ = new Permission(ENTITY.SENSORDATA, OPERATION.READ);
    public static final Permission SENSORDATA_UPDATE = new Permission(ENTITY.SENSORDATA, OPERATION.UPDATE);
    public static final Permission SENSORDATA_DELETE = new Permission(ENTITY.SENSORDATA, OPERATION.DELETE);
    public static final Permission SOURCE_CREATE = new Permission(ENTITY.SOURCE, OPERATION.CREATE);
    public static final Permission SOURCE_READ = new Permission(ENTITY.SOURCE, OPERATION.READ);
    public static final Permission SOURCE_UPDATE = new Permission(ENTITY.SOURCE, OPERATION.UPDATE);
    public static final Permission SOURCE_DELETE = new Permission(ENTITY.SOURCE, OPERATION.DELETE);
    public static final Permission SUBJECT_CREATE = new Permission(ENTITY.SUBJECT, OPERATION.CREATE);
    public static final Permission SUBJECT_READ = new Permission(ENTITY.SUBJECT, OPERATION.READ);
    public static final Permission SUBJECT_UPDATE = new Permission(ENTITY.SUBJECT, OPERATION.UPDATE);
    public static final Permission SUBJECT_DELETE = new Permission(ENTITY.SUBJECT, OPERATION.DELETE);
    public static final Permission USER_CREATE = new Permission(ENTITY.USER, OPERATION.CREATE);
    public static final Permission USER_READ = new Permission(ENTITY.USER, OPERATION.READ);
    public static final Permission USER_UPDATE = new Permission(ENTITY.USER, OPERATION.UPDATE);
    public static final Permission USER_DELETE = new Permission(ENTITY.USER, OPERATION.DELETE);
    public static final Permission ROLE_CREATE = new Permission(ENTITY.ROLE, OPERATION.CREATE);
    public static final Permission ROLE_READ = new Permission(ENTITY.ROLE, OPERATION.READ);
    public static final Permission ROLE_UPDATE = new Permission(ENTITY.ROLE, OPERATION.UPDATE);
    public static final Permission ROLE_DELETE = new Permission(ENTITY.ROLE, OPERATION.DELETE);
    public static final Permission PROJECT_CREATE = new Permission(ENTITY.PROJECT, OPERATION.CREATE);
    public static final Permission PROJECT_READ = new Permission(ENTITY.PROJECT, OPERATION.READ);
    public static final Permission PROJECT_UPDATE = new Permission(ENTITY.PROJECT, OPERATION.UPDATE);
    public static final Permission PROJECT_DELETE = new Permission(ENTITY.PROJECT, OPERATION.DELETE);
    public static final Permission OAUTHCLIENTS_READ = new Permission(ENTITY.OAUTHCLIENTS, OPERATION.READ);
    public static final Permission AUDIT_READ = new Permission(ENTITY.AUDIT, OPERATION.READ);
    public static final Permission AUTHORITY_READ = new Permission(ENTITY.AUTHORITY, OPERATION.READ);
    public static final Permission MEASUREMENT_READ = new Permission(ENTITY.MEASUREMENT, OPERATION.READ);
    public static final Permission MEASUREMENT_CREATE = new Permission(ENTITY.MEASUREMENT, OPERATION.CREATE);

    private final ENTITY entity;
    private final OPERATION operation;

    private Permission(ENTITY entity, OPERATION operation) {
        if (entity == null || operation == null) {
            throw new IllegalArgumentException("Entity and operation can not be null");
        }
        this.entity = entity;
        this.operation = operation;
    }

    public ENTITY getEntity() {
        return entity;
    }

    public OPERATION getOperation() {
        return operation;
    }

    public static List<Permission> allPermissions() {
        return Arrays.stream(Permission.class.getDeclaredFields())  // get delcared fields
            .filter(f -> Modifier.isStatic(f.getModifiers()))       // that are static
            .filter(f -> f.getType() == Permission.class)           // and of type Permission
            .map(f -> {
                try {
                    return (Permission) f.get(null);
                } catch (IllegalAccessException e) {
                    log.error("Could not get permissions through reflection. Fieldname: {}",
                        f.getName());
                    return null;
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;

        Permission that = (Permission) o;

        if (entity != that.entity) return false;
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
        return "Permission{" +
            "entity=" + entity +
            ", operation=" + operation +
            '}';
    }
}
