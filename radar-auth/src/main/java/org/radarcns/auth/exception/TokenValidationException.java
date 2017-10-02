package org.radarcns.auth.exception;

/**
 * Created by dverbeec on 15/09/2017.
 */
public class TokenValidationException extends RuntimeException {
    public TokenValidationException() {
        super();
    }

    public TokenValidationException(String message) {
        super(message);
    }

    public TokenValidationException(Throwable cause) {
        super(cause);
    }
}
