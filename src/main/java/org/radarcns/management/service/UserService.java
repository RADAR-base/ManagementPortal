package org.radarcns.management.service;

import org.radarcns.management.config.Constants;
import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.repository.RoleRepository;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.security.SecurityUtils;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.ProjectMapper;
import org.radarcns.management.service.mapper.RoleMapper;
import org.radarcns.management.service.mapper.UserMapper;
import org.radarcns.management.service.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

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
    private RoleMapper roleMapper;

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

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);

        return userRepository.findOneByResetKey(key)
            .filter(user -> {
                ZonedDateTime oneDayAgo = ZonedDateTime.now().minusHours(24);
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

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmail(mail)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(ZonedDateTime.now());
                return user;
            });
    }

    public User createUser(String login, String password, String firstName, String lastName,
        String email,
        String langKey) {

        User newUser = new User();
        // TODO check how this is used in the system, setting default user role?
        Role role = roleRepository.findRolesByAuthorityName(AuthoritiesConstants.USER).get(0);
        Set<Role> roles = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setLangKey(langKey);
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        roles.add(role);
        newUser.setRoles(roles);
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User createUser(UserDTO userDTO) {
        log.info("User project ----> "+userDTO.getProject());
        User user = new User();
        user.setLogin(userDTO.getLogin());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        if (userDTO.getLangKey() == null) {
            user.setLangKey("en"); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(ZonedDateTime.now());
        user.setActivated(false);

        Set<Role> roles = new HashSet<>();
        if(userDTO.getAuthorities().contains(AuthoritiesConstants.SYS_ADMIN)) {
            roles.addAll(roleRepository.findRolesByAuthorityName(AuthoritiesConstants.SYS_ADMIN));
        }

        for (String authority : userDTO.getAuthorities()) {
            if(!AuthoritiesConstants.SYS_ADMIN.equals(authority )  && userDTO.getProject().getId() !=null) {

                Role role = roleRepository.findOneByAuthorityNameAndProjectId(authority , userDTO.getProject().getId());
                if(role ==null) {
                    role = new Role();
                    role.setAuthority(new Authority(authority));
                    role.setProject(projectMapper.projectDTOToProject(userDTO.getProject()));
                    roleRepository.save(role);
                }
                roles.add(role);
            }
        }
        user.setRoles(roles);
        if(userDTO.getProject()!= null && userDTO.getProject().getId() != null) {
            Project project = projectMapper.projectDTOToProject(userDTO.getProject());
            user.setProject(project);
//            if(userDTO.getAuthorities() != null && userDTO.getAuthorities().contains(AuthoritiesConstants.PROJECT_ADMIN)) {
//                project.setProjectAdmin(user.getId());
//                projectRepository.save(project);
//            }
        }
        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
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
     * @param userDTO user to update
     * @return updated user
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        return Optional.of(userRepository
            .findOne(userDTO.getId()))
            .map(user -> {
                user.setLogin(userDTO.getLogin());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setEmail(userDTO.getEmail());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                Set<Role> managedRoles = user.getRoles();
                managedRoles.clear();
                if(userDTO.getProject()!=null && userDTO.getProject().getId() != null) {
                    Project project = projectMapper.projectDTOToProject(userDTO.getProject());
                    user.setProject(project);
                }
                if(userDTO.getAuthorities().contains(AuthoritiesConstants.SYS_ADMIN)) {
                    managedRoles.addAll(roleRepository.findRolesByAuthorityName(AuthoritiesConstants.SYS_ADMIN));
                }
                else {
                    for (String authority : userDTO.getAuthorities()) {
                        if(!AuthoritiesConstants.SYS_ADMIN.equals(authority )  && userDTO.getProject().getId() !=null) {

                            Role role = roleRepository.findOneByAuthorityNameAndProjectId(authority , userDTO.getProject().getId());
                            if(role ==null) {
                                role = new Role();
                                role.setAuthority(new Authority(authority));
                                role.setProject(projectMapper.projectDTOToProject(userDTO.getProject()));
                                roleRepository.save(role);
                            }
                            managedRoles.add(role);
                        }
                    }
                }

                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(userMapper::userToUserDTO);
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            log.debug("Deleted User: {}", user);
        });
    }

    public void changePassword(String password) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(user -> {
            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            log.debug("Changed password for User: {}", user);
        });
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        User currentUser = getUserWithAuthorities();
        List<String> currentUserAuthorities = currentUser.getAuthorities().stream().map(Authority::getName).collect(
            Collectors.toList());
        if(currentUserAuthorities.contains(AuthoritiesConstants.PROJECT_ADMIN)) {
            log.debug("Request to get all Projects");
            return userRepository.findAllByProjectId(pageable, currentUser.getProject().getId())
                .map(userMapper::userToUserDTO);
        }
        else {
            log.debug("Request to get all Users");
            return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER)
                .map(userMapper::userToUserDTO);
        }

    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login).map(userMapper::userToUserDTO);
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities(Long id) {
        return userRepository.findOneWithAuthoritiesById(id);
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities() {
        return userRepository.findOneWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin())
            .orElse(null);
    }


    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     * </p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        ZonedDateTime now = ZonedDateTime.now();
        List<User> users = userRepository
            .findAllByActivatedIsFalseAndCreatedDateBefore(now.minusDays(3));
        for (User user : users) {
            log.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
        }
    }
}
