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

    String USER = "ROLE_USER";

    String ANONYMOUS = "ROLE_ANONYMOUS";

    String PARTICIPANT = "ROLE_PARTICIPANT";

    String EXTERNAL_ERF_INTEGRATOR = "ROLE_EXTERNAL_ERF_INTEGRATOR";
}
