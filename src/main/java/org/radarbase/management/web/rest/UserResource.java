package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.domain.Subject;
import org.radarbase.management.domain.User;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.repository.UserRepository;
import org.radarbase.management.repository.filters.UserFilter;
import org.radarbase.management.service.MailService;
import org.radarbase.management.service.ResourceUriService;
import org.radarbase.management.service.UserService;
import org.radarbase.management.service.dto.RoleDTO;
import org.radarbase.management.service.dto.UserDTO;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.radarbase.management.web.rest.errors.InvalidRequestException;
import org.radarbase.management.web.rest.util.HeaderUtil;
import org.radarbase.management.web.rest.util.PaginationUtil;
import org.radarbase.management.web.rest.vm.ManagedUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.ResponseUtil;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.radarbase.auth.authorization.Permission.ROLE_READ;
import static org.radarbase.auth.authorization.Permission.ROLE_UPDATE;
import static org.radarbase.auth.authorization.Permission.USER_CREATE;
import static org.radarbase.auth.authorization.Permission.USER_DELETE;
import static org.radarbase.auth.authorization.Permission.USER_READ;
import static org.radarbase.auth.authorization.Permission.USER_UPDATE;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarbase.auth.authorization.RoleAuthority.SYS_ADMIN;
import static org.radarbase.management.web.rest.errors.EntityName.USER;

/**
 * REST controller for managing users.
 *
 * <p>This class accesses the User entity, and needs to fetch its collection of authorities.</p>
 *
 * <p>For a normal use-case, it would be better to have an eager relationship between User and
 * Authority, and send everything to the client side: there would be no View Model and DTO, a lot
 * less code, and an outer-join which would be good for performance. </p>
 *
 * <p>We use a View Model and a DTO for 3 reasons:
 * <ul>
 *     <li>We want to keep a lazy association between the user and the authorities, because
 *     people will quite often do relationships with the user, and we don't want them to get the
 *     authorities all the time for nothing (for performance reasons). This is the #1 goal: we
 *     should not impact our users' application because of this use-case.</li>
 *     <li> Not having an outer join causes n+1 requests to the database. This is not a real
 *     issue as we have by default a second-level cache. This means on the first HTTP call we do
 *     the n+1 requests, but then all authorities come from the cache, so in fact it's much
 *     better than doing an outer join (which will get lots of data from the database, for each
 *     HTTP call).</li>
 *     <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 *
 * <p>Another option would be to have a specific JPA entity graph to handle this case.</p>
 */
@RestController
@RequestMapping("/api")
public class UserResource {

    private static final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private RadarToken token;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    /**
     * POST  /users  : Creates a new user. <p> Creates a new user if the login and email are not
     * already used, and sends an mail with an activation link. The user needs to be activated on
     * creation. </p>
     *
     * @param managedUserVm the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the new user, or with
     *     status 400 (Bad Request) if the login or email is already in use
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/users")
    @Timed
    public ResponseEntity<User> createUser(@RequestBody ManagedUserVM managedUserVm)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to save User : {}", managedUserVm);
        checkPermission(token, USER_CREATE);
        if (managedUserVm.getId() != null) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert(USER, "idexists",
                            "A new user cannot already have an ID"))
                    .body(null);
            // Lowercase the user login before comparing with database
        } else if (userRepository.findOneByLogin(managedUserVm.getLogin().toLowerCase(Locale.ROOT))
                .isPresent()) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil
                            .createFailureAlert(USER, "userexists",
                                    "Login already in use"))
                    .body(null);
        } else if (userRepository.findOneByEmail(managedUserVm.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil
                            .createFailureAlert(USER, "emailexists",
                                    "Email already in use"))
                    .body(null);
        } else {
            User newUser = userService.createUser(managedUserVm);
            mailService.sendCreationEmail(newUser, managementPortalProperties.getCommon()
                    .getActivationKeyTimeoutInSeconds());
            return ResponseEntity.created(ResourceUriService.getUri(newUser))
                    .headers(HeaderUtil.createAlert("userManagement.created", newUser.getLogin()))
                    .body(newUser);
        }
    }

    /**
     * PUT  /users : Updates an existing User.
     *
     * @param managedUserVm the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated user, or with
     *     status 400 (Bad Request) if the login or email is already in use, or with status 500
     *     (Internal Server Error) if the user couldn't be updated
     */
    @PutMapping("/users")
    @Timed
    public ResponseEntity<UserDTO> updateUser(@RequestBody ManagedUserVM managedUserVm)
            throws NotAuthorizedException {
        log.debug("REST request to update User : {}", managedUserVm);
        checkPermission(token, USER_UPDATE);
        Optional<User> existingUser = userRepository.findOneByEmail(managedUserVm.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId()
                .equals(managedUserVm.getId()))) {
            throw new BadRequestException("Email already in use", USER, "emailexists");
        }
        existingUser = userRepository.findOneByLogin(managedUserVm.getLogin()
                .toLowerCase(Locale.US));
        if (existingUser.isPresent() && (!existingUser.get().getId()
                .equals(managedUserVm.getId()))) {
            throw new BadRequestException("Login already in use", USER, "emailexists");
        }

        Optional<Subject> subject = subjectRepository
                .findOneWithEagerBySubjectLogin(managedUserVm.getLogin());
        if (subject.isPresent() && managedUserVm.isActivated() && subject.get().isRemoved()) {
            // if the subject is also a user, check if the removed/activated states are valid
            throw new InvalidRequestException("Subject cannot be the user to request "
                + "this changes", USER, "error.invalidsubjectstate");

        }

        Optional<UserDTO> updatedUser = userService.updateUser(
                managedUserVm,
                token.hasAuthority(SYS_ADMIN));

        return ResponseUtil.wrapOrNotFound(updatedUser,
                HeaderUtil.createAlert("userManagement.updated", managedUserVm.getLogin()));
    }

    /**
     * GET  /users : get all users.
     *
     * @param pageable   the pagination information
     * @param userFilter filter parameters as follows.
     *      projectName Optional, if specified return only users associated this project
     *      authority Optional, if specified return only users that have this authority
     *      login Optional, if specified return only users that have this login
     *      email Optional, if specified return only users that have this email
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users")
    @Timed
    public ResponseEntity<List<UserDTO>> getUsers(
            @PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable pageable,
            UserFilter userFilter,
            @RequestParam(defaultValue = "true") boolean includeProvenance)
            throws NotAuthorizedException {
        checkPermission(token, USER_READ);

        Page<UserDTO> page = userService.findUsers(userFilter, pageable, includeProvenance);

        return new ResponseEntity<>(page.getContent(),
            PaginationUtil.generatePaginationHttpHeaders(page, "/api/users"), HttpStatus.OK);
    }

    /**
     * GET  /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "login" user, or with
     *     status 404 (Not Found)
     */
    @GetMapping("/users/{login:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<UserDTO> getUser(@PathVariable String login)
            throws NotAuthorizedException {
        log.debug("REST request to get User : {}", login);
        checkPermission(token, USER_READ);
        return ResponseUtil.wrapOrNotFound(
                userService.getUserWithAuthoritiesByLogin(login));
    }

    /**
     * DELETE /users/:login : delete the "login" User.
     *
     * @param login the login of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/users/{login:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteUser(@PathVariable String login)
            throws NotAuthorizedException {
        log.debug("REST request to delete User: {}", login);
        checkPermission(token, USER_DELETE);
        userService.deleteUser(login);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("userManagement.deleted", login))
                .build();
    }

    /**
     * Get /users/:login/roles : get the "login" User roles.
     *
     * @param login the login of the user to get roles from
     * @return the ResponseEntity with status 200 (OK)
     */
    @GetMapping("/users/{login:" + Constants.ENTITY_ID_REGEX + "}/roles")
    @Timed
    public ResponseEntity<Set<RoleDTO>> getUserRoles(@PathVariable String login)
            throws NotAuthorizedException {
        log.debug("REST request to read User roles: {}", login);
        checkPermission(token, ROLE_READ);
        return ResponseUtil.wrapOrNotFound(userService.getUserWithAuthoritiesByLogin(login)
                        .map(UserDTO::getRoles));
    }

    /**
     * PUT /users/:login/roles : update the "login" User roles.
     *
     * @param login the login of the user to get roles from
     * @return the ResponseEntity with status 200 (OK)
     */
    @PutMapping("/users/{login:" + Constants.ENTITY_ID_REGEX + "}/roles")
    @Timed
    public ResponseEntity<Void> putUserRoles(@PathVariable String login,
            @RequestBody Set<RoleDTO> roleDtos) throws NotAuthorizedException {
        log.debug("REST request to update User roles: {} to {}", login, roleDtos);
        checkPermission(token, ROLE_UPDATE);
        userService.updateRoles(login, roleDtos);
        return ResponseEntity.noContent().build();
    }
}
