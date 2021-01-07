package org.radarcns.management.service;

import static org.radarcns.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PARTICIPANT;
import static org.radarcns.management.web.rest.errors.EntityName.USER;

import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import java.util.stream.Collectors;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.auth.config.Constants;
import org.radarcns.management.config.ManagementPortalProperties;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.AuthorityRepository;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.repository.RoleRepository;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.repository.filters.UserFilter;
import org.radarcns.management.security.SecurityUtils;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.RoleDTO;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.UserMapper;
import org.radarcns.management.service.util.RandomUtil;
import org.radarcns.management.web.rest.errors.ErrorConstants;
import org.radarcns.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

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
                    user.setPassword(passwordEncoder.encode(newPassword));
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
                user.setResetKey(RandomUtil.generateResetKey());
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
                    user.setResetKey(RandomUtil.generateResetKey());
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
    public User createUser(UserDTO userDto) {
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
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(ZonedDateTime.now());
        user.setActivated(false);

        user.setRoles(getUserRoles(userDto));
        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    private Set<Role> getUserRoles(UserDTO userDto) {
        Set<Role> roles = new HashSet<>();
        for (RoleDTO roleDto : userDto.getRoles()) {
            Optional<Role> role = roleRepository.findOneByProjectIdAndAuthorityName(
                    roleDto.getProjectId(), roleDto.getAuthorityName());
            if (!role.isPresent() || role.get().getId() == null) {
                Role currentRole = new Role();
                // supplied authorityname can be anything, so check if we actually have one
                currentRole.setAuthority(
                        authorityRepository.findByAuthorityName(roleDto.getAuthorityName())
                        .orElseThrow(() -> new NotFoundException("Authority not found with "
                            + "authorityName", USER, ErrorConstants.ERR_INVALID_AUTHORITY,
                            Collections.singletonMap("authorityName",
                                roleDto.getAuthorityName()))));
                if (roleDto.getProjectId() != null) {
                    currentRole.setProject(projectRepository.getOne(roleDto.getProjectId()));
                }

                if (Objects.nonNull(currentRole.getAuthority())) {
                    roles.add(roleRepository.save(currentRole));
                }
            } else {
                roles.add(role.get());
            }
        }
        return roles;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email email id of user
     * @param langKey language key
     */
    public void updateUser(String firstName, String lastName, String email, String langKey) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(user -> {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setLangKey(langKey);
            log.debug("Changed Information for User: {}", user);
        });
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDto user to update
     * @return updated user
     */
    public Optional<UserDTO> updateUser(UserDTO userDto) {
        return Optional.of(userRepository
                .findOne(userDto.getId()))
                .map(user -> {
                    user.setLogin(userDto.getLogin());
                    user.setFirstName(userDto.getFirstName());
                    user.setLastName(userDto.getLastName());
                    user.setEmail(userDto.getEmail());
                    user.setActivated(userDto.isActivated());
                    user.setLangKey(userDto.getLangKey());
                    Set<Role> managedRoles = user.getRoles();
                    managedRoles.clear();
                    managedRoles.addAll(getUserRoles(userDto));

                    log.debug("Changed Information for User: {}", user);
                    return user;
                })
                .map(userMapper::userToUserDTO);
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
            String encryptedPassword = passwordEncoder.encode(password);
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
     * Get the projects a given user has any role in.
     * @param login the login of the user
     * @return the list of projects
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsAssignedToUser(String login) {
        User userByLogin = userRepository.findOneWithRolesByLogin(login)
                .orElseThrow(NoSuchElementException::new);

        List<Project> projectsOfUser;

        if (userByLogin.getRoles().stream()
                .anyMatch(r -> AuthoritiesConstants.SYS_ADMIN.equals(r.getAuthority().getName()))) {
            projectsOfUser = projectRepository.findAll();
        } else {
            projectsOfUser = userByLogin.getRoles().stream()
                    .map(Role::getProject)
                    .distinct()
                    .collect(Collectors.toList());
        }

        return projectMapper.projectsToProjectDTOs(projectsOfUser);
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

        userRepository.findAllByActivatedAndAuthoritiesNot(false,
                Arrays.asList(PARTICIPANT, INACTIVE_PARTICIPANT)).stream()
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
}
