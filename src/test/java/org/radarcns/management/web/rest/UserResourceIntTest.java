package org.radarcns.management.web.rest;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.config.ManagementPortalProperties;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.security.JwtAuthenticationFilter;
import org.radarcns.management.service.MailService;
import org.radarcns.management.service.UserService;
import org.radarcns.management.service.dto.RoleDTO;
import org.radarcns.management.web.rest.errors.ExceptionTranslator;
import org.radarcns.management.web.rest.vm.ManagedUserVM;
import org.radarcns.auth.authentication.OAuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.radarcns.management.service.UserServiceIntTest.DEFAULT_EMAIL;
import static org.radarcns.management.service.UserServiceIntTest.DEFAULT_FIRSTNAME;
import static org.radarcns.management.service.UserServiceIntTest.DEFAULT_LANGKEY;
import static org.radarcns.management.service.UserServiceIntTest.DEFAULT_LASTNAME;
import static org.radarcns.management.service.UserServiceIntTest.DEFAULT_LOGIN;
import static org.radarcns.management.service.UserServiceIntTest.DEFAULT_PASSWORD;
import static org.radarcns.management.service.UserServiceIntTest.UPDATED_EMAIL;
import static org.radarcns.management.service.UserServiceIntTest.UPDATED_FIRSTNAME;
import static org.radarcns.management.service.UserServiceIntTest.UPDATED_LANGKEY;
import static org.radarcns.management.service.UserServiceIntTest.UPDATED_LASTNAME;
import static org.radarcns.management.service.UserServiceIntTest.UPDATED_LOGIN;
import static org.radarcns.management.service.UserServiceIntTest.UPDATED_PASSWORD;
import static org.radarcns.management.service.UserServiceIntTest.createEntity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
public class UserResourceIntTest {

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private HttpServletRequest servletRequest;

    private MockMvc restUserMockMvc;

    private User user;

    @Before
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
        UserResource userResource = new UserResource();
        ReflectionTestUtils.setField(userResource, "userService", userService);
        ReflectionTestUtils.setField(userResource, "mailService", mailService);
        ReflectionTestUtils.setField(userResource, "userRepository", userRepository);
        ReflectionTestUtils.setField(userResource, "subjectRepository", subjectRepository);
        ReflectionTestUtils.setField(userResource, "servletRequest", servletRequest);
        ReflectionTestUtils.setField(userResource,
                "managementPortalProperties", managementPortalProperties);

        JwtAuthenticationFilter filter = OAuthHelper.createAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter)
                .addFilter(filter)
                .defaultRequest(get("/").with(OAuthHelper.bearerToken())).build();
    }



    @Before
    public void initTest() {
        user = createEntity();
    }

    @Test
    @Transactional
    public void createUser() throws Exception {
        final int databaseSizeBeforeCreate = userRepository.findAll().size();

        // Create the User
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.SYS_ADMIN);
        roles.add(role);

        ManagedUserVM managedUserVm = createDefaultUser(roles);

        restUserMockMvc.perform(post("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(managedUserVm)))
                .andExpect(status().isCreated());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(testUser.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(testUser.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(testUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testUser.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
    }

    @Test
    @Transactional
    public void createUserWithExistingId() throws Exception {
        final int databaseSizeBeforeCreate = userRepository.findAll().size();

        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.PARTICIPANT);
        roles.add(role);

        ManagedUserVM managedUserVm = createDefaultUser(roles);
        managedUserVm.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc.perform(post("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(managedUserVm)))
                .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        final int databaseSizeBeforeCreate = userRepository.findAll().size();

        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.PARTICIPANT);
        roles.add(role);
        ManagedUserVM managedUserVm = createDefaultUser(roles);
        managedUserVm.setEmail("anothermail@localhost");

        // Create the User
        restUserMockMvc.perform(post("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(managedUserVm)))
                .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithExistingEmail() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        final int databaseSizeBeforeCreate = userRepository.findAll().size();

        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.PARTICIPANT);
        roles.add(role);
        ManagedUserVM managedUserVm = createDefaultUser(roles);
        managedUserVm.setLogin("anotherlogin");

        // Create the User
        restUserMockMvc.perform(post("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(managedUserVm)))
                .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllUsers() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get all the users
        restUserMockMvc.perform(get("/api/users?sort=id,desc")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
                .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
                .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
                .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
                .andExpect(jsonPath("$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)));
    }

    @Test
    @Transactional
    public void getUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get the user
        restUserMockMvc.perform(get("/api/users/{login}", user.getLogin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
                .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
                .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
                .andExpect(jsonPath("$.langKey").value(DEFAULT_LANGKEY));
    }

    @Test
    @Transactional
    public void getNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/users/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        final int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        User updatedUser = userRepository.findOne(user.getId());

        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.PARTICIPANT);
        roles.add(role);

        ManagedUserVM managedUserVm = new ManagedUserVM();
        managedUserVm.setId(updatedUser.getId());
        managedUserVm.setLogin(updatedUser.getLogin());
        managedUserVm.setPassword(UPDATED_PASSWORD);
        managedUserVm.setFirstName(UPDATED_FIRSTNAME);
        managedUserVm.setLastName(UPDATED_LASTNAME);
        managedUserVm.setEmail(UPDATED_EMAIL);
        managedUserVm.setActivated(updatedUser.getActivated());
        managedUserVm.setLangKey(UPDATED_LANGKEY);
        managedUserVm.setRoles(roles);

        restUserMockMvc.perform(put("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(managedUserVm)))
                .andExpect(status().isOk());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
        assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
    }

    @Test
    @Transactional
    public void updateUserLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        final int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        User updatedUser = userRepository.findOne(user.getId());

        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.PARTICIPANT);
        roles.add(role);

        ManagedUserVM managedUserVm = new ManagedUserVM();
        managedUserVm.setId(updatedUser.getId());
        managedUserVm.setLogin(UPDATED_LOGIN);
        managedUserVm.setPassword(UPDATED_PASSWORD);
        managedUserVm.setFirstName(UPDATED_FIRSTNAME);
        managedUserVm.setLastName(UPDATED_LASTNAME);
        managedUserVm.setEmail(UPDATED_EMAIL);
        managedUserVm.setActivated(updatedUser.getActivated());
        managedUserVm.setLangKey(UPDATED_LANGKEY);
        managedUserVm.setRoles(roles);

        restUserMockMvc.perform(put("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(managedUserVm)))
                .andExpect(status().isOk());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getLogin()).isEqualTo(UPDATED_LOGIN);
        assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
        assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
    }

    @Test
    @Transactional
    public void updateUserExistingEmail() throws Exception {
        // Initialize the database with 2 users
        userRepository.saveAndFlush(user);

        User anotherUser = new User();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setLangKey("en");
        userRepository.saveAndFlush(anotherUser);

        // Update the user
        User updatedUser = userRepository.findOne(user.getId());
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.PARTICIPANT);
        roles.add(role);

        ManagedUserVM managedUserVm = new ManagedUserVM();
        managedUserVm.setId(updatedUser.getId());
        managedUserVm.setLogin(updatedUser.getLogin());
        managedUserVm.setPassword(updatedUser.getPassword());
        managedUserVm.setFirstName(updatedUser.getFirstName());
        managedUserVm.setLastName(updatedUser.getLastName());
        managedUserVm.setEmail("jhipster@localhost");
        managedUserVm.setActivated(updatedUser.getActivated());
        managedUserVm.setLangKey(updatedUser.getLangKey());
        managedUserVm.setRoles(roles);

        restUserMockMvc.perform(put("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(managedUserVm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateUserExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        User anotherUser = new User();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setLangKey("en");
        userRepository.saveAndFlush(anotherUser);

        // Update the user
        User updatedUser = userRepository.findOne(user.getId());

        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(AuthoritiesConstants.PARTICIPANT);
        roles.add(role);

        ManagedUserVM managedUserVm = new ManagedUserVM();
        managedUserVm.setId(updatedUser.getId());
        managedUserVm.setLogin("jhipster");
        managedUserVm.setPassword(updatedUser.getPassword());
        managedUserVm.setFirstName(updatedUser.getFirstName());
        managedUserVm.setLastName(updatedUser.getLastName());
        managedUserVm.setEmail(updatedUser.getEmail());
        managedUserVm.setActivated(updatedUser.getActivated());
        managedUserVm.setLangKey(updatedUser.getLangKey());
        managedUserVm.setRoles(roles);

        restUserMockMvc.perform(put("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(managedUserVm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void deleteUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        final int databaseSizeBeforeDelete = userRepository.findAll().size();

        // Delete the user
        restUserMockMvc.perform(delete("/api/users/{login}", user.getLogin())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        User userA = new User();
        userA.setLogin("AAA");
        User userB = new User();
        userB.setLogin("BBB");
        assertThat(userA).isNotEqualTo(userB);
    }

    private ManagedUserVM createDefaultUser(Set<RoleDTO> roles) {
        ManagedUserVM result = new ManagedUserVM();
        result.setLogin(DEFAULT_LOGIN);
        result.setPassword(DEFAULT_PASSWORD);
        result.setFirstName(DEFAULT_FIRSTNAME);
        result.setLastName(DEFAULT_LASTNAME);
        result.setActivated(true);
        result.setEmail(DEFAULT_EMAIL);
        result.setLangKey(DEFAULT_LANGKEY);
        result.setRoles(roles);
        return result;
    }
}
