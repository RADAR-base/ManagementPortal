package org.radarcns.auth.authorization;

/**
 * Constants for Spring Security authorities.
 */
public interface AuthoritiesConstants {
    String SYS_ADMIN = "ROLE_SYS_ADMIN";

    String PROJECT_ADMIN = "ROLE_PROJECT_ADMIN";

    String PROJECT_OWNER = "ROLE_PROJECT_OWNER";

    String PROJECT_AFFILIATE = "ROLE_PROJECT_AFFILIATE";

    String PROJECT_ANALYST = "ROLE_PROJECT_ANALYST";

    String PARTICIPANT = "ROLE_PARTICIPANT";

    String INACTIVE_PARTICIPANT = "ROLE_INACTIVE_PARTICIPANT";
}
