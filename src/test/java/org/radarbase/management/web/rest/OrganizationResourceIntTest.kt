package org.radarbase.management.web.rest

import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.Organization
import org.radarbase.management.domain.Project
import org.radarbase.management.repository.OrganizationRepository
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.OrganizationService
import org.radarbase.management.service.dto.OrganizationDTO
import org.radarbase.management.service.mapper.OrganizationMapper
import org.radarbase.management.web.rest.errors.ExceptionTranslator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockFilterConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import javax.servlet.ServletException

/**
 * Test class for the OrganizationResource REST controller.
 *
 * @see OrganizationResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal class OrganizationResourceIntTest(
    @Autowired private val organizationResource: OrganizationResource,
    @Autowired private val organizationService: OrganizationService,
    @Autowired private val authService: AuthService,
    @Autowired private val organizationMapper: OrganizationMapper,
    @Autowired private val organizationRepository: OrganizationRepository,
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val exceptionTranslator: ExceptionTranslator,
) {
    private lateinit var restOrganizationMockMvc: MockMvc
    private lateinit var organization: Organization

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restOrganizationMockMvc =
            MockMvcBuilders
                .standaloneSetup(organizationResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter)
                .addFilter<StandaloneMockMvcBuilder>(filter)
                .defaultRequest<StandaloneMockMvcBuilder>(
                    MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken()),
                )
                .build()

        organization = this.createEntity()
    }

    /**
     * Create an entity for this test.
     */
    private fun createEntity(): Organization {
        val org = Organization()
        org.name = "org1"
        org.description = "Test Organization 1"
        org.location = "Somewhere"
        return org
    }

    @Throws(Exception::class)
    @Test
    fun nonExistingOrganization() {
        // Get the organization
        restOrganizationMockMvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/api/organizations/{name}",
                    organization.name,
                ),
            ).andExpect(MockMvcResultMatchers.status().isNotFound())
    }

    @Throws(Exception::class)
    @Test
    fun allOrganizations() {
        // Initialize the database
        organizationRepository.saveAndFlush(organization)

        // Get all the organizations
        restOrganizationMockMvc
            .perform(MockMvcRequestBuilders.get("/api/organizations"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].name").value<Iterable<String?>>(Matchers.hasItem("org1")),
            )
    }

    @Throws(Exception::class)
    @Test
    fun projectsByOrganizationName() {
        // Initialize the database
        organizationRepository.saveAndFlush(organization)
        val project: Project =
            ProjectResourceIntTest.Companion
                .createEntity()
                .organization(organization)
                .projectName("organization_project")
        projectRepository.saveAndFlush(project)

        // Get projects of the organization
        restOrganizationMockMvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/api/organizations/{name}/projects",
                    organization.name,
                ),
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[*].projectName").value("organization_project"))
        projectRepository.delete(project)
    }

    @Test
    @Throws(Exception::class)
    fun createOrganization() {
        val orgDto = organizationMapper.organizationToOrganizationDTO(organization)
        restOrganizationMockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/organizations")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(orgDto)),
            ).andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Organization in the database
        val savedOrg = orgDto.name?.let { organizationRepository.findOneByName(it) }
        Assertions.assertThat(savedOrg).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun createOrganizationWithExistingName() {
        val orgDto = organizationMapper.organizationToOrganizationDTO(organization)
        restOrganizationMockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/organizations")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(orgDto)),
            ).andExpect(MockMvcResultMatchers.status().isCreated())

        // Second request should fail
        restOrganizationMockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/organizations")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(orgDto)),
            ).andExpect(MockMvcResultMatchers.status().isConflict())
    }

    @Test
    @Throws(Exception::class)
    fun checkGroupNameIsRequired() {
        val orgDto: OrganizationDTO = organizationMapper.organizationToOrganizationDTO(organization)
        orgDto.name = null
        restOrganizationMockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/api/organizations")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(orgDto)),
            ).andExpect(MockMvcResultMatchers.status().isBadRequest())
    }

    @Test
    @Throws(Exception::class)
    fun getOrganization() {
        // Initialize the database
        organizationRepository.saveAndFlush(organization)

        // Get the organization
        restOrganizationMockMvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/api/organizations/{name}",
                    organization.name,
                ),
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("org1"))
    }

    @Test
    @Throws(Exception::class)
    fun editOrganization() {
        // Initialize the database
        organizationRepository.saveAndFlush(organization)
        val updatedOrgDto =
            organizationMapper
                .organizationToOrganizationDTO(organization)
        updatedOrgDto.location = "Other location"

        // Update the organization
        restOrganizationMockMvc
            .perform(
                MockMvcRequestBuilders
                    .put("/api/organizations")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(updatedOrgDto)),
            ).andExpect(MockMvcResultMatchers.status().isOk())

        // Get the organization
        restOrganizationMockMvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/api/organizations/{name}",
                    organization.name,
                ),
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.location").value("Other location"))
    }

    @AfterEach
    fun tearDown() {
        val testOrg =
            organizationRepository.findOneByName(
                organization.name!!,
            )
        if (testOrg != null) {
            organizationRepository.delete(testOrg)
        }
    }
}
