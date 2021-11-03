package org.radarbase.management.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.radarbase.management.web.rest.TestUtil.sameInstant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import javax.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockitoAnnotations;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.domain.Project;
import org.radarbase.management.domain.enumeration.ProjectStatus;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.security.JwtAuthenticationFilter;
import org.radarbase.management.service.ProjectService;
import org.radarbase.management.service.dto.ProjectDTO;
import org.radarbase.management.service.mapper.ProjectMapper;
import org.radarbase.management.web.rest.errors.ExceptionTranslator;
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

/**
 * Test class for the ProjectResource REST controller.
 *
 * @see ProjectResource
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
class ProjectResourceIntTest {

    private static final String DEFAULT_PROJECT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_PROJECT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_ORGANIZATION = "AAAAAAAAAA";
    private static final String UPDATED_ORGANIZATION = "BBBBBBBBBB";

    private static final String DEFAULT_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_LOCATION = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_START_DATE = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_START_DATE = ZonedDateTime.now(
            ZoneId.systemDefault()).withNano(0);

    private static final ProjectStatus DEFAULT_PROJECT_STATUS = ProjectStatus.PLANNING;
    private static final ProjectStatus UPDATED_PROJECT_STATUS = ProjectStatus.ONGOING;

    private static final ZonedDateTime DEFAULT_END_DATE = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_END_DATE = ZonedDateTime.now(
            ZoneId.systemDefault()).withNano(0);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private RadarToken radarToken;

    private MockMvc restProjectMockMvc;

    private Project project;

    @BeforeEach
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
        ProjectResource projectResource = new ProjectResource();
        ReflectionTestUtils.setField(projectResource, "projectRepository", projectRepository);
        ReflectionTestUtils.setField(projectResource, "projectService", projectService);
        ReflectionTestUtils.setField(projectResource, "token", radarToken);

        JwtAuthenticationFilter filter = OAuthHelper.createAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restProjectMockMvc = MockMvcBuilders.standaloneSetup(projectResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter(filter)
            .defaultRequest(get("/").with(OAuthHelper.bearerToken())).build();
    }

    /**
     * Create an entity for this test.
     *
     * <p>This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.</p>
     */
    public static Project createEntity() {
        Project project = new Project()
                .projectName(DEFAULT_PROJECT_NAME)
                .description(DEFAULT_DESCRIPTION)
                .organization(DEFAULT_ORGANIZATION)
                .location(DEFAULT_LOCATION)
                .startDate(DEFAULT_START_DATE)
                .projectStatus(DEFAULT_PROJECT_STATUS)
                .endDate(DEFAULT_END_DATE);
        return project;
    }

    @BeforeEach
    public void initTest() {
        project = createEntity();
    }

    @Test
    @Transactional
    void createProject() throws Exception {
        int databaseSizeBeforeCreate = projectRepository.findAll().size();

        // Create the Project
        ProjectDTO projectDto = projectMapper.projectToProjectDTO(project);
        restProjectMockMvc.perform(post("/api/projects")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(projectDto)))
            .andExpect(status().isCreated());

        // Validate the Project in the database
        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeCreate + 1);
        Project testProject = projectList.get(projectList.size() - 1);
        assertThat(testProject.getProjectName()).isEqualTo(DEFAULT_PROJECT_NAME);
        assertThat(testProject.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testProject.getOrganization()).isEqualTo(DEFAULT_ORGANIZATION);
        assertThat(testProject.getLocation()).isEqualTo(DEFAULT_LOCATION);
        assertThat(testProject.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testProject.getProjectStatus()).isEqualTo(DEFAULT_PROJECT_STATUS);
        assertThat(testProject.getEndDate()).isEqualTo(DEFAULT_END_DATE);
    }

    @Test
    @Transactional
    void createProjectWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = projectRepository.findAll().size();

        // Create the Project with an existing ID
        project.setId(1L);
        ProjectDTO projectDto = projectMapper.projectToProjectDTO(project);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProjectMockMvc.perform(post("/api/projects")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(projectDto)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkProjectNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = projectRepository.findAll().size();
        // set the field null
        project.setProjectName(null);

        // Create the Project, which fails.
        ProjectDTO projectDto = projectMapper.projectToProjectDTO(project);

        restProjectMockMvc.perform(post("/api/projects")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(projectDto)))
            .andExpect(status().isBadRequest());

        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDescriptionIsRequired() throws Exception {
        int databaseSizeBeforeTest = projectRepository.findAll().size();
        // set the field null
        project.setDescription(null);

        // Create the Project, which fails.
        ProjectDTO projectDto = projectMapper.projectToProjectDTO(project);

        restProjectMockMvc.perform(post("/api/projects")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(projectDto)))
            .andExpect(status().isBadRequest());

        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLocationIsRequired() throws Exception {
        int databaseSizeBeforeTest = projectRepository.findAll().size();
        // set the field null
        project.setLocation(null);

        // Create the Project, which fails.
        ProjectDTO projectDto = projectMapper.projectToProjectDTO(project);

        restProjectMockMvc.perform(post("/api/projects")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(projectDto)))
            .andExpect(status().isBadRequest());

        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProjects() throws Exception {
        // Initialize the database
        projectRepository.saveAndFlush(project);

        // Get all the projectList
        restProjectMockMvc.perform(get("/api/projects?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(project.getId().intValue())))
                .andExpect(jsonPath("$.[*].projectName").value(hasItem(DEFAULT_PROJECT_NAME)))
                .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
                .andExpect(jsonPath("$.[*].organization").value(hasItem(DEFAULT_ORGANIZATION)))
                .andExpect(jsonPath("$.[*].location").value(hasItem(DEFAULT_LOCATION)))
                .andExpect(jsonPath("$.[*].startDate").value(
                        hasItem(sameInstant(DEFAULT_START_DATE))))
                .andExpect(jsonPath("$.[*].projectStatus").value(
                        hasItem(DEFAULT_PROJECT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].endDate").value(hasItem(sameInstant(DEFAULT_END_DATE))));
    }

    @Test
    @Transactional
    void getProject() throws Exception {
        // Initialize the database
        projectRepository.saveAndFlush(project);

        // Get the project
        restProjectMockMvc.perform(get("/api/projects/{projectName}", project.getProjectName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(project.getId().intValue()))
                .andExpect(jsonPath("$.projectName").value(DEFAULT_PROJECT_NAME))
                .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
                .andExpect(jsonPath("$.organization").value(DEFAULT_ORGANIZATION))
                .andExpect(jsonPath("$.location").value(DEFAULT_LOCATION))
                .andExpect(jsonPath("$.startDate").value(sameInstant(DEFAULT_START_DATE)))
                .andExpect(jsonPath("$.projectStatus").value(DEFAULT_PROJECT_STATUS.toString()))
                .andExpect(jsonPath("$.endDate").value(sameInstant(DEFAULT_END_DATE)));
    }

    @Test
    @Transactional
    void getNonExistingProject() throws Exception {
        // Get the project
        restProjectMockMvc.perform(get("/api/projects/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void updateProject() throws Exception {
        // Initialize the database
        projectRepository.saveAndFlush(project);
        int databaseSizeBeforeUpdate = projectRepository.findAll().size();

        // Update the project
        Project updatedProject = projectRepository.findById(project.getId()).get();
        updatedProject
                .projectName(UPDATED_PROJECT_NAME)
                .description(UPDATED_DESCRIPTION)
                .organization(UPDATED_ORGANIZATION)
                .location(UPDATED_LOCATION)
                .startDate(UPDATED_START_DATE)
                .projectStatus(UPDATED_PROJECT_STATUS)
                .endDate(UPDATED_END_DATE);
        ProjectDTO projectDto = projectMapper.projectToProjectDTO(updatedProject);

        restProjectMockMvc.perform(put("/api/projects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(projectDto)))
                .andExpect(status().isOk());

        // Validate the Project in the database
        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeUpdate);
        Project testProject = projectList.get(projectList.size() - 1);
        assertThat(testProject.getProjectName()).isEqualTo(UPDATED_PROJECT_NAME);
        assertThat(testProject.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testProject.getOrganization()).isEqualTo(UPDATED_ORGANIZATION);
        assertThat(testProject.getLocation()).isEqualTo(UPDATED_LOCATION);
        assertThat(testProject.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testProject.getProjectStatus()).isEqualTo(UPDATED_PROJECT_STATUS);
        assertThat(testProject.getEndDate()).isEqualTo(UPDATED_END_DATE);
    }

    @Test
    @Transactional
    void updateNonExistingProject() throws Exception {
        int databaseSizeBeforeUpdate = projectRepository.findAll().size();

        // Create the Project
        ProjectDTO projectDto = projectMapper.projectToProjectDTO(project);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restProjectMockMvc.perform(put("/api/projects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(projectDto)))
                .andExpect(status().isCreated());

        // Validate the Project in the database
        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    void deleteProject() throws Exception {
        // Initialize the database
        projectRepository.saveAndFlush(project);
        int databaseSizeBeforeDelete = projectRepository.findAll().size();

        // Get the project
        restProjectMockMvc.perform(delete("/api/projects/{projectName}", project.getProjectName())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Project> projectList = projectRepository.findAll();
        assertThat(projectList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void equalsVerifier() throws Exception {
        Assertions.assertTrue(TestUtil.equalsVerifier(Project.class));
    }
}
