package org.radarcns.management.web.rest;

import org.radarcns.management.ManagementPortalApp;
import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.AuthorityRepository;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.radarcns.management.service.MailService;
import org.radarcns.management.service.UserService;
import org.radarcns.management.service.dto.RoleDTO;
import org.radarcns.management.service.dto.UserDTO;
import org.radarcns.management.service.mapper.UserMapper;
import org.radarcns.management.web.rest.vm.ManagedUserVM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AccountResource REST controller.
 *
 * @see AccountResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalApp.class)
public class AccountResourceIntTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Mock
    private UserService mockUserService;

    @Mock
    private MailService mockMailService;

    private MockMvc restUserMockMvc;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendActivationEmail(anyObject());

        AccountResource accountResource = new AccountResource();
        ReflectionTestUtils.setField(accountResource, "userService", userService);
        ReflectionTestUtils.setField(accountResource, "userMapper", userMapper);
        ReflectionTestUtils.setField(accountResource, "mailService", mockMailService);
        ReflectionTestUtils.setField(accountResource, "userRepository", userRepository);

        AccountResource accountUserMockResource = new AccountResource();
        ReflectionTestUtils.setField(accountUserMockResource, "userService", mockUserService);
        ReflectionTestUtils.setField(accountUserMockResource, "userMapper", userMapper);
        ReflectionTestUtils.setField(accountUserMockResource, "mailService", mockMailService);
        ReflectionTestUtils.setField(accountUserMockResource, "userRepository", userRepository);

        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource).build();
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource).build();
    }

    @Test
    public void testNonAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    public void testAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate")
            .with(request -> {
                request.setRemoteUser("test");
                return request;
            })
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("test"));
    }

    @Test
    public void testGetExistingAccount() throws Exception {
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.SYS_ADMIN);
        role.setAuthority(authority);
//        Authority authority2 = new Authority();
//        authority2.setName(AuthoritiesConstants.SYS_ADMIN);
        roles.add(role);
//        authorities.add(authority2);

        User user = new User();
        user.setLogin("test");
        user.setFirstName("john");
        user.setLastName("doe");
        user.setEmail("john.doe@jhipster.com");
        user.setLangKey("en");
        user.setRoles(roles);
        when(mockUserService.getUserWithAuthorities()).thenReturn(user);

        restUserMockMvc.perform(get("/api/account")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value("test"))
            .andExpect(jsonPath("$.firstName").value("john"))
            .andExpect(jsonPath("$.lastName").value("doe"))
            .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("$.langKey").value("en"))
            .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.SYS_ADMIN));
    }

    @Test
    public void testGetUnknownAccount() throws Exception {
        when(mockUserService.getUserWithAuthorities()).thenReturn(null);

        restUserMockMvc.perform(get("/api/account")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    public void testRegisterValid() throws Exception {
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.USER);
        roles.add(role);
        ManagedUserVM validUser = new ManagedUserVM(
            null,                   // id
            "joe",                  // login
            "password",             // password
            "Joe",                  // firstName
            "Shmoe",                // lastName
            "joe@example.com",      // email
            true,                   // activated
            "en",                   // langKey
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null,                   // lastModifiedDate
            roles,
            null);

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> user = userRepository.findOneByLogin("joe");
        assertThat(user.isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testRegisterInvalidLogin() throws Exception {
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.USER);
        roles.add(role);
        ManagedUserVM invalidUser = new ManagedUserVM(
            null,                   // id
            "funky-log!n",          // login <-- invalid
            "password",             // password
            "Funky",                // firstName
            "One",                  // lastName
            "funky@example.com",    // email
            true,                   // activated
            "en",                   // langKey
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null,                   // lastModifiedDate
            roles,
            null);

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByEmail("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidEmail() throws Exception {
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.USER);
        roles.add(role);
        ManagedUserVM invalidUser = new ManagedUserVM(
            null,               // id
            "bob",              // login
            "password",         // password
            "Bob",              // firstName
            "Green",            // lastName
            "invalid",          // email <-- invalid
            true,               // activated
            "en",                   // langKey
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null,                   // lastModifiedDate
             roles,
            null);

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidPassword() throws Exception {
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.USER);
        roles.add(role);

        ManagedUserVM invalidUser = new ManagedUserVM(
            null,               // id
            "bob",              // login
            "123",              // password with only 3 digits
            "Bob",              // firstName
            "Green",            // lastName
            "bob@example.com",  // email
            true,               // activated
            "en",                   // langKey
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null,                   // lastModifiedDate
            roles,
            null);

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateLogin() throws Exception {
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.USER);
        roles.add(role);

        // Good
        ManagedUserVM validUser = new ManagedUserVM(
            null,                   // id
            "alice",                // login
            "password",             // password
            "Alice",                // firstName
            "Something",            // lastName
            "alice@example.com",    // email
            true,                   // activated
            "en",                   // langKey
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null,                   // lastModifiedDate
            roles,
            null);

        // Duplicate login, different email
        ManagedUserVM duplicatedUser = new ManagedUserVM(validUser.getId(), validUser.getLogin(), validUser.getPassword(), validUser.getFirstName(), validUser.getLastName(),
            "alicejr@example.com", true, validUser.getLangKey(), validUser.getCreatedBy(), validUser.getCreatedDate(), validUser.getLastModifiedBy(), validUser.getLastModifiedDate(), validUser.getRoles(), validUser.getProject());

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        // Duplicate login
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
            .andExpect(status().is4xxClientError());

        Optional<User> userDup = userRepository.findOneByEmail("alicejr@example.com");
        assertThat(userDup.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateEmail() throws Exception {
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.USER);
        roles.add(role);

        // Good
        ManagedUserVM validUser = new ManagedUserVM(
            null,                   // id
            "john",                 // login
            "password",             // password
            "John",                 // firstName
            "Doe",                  // lastName
            "john@example.com",     // email
            true,                   // activated
            "en",                   // langKey
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null,                   // lastModifiedDate
            roles,
            null);

        // Duplicate email, different login
        ManagedUserVM duplicatedUser = new ManagedUserVM(validUser.getId(), "johnjr", validUser.getPassword(), validUser.getLogin(), validUser.getLastName(),
            validUser.getEmail(), true, validUser.getLangKey(), validUser.getCreatedBy(), validUser.getCreatedDate(), validUser.getLastModifiedBy(), validUser.getLastModifiedDate(), validUser.getRoles() , validUser.getProject());

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        // Duplicate email
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
            .andExpect(status().is4xxClientError());

        Optional<User> userDup = userRepository.findOneByLogin("johnjr");
        assertThat(userDup.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterAdminIsIgnored() throws Exception {
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.SYS_ADMIN);
        roles.add(role);

        ManagedUserVM validUser = new ManagedUserVM(
            null,                   // id
            "badguy",               // login
            "password",             // password
            "Bad",                  // firstName
            "Guy",                  // lastName
            "badguy@example.com",   // email
            true,                   // activated
            "en",                   // langKey
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null,                   // lastModifiedDate
            roles,
            null);

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> userDup = userRepository.findOneByLogin("badguy");
        assertThat(userDup.isPresent()).isTrue();
        assertThat(userDup.get().getAuthorities()).hasSize(1)
            .containsExactly(authorityRepository.findOne(AuthoritiesConstants.USER));
    }

    @Test
    @Transactional
    public void testSaveInvalidLogin() throws Exception {
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.USER);
        roles.add(role);

        UserDTO invalidUser = new UserDTO(
            null,                   // id
            "funky-log!n",          // login <-- invalid
            "Funky",                // firstName
            "One",                  // lastName
            "funky@example.com",    // email
            true,                   // activated
            "en",                   // langKey
            null,                   // createdBy
            null,                   // createdDate
            null,                   // lastModifiedBy
            null,                   // lastModifiedDate
            roles,
            null);

        restUserMockMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByEmail("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }
}
