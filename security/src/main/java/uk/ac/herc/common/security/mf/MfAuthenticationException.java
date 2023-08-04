package uk.ac.herc.common.security.mf;

public class MfAuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private MfAuthenticationExceptionReason reason;

    private Integer failedAttempts;

    public MfAuthenticationException(MfAuthenticationExceptionReason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public MfAuthenticationException(MfAuthenticationExceptionReason reason, int failedAttempts) {
        this(reason);
        this.failedAttempts = failedAttempts;
    }

    public String getMessage() {
        return reason.getMessage();
    }

    public MfAuthenticationExceptionReason getReason() {
        return reason;
    }

    public void setReason(MfAuthenticationExceptionReason reason) {
        this.reason = reason;
    }

}
