package uk.ac.herc.common.security.mf;


public class MfValidateResultDTO {

    private Boolean success;

    private Integer failedAttempts;

    private MfAuthenticationExceptionReason failReason;

    public MfValidateResultDTO() {
    }

    public MfValidateResultDTO(Boolean success, Integer failedAttempts) {
        this.success = success;
        this.failedAttempts = failedAttempts;
    }

    public MfValidateResultDTO(Boolean success, Integer failedAttempts, MfAuthenticationExceptionReason failReason) {
        this.success = success;
        this.failedAttempts = failedAttempts;
        this.failReason = failReason;
    }

    public MfValidateResultDTO(Boolean success, MfAuthenticationExceptionReason failReason) {
        this.success = success;
        this.failReason = failReason;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
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
