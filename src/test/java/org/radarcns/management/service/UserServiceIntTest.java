package org.radarcns.management.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.radarcns.management.web.rest.TestUtil.commitTransactionAndStartNew;

import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarcns.auth.config.Constants;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.domain.User;
import org.radarcns.management.domain.audit.CustomRevisionEntity;
import org.radarcns.management.repository.CustomRevisionEntityRepository;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.repository.filters.UserFilter;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.UserMapper;
import org.radarcns.management.service.util.RandomUtil;
import org.radarcns.management.web.rest.UserResourceIntTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@Transactional
public class UserServiceIntTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomRevisionEntityRepository revisionEntityRepository;

    @Autowired
    private RevisionService revisionService;

    private UserDTO userDto;

    @Before
    public void setUp() {
        userDto = userMapper.userToUserDTO(UserResourceIntTest.createEntity());
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
        User expiredUser = addExpiredUser();
        commitTransactionAndStartNew();

        // Get the first revision of our new user (there should only be one anyway)
        CustomRevisionEntity firstRevision = userRepository.findRevisions(expiredUser.getId())
                .getContent().get(0).getMetadata().getDelegate();

        // Update the timestamp of the revision so it appears to have been created 5 days ago
        Instant expInstant = Instant.now().minus(Period.ofDays(5));
        firstRevision.setTimestamp(Date.from(expInstant));
        revisionEntityRepository.save(firstRevision);

        // make sure when we reload the expired user we have the new created date
        expiredUser = userRepository.findOne(expiredUser.getId());
        assertThat(revisionService.getCreatedAt(expiredUser).get()).isEqualTo(expInstant);

        // Now we know we have an 'old' user in the database, we can test our deletion method
        int numUsers = userRepository.findAll().size();
        userService.removeNotActivatedUsers();
        List<User> users = userRepository.findAll();
        // make sure have actually deleted some users, otherwise this test is pointless
        assertThat(numUsers - users.size()).isEqualTo(1);
        // remaining users should be either activated or have a created date less then 3 days ago
        Instant cutoff = Instant.now().minus(Period.ofDays(3));
        users.forEach(u -> assertThat(u.getActivated() || revisionService.getCreatedAt(u).get()
                .isAfter(cutoff)).isTrue());
    }

    @Test
    public void assertThatAnonymousUserIsNotGet() {
        final PageRequest pageable = new PageRequest(0, (int) userRepository.count());
        final Page<UserDTO> allManagedUsers = userService.findUsers(new UserFilter(), pageable);
        assertThat(allManagedUsers.getContent().stream()
                .noneMatch(user -> Constants.ANONYMOUS_USER.equals(user.getLogin())))
                .isTrue();
    }

    private User addExpiredUser() {
        User user = new User();
        user.setLogin("expired");
        user.setEmail("expired@expired");
        user.setFirstName("ex");
        user.setLastName("pired");
        user.setActivated(false);
        user.setPassword(RandomStringUtils.random(60));
        return userRepository.save(user);
    }

}
