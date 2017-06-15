package org.radarcns.security.exceptions;

/**
 * Created by dverbeec on 14/06/2017.
 */
public class InvalidSigningKeyException extends Exception {
    public InvalidSigningKeyException() {
        super();
    }

    public InvalidSigningKeyException(String message) {
        super(message);
    }
}
