package org.radarbase.management.web.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.domain.Authority;
import org.radarbase.management.domain.User;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.repository.UserRepository;
import org.radarbase.management.security.JwtAuthenticationFilter;
import org.radarbase.management.service.MailService;
import org.radarbase.management.service.PasswordService;
import org.radarbase.management.service.UserService;
import org.radarbase.management.service.dto.RoleDTO;
import org.radarbase.management.web.rest.errors.ExceptionTranslator;
import org.radarbase.management.web.rest.vm.ManagedUserVM;
import org.radarbase.auth.authentication.OAuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.radarbase.auth.authorization.RoleAuthority.SYS_ADMIN_AUTHORITY;
import static org.radarbase.management.service.UserServiceIntTest.DEFAULT_EMAIL;
import static org.radarbase.management.service.UserServiceIntTest.DEFAULT_FIRSTNAME;
import static org.radarbase.management.service.UserServiceIntTest.DEFAULT_LANGKEY;
import static org.radarbase.management.service.UserServiceIntTest.DEFAULT_LASTNAME;
import static org.radarbase.management.service.UserServiceIntTest.DEFAULT_LOGIN;
import static org.radarbase.management.service.UserServiceIntTest.DEFAULT_PASSWORD;
import static org.radarbase.management.service.UserServiceIntTest.UPDATED_EMAIL;
import static org.radarbase.management.service.UserServiceIntTest.UPDATED_FIRSTNAME;
import static org.radarbase.management.service.UserServiceIntTest.UPDATED_LANGKEY;
import static org.radarbase.management.service.UserServiceIntTest.UPDATED_LASTNAME;
import static org.radarbase.management.service.UserServiceIntTest.UPDATED_LOGIN;
import static org.radarbase.management.service.UserServiceIntTest.UPDATED_PASSWORD;
import static org.radarbase.auth.authorization.RoleAuthority.SYS_ADMIN;
import static org.radarbase.management.service.UserServiceIntTest.createEntity;
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
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
class UserResourceIntTest {

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
    private RadarToken radarToken;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private ProjectRepository projectRepository;

    private MockMvc restUserMockMvc;

    private User user;

    @BeforeEach
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
        UserResource userResource = new UserResource();
        ReflectionTestUtils.setField(userResource, "userService", userService);
        ReflectionTestUtils.setField(userResource, "mailService", mailService);
        ReflectionTestUtils.setField(userResource, "userRepository", userRepository);
        ReflectionTestUtils.setField(userResource, "subjectRepository", subjectRepository);
        ReflectionTestUtils.setField(userResource, "token", radarToken);
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


    @BeforeEach
    public void initTest() {
        user = createEntity(passwordService);
    }

    @Test
    @Transactional
    void createUser() throws Exception {
        final int databaseSizeBeforeCreate = userRepository.findAll().size();

        // Create the User
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(SYS_ADMIN_AUTHORITY);
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
    void createUserWithExistingId() throws Exception {
        final int databaseSizeBeforeCreate = userRepository.findAll().size();

        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(RoleAuthority.PARTICIPANT.authority());
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
    void createUserWithExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        final int databaseSizeBeforeCreate = userRepository.findAll().size();

        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(RoleAuthority.PARTICIPANT.authority());
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
    void createUserWithExistingEmail() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        final int databaseSizeBeforeCreate = userRepository.findAll().size();

        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(RoleAuthority.PARTICIPANT.authority());
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
    void getAllUsers() throws Exception {
        // Initialize the database
        org.radarbase.management.domain.Role adminRole = new org.radarbase.management.domain.Role();
        adminRole.setId(1L);
        adminRole.setAuthority(new Authority(SYS_ADMIN));
        adminRole.setProject(null);

        User userWithRole = new User();
        userWithRole.setLogin(DEFAULT_LOGIN);
        userWithRole.setPassword(passwordService.generateEncodedPassword());
        userWithRole.setActivated(true);
        userWithRole.setEmail(DEFAULT_EMAIL);
        userWithRole.setFirstName(DEFAULT_FIRSTNAME);
        userWithRole.setLastName(DEFAULT_LASTNAME);
        userWithRole.setLangKey(DEFAULT_LANGKEY);
        userWithRole.setRoles(Collections.singleton(adminRole));
        userRepository.saveAndFlush(userWithRole);

        // Get all the users
        restUserMockMvc.perform(get("/api/users?sort=id,desc")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
                .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
                .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
                .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
                .andExpect(jsonPath("$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)));
    }

    @Test
    @Transactional
    void getUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get the user
        restUserMockMvc.perform(get("/api/users/{login}", user.getLogin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
                .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
                .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
                .andExpect(jsonPath("$.langKey").value(DEFAULT_LANGKEY));
    }

    @Test
    @Transactional
    void getNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/users/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void updateUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        final int databaseSizeBeforeUpdate = userRepository.findAll().size();
        var project = ProjectResourceIntTest.createEntity();
        projectRepository.save(project);

        // Update the user
        User updatedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new AssertionError("Cannot find user " + user.getId()));

        ManagedUserVM managedUserVm = new ManagedUserVM();
        managedUserVm.setId(updatedUser.getId());
        managedUserVm.setLogin(updatedUser.getLogin());
        managedUserVm.setPassword(UPDATED_PASSWORD);
        managedUserVm.setFirstName(UPDATED_FIRSTNAME);
        managedUserVm.setLastName(UPDATED_LASTNAME);
        managedUserVm.setEmail(UPDATED_EMAIL);
        managedUserVm.setActivated(updatedUser.getActivated());
        managedUserVm.setLangKey(UPDATED_LANGKEY);

        RoleDTO role = new RoleDTO();
        role.setProjectId(project.getId());
        role.setAuthorityName(RoleAuthority.PARTICIPANT.authority());
        managedUserVm.setRoles(Set.of(role));

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
    void updateUserLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        var project = ProjectResourceIntTest.createEntity();
        projectRepository.save(project);

        final int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        User updatedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new AssertionError("Cannot find user " + user.getId()));

        ManagedUserVM managedUserVm = new ManagedUserVM();
        managedUserVm.setId(updatedUser.getId());
        managedUserVm.setLogin(UPDATED_LOGIN);
        managedUserVm.setPassword(UPDATED_PASSWORD);
        managedUserVm.setFirstName(UPDATED_FIRSTNAME);
        managedUserVm.setLastName(UPDATED_LASTNAME);
        managedUserVm.setEmail(UPDATED_EMAIL);
        managedUserVm.setActivated(updatedUser.getActivated());
        managedUserVm.setLangKey(UPDATED_LANGKEY);

        RoleDTO role = new RoleDTO();
        role.setProjectId(project.getId());
        role.setAuthorityName(RoleAuthority.PARTICIPANT.authority());
        managedUserVm.setRoles(Set.of(role));

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
    void updateUserExistingEmail() throws Exception {
        // Initialize the database with 2 users
        userRepository.saveAndFlush(user);
        var project = ProjectResourceIntTest.createEntity();
        projectRepository.save(project);

        User anotherUser = new User();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(passwordService.generateEncodedPassword());
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setLangKey("en");
        userRepository.saveAndFlush(anotherUser);

        // Update the user
        User updatedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new AssertionError("Cannot find user " + user.getId()));

        ManagedUserVM managedUserVm = new ManagedUserVM();
        managedUserVm.setId(updatedUser.getId());
        managedUserVm.setLogin(updatedUser.getLogin());
        managedUserVm.setPassword(updatedUser.getPassword());
        managedUserVm.setFirstName(updatedUser.getFirstName());
        managedUserVm.setLastName(updatedUser.getLastName());
        managedUserVm.setEmail("jhipster@localhost");
        managedUserVm.setActivated(updatedUser.getActivated());
        managedUserVm.setLangKey(updatedUser.getLangKey());

        RoleDTO role = new RoleDTO();
        role.setProjectId(project.getId());
        role.setAuthorityName(RoleAuthority.PARTICIPANT.authority());
        managedUserVm.setRoles(Set.of(role));

        restUserMockMvc.perform(put("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(managedUserVm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void updateUserExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        var project = ProjectResourceIntTest.createEntity();
        projectRepository.save(project);

        User anotherUser = new User();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(passwordService.generateEncodedPassword());
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setLangKey("en");
        userRepository.saveAndFlush(anotherUser);

        // Update the user
        User updatedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new AssertionError("Cannot find user " + user.getId()));

        ManagedUserVM managedUserVm = new ManagedUserVM();
        managedUserVm.setId(updatedUser.getId());
        managedUserVm.setLogin("jhipster");
        managedUserVm.setPassword(updatedUser.getPassword());
        managedUserVm.setFirstName(updatedUser.getFirstName());
        managedUserVm.setLastName(updatedUser.getLastName());
        managedUserVm.setEmail(updatedUser.getEmail());
        managedUserVm.setActivated(updatedUser.getActivated());
        managedUserVm.setLangKey(updatedUser.getLangKey());

        RoleDTO role = new RoleDTO();
        role.setProjectId(project.getId());
        role.setAuthorityName(RoleAuthority.PARTICIPANT.authority());
        managedUserVm.setRoles(Set.of(role));

        restUserMockMvc.perform(put("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(managedUserVm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void deleteUser() throws Exception {
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
    void equalsVerifier() {
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
