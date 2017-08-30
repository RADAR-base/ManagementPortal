package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.StringUtils;
import org.radarcns.management.config.Constants;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.security.SecurityUtils;
import org.radarcns.management.service.MailService;
import org.radarcns.management.service.UserService;
import org.radarcns.management.service.dto.ClientPairInfoDTO;
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
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    @Autowired
    private AuthorizationServerEndpointsConfiguration authorizationServerEndpointsConfiguration;

//    public AccountResource(UserRepository userRepository, UserService userService,
//            MailService mailService) {
//
//        this.userRepository = userRepository;
//        this.userService = userService;
//        this.mailService = mailService;
//    }

//    /**
//     * POST  /register : register the user.
//     *
//     * @param managedUserVM the managed user View Model
//     * @return the ResponseEntity with status 201 (Created) if the user is registered or 400 (Bad Request) if the login or email is already in use
//     */
//    @PostMapping(path = "/register",
//                    produces={MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
//    @Secured({ AuthoritiesConstants.SYS_ADMIN})
//    @Timed
//    public ResponseEntity registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
//
//        HttpHeaders textPlainHeaders = new HttpHeaders();
//        textPlainHeaders.setContentType(MediaType.TEXT_PLAIN);
//
//        return userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase())
//            .map(user -> new ResponseEntity<>("login already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
//            .orElseGet(() -> userRepository.findOneByEmail(managedUserVM.getEmail())
//                .map(user -> new ResponseEntity<>("email address already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
//                .orElseGet(() -> {
//                    User user = userService
//                        .createUser(managedUserVM.getLogin(), managedUserVM.getPassword(),
//                            managedUserVM.getFirstName(), managedUserVM.getLastName(),
//                            managedUserVM.getEmail().toLowerCase(), managedUserVM.getLangKey());
//
//                    mailService.sendActivationEmail(user);
//                    return new ResponseEntity<>(HttpStatus.CREATED);
//                })
//        );
//    }

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @return the ResponseEntity with status 200 (OK) and the activated user in body, or status 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    public ResponseEntity<String> activateAccount(@RequestParam(value = "key") String key) {
        return userService.activateRegistration(key)
            .map(user -> new ResponseEntity<String>(HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
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
     * @return the ResponseEntity with status 200 (OK) and the current user in body, or status 500 (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    @Timed
    public ResponseEntity<UserDTO> getAccount() {
        return Optional.ofNullable(userService.getUserWithAuthorities())
            .map(user -> new ResponseEntity<>(userMapper.userToUserDTO(user), HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) or 500 (Internal Server Error) if the user couldn't be updated
     */
    @PostMapping("/account")
    @Timed
    public ResponseEntity saveAccount(@Valid @RequestBody UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findOneByEmail(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userDTO.getLogin()))) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("user-management", "emailexists", "Email already in use")).body(null);
        }
        return userRepository
            .findOneByLogin(SecurityUtils.getCurrentUserLogin())
            .map(u -> {
                userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(),
                    userDTO.getLangKey());
                return new ResponseEntity(HttpStatus.OK);
            })
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * POST  /account/change_password : changes the current user's password
     *
     * @param password the new password
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) if the new password is not strong enough
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
     * POST   /account/reset_password/init : Send an email to reset the password of the user
     *
     * @param mail the mail of the user
     * @return the ResponseEntity with status 200 (OK) if the email was sent, or status 400 (Bad Request) if the email address is not registered
     */
    @PostMapping(path = "/account/reset_password/init",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity requestPasswordReset(@RequestBody String mail) {
        return userService.requestPasswordReset(mail)
            .map(user -> {
                mailService.sendPasswordResetMail(user);
                return new ResponseEntity<>("email was sent", HttpStatus.OK);
            }).orElse(new ResponseEntity<>("email address not registered", HttpStatus.BAD_REQUEST));
    }

    /**
     * POST   /account/reset_password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @return the ResponseEntity with status 200 (OK) if the password has been reset,
     * or status 400 (Bad Request) or 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = "/account/reset_password/finish",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<String> finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
            return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
        }
        return userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())
              .map(user -> new ResponseEntity<String>(HttpStatus.OK))
              .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * GET /account/pair_client/:login : delete the "login" User.
     *
     * Generates OAuth2 refresh tokens for the given user, to be used to bootstrap the
     * authentication of client apps. This will generate a refresh token which can be used to
     * access ManagementPortal and a different one for the given resource ids. This allows for a
     * client to use the MP token to add some information to the user during setup, but use a
     * different token for the resources it actually needs access to, thus increasing security.
     *
     * @param login the login of the user for whom to generate pairing information
     * @param clientId the OAuth client id
     * @param resourceIds one or more resource ids for which to generate an OAuth refresh token
     * @return the ResponseEntity with status 200 (OK)
     */
    @GetMapping("/account/{login:" + Constants.LOGIN_REGEX + "}/pair_client")
    @Timed
    @Secured({ AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN})
    public ResponseEntity<ClientPairInfoDTO> getRefreshToken(@PathVariable String login,
            @RequestParam(value="client_id") String clientId,
            @RequestParam(value="resource_id") List<String> resourceIds) {
        log.debug("REST request to generate refresh token for User: {}", login);
        Optional<UserDTO> maybeUser = userService.getUserWithAuthoritiesByLogin(login);
        if (!maybeUser.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User user = userService.getUserWithAuthorities(maybeUser.get().getId());
        User currentUser = userService.getUserWithAuthorities();

        // check if current user has project_admin role in the project the requested user is in
        // TODO finish this when roles implementation is complete
        /*
        if (!currentUser.getAuthorities().contains(AuthoritiesConstants.SYS_ADMIN) && currentUser.getRoles().stream()
            .filter(role -> role.getProject().getProjectName().equals(user.getProject().getProjectName()))
            .filter(role -> role.getAuthority().getName().equals(AuthoritiesConstants.PROJECT_ADMIN))
            .collect(Collectors.toList()).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }*/

        // add the user's authorities
        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getAuthorities().stream()
            .forEach(a -> authorities.add(new SimpleGrantedAuthority(a.getName())));

        Set<String> scope = new HashSet<>();
        scope.add("write");
        scope.add("read");

        // Create a token for ManagementPortal access
        Set<String> resources = new HashSet<>();
        resources.add("res_ManagementPortal");

        OAuth2AccessToken mpToken = createToken(clientId, user.getLogin(), authorities, scope,
            resources);

        // Create a token for the requested resource
        resources = new HashSet<>(resourceIds);

        OAuth2AccessToken resToken = createToken(clientId, user.getLogin(), authorities, scope,
            resources);

        ClientPairInfoDTO cpi = new ClientPairInfoDTO(mpToken.getRefreshToken().getValue(),
            resToken.getRefreshToken().getValue(), user.getLogin());

        return new ResponseEntity<>(cpi, HttpStatus.OK);
    }

    private OAuth2AccessToken createToken(String clientId, String login,
        Set<GrantedAuthority> authorities, Set<String> scope, Set<String> resourceIds) {
        Map<String, String> requestParameters = new HashMap<>();

        boolean approved = true;

        Set<String> responseTypes = new HashSet<>();
        responseTypes.add("code");
        Map<String, Serializable> extensionProperties = new HashMap<>();

        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, clientId,
            authorities, approved, scope,
            resourceIds, null, responseTypes, extensionProperties);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login, null, authorities);
        OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);

        AuthorizationServerTokenServices tokenServices = authorizationServerEndpointsConfiguration.getEndpointsConfigurer().getTokenServices();

        return tokenServices.createAccessToken(auth);
    }

    private boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password) &&
            password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
            password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH;
    }
}
