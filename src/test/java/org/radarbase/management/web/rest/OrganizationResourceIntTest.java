package org.radarbase.management.web.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.radarbase.auth.authentication.OAuthHelper;
// import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.domain.Organization;
import org.radarbase.management.repository.OrganizationRepository;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.service.OrganizationService;
import org.radarbase.management.service.mapper.OrganizationMapper;
import org.radarbase.management.web.rest.errors.ExceptionTranslator;
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

import javax.servlet.ServletException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the OrganizationResource REST controller.
 *
 * @see OrganizationResource
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
class OrganizationResourceIntTest {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    // @Autowired
    // private RadarToken token;

    private MockMvc restOrganizationMockMvc;

    private Organization organization;

    @BeforeEach
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
        var orgResource = new OrganizationResource();
        ReflectionTestUtils
            .setField(orgResource, "organizationService", organizationService);
        // ReflectionTestUtils.setField(orgResource, "token", token);

        var filter = OAuthHelper.createAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restOrganizationMockMvc = MockMvcBuilders.standaloneSetup(orgResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter)
                .addFilter(filter)
                .defaultRequest(get("/").with(OAuthHelper.bearerToken())).build();
        organization = createEntity();
    }

    @AfterEach
    public void tearDown() {
        var testOrg = organizationRepository.findOneByName(organization.getName());
        testOrg.ifPresent(organizationRepository::delete);
    }

    /**
     * Create an entity for this test.
     */
    private Organization createEntity() {
        var org = new Organization();
        org.setName("org1");
        org.setDescription("Test Organization 1");
        org.setLocation("Somewhere");
        return org;
    }

    @Test
    void createOrganization() throws Exception {
        var orgDto = organizationMapper.organizationToOrganizationDTO(organization);
        restOrganizationMockMvc.perform(post("/api/organizations")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(orgDto)))
                .andExpect(status().isCreated());
                
        // Validate the Organization in the database
        var savedOrg = organizationRepository.findOneByName(orgDto.getName());
        assertThat(savedOrg).isNotEmpty();
    }

    @Test
    void createOrganizationWithExistingName() throws Exception {
        var orgDto = organizationMapper.organizationToOrganizationDTO(organization);
        restOrganizationMockMvc.perform(post("/api/organizations")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(orgDto)))
                .andExpect(status().isCreated());

        // Second request should fail
        restOrganizationMockMvc.perform(post("/api/organizations")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(orgDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void checkGroupNameIsRequired() throws Exception {
        var orgDto = organizationMapper.organizationToOrganizationDTO(organization);
        orgDto.setName(null);
        restOrganizationMockMvc.perform(post("/api/organizations")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(orgDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllOrganizations() throws Exception {
        // Initialize the database
        organizationRepository.saveAndFlush(organization);

        // Get all the organizations
        restOrganizationMockMvc.perform(get("/api/organizations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].name").value(hasItem("org1")));
    }

    @Test
    void getOrganization() throws Exception {
        // Initialize the database
        organizationRepository.saveAndFlush(organization);

        // Get the organization
        restOrganizationMockMvc.perform(get("/api/organizations/{name}",
                        organization.getName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("org1"));
    }

    @Test
    void editOrganization() throws Exception {
        // Initialize the database
        organizationRepository.saveAndFlush(organization);

        var updatedOrgDto = organizationMapper
                .organizationToOrganizationDTO(organization);
        updatedOrgDto.setLocation("Other location");

        // Update the organization
        restOrganizationMockMvc.perform(put("/api/organizations")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(updatedOrgDto)))
                .andExpect(status().isOk());

        // Get the organization
        restOrganizationMockMvc.perform(get("/api/organizations/{name}",
                        organization.getName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.location").value("Other location"));
    }

    @Test
    void getNonExistingOrganization() throws Exception {
        // Get the organization
        restOrganizationMockMvc.perform(get("/api/organizations/{name}",
                        organization.getName()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProjectsByOrganizationName() throws Exception {
        // Initialize the database
        organizationRepository.saveAndFlush(organization);

        var project = ProjectResourceIntTest.createEntity()
                .organization(organization)
                .projectName("organization_project");
        projectRepository.saveAndFlush(project);

        // Get projects of the organization
        restOrganizationMockMvc.perform(get("/api/organizations/{name}/projects",
                        organization.getName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].projectName").value("organization_project"));
        
        projectRepository.delete(project);
    }
}
