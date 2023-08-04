package uk.ac.herc.common.security.mf;


public class MfLoginResultDTO {

    private Boolean authenticated;

    private Integer failedAttempts;

    private MfAuthenticationExceptionReason failReason;

    public MfLoginResultDTO() {
    }

    public MfLoginResultDTO(Boolean authenticated, Integer failedAttempts) {
        this.authenticated = authenticated;
        this.failedAttempts = failedAttempts;
    }

    public MfLoginResultDTO(Boolean authenticated, Integer failedAttempts, MfAuthenticationExceptionReason failReason) {
        this.authenticated = authenticated;
        this.failedAttempts = failedAttempts;
        this.failReason = failReason;
    }

    public MfLoginResultDTO(Boolean authenticated, MfAuthenticationExceptionReason failReason) {
        this.authenticated = authenticated;
        this.failReason = failReason;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public MfAuthenticationExceptionReason getFailReason() {
        return failReason;
    }

    public void setFailReason(MfAuthenticationExceptionReason failReason) {
        this.failReason = failReason;
    }
}
