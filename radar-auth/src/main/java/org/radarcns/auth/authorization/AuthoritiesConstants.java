package org.radarcns.auth.authorization;

/**
 * Constants for Spring Security authorities.
 */
public class AuthoritiesConstants {

    private AuthoritiesConstants() {
    }

    public static final String SYS_ADMIN = "ROLE_SYS_ADMIN";

    public static final String PROJECT_ADMIN = "ROLE_PROJECT_ADMIN";

    public static final String PROJECT_OWNER = "ROLE_PROJECT_OWNER";

    public static final String PROJECT_AFFILIATE = "ROLE_PROJECT_AFFILIATE";

    public static final String PROJECT_ANALYST = "ROLE_PROJECT_ANALYST";

    public static final String PARTICIPANT = "ROLE_PARTICIPANT";
}
