package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.domain.User;
import org.radarbase.management.security.SessionRadarToken;
import org.radarbase.management.service.MailService;
import org.radarbase.management.service.UserService;
import org.radarbase.management.service.dto.UserDTO;
import org.radarbase.management.service.mapper.UserMapper;
import org.radarbase.management.service.util.PasswordUtil;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.radarbase.management.web.rest.errors.RadarWebApplicationException;
import org.radarbase.management.web.rest.vm.KeyAndPasswordVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.radarbase.management.security.JwtAuthenticationFilter.TOKEN_ATTRIBUTE;
import static org.radarbase.management.web.rest.errors.EntityName.USER;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_ACCESS_DENIED;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_EMAIL_NOT_REGISTERED;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_PASSWORD_TOO_LONG;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_PASSWORD_TOO_WEAK;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static final Logger log = LoggerFactory.getLogger(AccountResource.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired(required = false)
    private RadarToken token;

    private final PasswordUtil passwordUtil = new PasswordUtil();

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @return the ResponseEntity with status 200 (OK) and the activated user in body, or status 500
     *     (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    public ResponseEntity<String> activateAccount(@RequestParam(value = "key") String key) {
        return userService.activateRegistration(key)
                .map(user -> new ResponseEntity<String>(HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /login : check if the user is authenticated.
     *
     * @param session the HTTP session
     * @return user account details if the user is authenticated
     */
    @PostMapping("/login")
    @Timed
    public ResponseEntity<UserDTO> login(HttpSession session) {
        log.debug("Logging in user to session with principal {}", token.getUsername());
        RadarToken sessionToken = new SessionRadarToken(token);
        session.setAttribute(TOKEN_ATTRIBUTE, sessionToken);
        return getAccount();
    }

    /**
     * POST /logout : log out.
     *
     * @param request the HTTP request
     * @return no content response if the user is authenticated
     */
    @PostMapping("/logout")
    @Timed
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        log.debug("Unauthenticate a user");
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * GET  /account : get the current user.
     *
     * @return the ResponseEntity with status 200 (OK) and the current user in body, or status 500
     *     (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    @Timed
    public ResponseEntity<UserDTO> getAccount() {
        return Optional.ofNullable(userService.getUserWithAuthorities())
                .map(user -> new ResponseEntity<>(userMapper.userToUserDTO(user), HttpStatus.OK))
                .orElseThrow(() -> new RadarWebApplicationException(Response.Status.FORBIDDEN,
                        "Cannot get account without user", USER, ERR_ACCESS_DENIED));
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDto the current user information
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) or 500 (Internal
     *     Server Error) if the user couldn't be updated
     */
    @PostMapping("/account")
    @Timed
    public ResponseEntity<Void> saveAccount(@Valid @RequestBody UserDTO userDto,
            Authentication authentication) {
        if (authentication.getPrincipal() == null) {
            throw new RadarWebApplicationException(Response.Status.FORBIDDEN,
                    "Cannot update account without user", USER, ERR_ACCESS_DENIED);
        }

        userService.updateUser(authentication.getName(), userDto.getFirstName(),
                userDto.getLastName(), userDto.getEmail(), userDto.getLangKey());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * POST  /account/change_password : changes the current user's password.
     *
     * @param password the new password
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) if the new
     *     password is not strong enough
     */
    @PostMapping(path = "/account/change_password",
            produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<String> changePassword(@RequestBody String password) {
        checkPasswordLength(password);
        userService.changePassword(password);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    /**
     * POST   /account/reset-activation/init : Send an email to resend the password activation
     * for the the user.
     *
     * @param login the login of the user
     * @return the ResponseEntity with status 200 (OK) if the email was sent, or status 400 (Bad
     *     Request) if the email address is not registered or user is not deactivated
     */
    @PostMapping(path = "/account/reset-activation/init")
    @Timed
    public ResponseEntity<Void>  requestActivationReset(@RequestBody String login) {
        User user = userService.requestActivationReset(login)
                .orElseThrow(() -> new BadRequestException(
                        "Cannot find a deactivated user with login " + login,
                        USER, ERR_EMAIL_NOT_REGISTERED));

        mailService.sendCreationEmail(user, managementPortalProperties.getCommon()
                .getActivationKeyTimeoutInSeconds());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * POST   /account/reset_password/init : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user
     * @return the ResponseEntity with status 200 (OK) if the email was sent, or status 400 (Bad
     *     Request) if the email address is not registered
     */
    @PostMapping(path = "/account/reset_password/init")
    @Timed
    public ResponseEntity<Void>  requestPasswordReset(@RequestBody String mail) {
        User user = userService.requestPasswordReset(mail)
                .orElseThrow(() -> new BadRequestException("email address not registered",
                        USER, ERR_EMAIL_NOT_REGISTERED));

        mailService.sendPasswordResetMail(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * POST   /account/reset_password/finish : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password
     * @return the ResponseEntity with status 200 (OK) if the password has been reset, or status 400
     *     (Bad Request) or 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = "/account/reset_password/finish",
            produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<Void> finishPasswordReset(
            @RequestBody KeyAndPasswordVM keyAndPassword) {
        checkPasswordLength(keyAndPassword.getNewPassword());
        return userService
                .completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())
                .map(user -> new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private void checkPasswordLength(String password) {
        if (passwordUtil.isPasswordWeak(password)) {
            throw new BadRequestException("Weak password. Use a password with more variety of"
                    + "numeric, alphabetical and symbol characters.", USER, ERR_PASSWORD_TOO_WEAK);
        } else if (password.length() > 100) {
            throw new BadRequestException("Password too long", USER, ERR_PASSWORD_TOO_LONG);
        }
    }
}
