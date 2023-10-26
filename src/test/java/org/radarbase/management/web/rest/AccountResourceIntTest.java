package org.radarbase.management.web.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.domain.Authority;
import org.radarbase.management.domain.User;
import org.radarbase.management.repository.UserRepository;
import org.radarbase.management.security.RadarAuthentication;
import org.radarbase.management.service.AuthService;
import org.radarbase.management.service.MailService;
import org.radarbase.management.service.UserService;
import org.radarbase.management.service.dto.RoleDTO;
import org.radarbase.management.service.dto.UserDTO;
import org.radarbase.management.service.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.radarbase.management.security.JwtAuthenticationFilter.setRadarToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the AccountResource REST controller.
 *
 * @see AccountResource
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
class AccountResourceIntTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Mock
    private UserService mockUserService;

    @Mock
    private MailService mockMailService;

    private MockMvc restUserMockMvc;

    @Autowired
    private RadarToken radarToken;

    @Autowired
    private AuthService authService;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendActivationEmail(any(User.class));

        SecurityContextHolder.getContext().setAuthentication(new RadarAuthentication(radarToken));

        AccountResource accountResource = new AccountResource();
        ReflectionTestUtils.setField(accountResource, "userService", userService);
        ReflectionTestUtils.setField(accountResource, "userMapper", userMapper);
        ReflectionTestUtils.setField(accountResource, "mailService", mockMailService);
        ReflectionTestUtils.setField(accountResource, "authService", authService);
        ReflectionTestUtils.setField(accountResource, "token", radarToken);
        ReflectionTestUtils.setField(accountResource, "managementPortalProperties",
                managementPortalProperties);

        AccountResource accountUserMockResource = new AccountResource();
        ReflectionTestUtils.setField(accountUserMockResource, "userService", mockUserService);
        ReflectionTestUtils.setField(accountUserMockResource, "userMapper", userMapper);
        ReflectionTestUtils.setField(accountUserMockResource, "mailService", mockMailService);
        ReflectionTestUtils.setField(accountUserMockResource, "authService", authService);
        ReflectionTestUtils.setField(accountUserMockResource, "token", radarToken);
        ReflectionTestUtils.setField(accountUserMockResource, "managementPortalProperties",
                managementPortalProperties);

        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource).build();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    void testNonAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(post("/api/login")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAuthenticatedUser() throws Exception {
        final RadarToken token = mock(RadarToken.class);

        Set<org.radarbase.management.domain.Role> roles = new HashSet<>();
        org.radarbase.management.domain.Role role = new org.radarbase.management.domain.Role();
        Authority authority = new Authority();
        authority.name = RoleAuthority.SYS_ADMIN.getAuthority();
        role.authority = authority;
        roles.add(role);

        User user = new User();
        user.setLogin("test");
        user.firstName = "john";
        user.lastName = "doe";
        user.email = "john.doe@jhipster.com";
        user.langKey = "en";
        user.setRoles(roles);
        when(mockUserService.getUserWithAuthorities()).thenReturn(Optional.of(user));

        restUserMockMvc.perform(post("/api/login")
                .with(request -> {
                    setRadarToken(request, token);
                    request.setRemoteUser("test");
                    return request;
                })
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login").value("test"))
                .andExpect(jsonPath("$.firstName").value("john"))
                .andExpect(jsonPath("$.lastName").value("doe"))
                .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
                .andExpect(jsonPath("$.langKey").value("en"))
                .andExpect(jsonPath("$.authorities").value(
                        RoleAuthority.SYS_ADMIN.getAuthority()));
    }

    @Test
    void testGetExistingAccount() throws Exception {
        Set<org.radarbase.management.domain.Role> roles = new HashSet<>();
        org.radarbase.management.domain.Role role = new org.radarbase.management.domain.Role();
        Authority authority = new Authority();
        authority.name = RoleAuthority.SYS_ADMIN.getAuthority();
        role.authority = authority;
        roles.add(role);

        User user = new User();
        user.setLogin("test");
        user.firstName = "john";
        user.lastName = "doe";
        user.email = "john.doe@jhipster.com";
        user.langKey = "en";
        user.setRoles(roles);
        when(mockUserService.getUserWithAuthorities()).thenReturn(Optional.of(user));

        restUserMockMvc.perform(get("/api/account")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login").value("test"))
                .andExpect(jsonPath("$.firstName").value("john"))
                .andExpect(jsonPath("$.lastName").value("doe"))
                .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
                .andExpect(jsonPath("$.langKey").value("en"))
                .andExpect(jsonPath("$.authorities").value(
                        RoleAuthority.SYS_ADMIN.getAuthority()));
    }

    @Test
    void testGetUnknownAccount() throws Exception {
        when(mockUserService.getUserWithAuthorities()).thenReturn(Optional.empty());

        restUserMockMvc.perform(get("/api/account")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void testSaveInvalidLogin() throws Exception {
        Set<RoleDTO> roles = new HashSet<>();
        RoleDTO role = new RoleDTO();
        role.setAuthorityName(RoleAuthority.PARTICIPANT.getAuthority());
        roles.add(role);

        UserDTO invalidUser = new UserDTO();
        invalidUser.setLogin("funky-log!n");          // invalid login
        invalidUser.setFirstName("Funky");
        invalidUser.setLastName("One");
        invalidUser.setEmail("funky@example.com");
        invalidUser.setActivated(true);
        invalidUser.setLangKey("en");
        invalidUser.setRoles(roles);

        restUserMockMvc.perform(post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
                .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByEmail("funky@example.com");
        assertThat(user).isNotPresent();
    }
}
