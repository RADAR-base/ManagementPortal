package org.radarbase.auth.exception;

import java.security.GeneralSecurityException;

/**
 * Created by dverbeec on 27/09/2017.
 */
public class NotAuthorizedException extends GeneralSecurityException {
    public NotAuthorizedException() {
        super();
    }

    public NotAuthorizedException(String message) {
        super(message);
    }

    public NotAuthorizedException(Throwable cause) {
        super(cause);
    }

    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
