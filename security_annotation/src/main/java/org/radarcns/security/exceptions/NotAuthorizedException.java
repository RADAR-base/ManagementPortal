package org.radarcns.security.exceptions;

/**
 * Created by dverbeec on 5/04/2017.
 */
public class NotAuthorizedException extends Exception {

    public NotAuthorizedException() {
        super();
    }

    public NotAuthorizedException(String message) {
        super(message);
    }
}
