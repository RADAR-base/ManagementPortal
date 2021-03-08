package org.radarbase.exception;

import java.security.GeneralSecurityException;

/**
 * Created by dverbeec on 31/08/2017.
 */
public class TokenException extends GeneralSecurityException {

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
