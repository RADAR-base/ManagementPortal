package org.radarbase.management.web.rest

import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalApp
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.OAuthClientService
import org.radarbase.management.service.OAuthClientServiceTestUtil
import org.radarbase.management.service.SubjectService
import org.radarbase.management.service.UserService
import org.radarbase.management.service.dto.ClientDetailsDTO
import org.radarbase.management.service.mapper.ClientDetailsMapper
import org.radarbase.management.web.rest.errors.ExceptionTranslator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockFilterConfig
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.transaction.annotation.Transactional
import java.util.function.Consumer

/**
 * Test class for the ProjectResource REST controller.
 *
 * @see ProjectResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalApp::class])
internal open class OAuthClientsResourceIntTest(
    @Autowired private val clientDetailsService: JdbcClientDetailsService,
    @Autowired private val clientDetailsMapper: ClientDetailsMapper,
    @Autowired private val subjectService: SubjectService,
    @Autowired private val userService: UserService,
    @Autowired private val oAuthClientService: OAuthClientService,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val exceptionTranslator: ExceptionTranslator,
    private var restOauthClientMvc: MockMvc,
    private var details: ClientDetailsDTO,
    private var clientDetailsList: List<ClientDetails>,
    private var databaseSizeBeforeCreate: Int = 0,
    @Autowired private val authService: AuthService
) {


    @BeforeEach
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val oauthClientsResource = OAuthClientsResource()
        ReflectionTestUtils.setField(
            oauthClientsResource, "clientDetailsMapper",
            clientDetailsMapper
        )
        ReflectionTestUtils.setField(
            oauthClientsResource, "subjectService",
            subjectService
        )
        ReflectionTestUtils.setField(
            oauthClientsResource, "userService",
            userService
        )
        ReflectionTestUtils.setField(
            oauthClientsResource, "authService",
            authService
        )
        ReflectionTestUtils.setField(
            oauthClientsResource, "oAuthClientService",
            oAuthClientService
        )
        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restOauthClientMvc = MockMvcBuilders.standaloneSetup(oauthClientsResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter<StandaloneMockMvcBuilder>(filter)
            .defaultRequest<StandaloneMockMvcBuilder>(MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken()))
            .build()
        databaseSizeBeforeCreate = clientDetailsService.listClientDetails().size

        // Create the OAuth Client
        details = OAuthClientServiceTestUtil.createClient()
        restOauthClientMvc.perform(
            MockMvcRequestBuilders.post("/api/oauth-clients")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Project in the database
        clientDetailsList = clientDetailsService.listClientDetails()
        Assertions.assertThat(clientDetailsList).hasSize(databaseSizeBeforeCreate + 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun createAndFetchOAuthClient() {
        // fetch the created oauth client and check the json result
        restOauthClientMvc.perform(
            MockMvcRequestBuilders.get("/api/oauth-clients/" + details.clientId)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.clientId").value(
                    Matchers.equalTo(
                        details.clientId
                    )
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.clientSecret").value(Matchers.nullValue()))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.accessTokenValiditySeconds").value(
                    Matchers.equalTo(
                        details
                            .accessTokenValiditySeconds?.toInt()
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.refreshTokenValiditySeconds").value(
                    Matchers.equalTo(
                        details
                            .refreshTokenValiditySeconds?.toInt()
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.scope").value(
                    Matchers.containsInAnyOrder<Any>(
                        details.scope?.toTypedArray()
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.autoApproveScopes").value(
                    Matchers.containsInAnyOrder<Any>(
                        details.autoApproveScopes?.toTypedArray()
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.authorizedGrantTypes").value(
                    Matchers.containsInAnyOrder<Any>(
                        details.authorizedGrantTypes?.toTypedArray()
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.authorities").value(
                    Matchers.containsInAnyOrder<Any>(details.authorities?.toTypedArray())
                )
            )
        val testDetails = clientDetailsList.stream()
            .filter { d: ClientDetails -> d.clientId == details.clientId }
            .findFirst()
            .orElseThrow()
        Assertions.assertThat(testDetails.clientSecret).startsWith("$2a$10$")
        Assertions.assertThat(testDetails.scope).containsExactlyInAnyOrderElementsOf(
            details.scope
        )
        Assertions.assertThat(testDetails.resourceIds).containsExactlyInAnyOrderElementsOf(
            details.resourceIds
        )
        Assertions.assertThat(testDetails.authorizedGrantTypes).containsExactlyInAnyOrderElementsOf(
            details.authorizedGrantTypes
        )
        details.autoApproveScopes?.forEach(Consumer { scope: String? ->
            Assertions.assertThat(
                testDetails.isAutoApprove(
                    scope
                )
            ).isTrue()
        })
        Assertions.assertThat(testDetails.accessTokenValiditySeconds).isEqualTo(
            details.accessTokenValiditySeconds?.toInt()
        )
        Assertions.assertThat(testDetails.refreshTokenValiditySeconds).isEqualTo(
            details.refreshTokenValiditySeconds?.toInt()
        )
        Assertions.assertThat(testDetails.authorities.stream().map { obj: GrantedAuthority -> obj.authority })
            .containsExactlyInAnyOrderElementsOf(details.authorities)
        Assertions.assertThat(testDetails.additionalInformation).containsAllEntriesOf(
            details.additionalInformation
        )
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun dupliceOAuthClient() {
        restOauthClientMvc.perform(
            MockMvcRequestBuilders.post("/api/oauth-clients")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details))
        )
            .andExpect(MockMvcResultMatchers.status().isConflict())
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun updateOAuthClient() {
        // update the client
        details.refreshTokenValiditySeconds = 20L
        restOauthClientMvc.perform(
            MockMvcRequestBuilders.put("/api/oauth-clients")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // fetch the client
        clientDetailsList = clientDetailsService.listClientDetails()
        Assertions.assertThat(clientDetailsList).hasSize(databaseSizeBeforeCreate + 1)
        val testDetails = clientDetailsList.stream()
            .filter { d: ClientDetails -> d.clientId == details.clientId }
            .findFirst()
            .orElseThrow()
        Assertions.assertThat(testDetails.refreshTokenValiditySeconds).isEqualTo(20)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun deleteOAuthClient() {
        restOauthClientMvc.perform(
            MockMvcRequestBuilders.delete("/api/oauth-clients/" + details.clientId)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
        val clientDetailsList = clientDetailsService.listClientDetails()
        Assertions.assertThat(clientDetailsList.size).isEqualTo(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun cannotModifyProtected() {
        // first change our test client to be protected
        details.additionalInformation!!["protected"] = "true"
        restOauthClientMvc.perform(
            MockMvcRequestBuilders.put("/api/oauth-clients")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // expect we can not delete it now
        restOauthClientMvc.perform(
            MockMvcRequestBuilders.delete("/api/oauth-clients/" + details.clientId)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details))
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden())

        // expect we can not update it now
        details.refreshTokenValiditySeconds = 20L
        restOauthClientMvc.perform(
            MockMvcRequestBuilders.put("/api/oauth-clients")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(details))
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden())
    }
}
