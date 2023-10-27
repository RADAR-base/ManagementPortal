package org.radarbase.management.web.rest

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.token.RadarToken
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.domain.Authority
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.User
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.security.JwtAuthenticationFilter.Companion.radarToken
import org.radarbase.management.security.RadarAuthentication
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.MailService
import org.radarbase.management.service.UserService
import org.radarbase.management.service.dto.RoleDTO
import org.radarbase.management.service.dto.UserDTO
import org.radarbase.management.service.mapper.UserMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Test class for the AccountResource REST controller.
 *
 * @see AccountResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
internal open class AccountResourceIntTest(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val userService: UserService,
    @Autowired private val userMapper: UserMapper,
    @Mock private val mockUserService: UserService,
    @Mock private val mockMailService: MailService,
    private var restUserMockMvc: MockMvc,
    @Autowired private val radarToken: RadarToken,
    @Autowired private val authService: AuthService,
    @Autowired private val managementPortalProperties: ManagementPortalProperties
) {

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Mockito.doNothing().`when`(mockMailService).sendActivationEmail(
            ArgumentMatchers.any(
                User::class.java
            )
        )
        SecurityContextHolder.getContext().authentication = RadarAuthentication(radarToken)
        val accountResource = AccountResource()
        ReflectionTestUtils.setField(accountResource, "userService", userService)
        ReflectionTestUtils.setField(accountResource, "userMapper", userMapper)
        ReflectionTestUtils.setField(accountResource, "mailService", mockMailService)
        ReflectionTestUtils.setField(accountResource, "authService", authService)
        ReflectionTestUtils.setField(accountResource, "token", radarToken)
        ReflectionTestUtils.setField(
            accountResource, "managementPortalProperties",
            managementPortalProperties
        )
        val accountUserMockResource = AccountResource()
        ReflectionTestUtils.setField(accountUserMockResource, "userService", mockUserService)
        ReflectionTestUtils.setField(accountUserMockResource, "userMapper", userMapper)
        ReflectionTestUtils.setField(accountUserMockResource, "mailService", mockMailService)
        ReflectionTestUtils.setField(accountUserMockResource, "authService", authService)
        ReflectionTestUtils.setField(accountUserMockResource, "token", radarToken)
        ReflectionTestUtils.setField(
            accountUserMockResource, "managementPortalProperties",
            managementPortalProperties
        )
        restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource).build()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.getContext().authentication = null
    }

    @Test
    @Throws(Exception::class)
    fun testNonAuthenticatedUser() {
        restUserMockMvc.perform(
            MockMvcRequestBuilders.post("/api/login")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden())
    }

    @Test
    @Throws(Exception::class)
    fun testAuthenticatedUser() {
        val token = Mockito.mock(RadarToken::class.java)
        val roles: MutableSet<Role> = HashSet()
        val role = Role()
        val authority = Authority()
        authority.name = RoleAuthority.SYS_ADMIN.authority
        role.authority = authority
        roles.add(role)
        val user = User()
        user.setLogin("test")
        user.firstName = "john"
        user.lastName = "doe"
        user.email = "john.doe@jhipster.com"
        user.langKey = "en"
        user.roles = roles
        Mockito.`when`(mockUserService.userWithAuthorities).thenReturn(Optional.of(user))
        restUserMockMvc.perform(MockMvcRequestBuilders.post("/api/login")
            .with { request: MockHttpServletRequest ->
                request.radarToken = token
                request.remoteUser = "test"
                request
            }
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.login").value("test"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("john"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("doe"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.langKey").value("en"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.authorities").value(
                    RoleAuthority.SYS_ADMIN.authority
                )
            )
    }

    @Test
    @Throws(Exception::class)
    fun testGetExistingAccount() {
        val roles: MutableSet<Role> = HashSet()
        val role = Role()
        val authority = Authority()
        authority.name = RoleAuthority.SYS_ADMIN.authority
        role.authority = authority
        roles.add(role)
        val user = User()
        user.setLogin("test")
        user.firstName = "john"
        user.lastName = "doe"
        user.email = "john.doe@jhipster.com"
        user.langKey = "en"
        user.roles = roles
        Mockito.`when`(mockUserService.userWithAuthorities).thenReturn(Optional.of(user))
        restUserMockMvc.perform(
            MockMvcRequestBuilders.get("/api/account")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.login").value("test"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("john"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("doe"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.langKey").value("en"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.authorities").value(
                    RoleAuthority.SYS_ADMIN.authority
                )
            )
    }

    @Test
    @Throws(Exception::class)
    fun testGetUnknownAccount() {
        Mockito.`when`(mockUserService.userWithAuthorities).thenReturn(Optional.empty())
        restUserMockMvc.perform(
            MockMvcRequestBuilders.get("/api/account")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden())
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun testSaveInvalidLogin() {
        val roles: MutableSet<RoleDTO> = HashSet()
        val role = RoleDTO()
        role.authorityName = RoleAuthority.PARTICIPANT.authority
        roles.add(role)
        val invalidUser = UserDTO()
        invalidUser.login = "funky-log!n" // invalid login
        invalidUser.firstName = "Funky"
        invalidUser.lastName = "One"
        invalidUser.email = "funky@example.com"
        invalidUser.isActivated = true
        invalidUser.langKey = "en"
        invalidUser.roles = roles
        restUserMockMvc.perform(
            MockMvcRequestBuilders.post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
        val user = userRepository.findOneByEmail("funky@example.com")
        Assertions.assertThat(user).isNotPresent()
    }
}
