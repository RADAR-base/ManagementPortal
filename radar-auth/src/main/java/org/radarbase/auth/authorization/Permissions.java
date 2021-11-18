package org.radarbase.auth.authorization;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.radarbase.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarbase.auth.authorization.AuthoritiesConstants.ORGANIZATION_ADMIN;
import static org.radarbase.auth.authorization.AuthoritiesConstants.PARTICIPANT;
import static org.radarbase.auth.authorization.AuthoritiesConstants.PROJECT_ADMIN;
import static org.radarbase.auth.authorization.AuthoritiesConstants.PROJECT_AFFILIATE;
import static org.radarbase.auth.authorization.AuthoritiesConstants.PROJECT_ANALYST;
import static org.radarbase.auth.authorization.AuthoritiesConstants.PROJECT_OWNER;
import static org.radarbase.auth.authorization.AuthoritiesConstants.SYS_ADMIN;

/**
 * Created by dverbeec on 22/09/2017.
 */
public final class Permissions {

    private static final Map<Permission, Set<String>> PERMISSION_MATRIX;

    static {
        PERMISSION_MATRIX = createPermissions();
    }

    private Permissions() {
        // utility class
    }

    /**
     * Look up the allowed authorities for a given permission. Authorities are String constants that
     * appear in {@link AuthoritiesConstants}.
     * @param permission The permission to look up.
     * @return An unmodifiable view of the set of allowed authorities.
     */
    public static Set<String> allowedAuthorities(Permission permission) {
        return PERMISSION_MATRIX.getOrDefault(permission, Set.of());
    }

    /**
     * Get the permission matrix.
     *
     * <p>The permission matrix maps each {@link Permission} to a set of authorities that have that
     * permission.</p>
     * @return An unmodifiable view of the permission matrix.
     */
    public static Map<Permission, Set<String>> getPermissionMatrix() {
        return PERMISSION_MATRIX;
    }

    /**
     * Static permission matrix based on the currently agreed upon security rules.
     */
    private static Map<Permission, Set<String>> createPermissions() {
        Map<String, Stream<Permission>> rolePermissions = new HashMap<>();

        // System admin can do everything.
        rolePermissions.put(SYS_ADMIN, Permission.stream());

        // Organization admin can do most things, but not view subjects or measurements
        rolePermissions.put(ORGANIZATION_ADMIN, Permission.stream()
                .filter(excludePermissions(Permission.ORGANIZATION_CREATE))
                .filter(excludeEntities(Permission.Entity.SUBJECT, Permission.Entity.MEASUREMENT)));

        // for all authorities except for SYS_ADMIN, the authority is scoped to a project, which
        // is checked elsewhere
        // Project Admin - has all currently defined permissions except creating new projects
        // Note: from radar-auth:0.5.7 we allow PROJECT_ADMIN to create measurements.
        // This can be done by uploading data through the web application.
        rolePermissions.put(PROJECT_ADMIN, Permission.stream()
                .filter(excludeEntities(
                        Permission.Entity.AUDIT,
                        Permission.Entity.AUTHORITY))
                .filter(excludeOtherEntityOperations(
                        Permission.Entity.ORGANIZATION, Permission.Operation.READ))
                .filter(excludePermissions(Permission.PROJECT_CREATE)));

        /* Project Owner */
        // CRUD operations on subjects to allow enrollment
        rolePermissions.put(PROJECT_OWNER, Permission.stream()
                .filter(excludeEntities(
                        Permission.Entity.AUDIT,
                        Permission.Entity.AUTHORITY,
                        Permission.Entity.USER))
                .filter(excludeOtherEntityOperations(
                        Permission.Entity.ORGANIZATION, Permission.Operation.READ))
                .filter(excludePermissions(Permission.PROJECT_CREATE)));

        /* Project affiliate */
        // Create, read and update participant (no delete)
        rolePermissions.put(PROJECT_AFFILIATE, Permission.stream()
                .filter(excludeEntities(
                        Permission.Entity.AUDIT,
                        Permission.Entity.AUTHORITY,
                        Permission.Entity.USER))
                .filter(excludeOtherEntityOperations(
                        Permission.Entity.ORGANIZATION, Permission.Operation.READ))
                .filter(excludeOtherEntityOperations(
                        Permission.Entity.PROJECT, Permission.Operation.READ))
                .filter(excludePermissions(
                        Permission.SUBJECT_DELETE)));

        /* Project analyst */
        // Can read everything except users, authorities and audits
        rolePermissions.put(PROJECT_ANALYST, Permission.stream()
                .filter(excludeEntities(
                        Permission.Entity.AUDIT,
                        Permission.Entity.AUTHORITY,
                        Permission.Entity.USER))
                // Can add metadata to sources, only read other things.
                .filter(p -> p.getOperation() == Permission.Operation.READ
                        || p == Permission.SUBJECT_UPDATE));

        /* Participant */
        // Can update and read own data and can read and write own measurements
        rolePermissions.put(PARTICIPANT, Stream.of(
                Permission.SUBJECT_READ,
                Permission.SUBJECT_UPDATE,
                Permission.MEASUREMENT_CREATE,
                Permission.MEASUREMENT_READ));

        /* Inactive participant */
        // Doesn't have any permissions
        rolePermissions.put(INACTIVE_PARTICIPANT, Stream.empty());

        // invert map
        return Collections.unmodifiableMap(
                rolePermissions.entrySet().stream()
                        .flatMap(rolePerm -> rolePerm.getValue()
                                .map(p -> Map.entry(p, rolePerm.getKey())))
                        .collect(groupingBy(
                                Map.Entry::getKey,
                                () -> new EnumMap<>(Permission.class),
                                mapping(Map.Entry::getValue, toUnmodifiableSet()))));
    }

    private static Predicate<Permission> excludeOtherEntityOperations(
            Permission.Entity entity,
            Permission.Operation... operations) {
        return p -> p.getEntity() != entity || Arrays.asList(operations).contains(p.getOperation());
    }

    private static Predicate<Permission> excludePermissions(Permission... permissions) {
        return p -> !Arrays.asList(permissions).contains(p);
    }

    private static Predicate<Permission> excludeEntities(Permission.Entity... entities) {
        return p -> !Arrays.asList(entities).contains(p.getEntity());
    }
}
