package uk.ac.herc.common.security.mf;

public interface MfOptSender {
    void sendUserOpt(String userName, String otp);
}
