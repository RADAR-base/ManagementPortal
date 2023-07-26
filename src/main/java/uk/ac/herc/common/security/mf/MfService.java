package uk.ac.herc.common.security.mf;

public interface MfService {
    String generateOTP(String userName);

    MfValidateResultDTO validateOTP(String userName, String pin);
}
