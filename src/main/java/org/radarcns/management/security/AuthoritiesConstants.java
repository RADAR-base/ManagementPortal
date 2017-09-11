package org.radarcns.management.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

//    public static final String ADMIN = "ROLE_ADMIN";

    public static final String SYS_ADMIN = "ROLE_SYS_ADMIN";

    public static final String PROJECT_ADMIN = "ROLE_PROJECT_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String PARTICIPANT = "ROLE_PARTICIPANT";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String EXTERNAL_ERF_INTEGRATOR = "ROLE_EXTERNAL_ERF_INTEGRATOR";

    private AuthoritiesConstants() {
    }
}
