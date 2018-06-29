package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.security.SecurityUtils;
import org.radarcns.management.service.MailService;
import org.radarcns.management.service.UserService;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.UserMapper;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.radarcns.management.web.rest.vm.KeyAndPasswordVM;
import org.radarcns.management.web.rest.vm.ManagedUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserMapper userMapper;

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
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
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
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
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
    public ResponseEntity saveAccount(@Valid @RequestBody UserDTO userDto) {
        Optional<User> existingUser = userRepository.findOneByEmail(userDto.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin()
                .equalsIgnoreCase(userDto.getLogin()))) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert("user-management", "emailexists", "Email already in use"))
                    .body(null);
        }
        return userRepository
                .findOneByLogin(SecurityUtils.getCurrentUserLogin())
                .map(u -> {
                    userService.updateUser(userDto.getFirstName(), userDto.getLastName(),
                            userDto.getEmail(),
                            userDto.getLangKey());
                    return new ResponseEntity(HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
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
    public ResponseEntity changePassword(@RequestBody String password) {
        if (!checkPasswordLength(password)) {
            return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
        }
        userService.changePassword(password);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * POST   /account/reset-activation/init : Send an email to resend the password activation
     * for the the user.
     *
     * @param login the login of the user
     * @return the ResponseEntity with status 200 (OK) if the email was sent, or status 400 (Bad
     *     Request) if the email address is not registered or user is not deactivated
     */
    @PostMapping(path = "/account/reset-activation/init",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity requestActivationReset(@RequestBody String login) {
        return userService.requestActivationReset(login)
            .map(user -> {
                // this will be the similar email with newly set reset-key
                mailService.sendCreationEmail(user);
                return new ResponseEntity<>("Activation email was sent", HttpStatus.OK);
            }).orElse(new ResponseEntity<>("Cannot find a deactivated user with login " + login,
                HttpStatus.BAD_REQUEST));
    }


    /**
     * POST   /account/reset_password/init : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user
     * @return the ResponseEntity with status 200 (OK) if the email was sent, or status 400 (Bad
     *     Request) if the email address is not registered
     */
    @PostMapping(path = "/account/reset_password/init",
            produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity requestPasswordReset(@RequestBody String mail) {
        return userService.requestPasswordReset(mail)
                .map(user -> {
                    mailService.sendPasswordResetMail(user);
                    return new ResponseEntity<>("email was sent", HttpStatus.OK);
                }).orElse(new ResponseEntity<>("email address not registered",
                        HttpStatus.BAD_REQUEST));
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
    public ResponseEntity<String> finishPasswordReset(
            @RequestBody KeyAndPasswordVM keyAndPassword) {
        if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
            return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
        }
        return userService
                .completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())
                .map(user -> new ResponseEntity<String>(HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password)
                && password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH
                && password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH;
    }
}
