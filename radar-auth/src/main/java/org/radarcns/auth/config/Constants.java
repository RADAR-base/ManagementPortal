package org.radarcns.auth.config;

/**
 * Application constants.
 */
public final class Constants {

    //Regex for acceptable logins
    public static final String ENTITY_ID_REGEX = "^[_'.@A-Za-z0-9- ]*$";
    public static final String TOKEN_NAME_REGEX = "^[A-Za-z0-9.-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";

    private Constants() {
    }
}
