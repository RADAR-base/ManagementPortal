package uk.ac.herc.common.security.mf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "mf")
public class MfProperties {
    private Boolean enable = false;

    private Long expireTime = 30L;

    private String timeUnit = TimeUnit.MINUTES.name();

    private Integer maxAttemptTimes = 5;

    private Boolean authBeforeGenerateOtp = false;

    private Boolean sendOtp = true;

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public Integer getMaxAttemptTimes() {
        return maxAttemptTimes;
    }

    public void setMaxAttemptTimes(Integer maxAttemptTimes) {
        this.maxAttemptTimes = maxAttemptTimes;
    }

    public Boolean getAuthBeforeGenerateOtp() {
        return authBeforeGenerateOtp;
    }

    public void setAuthBeforeGenerateOtp(Boolean authBeforeGenerateOtp) {
        this.authBeforeGenerateOtp = authBeforeGenerateOtp;
    }

    public Boolean getSendOtp() {
        return sendOtp;
    }

    public void setSendOtp(Boolean sendOtp) {
        this.sendOtp = sendOtp;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
