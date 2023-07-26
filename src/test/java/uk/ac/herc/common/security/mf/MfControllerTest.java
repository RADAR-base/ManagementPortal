package uk.ac.herc.common.security.mf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import uk.ac.herc.common.security.JwtTokenProvider;
import uk.ac.herc.common.security.TestApp;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TestApp.class)
public class MfControllerTest {

    @Autowired
    MfController mfController;

    @Autowired
    MfAuthenticationRepository repository;

    private MockedStatic<ApplicationContextHolder> contextHolderMockedStatic;

    @BeforeEach
    public void beforeEach() {
        contextHolderMockedStatic = Mockito.mockStatic(ApplicationContextHolder.class);
    }

    @AfterEach
    public void afterEach() {
        contextHolderMockedStatic.close();
    }

    @Test
    public void testGenerateAndSendFailBecauseNoSenderConfigured(){
        mfController.getProperties().setSendOtp(true);
        mfController.getProperties().setAuthBeforeGenerateOtp(false);
        MfCodeDTO dto = new MfCodeDTO();
        dto.setUserName("test");
        assertThrows(MfAuthenticationException.class, ()->{
            mfController.sendMfCode(dto);
        });
    }

    @Test
    public void testGenerateAndNoSend(){
        String otp = generateAndNoSend("test");
        assertThat(otp.length()).isEqualTo(6);
    }

    @Test
    public void testGenerateAndSend(){
        mfController.getProperties().setAuthBeforeGenerateOtp(false);
        MfCodeDTO dto = new MfCodeDTO();
        dto.setUserName("test");
        MfOptSender sender = Mockito.mock(MfOptSender.class);
        contextHolderMockedStatic.when(() -> ApplicationContextHolder.getBeanIfExist(MfOptSender.class))
                .thenReturn(sender);
        doNothing().when(sender).sendUserOpt(anyString(), anyString());
        ResponseEntity<Boolean> responseEntity = mfController.sendMfCode(dto);
        assertThat(responseEntity.getBody()).isTrue();
    }

    @Test
    public void testGenerateAndSendAndAuthenticateSuccess() {
        mfController.getProperties().setAuthBeforeGenerateOtp(true);
        MfCodeDTO dto = new MfCodeDTO();
        dto.setUserName("test");
        dto.setPassword("password");

        MfOptSender sender = Mockito.mock(MfOptSender.class);

        contextHolderMockedStatic.when(() -> ApplicationContextHolder.getBeanIfExist(MfOptSender.class))
                .thenReturn(sender);

        AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
        contextHolderMockedStatic.when(() -> ApplicationContextHolder.getBean(AuthenticationManager.class))
                .thenReturn(authenticationManager);

        doReturn(new UsernamePasswordAuthenticationToken(dto.getUserName(), dto.getPassword())).when(authenticationManager).authenticate(any());
        doNothing().when(sender).sendUserOpt(anyString(), anyString());

        ResponseEntity<Boolean> responseEntity = mfController.sendMfCode(dto);
        assertThat(responseEntity.getBody()).isTrue();
    }

    @Test
    public void testGenerateAndSendAndAuthenticateFail() {
        mfController.getProperties().setAuthBeforeGenerateOtp(true);
        MfCodeDTO dto = new MfCodeDTO();
        dto.setUserName("test");

        MfOptSender sender = Mockito.mock(MfOptSender.class);

        contextHolderMockedStatic.when(() -> ApplicationContextHolder.getBeanIfExist(MfOptSender.class))
                .thenReturn(sender);

        AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
        contextHolderMockedStatic.when(() -> ApplicationContextHolder.getBean(AuthenticationManager.class))
                .thenReturn(authenticationManager);

        doThrow(new BadCredentialsException("error")).when(authenticationManager).authenticate(any());
        doNothing().when(sender).sendUserOpt(anyString(), anyString());

        assertThrows(BadCredentialsException.class, ()->{
            mfController.sendMfCode(dto);
        });
    }

    @Test
    public void validateAndAuthFail(){
        String code = generateAndNoSend("test");
        MfAuthDTO mfAuthDTO = new MfAuthDTO();
        mfAuthDTO.setUserName("test");
        mfAuthDTO.setPassword("password");
        mfAuthDTO.setCode(code);
        AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
        contextHolderMockedStatic.when(() -> ApplicationContextHolder.getBean(AuthenticationManager.class))
                .thenReturn(authenticationManager);
        doThrow(new BadCredentialsException("error")).when(authenticationManager).authenticate(any());
        assertThrows(BadCredentialsException.class, ()->{
            mfController.authenticate(mfAuthDTO);
        });
    }

    @Test
    public void validateAndAuthSuccessButNoJwtProvider(){
        String code = generateAndNoSend("test");
        MfAuthDTO mfAuthDTO = new MfAuthDTO();
        mfAuthDTO.setUserName("test");
        mfAuthDTO.setPassword("password");
        mfAuthDTO.setCode(code);
        AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
        contextHolderMockedStatic.when(() -> ApplicationContextHolder.getBean(AuthenticationManager.class))
                .thenReturn(authenticationManager);
        doReturn(new UsernamePasswordAuthenticationToken(mfAuthDTO.getUserName(), mfAuthDTO.getPassword())).when(authenticationManager).authenticate(any());

        assertThrows(MfAuthenticationException.class, ()->{
            mfController.authenticate(mfAuthDTO);
        });
    }

    @Test
    public void validateAndAuthSuccessAndCreateJwtToken() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String code = generateAndNoSend("test");
        MfAuthDTO mfAuthDTO = new MfAuthDTO();
        mfAuthDTO.setUserName("test");
        mfAuthDTO.setPassword("password");
        mfAuthDTO.setCode(code);
        AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
        contextHolderMockedStatic.when(() -> ApplicationContextHolder.getBean(AuthenticationManager.class))
                .thenReturn(authenticationManager);
        doReturn(new UsernamePasswordAuthenticationToken(mfAuthDTO.getUserName(), mfAuthDTO.getPassword())).when(authenticationManager).authenticate(any());
        JwtTokenProvider jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        contextHolderMockedStatic.when(() -> ApplicationContextHolder.getBeanIfExist(JwtTokenProvider.class))
                .thenReturn(jwtTokenProvider);
        doReturn("token").when(jwtTokenProvider).createToken(any(), anyBoolean());
        ResponseEntity<String> authenticate = mfController.authenticate(mfAuthDTO);
        assertThat(authenticate.getBody()).isNotNull();
    }

    private String generateAndNoSend(String userName) {
        mfController.getProperties().setSendOtp(false);
        mfController.getProperties().setAuthBeforeGenerateOtp(false);
        MfCodeDTO dto = new MfCodeDTO();
        dto.setUserName(userName);
        ResponseEntity<Boolean> responseEntity = mfController.sendMfCode(dto);
        assertThat(responseEntity.getBody()).isTrue();
        return repository.findOneByUserName(userName).get().getPin();
    }

}
