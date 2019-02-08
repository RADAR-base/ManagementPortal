package org.radarcns.management.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarcns.auth.config.Constants;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.domain.audit.CustomRevisionEntity;
import org.radarcns.management.repository.CustomRevisionEntityRepository;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.repository.filters.UserFilter;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.UserMapper;
import org.radarcns.management.service.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
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
import static org.radarcns.auth.authorization.AuthoritiesConstants.SYS_ADMIN;
import static org.radarcns.management.web.rest.TestUtil.commitTransactionAndStartNew;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
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

    private UserDTO userDto;

    @Before
    public void setUp() {
        userDto = userMapper.userToUserDTO(createEntity());
        ReflectionTestUtils.setField(revisionService, "revisionEntityRepository",
                revisionEntityRepository);
        ReflectionTestUtils.setField(revisionService, "entityManagerFactory", entityManagerFactory);
        ReflectionTestUtils.setField(userService, "userMapper", userMapper);
        ReflectionTestUtils.setField(userService, "userRepository", userRepository);
    }

    /**
     * Create a User.
     *
     * <p>This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.</p>
     */
    public static User createEntity() {
        User user = new User();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setLangKey(DEFAULT_LANGKEY);
        return user;
    }

    @Test
    public void assertThatUserMustExistToResetPassword() {
        Optional<User> maybeUser = userService.requestPasswordReset("john.doe@localhost");
        assertThat(maybeUser.isPresent()).isFalse();

        maybeUser = userService.requestPasswordReset("admin@localhost");
        assertThat(maybeUser.isPresent()).isTrue();

        assertThat(maybeUser.get().getEmail()).isEqualTo("admin@localhost");
        assertThat(maybeUser.get().getResetDate()).isNotNull();
        assertThat(maybeUser.get().getResetKey()).isNotNull();
    }

    @Test
    public void assertThatOnlyActivatedUserCanRequestPasswordReset() {
        User user = userService.createUser(userDto);
        Optional<User> maybeUser = userService.requestPasswordReset(userDto.getEmail());
        assertThat(maybeUser.isPresent()).isFalse();
        userRepository.delete(user);
    }

    @Test
    public void assertThatResetKeyMustNotBeOlderThan24Hours() {
        User user = userService.createUser(userDto);

        ZonedDateTime daysAgo = ZonedDateTime.now().minusHours(25);
        String resetKey = RandomUtil.generateResetKey();
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);

        userRepository.save(user);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2",
                user.getResetKey());

        assertThat(maybeUser.isPresent()).isFalse();

        userRepository.delete(user);
    }

    @Test
    public void assertThatResetKeyMustBeValid() {
        User user = userService.createUser(userDto);
        ZonedDateTime daysAgo = ZonedDateTime.now().minusHours(25);
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey("1234");
        userRepository.save(user);
        Optional<User> maybeUser = userService.completePasswordReset("johndoe2",
                user.getResetKey());
        assertThat(maybeUser.isPresent()).isFalse();
        userRepository.delete(user);
    }

    @Test
    public void assertThatUserCanResetPassword() {
        User user = userService.createUser(userDto);
        final String oldPassword = user.getPassword();
        ZonedDateTime daysAgo = ZonedDateTime.now().minusHours(2);
        String resetKey = RandomUtil.generateResetKey();
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);
        userRepository.save(user);
        Optional<User> maybeUser = userService.completePasswordReset("johndoe2",
                user.getResetKey());
        assertThat(maybeUser.isPresent()).isTrue();
        assertThat(maybeUser.get().getResetDate()).isNull();
        assertThat(maybeUser.get().getResetKey()).isNull();
        assertThat(maybeUser.get().getPassword()).isNotEqualTo(oldPassword);

        userRepository.delete(user);
    }

    @Test
    public void testFindNotActivatedUsersByCreationDateBefore() {
        User expiredUser = addExpiredUser(userRepository);
        commitTransactionAndStartNew();

        AuditReader auditReader = ((AuditReader) ReflectionTestUtils
                .getField(revisionService, "auditReader"));
        Object[] firstRevision = (Object[]) auditReader.createQuery()
                .forRevisionsOfEntity(expiredUser.getClass(), false, true)
                .add(AuditEntity.id().eq(expiredUser.getId()))
                .add(AuditEntity.revisionNumber().minimize()
                        .computeAggregationInInstanceContext())
                .getSingleResult();
        CustomRevisionEntity first = (CustomRevisionEntity) firstRevision[1];
        // Update the timestamp of the revision so it appears to have been created 5 days ago
        ZonedDateTime expDateTime = ZonedDateTime.now().minus(Period.ofDays(5));
        first.setTimestamp(Date.from(expDateTime.toInstant()));
        EntityManager entityManager = ((EntityManager) ReflectionTestUtils
                .getField(revisionService, "entityManager"));
        entityManager.persist(first);

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
    public void assertThatAnonymousUserIsNotGet() {
        final PageRequest pageable = new PageRequest(0, (int) userRepository.count());
        final Page<UserDTO> allManagedUsers = userService.findUsers(new UserFilter(), pageable);
        assertThat(allManagedUsers.getContent().stream()
                .noneMatch(user -> Constants.ANONYMOUS_USER.equals(user.getLogin())))
                .isTrue();
    }

    /**
     * Create an expired user, save it and return the saved object.
     * @param userRepository The UserRepository that will be used to save the object
     * @return the saved object
     */
    public static User addExpiredUser(UserRepository userRepository) {

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
        user.setPassword(RandomStringUtils.random(60));
        return userRepository.save(user);
    }

}
