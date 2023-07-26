package uk.ac.herc.common.security.mf;


import javax.validation.constraints.NotNull;

public class MfCodeDTO {
    @NotNull
    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
