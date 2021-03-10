package org.radarbase.management.security.jwt;

public class SignatureException extends RuntimeException {

    public SignatureException() {
        super();
    }

    public SignatureException(String message) {
        super(message);
    }

    public SignatureException(String message, Throwable ex) {
        super(message, ex);
    }

    public SignatureException(Throwable ex) {
        super(ex);
    }
}
