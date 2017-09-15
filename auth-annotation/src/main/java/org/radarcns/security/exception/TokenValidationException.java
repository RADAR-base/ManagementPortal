package org.radarcns.security.exception;

import org.radarcns.security.filter.TokenAuthenticationFilter;

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

    public TokenValidationException(Exception cause) {
        super(cause);
    }
}
