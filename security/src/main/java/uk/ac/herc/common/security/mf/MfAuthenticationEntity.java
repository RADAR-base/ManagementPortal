package uk.ac.herc.common.security.mf;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "two_factor_authentication")
public class MfAuthenticationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "user_name")
    private String userName;
    @NotNull
    private String pin;
    @NotNull
    @Column(name = "expiry_date_time")
    private Date expiryDateTime;
    @NotNull
    @Column(name = "failed_attempts")
    private Integer failedAttempts;

    public MfAuthenticationEntity() {

    }

    public MfAuthenticationEntity(String userName, String pin, Date expiryDateTime, Integer failedAttempts) {
        this.userName = userName;
        this.pin = pin;
        this.expiryDateTime = expiryDateTime;
        this.failedAttempts = failedAttempts;
    }

    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Date getExpiryDateTime() {
        return expiryDateTime;
    }

    public void setExpiryDateTime(Date expiryDateTime) {
        this.expiryDateTime = expiryDateTime;
    }

    public boolean isExpired() {
        Date now = new Date();
        return now.after(expiryDateTime);
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    @Override
    public String toString() {
        return "[ " + "authenticationId" + id + " " + "userName= " + userName + " " + "pin= " + pin + " " + "expiryDateTime= "
                + expiryDateTime + "failedAttempts= " + failedAttempts + "]";
    }
}
