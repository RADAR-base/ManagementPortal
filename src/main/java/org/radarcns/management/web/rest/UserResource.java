package org.radarcns.management.web.rest;

import static org.radarcns.auth.authorization.Permission.PROJECT_READ;
import static org.radarcns.auth.authorization.Permission.USER_CREATE;
import static org.radarcns.auth.authorization.Permission.USER_DELETE;
import static org.radarcns.auth.authorization.Permission.USER_READ;
import static org.radarcns.auth.authorization.Permission.USER_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.management.security.SecurityUtils.getJWT;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.radarcns.auth.config.Constants;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.service.MailService;
import org.radarcns.management.service.UserService;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.web.rest.errors.CustomParameterizedException;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.radarcns.management.web.rest.util.PaginationUtil;
import org.radarcns.management.web.rest.vm.ManagedUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
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

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    private static final String ENTITY_NAME = "userManagement";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private HttpServletRequest servletRequest;

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
    public ResponseEntity createUser(@RequestBody ManagedUserVM managedUserVm)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to save User : {}", managedUserVm);
        checkPermission(getJWT(servletRequest), USER_CREATE);
        if (managedUserVm.getId() != null) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists",
                            "A new user cannot already have an ID"))
                    .body(null);
            // Lowercase the user login before comparing with database
        } else if (userRepository.findOneByLogin(managedUserVm.getLogin().toLowerCase())
                .isPresent()) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil
                            .createFailureAlert(ENTITY_NAME, "userexists",
                                    "Login already in use"))
                    .body(null);
        } else if (userRepository.findOneByEmail(managedUserVm.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil
                            .createFailureAlert(ENTITY_NAME, "emailexists",
                                    "Email already in use"))
                    .body(null);
        } else if (managedUserVm.getRoles() == null || managedUserVm.getRoles().isEmpty()) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "rolesRequired",
                            "One or more roles are required"))
                    .body(null);
        } else {
            User newUser = userService.createUser(managedUserVm);
            mailService.sendCreationEmail(newUser);
            return ResponseEntity.created(new URI(HeaderUtil.buildPath("api", "users",
                    newUser.getLogin())))
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
        checkPermission(getJWT(servletRequest), USER_UPDATE);
        Optional<User> existingUser = userRepository.findOneByEmail(managedUserVm.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId()
                .equals(managedUserVm.getId()))) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "emailexists", "Email already in use"))
                    .body(null);
        }
        existingUser = userRepository.findOneByLogin(managedUserVm.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId()
                .equals(managedUserVm.getId()))) {
            return ResponseEntity.badRequest().headers(HeaderUtil
                    .createFailureAlert(ENTITY_NAME, "userexists", "Login already in use"))
                    .body(null);
        }
        if (managedUserVm.getRoles() == null || managedUserVm.getRoles().isEmpty()) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "rolesRequired"))
                    .body(null);
        }

        Optional<Subject> subject = subjectRepository
                .findOneWithEagerBySubjectLogin(managedUserVm.getLogin());
        if (subject.isPresent() && managedUserVm.isActivated() && subject.get().isRemoved()) {
            // if the subject is also a user, check if the removed/activated states are valid
            throw new CustomParameterizedException("error.invalidsubjectstate");

        }

        Optional<UserDTO> updatedUser = userService.updateUser(managedUserVm);

        return ResponseUtil.wrapOrNotFound(updatedUser,
                HeaderUtil.createAlert("userManagement.updated", managedUserVm.getLogin()));
    }

    /**
     * GET  /users : get all users.
     *
     * @param pageable the pagination information
     * @param projectName Optional, if specified return only users associated with this project
     * @param authority Optional, if specified return only users that have this authority
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users")
    @Timed
    public ResponseEntity<List<UserDTO>> getAllUsers(@ApiParam Pageable pageable,
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "authority", required = false) String authority)
            throws NotAuthorizedException {
        checkPermission(getJWT(servletRequest), USER_READ);
        Page<UserDTO> page;
        if (projectName != null && authority != null) {
            page = userService.findAllByProjectNameAndAuthority(pageable, projectName, authority);
        } else if (projectName != null && authority == null) {
            page = userService.findAllByProjectName(pageable, projectName);
        } else if (projectName == null && authority != null) {
            page = userService.findAllByAuthority(pageable, authority);
        } else {
            page = userService.getAllManagedUsers(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
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
        checkPermission(getJWT(servletRequest), USER_READ);
        return ResponseUtil.wrapOrNotFound(
                userService.getUserWithAuthoritiesByLogin(login));
    }

    /**
     * Returns all project if the user is s `SYS_ADMIN`. Otherwise projects that are assigned to a
     * user using roles.
     */
    @GetMapping("/users/{login:" + Constants.ENTITY_ID_REGEX + "}/projects")
    @Timed
    public List<ProjectDTO> getUserProjects(@PathVariable String login)
            throws NotAuthorizedException {
        log.debug("REST request to get User's project : {}", login);
        checkPermission(getJWT(servletRequest), PROJECT_READ);
        return userService.getProjectsAssignedToUser(login);
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
        checkPermission(getJWT(servletRequest), USER_DELETE);
        userService.deleteUser(login);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("userManagement.deleted", login))
                .build();
    }
}
