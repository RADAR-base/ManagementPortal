package org.radarbase.management.security

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.ProjectService
import org.radarbase.management.web.rest.ProjectResource
import org.radarbase.management.web.rest.errors.ExceptionTranslator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockFilterConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import javax.servlet.ServletException

/**
 * Test class for the JwtAuthenticationFilter class.
 *
 * @see JwtAuthenticationFilter
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal class JwtAuthenticationFilterIntTest(
    @Autowired private val projectService: ProjectService,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val exceptionTranslator: ExceptionTranslator,
    private var rsaRestProjectMockMvc: MockMvc,
    private var ecRestProjectMockMvc: MockMvc,
    @Autowired private val authService: AuthService
) {

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val projectResource = ProjectResource
        ReflectionTestUtils.setField(projectResource, "projectService", projectService)
        ReflectionTestUtils.setField(projectResource, "authService", authService)
        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        rsaRestProjectMockMvc = MockMvcBuilders.standaloneSetup(projectResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter<StandaloneMockMvcBuilder>(filter)
            .defaultRequest<StandaloneMockMvcBuilder>(
                MockMvcRequestBuilders.get("/")
                    .with(OAuthHelper.rsaBearerToken())
            )
            .build()
        ecRestProjectMockMvc = MockMvcBuilders.standaloneSetup(projectResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter<StandaloneMockMvcBuilder>(filter)
            .defaultRequest<StandaloneMockMvcBuilder>(
                MockMvcRequestBuilders.get("/")
                    .with(OAuthHelper.bearerToken())
            )
            .build()
    }

    @Test
    @Throws(Exception::class)
    fun testMultipleSigningKeys() {
        // Check that we can get the project list with both RSA and EC signed token. We are testing
        // acceptance of the tokens, so no test on the content of the response is performed here.
        rsaRestProjectMockMvc.perform(MockMvcRequestBuilders.get("/api/projects?sort=id,desc"))
            .andExpect(MockMvcResultMatchers.status().isOk())
        ecRestProjectMockMvc.perform(MockMvcRequestBuilders.get("/api/projects?sort=id,desc"))
            .andExpect(MockMvcResultMatchers.status().isOk())
    }
}
