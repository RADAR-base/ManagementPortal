package org.radarcns.auth.authorization;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.radarcns.auth.authorization.AuthoritiesConstants.PARTICIPANT;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PROJECT_ADMIN;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PROJECT_AFFILIATE;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PROJECT_ANALYST;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PROJECT_OWNER;
import static org.radarcns.auth.authorization.AuthoritiesConstants.SYS_ADMIN;

/**
 * Created by dverbeec on 22/09/2017.
 */
public class Permissions {

    private static Map<Permission, Set<String>> PERMISSION_MATRIX;

    static {
        initPermissions();
    }

    /**
     * Look up the allowed authorities for a given permission. Authorities are String constants that
     * appear in {@link AuthoritiesConstants}.
     * @param permission The permission to look up.
     * @return An unmodifiable view of the set of allowed authorities.
     */
    public static Set<String> allowedAuthorities(Permission permission) {
        if (PERMISSION_MATRIX.containsKey(permission)) {
            return Collections.unmodifiableSet(PERMISSION_MATRIX.get(permission));
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * @return An unmodifiable view of the permission matrix.
     */
    public static Map<Permission, Set<String>> getPermissionMatrix() {
        return Collections.unmodifiableMap(PERMISSION_MATRIX);
    }

    /**
     * Static permission matrix based on the currently agreed upon security rules.
     */
    private static void initPermissions() {
        PERMISSION_MATRIX = new HashMap<>();
        Permission.allPermissions().forEach(p -> PERMISSION_MATRIX.put(p, new HashSet<>()));

        /* System Administrator - has all currently defined permissions */
        PERMISSION_MATRIX.values().forEach(s -> s.add(SYS_ADMIN));

        // for all authorities except for SYS_ADMIN, the authority is scoped to a project, which
        // is checked elsewhere
        // Project Admin - has all currently defined permissions except creating new projects
        // and writing data
        PERMISSION_MATRIX.entrySet().stream()
            .filter(e -> !Arrays.asList(Permission.PROJECT_CREATE, Permission.MEASUREMENT_CREATE)
                .contains(e.getKey()))
            .forEach(e -> e.getValue().add(PROJECT_ADMIN));

        /* Project Owner */
        // CRUD operations on subjects to allow enrollment
        PERMISSION_MATRIX.entrySet().stream()
            .filter(e -> e.getKey().getEntity() == Permission.Entity.SUBJECT)
            .forEach(e -> e.getValue().add(PROJECT_OWNER));

        // can also read all other things except users, audits and authorities
        PERMISSION_MATRIX.entrySet().stream()
            .filter(e -> !Arrays.asList(Permission.Entity.AUDIT, Permission.Entity.AUTHORITY,
                Permission.Entity.USER)
                .contains(e.getKey().getEntity()))
            .filter(e -> e.getKey().getOperation() == Permission.Operation.READ)
            .forEach(e -> e.getValue().add(PROJECT_OWNER));

        /* Project affiliate */
        // Create, read and update participant (no delete)
        PERMISSION_MATRIX.entrySet().stream()
            .filter(e -> e.getKey().getEntity() == Permission.Entity.SUBJECT)
            .filter(e -> e.getKey().getOperation() != Permission.Operation.DELETE)
            .forEach(e -> e.getValue().add(PROJECT_AFFILIATE));

        // can also read all other things except users, audits and authorities
        PERMISSION_MATRIX.entrySet().stream()
            .filter(e -> !Arrays.asList(Permission.Entity.AUDIT, Permission.Entity.AUTHORITY,
                Permission.Entity.USER)
                .contains(e.getKey().getEntity()))
            .filter(e -> e.getKey().getOperation() == Permission.Operation.READ)
            .forEach(e -> e.getValue().add(PROJECT_AFFILIATE));

        /* Project analyst */
        // Can read everything except users, authorities and audits
        PERMISSION_MATRIX.entrySet().stream()
            .filter(e -> !Arrays.asList(Permission.Entity.AUDIT, Permission.Entity.AUTHORITY,
                Permission.Entity.USER)
                .contains(e.getKey().getEntity()))
            .filter(e -> e.getKey().getOperation() == Permission.Operation.READ)
            .forEach(e -> e.getValue().add(PROJECT_ANALYST));

        // Can add metadata to sources
        PERMISSION_MATRIX.get(Permission.SOURCE_UPDATE).add(PROJECT_ANALYST);

        /* Participant */
        // Can update and read own data and can read and write own measurements
        Arrays.asList(Permission.SUBJECT_READ, Permission.SUBJECT_UPDATE,
                Permission.MEASUREMENT_CREATE, Permission.MEASUREMENT_READ).stream()
                        .forEach(p -> PERMISSION_MATRIX.get(p).add(PARTICIPANT));
    }
}
