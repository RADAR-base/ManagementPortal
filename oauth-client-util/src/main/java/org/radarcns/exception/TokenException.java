package org.radarcns.exception;

/**
 * Created by dverbeec on 31/08/2017.
 */
public class TokenException extends RuntimeException {
    public TokenException() {
        super();
    }

    public TokenException(String message) {
        super(message);
    }

    public TokenException(Throwable cause) {
        super(cause);
    }
}
