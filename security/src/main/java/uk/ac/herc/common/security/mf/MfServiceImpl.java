package uk.ac.herc.common.security.mf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static uk.ac.herc.common.security.mf.MfAuthenticationExceptionReason.*;

@Service
@Transactional
public class MfServiceImpl implements MfService {

    public static final int OTP_LENGTH = 6;
    //consider config the value
    public static final int MAX_ATTEMPT = 5;
    @Autowired
    private MfAuthenticationRepository mfAuthenticationRepository;
    @Autowired
    private MfProperties mfProperties;

    /**
     * This method will generate the otp for user userNme,
     * if the user has attempted validate mfProperties.getMaxAttemptTimes() and last otp didn't expire,
     * it will throw exception
     * But there still should be an enhancement here: stop users keep generating otp,
     * even if this can be dane by rate limiting in the Parent Application
     *
     * @param userName
     * @return otp valid in mfProperties.getExpireTime()
     */
    @Override
    public String generateOTP(String userName) {
        String otp = generateOTP();
        Date expiryDateTime = new Date(System.currentTimeMillis() + TimeUnit.valueOf(mfProperties.getTimeUnit()).toMillis(mfProperties.getExpireTime()));
        MfAuthenticationEntity mfEntity = null;
        Optional<MfAuthenticationEntity> mfEntityName = mfAuthenticationRepository.findOneByUserName(userName);
        if (!mfEntityName.isPresent()) {
            mfEntity = new MfAuthenticationEntity(userName, otp, expiryDateTime, 0);
            mfAuthenticationRepository.save(mfEntity);
        } else {
            mfEntity = mfEntityName.get();
            if (mfEntity.getFailedAttempts() >= mfProperties.getMaxAttemptTimes() && !mfEntity.isExpired()) {
                throw new MfAuthenticationException(
                    PIN_VALIDATION_ATTEMPT_EXCEEDED);
            }
            mfEntity.setPin(otp);
            mfEntity.setExpiryDateTime(expiryDateTime);
            mfEntity.setFailedAttempts(0);
            mfAuthenticationRepository.save(mfEntity);
        }
        return otp;
    }

    @Override
    public MfValidateResultDTO validateOTP(String userName, String otp) {
        if (skipValidate(otp)) {
            return new MfValidateResultDTO(true, 0);
        }
        Optional<MfAuthenticationEntity> authenticationOptional = mfAuthenticationRepository.findOneByUserName(userName);
        if (!authenticationOptional.isPresent()) {
            return new MfValidateResultDTO(false, PIN_NOT_EXIST);
        }
        MfAuthenticationEntity authentication = authenticationOptional.get();
        if (authentication.isExpired()) {
            return new MfValidateResultDTO(false, authentication.getFailedAttempts(), PIN_HAS_EXPIRED);
        }
        Integer failedAttempts = authentication.getFailedAttempts();
        if (failedAttempts >= mfProperties.getMaxAttemptTimes()) {
            return new MfValidateResultDTO(false, failedAttempts, PIN_VALIDATION_ATTEMPT_EXCEEDED);
        } else {
            Boolean isPinValid = authentication.getPin().equals(otp);
            if (!isPinValid) {
                failedAttempts++;
                authentication.setFailedAttempts(failedAttempts);
                mfAuthenticationRepository.save(authentication);
                return new MfValidateResultDTO(isPinValid, authentication.getFailedAttempts(), PIN_NOT_MATCH);
            }
            return new MfValidateResultDTO(isPinValid, authentication.getFailedAttempts());
        }
    }

    protected boolean skipValidate(String pin) {
        return false;
    }

    private String generateOTP() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            code.append(randomInteger(0, 9));
        }

        return code.toString();
    }

    private int randomInteger(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }
}
