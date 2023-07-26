package uk.ac.herc.common.security.mf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static uk.ac.herc.common.security.Constants.AUTHORIZATION_HEADER;
import static uk.ac.herc.common.security.mf.MfAuthenticationExceptionReason.MFOTPSENDER_NOT_CONFIGURED;


@Controller
@RequestMapping("/api")
public class MfController {

    @Autowired
    private MfProperties properties;

    @Autowired
    private MfService mfService;

    @PostMapping(path = "/mf-authenticate/code")
    public ResponseEntity<Boolean> sendMfCode(@Valid @RequestBody MfCodeDTO login) {
        if (properties.getAuthBeforeGenerateOtp()) {
            AuthenticationManager authenticationManager = ApplicationContextHolder.getBean(AuthenticationManagerBuilder.class).getObject();
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login.getUserName(), login.getPassword());
            authenticationManager.authenticate(authenticationToken);
        }
        String otp = mfService.generateOTP(login.getUserName());
        if (properties.getSendOtp()) {
            MfOptSender sender = ApplicationContextHolder.getBeanIfExist(MfOptSender.class);
            if (null == sender) {
                throw new MfAuthenticationException(MFOTPSENDER_NOT_CONFIGURED);
            }
            sender.sendUserOpt(login.getUserName(), otp);
        }
        return ResponseEntity.ok(true);
    }

    @PostMapping(value = "/mf-authenticate")
    public ResponseEntity<String> authenticate(@Valid @RequestBody MfAuthDTO loginVM) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginVM.getUserName(),
                loginVM.getPassword()
        );
        AuthenticationManagerBuilder authenticationManagerBuilder = ApplicationContextHolder.getBean(AuthenticationManagerBuilder.class);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        MfValidateResultDTO validateOTPResult = mfService.validateOTP(loginVM.getUserName(), loginVM.getCode());
        if (!validateOTPResult.getSuccess()) {
            throw new MfAuthenticationException(validateOTPResult.getFailReason(), validateOTPResult.getFailedAttempts());
        }

        //Either provide JWT Authentication
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Object jwtTokenProvider = ApplicationContextHolder.getBeanIfExist("tokenProvider");
        if (null == jwtTokenProvider)
            throw new MfAuthenticationException(MfAuthenticationExceptionReason.JWTTOKENPROVIDER_NOT_CONFIGURED);
        Method createTokenMethod = jwtTokenProvider.getClass().getMethod("createToken", Authentication.class, boolean.class);
        String jwt = (String) createTokenMethod.invoke(jwtTokenProvider, authentication, loginVM.isRememberMe());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER, "Bearer " + jwt);

        //OR Call the application's default authentication api

        return new ResponseEntity(jwt, httpHeaders, HttpStatus.OK);
    }

    public MfProperties getProperties() {
        return properties;
    }

    public void setProperties(MfProperties properties) {
        this.properties = properties;
    }

    public MfService getMfService() {
        return mfService;
    }

    public void setMfService(MfService mfService) {
        this.mfService = mfService;
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;

        public JWTToken() {
        }

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
