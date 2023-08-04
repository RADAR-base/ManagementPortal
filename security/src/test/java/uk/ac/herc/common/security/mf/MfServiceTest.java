package uk.ac.herc.common.security.mf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.herc.common.security.TestApp;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@SpringBootTest(classes = TestApp.class)
public class MfServiceTest {

    @Autowired
    private MfService mfService;

    @Autowired
    private MfAuthenticationRepository repository;

    @Autowired
    private MfProperties mfProperties;

    @AfterEach
    public void cleanDB(){
        repository.deleteAll();
    }

    @Test
    public void contextLoads(){
        assertThat(mfService).isNotNull();
        assertThat(repository).isNotNull();
    }

    @Test
    public void testGenerate(){
        String username = "test";
        String otp = mfService.generateOTP(username);
        assertThat(otp.length()).isEqualTo(6);
        assertThat(repository.findOneByUserName(username).isPresent()).isTrue();
    }

    @Test
    public void testGenerateFailWhenExceedMaxAttemps(){
        String username = "test";
        String otp = mfService.generateOTP(username);
        tryMaxAttempTimes(username, otp+"_1");
        assertThrows(MfAuthenticationException.class, ()->{
            mfService.generateOTP(username);
        });
    }

    @Test
    public void generateSuccessWhenExceedMaxAttempsButOtpExpired(){
        String username = "test";
        String otp = mfService.generateOTP(username);
        tryMaxAttempTimes(username, otp+"_1");
        expireOTP(username);
        String otp2 = mfService.generateOTP(username);
        assertThat(otp2.length()).isEqualTo(6);
    }

    @Test
    public void testValidateSuccess(){
        String username = "test";
        String otp = mfService.generateOTP(username);
        MfValidateResultDTO validateResultDTO = mfService.validateOTP(username, otp);
        assertThat(validateResultDTO.getSuccess()).isTrue();
    }

    @Test
    public void testValidateNotExistOTP(){
        String username = "test";
        MfValidateResultDTO validateResultDTO = mfService.validateOTP(username, "notexist");
        assertThat(validateResultDTO.getSuccess()).isFalse();
        assertThat(validateResultDTO.getFailReason()).isEqualTo(MfAuthenticationExceptionReason.PIN_NOT_EXIST);
        assertThat(validateResultDTO.getFailedAttempts()).isEqualTo(null);
    }

    @Test
    public void testValidateNotMatchOTP(){
        String username = "test";
        String otp = mfService.generateOTP(username);
        MfValidateResultDTO validateResultDTO = mfService.validateOTP(username, otp+"-1");
        assertThat(validateResultDTO.getSuccess()).isFalse();
        assertThat(validateResultDTO.getFailReason()).isEqualTo(MfAuthenticationExceptionReason.PIN_NOT_MATCH);
        assertThat(validateResultDTO.getFailedAttempts()).isEqualTo(1);
    }

    @Test
    public void testValidateExpiredOTP(){
        String username = "test";
        String otp = mfService.generateOTP(username);
        expireOTP(username);
        MfValidateResultDTO validateResultDTO = mfService.validateOTP(username, otp);
        assertThat(validateResultDTO.getSuccess()).isFalse();
        assertThat(validateResultDTO.getFailReason()).isEqualTo(MfAuthenticationExceptionReason.PIN_HAS_EXPIRED);
        assertThat(validateResultDTO.getFailedAttempts()).isEqualTo(0);
    }

    @Test
    public void testValidateExceedMaxAttemps(){
        String username = "test";
        String otp = mfService.generateOTP(username);
        String wrongOTP = otp+"_1";
        tryMaxAttempTimes(username, wrongOTP);
        MfValidateResultDTO validateResultDTO = mfService.validateOTP(username, wrongOTP);
        assertThat(validateResultDTO.getSuccess()).isFalse();
        assertThat(validateResultDTO.getFailReason()).isEqualTo(MfAuthenticationExceptionReason.PIN_VALIDATION_ATTEMPT_EXCEEDED);
        assertThat(validateResultDTO.getFailedAttempts()).isEqualTo(5);
    }

    private void tryMaxAttempTimes(String username, String wrongOTP) {
        for(int i=0; i<MfServiceImpl.MAX_ATTEMPT; i++){
            mfService.validateOTP(username, wrongOTP);
        }
    }

    private void expireOTP(String username) {
        MfAuthenticationEntity mfAuthenticationEntity = repository.findOneByUserName(username).get();
        mfAuthenticationEntity.setExpiryDateTime(new Date(System.currentTimeMillis()-10));
        repository.save(mfAuthenticationEntity);
    }

}
