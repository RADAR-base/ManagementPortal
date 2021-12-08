package org.radarbase.management.service;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.domain.Authority;
import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.User;
import org.radarbase.management.domain.audit.CustomRevisionEntity;
import org.radarbase.management.repository.CustomRevisionEntityRepository;
import org.radarbase.management.repository.UserRepository;
import org.radarbase.management.repository.filters.UserFilter;
import org.radarbase.management.service.dto.UserDTO;
import org.radarbase.management.service.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.radarbase.auth.authorization.RoleAuthority.SYS_ADMIN;
import static org.radarbase.management.web.rest.TestUtil.commitTransactionAndStartNew;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@Transactional
public class UserServiceIntTest {

    public static final String DEFAULT_LOGIN = "johndoe";
    public static final String UPDATED_LOGIN = "jhipster";

    public static final String DEFAULT_PASSWORD = "passjohndoe";
    public static final String UPDATED_PASSWORD = "passjhipster";

    public static final String DEFAULT_EMAIL = "johndoe@localhost";
    public static final String UPDATED_EMAIL = "jhipster@localhost";

    public static final String DEFAULT_FIRSTNAME = "john";
    public static final String UPDATED_FIRSTNAME = "jhipsterFirstName";

    public static final String DEFAULT_LASTNAME = "doe";
    public static final String UPDATED_LASTNAME = "jhipsterLastName";

    public static final String DEFAULT_LANGKEY = "en";
    public static final String UPDATED_LANGKEY = "fr";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private CustomRevisionEntityRepository revisionEntityRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PasswordService passwordService;

    private EntityManager entityManager;

    private UserDTO userDto;

    @BeforeEach
    public void setUp() {
        entityManager = entityManagerFactory.createEntityManager(
                entityManagerFactory.getProperties());
        userDto = userMapper.userToUserDTO(createEntity(passwordService));
        ReflectionTestUtils.setField(revisionService, "revisionEntityRepository",
                revisionEntityRepository);
        ReflectionTestUtils.setField(revisionService, "entityManager", entityManager);
        ReflectionTestUtils.setField(userService, "userMapper", userMapper);
        ReflectionTestUtils.setField(userService, "userRepository", userRepository);

        userRepository.findOneByLogin(userDto.getLogin())
                .ifPresent(userRepository::delete);
    }

    /**
     * Create a User.
     *
     * <p>This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.</p>
     */
    public static User createEntity(PasswordService passwordService) {
        User user = new User();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(passwordService.generateEncodedPassword());
        user.setActivated(true);
        user.setEmail(DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setLangKey(DEFAULT_LANGKEY);
        return user;
    }

    @Test
    void assertThatUserMustExistToResetPassword() {
        Optional<User> maybeUser = userService.requestPasswordReset("john.doe@localhost");
        assertThat(maybeUser).isNotPresent();

        maybeUser = userService.requestPasswordReset("admin@localhost");
        assertThat(maybeUser).isPresent();

        assertThat(maybeUser.get().getEmail()).isEqualTo("admin@localhost");
        assertThat(maybeUser.get().getResetDate()).isNotNull();
        assertThat(maybeUser.get().getResetKey()).isNotNull();
    }

    @Test
    void assertThatOnlyActivatedUserCanRequestPasswordReset() throws NotAuthorizedException {
        User user = userService.createUser(userDto);
        Optional<User> maybeUser = userService.requestPasswordReset(userDto.getEmail());
        assertThat(maybeUser).isNotPresent();
        userRepository.delete(user);
    }

    @Test
    void assertThatResetKeyMustNotBeOlderThan24Hours() throws NotAuthorizedException {
        User user = userService.createUser(userDto);

        ZonedDateTime daysAgo = ZonedDateTime.now().minusHours(25);
        String resetKey = passwordService.generateResetKey();
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);

        userRepository.save(user);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2",
                user.getResetKey());

        assertThat(maybeUser).isNotPresent();

        userRepository.delete(user);
    }

    @Test
    void assertThatResetKeyMustBeValid() throws NotAuthorizedException {
        User user = userService.createUser(userDto);
        ZonedDateTime daysAgo = ZonedDateTime.now().minusHours(25);
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey("1234");
        userRepository.save(user);
        Optional<User> maybeUser = userService.completePasswordReset("johndoe2",
                user.getResetKey());
        assertThat(maybeUser).isNotPresent();
        userRepository.delete(user);
    }

    @Test
    void assertThatUserCanResetPassword() throws NotAuthorizedException {
        User user = userService.createUser(userDto);
        final String oldPassword = user.getPassword();
        ZonedDateTime daysAgo = ZonedDateTime.now().minusHours(2);
        String resetKey = passwordService.generateResetKey();
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);
        userRepository.save(user);
        Optional<User> maybeUser = userService.completePasswordReset("johndoe2",
                user.getResetKey());
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.get().getResetDate()).isNull();
        assertThat(maybeUser.get().getResetKey()).isNull();
        assertThat(maybeUser.get().getPassword()).isNotEqualTo(oldPassword);

        userRepository.delete(user);
    }

    @Test
    void testFindNotActivatedUsersByCreationDateBefore() {
        User expiredUser = addExpiredUser(userRepository);
        commitTransactionAndStartNew();

        // Update the timestamp of the revision so it appears to have been created 5 days ago
        ZonedDateTime expDateTime = ZonedDateTime.now().minus(Period.ofDays(5)).withNano(0);

        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        Object[] firstRevision = (Object[]) auditReader.createQuery()
                .forRevisionsOfEntity(expiredUser.getClass(), false, true)
                .add(AuditEntity.id().eq(expiredUser.getId()))
                .add(AuditEntity.revisionNumber().minimize()
                        .computeAggregationInInstanceContext())
                .getSingleResult();
        CustomRevisionEntity first = (CustomRevisionEntity) firstRevision[1];
        first.setTimestamp(Date.from(expDateTime.toInstant()));
        entityManager.joinTransaction();
        CustomRevisionEntity updated = entityManager.merge(first);
        commitTransactionAndStartNew();
        assertThat(updated.getTimestamp()).isEqualTo(first.getTimestamp());
        assertThat(updated.getTimestamp()).isEqualTo(Date.from(expDateTime.toInstant()));

        // make sure when we reload the expired user we have the new created date
        assertThat(revisionService.getAuditInfo(expiredUser).getCreatedAt()).isEqualTo(expDateTime);

        // Now we know we have an 'old' user in the database, we can test our deletion method
        int numUsers = userRepository.findAll().size();
        userService.removeNotActivatedUsers();
        List<User> users = userRepository.findAll();
        // make sure have actually deleted some users, otherwise this test is pointless
        assertThat(numUsers - users.size()).isEqualTo(1);
        // remaining users should be either activated or have a created date less then 3 days ago
        ZonedDateTime cutoff = ZonedDateTime.now().minus(Period.ofDays(3));
        users.forEach(u -> assertThat(u.getActivated() || revisionService.getAuditInfo(u)
                .getCreatedAt().isAfter(cutoff)).isTrue());
        // commit the deletion, otherwise the deletion will be rolled back
        commitTransactionAndStartNew();
    }

    @Test
    void assertThatAnonymousUserIsNotGet() {
        final PageRequest pageable = PageRequest.of(0, (int) userRepository.count());
        final Page<UserDTO> allManagedUsers = userService.findUsers(new UserFilter(), pageable,
                false);
        assertThat(allManagedUsers.getContent().stream()
                .noneMatch(user -> Constants.ANONYMOUS_USER.equals(user.getLogin())))
                .isTrue();
    }

    /**
     * Create an expired user, save it and return the saved object.
     * @param userRepository The UserRepository that will be used to save the object
     * @return the saved object
     */
    public User addExpiredUser(UserRepository userRepository) {

        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setAuthority(new Authority(SYS_ADMIN));
        adminRole.setProject(null);

        User user = new User();
        user.setLogin("expired");
        user.setEmail("expired@expired");
        user.setFirstName("ex");
        user.setLastName("pired");
        user.setRoles(Collections.singleton(adminRole));
        user.setActivated(false);
        user.setPassword(passwordService.generateEncodedPassword());
        return userRepository.save(user);
    }

}
