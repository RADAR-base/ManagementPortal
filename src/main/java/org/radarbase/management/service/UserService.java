package org.radarbase.management.service;

import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.User;
import org.radarbase.management.repository.UserRepository;
import org.radarbase.management.repository.filters.UserFilter;
import org.radarbase.management.security.SecurityUtils;
import org.radarbase.management.service.dto.RoleDTO;
import org.radarbase.management.service.dto.UserDTO;
import org.radarbase.management.service.mapper.UserMapper;
import org.radarbase.management.web.rest.errors.ConflictException;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.radarbase.auth.authorization.Permission.ROLE_UPDATE;
import static org.radarbase.auth.authorization.Permission.USER_UPDATE;
import static org.radarbase.auth.authorization.RadarAuthorization.checkGlobalPermission;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnOrganization;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnOrganizationAndProject;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarbase.auth.authorization.RoleAuthority.INACTIVE_PARTICIPANT;
import static org.radarbase.auth.authorization.RoleAuthority.PARTICIPANT;
import static org.radarbase.management.service.RoleService.getRoleAuthority;
import static org.radarbase.management.web.rest.errors.EntityName.USER;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_EMAIL_EXISTS;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_ENTITY_NOT_FOUND;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private RadarToken token;

    /**
     * Activate a user with the given activation key.
     * @param key the activation key
     * @return an {@link Optional} which is populated with the activated user if the registration
     *     key was found, and is empty otherwise.
     */
    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
                .map(user -> {
                    // activate given user for the registration key.
                    user.setActivated(true);
                    user.setActivationKey(null);
                    log.debug("Activated user: {}", user);
                    return user;
                });
    }

    /**
     * Update a user password with a given reset key.
     * @param newPassword the updated password
     * @param key the reset key
     * @return an {@link Optional} which is populated with the user whose password was reset if
     *     the reset key was found, and is empty otherwise
     */
    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);

        return userRepository.findOneByResetKey(key)
                .filter(user -> {
                    ZonedDateTime oneDayAgo = ZonedDateTime.now()
                            .minusSeconds(managementPortalProperties.getCommon()
                                    .getActivationKeyTimeoutInSeconds());
                    return user.getResetDate().isAfter(oneDayAgo);
                })
                .map(user -> {
                    user.setPassword(passwordService.encode(newPassword));
                    user.setResetKey(null);
                    user.setResetDate(null);
                    user.setActivated(true);
                    return user;
                });
    }

    /**
     * Find the deactivated user and set the user's reset key to a new random value and set their
     * reset date to now.
     * Note: We do not use activation key for activating an account. It happens by resetting
     * generated password. Resetting activation is by resetting reset-key and reset-date to now.
     * @param login the login of the user
     * @return an {@link Optional} which holds the user if an deactivated user was found with the
     *     given login, and is empty otherwise
     */
    public Optional<User> requestActivationReset(String login) {
        return userRepository.findOneByLogin(login)
            .filter((p) -> !p.getActivated())
            .map(user -> {
                user.setResetKey(passwordService.generateResetKey());
                user.setResetDate(ZonedDateTime.now());
                return user;
            });
    }

    /**
     * Set a user's reset key to a new random value and set their reset date to now.
     * @param mail the email address of the user
     * @return an {@link Optional} which holds the user if an activated user was found with the
     *     given email address, and is empty otherwise
     */
    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmail(mail)
                .filter(User::getActivated)
                .map(user -> {
                    user.setResetKey(passwordService.generateResetKey());
                    user.setResetDate(ZonedDateTime.now());
                    return user;
                });
    }

    /**
     * Add a new user to the database.
     *
     * <p>The new user will not be activated and have a random password assigned. It is the
     * responsibility of the caller to make sure the new user has a means of activating their
     * account.</p>
     * @param userDto the user information
     * @return the newly created user
     */
    public User createUser(UserDTO userDto) throws NotAuthorizedException {
        User user = new User();
        user.setLogin(userDto.getLogin());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        if (userDto.getLangKey() == null) {
            user.setLangKey("en"); // default language
        } else {
            user.setLangKey(userDto.getLangKey());
        }
        user.setPassword(passwordService.generateEncodedPassword());
        user.setResetKey(passwordService.generateResetKey());
        user.setResetDate(ZonedDateTime.now());
        user.setActivated(false);

        user.setRoles(getUserRoles(userDto.getRoles(), Set.of()));
        user = userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    private Set<Role> getUserRoles(Set<RoleDTO> roleDtos, Set<Role> oldRoles)
            throws NotAuthorizedException {
        if (roleDtos == null) {
            return null;
        }
        var roles = roleDtos.stream()
                .map(roleDto -> {
                    RoleAuthority authority = getRoleAuthority(roleDto);
                    return switch (authority.scope()) {
                        case GLOBAL -> roleService.getGlobalRole(authority);
                        case ORGANIZATION -> roleService.getOrganizationRole(authority,
                                roleDto.getOrganizationId());
                        case PROJECT -> roleService.getProjectRole(authority,
                                roleDto.getProjectId());
                    };
                })
                .collect(Collectors.toSet());

        checkAuthorityForRoleChange(roles, oldRoles);

        return roles;
    }

    private void checkAuthorityForRoleChange(Set<Role> roles, Set<Role> oldRoles)
            throws NotAuthorizedException {
        var updatedRoles = new HashSet<>(roles);
        updatedRoles.removeAll(oldRoles);
        for (Role r : updatedRoles) {
            checkAuthorityForRoleChange(r);
        }

        var removedRoles = new HashSet<>(oldRoles);
        removedRoles.removeAll(roles);
        for (Role r : removedRoles) {
            checkAuthorityForRoleChange(r);
        }
    }

    private void checkAuthorityForRoleChange(Role role)
            throws NotAuthorizedException {
        switch (role.getRole().scope()) {
            case GLOBAL -> checkGlobalPermission(token, ROLE_UPDATE);
            case ORGANIZATION -> checkPermissionOnOrganization(token, ROLE_UPDATE,
                    role.getOrganization().getName());
            case PROJECT -> {
                if (role.getProject().getOrganization() != null) {
                    checkPermissionOnOrganizationAndProject(token, ROLE_UPDATE,
                            role.getProject().getOrganization().getName(),
                            role.getProject().getProjectName());
                } else {
                    checkPermissionOnProject(token, ROLE_UPDATE,
                            role.getProject().getProjectName());
                }
            }
            default -> { }
        }
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email email id of user
     * @param langKey language key
     */
    public void updateUser(String userName, String firstName, String lastName,
            String email, String langKey) {
        Optional<User> userWithEmail = userRepository.findOneByEmail(email);
        User user;
        if (userWithEmail.isPresent()) {
            user = userWithEmail.get();
            if (!user.getLogin().equalsIgnoreCase(userName)) {
                throw new ConflictException("Email address " + email + " already in use", USER,
                        ERR_EMAIL_EXISTS, Map.of("email", email));
            }
        } else {
            user = userRepository.findOneByLogin(userName)
                    .orElseThrow(() -> new NotFoundException(
                            "User with login " + userName + " not found", USER,
                            ERR_ENTITY_NOT_FOUND, Map.of("user", userName)));
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setLangKey(langKey);
        log.debug("Changed Information for User: {}", user);
        userRepository.save(user);
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDto user to update
     * @return updated user
     */
    @Transactional
    public Optional<UserDTO> updateUser(UserDTO userDto) throws NotAuthorizedException {
        Optional<User> userOpt = userRepository.findById(userDto.getId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLogin(userDto.getLogin());
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setEmail(userDto.getEmail());
            user.setActivated(userDto.isActivated());
            user.setLangKey(userDto.getLangKey());
            Set<Role> managedRoles = user.getRoles();
            Set<Role> oldRoles = Set.copyOf(managedRoles);
            managedRoles.clear();
            managedRoles.addAll(getUserRoles(userDto.getRoles(), oldRoles));
            user = userRepository.save(user);
            log.debug("Changed Information for User: {}", user);
            return Optional.of(userMapper.userToUserDTO(user));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete the user with the given login.
     * @param login the login to delete
     */
    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            log.debug("Deleted User: {}", user);
        });
    }

    /**
     * Change the password of the user with the given login.
     * @param password the new password
     */
    public void changePassword(String password) {
        changePassword(SecurityUtils.getCurrentUserLogin(), password);
    }

    /**
     * Change the user's password.
     * @param password the new password
     * @param login of the user to change password
     */
    public void changePassword(String login, String password) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            String encryptedPassword = passwordService.encode(password);
            user.setPassword(encryptedPassword);
            log.debug("Changed password for User: {}", user);
        });
    }

    /**
     * Get a page of users.
     * @param pageable the page information
     * @return the requested page of users
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        log.debug("Request to get all Users");
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER)
                .map(userMapper::userToUserDTO);
    }

    /**
     * Get the user with the given login.
     * @param login the login
     * @return an {@link Optional} which holds the user if one was found with the given login,
     *     and is empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithRolesByLogin(login).map(userMapper::userToUserDTO);
    }

    /**
     * Get the current user.
     * @return the currently authenticated user, or null if no user is currently authenticated
     */
    @Transactional(readOnly = true)
    public User getUserWithAuthorities() {
        return userRepository.findOneWithRolesByLogin(SecurityUtils.getCurrentUserLogin())
                .orElse(null);
    }


    /**
     * Not activated users should be automatically deleted after 3 days. <p> This is scheduled to
     * get fired everyday, at 01:00 (am). This is aimed at users, not subjects. So filter our
     * users with *PARTICIPANT role and perform the action.</p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        log.info("Scheduled scan for expired user accounts starting now");
        ZonedDateTime cutoff = ZonedDateTime.now().minus(Period.ofDays(3));

        List<String> authorities = Arrays.asList(
                PARTICIPANT.authority(), INACTIVE_PARTICIPANT.authority());

        userRepository.findAllByActivatedAndAuthoritiesNot(false, authorities).stream()
                .filter(user -> revisionService.getAuditInfo(user).getCreatedAt().isBefore(cutoff))
                .forEach(user -> {
                    try {
                        userRepository.delete(user);
                        log.info("Deleted not activated user after 3 days: {}", user.getLogin());
                    } catch (DataIntegrityViolationException ex) {
                        log.error("Could not delete user with login " + user.getLogin(), ex);
                    }
                });
    }

    public Page<UserDTO> findUsers(UserFilter userFilter, Pageable pageable) {
        return userRepository.findAll(userFilter, pageable).map(userMapper::userToUserDTO);
    }

    @Transactional
    public void updateRoles(String login, Set<RoleDTO> roleDtos) throws NotAuthorizedException {
        var user = userRepository.findOneByLogin(login)
                .orElseThrow(() -> new NotFoundException(
                        "User with login " + login + " not found", USER,
                        ERR_ENTITY_NOT_FOUND, Map.of("user", login)));

        Set<Role> managedRoles = user.getRoles();
        Set<Role> oldRoles = Set.copyOf(managedRoles);
        managedRoles.clear();
        managedRoles.addAll(getUserRoles(roleDtos, oldRoles));
        userRepository.save(user);
    }
}
