package org.radarbase.auth.authorization;

import java.util.Locale;

/**
 * Constants for Spring Security authorities.
 */

public enum AuthoritiesConstants {
    SYS_ADMIN(Scope.GLOBAL),
    PROJECT_ADMIN(Scope.PROJECT),
    PROJECT_OWNER(Scope.PROJECT),
    PROJECT_AFFILIATE(Scope.PROJECT),
    PROJECT_ANALYST(Scope.PROJECT),
    PARTICIPANT(Scope.PROJECT),
    INACTIVE_PARTICIPANT(Scope.PROJECT),
    ORGANIZATION_ADMIN(Scope.ORGANIZATION);

    private final Scope scope;

    AuthoritiesConstants(Scope scope) {
        this.scope = scope;
    }

    public Scope scope() {
        return scope;
    }

    public String role() {
        return "ROLE_" + name();
    }

    public static AuthoritiesConstants valueOfRole(String role) {
        String upperRole = role.toUpperCase(Locale.ROOT);
        if (!upperRole.startsWith("ROLE_")) {
            throw new IllegalArgumentException("Cannot map role without 'ROLE_' prefix");
        }
        return valueOf(upperRole.substring(5));
    }

    public static AuthoritiesConstants valueOfRoleOrNull(String role) {
        try {
            return valueOfRole(role);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public enum Scope {
        GLOBAL, ORGANIZATION, PROJECT
    }
}
