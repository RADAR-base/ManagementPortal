package org.radarbase.auth.authorization;

import java.io.Serializable;
import java.util.Locale;

/**
 * Constants for Spring Security authorities.
 */
public enum RoleAuthority implements Serializable {
    SYS_ADMIN(Scope.GLOBAL, false),
    PROJECT_ADMIN(Scope.PROJECT, false),
    PROJECT_OWNER(Scope.PROJECT, false),
    PROJECT_AFFILIATE(Scope.PROJECT, false),
    PROJECT_ANALYST(Scope.PROJECT, false),
    PARTICIPANT(Scope.PROJECT, true),
    INACTIVE_PARTICIPANT(Scope.PROJECT, true),
    ORGANIZATION_ADMIN(Scope.ORGANIZATION, false);

    public static final String SYS_ADMIN_AUTHORITY = "ROLE_SYS_ADMIN";

    private final Scope scope;
    private final boolean isPersonal;

    RoleAuthority(Scope scope, boolean isPersonal) {
        this.scope = scope;
        this.isPersonal = isPersonal;
    }

    public Scope scope() {
        return scope;
    }

    public String authority() {
        return "ROLE_" + name();
    }

    public boolean isPersonal() {
        return this.isPersonal;
    }

    /**
     * Find role authority based on authority name.
     * @param authority authority name
     * @return RoleAuthority
     * @throws IllegalArgumentException if no role authority exists with the given name.
     * @throws NullPointerException if given authority is null.
     */
    public static RoleAuthority valueOfAuthority(String authority) {
        String upperAuthority = authority.toUpperCase(Locale.ROOT);
        if (!upperAuthority.startsWith("ROLE_")) {
            throw new IllegalArgumentException("Cannot map role without 'ROLE_' prefix");
        }
        return valueOf(upperAuthority.substring(5));
    }

    /**
     * Find role authority based on authority name.
     * @param authority authority name
     * @return RoleAuthority or null if no role authority exists with the given name.
     */
    public static RoleAuthority valueOfAuthorityOrNull(String authority) {
        if (authority == null) {
            return null;
        }
        try {
            return valueOfAuthority(authority);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public enum Scope {
        GLOBAL, ORGANIZATION, PROJECT
    }
}
