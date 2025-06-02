package org.radarbase.management.web.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.config.BasePostgresIntegrationTest
import org.radarbase.management.domain.Organization
import org.radarbase.management.domain.Project
import org.radarbase.management.domain.enumeration.ProjectStatus
import org.radarbase.management.repository.OrganizationRepository
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.mapper.ProjectMapper
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
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.servlet.ServletException

/**
 * Test class for the ProjectResource REST controller.
 *
 * @see ProjectResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal class ProjectResourceIntTest(
    @Autowired private val projectResource: ProjectResource,

//    @Autowired private val subjectMapper: SubjectMapper,
    @Autowired private val projectRepository: ProjectRepository,
//    @Autowired private val projectService: ProjectService,
//    @Autowired private val roleService: RoleService,
//    @Autowired private val subjectService: SubjectService,
//    @Autowired private val sourceService: SourceService,
//    @Autowired private val authService: AuthService,

    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val exceptionTranslator: ExceptionTranslator,

    @Autowired private val projectMapper: ProjectMapper,
    @Autowired private val organizationRepository: OrganizationRepository,
) : BasePostgresIntegrationTest() {
    private lateinit var restProjectMockMvc: MockMvc
    private lateinit var project: Project

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restProjectMockMvc = MockMvcBuilders.standaloneSetup(projectResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter<StandaloneMockMvcBuilder>(filter)
            .defaultRequest<StandaloneMockMvcBuilder>(MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken()))
            .build()
    }

    @BeforeEach
    fun initTest() {
        project = createEntity()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createProject() {
        val databaseSizeBeforeCreate = projectRepository.findAll().size

        // Create the Project
        val projectDto = projectMapper.projectToProjectDTO(project)
        restProjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/projects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(projectDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Project in the database
        val projectList = projectRepository.findAll()
        assertThat(projectList).hasSize(databaseSizeBeforeCreate + 1)
        val testProject = projectList[projectList.size - 1]
        assertThat(testProject!!.projectName).isEqualTo(DEFAULT_PROJECT_NAME)
        assertThat(testProject.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testProject.organizationName).isEqualTo(DEFAULT_ORGANIZATION)
        assertThat(testProject.location).isEqualTo(DEFAULT_LOCATION)
        assertThat(testProject.startDate).isEqualTo(DEFAULT_START_DATE)
        assertThat(testProject.projectStatus).isEqualTo(DEFAULT_PROJECT_STATUS)
        assertThat(testProject.endDate).isEqualTo(DEFAULT_END_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createProjectWithExistingId() {
        val databaseSizeBeforeCreate = projectRepository.findAll().size

        // Create the Project with an existing ID
        project.id = 1L
        val projectDto = projectMapper.projectToProjectDTO(project)

        // An entity with an existing ID cannot be created, so this API call must fail
        restProjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/projects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(projectDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())

        // Validate the Alice in the database
        val projectList = projectRepository.findAll()
        assertThat(projectList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkProjectNameIsRequired() {
        val databaseSizeBeforeTest = projectRepository.findAll().size
        // set the field null
        project.projectName = null

        // Create the Project, which fails.
        val projectDto: ProjectDTO? = projectMapper.projectToProjectDTO(project)
        restProjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/projects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(projectDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
        val projectList = projectRepository.findAll()
        assertThat(projectList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkDescriptionIsRequired() {
        val databaseSizeBeforeTest = projectRepository.findAll().size
        // set the field null
        project.description = null

        // Create the Project, which fails.
        val projectDto = projectMapper.projectToProjectDTO(project)
        restProjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/projects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(projectDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
        val projectList = projectRepository.findAll()
        assertThat(projectList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkLocationIsRequired() {
        val databaseSizeBeforeTest = projectRepository.findAll().size
        // set the field null
        project.location = null

        // Create the Project, which fails.
        val projectDto: ProjectDTO? = projectMapper.projectToProjectDTO(project)
        restProjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/projects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(projectDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
        val projectList = projectRepository.findAll()
        assertThat(projectList).hasSize(databaseSizeBeforeTest)
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun allProjects() {
            // Initialize the database
            projectRepository.saveAndFlush(project)

            // Get all the projectList
            restProjectMockMvc.perform(MockMvcRequestBuilders.get("/api/projects?sort=id,desc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].id").value<Iterable<Int?>>(
                        Matchers.hasItem(
                            project.id!!.toInt()
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].projectName").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_PROJECT_NAME
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].description").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_DESCRIPTION
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].organizationName").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_ORGANIZATION
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].location").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_LOCATION
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].startDate").value<Iterable<String?>>(
                        Matchers.hasItem(TestUtil.sameInstant(DEFAULT_START_DATE))
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].projectStatus").value<Iterable<String?>>(
                        Matchers.hasItem(DEFAULT_PROJECT_STATUS.toString())
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].endDate").value<Iterable<String?>>(
                        Matchers.hasItem(
                            TestUtil.sameInstant(
                                DEFAULT_END_DATE
                            )
                        )
                    )
                )
        }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getProject() {
        // Initialize the database
        projectRepository.saveAndFlush(project)

        // Get the project
        restProjectMockMvc.perform(MockMvcRequestBuilders.get("/api/projects/{projectName}", project.projectName))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(project.id!!.toInt()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.projectName").value(DEFAULT_PROJECT_NAME))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(MockMvcResultMatchers.jsonPath("$.organizationName").value(DEFAULT_ORGANIZATION))
            .andExpect(MockMvcResultMatchers.jsonPath("$.location").value(DEFAULT_LOCATION))
            .andExpect(MockMvcResultMatchers.jsonPath("$.startDate").value(TestUtil.sameInstant(DEFAULT_START_DATE)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.projectStatus").value(DEFAULT_PROJECT_STATUS.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.endDate").value(TestUtil.sameInstant(DEFAULT_END_DATE)))
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun nonExistingProject() {
            // Get the project
            restProjectMockMvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}", Long.MAX_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
        }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateProject() {
        // Initialize the database
        projectRepository.saveAndFlush(project)
        val org = Organization()
        org.name = UPDATED_ORGANIZATION
        org.description = "Test Organization 1"
        org.location = "Somewhere"
        organizationRepository.saveAndFlush(org)
        assertThat(org.id).isNotNull()
        val databaseSizeBeforeUpdate = projectRepository.findAll().size

        // Update the project
        val updatedProject = projectRepository.findById(project.id!!).get()
        updatedProject
            .projectName(UPDATED_PROJECT_NAME)
            .description(UPDATED_DESCRIPTION)
            .organization(org)
            .location(UPDATED_LOCATION)
            .startDate(UPDATED_START_DATE)
            .projectStatus(UPDATED_PROJECT_STATUS)
            .endDate(UPDATED_END_DATE)
        val projectDto = projectMapper.projectToProjectDTO(updatedProject)
        restProjectMockMvc.perform(
            MockMvcRequestBuilders.put("/api/projects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(projectDto))
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the Project in the database
        val projectList = projectRepository.findAll()
        assertThat(projectList).hasSize(databaseSizeBeforeUpdate)
        val testProject = projectList[projectList.size - 1]
        assertThat(testProject!!.projectName).isEqualTo(UPDATED_PROJECT_NAME)
        assertThat(testProject.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testProject.organizationName).isEqualTo(UPDATED_ORGANIZATION)
        assertThat<Organization>(testProject.organization).isEqualTo(org)
        assertThat(testProject.location).isEqualTo(UPDATED_LOCATION)
        assertThat(testProject.startDate).isEqualTo(UPDATED_START_DATE)
        assertThat<ProjectStatus>(testProject.projectStatus).isEqualTo(
            UPDATED_PROJECT_STATUS
        )
        assertThat(testProject.endDate).isEqualTo(UPDATED_END_DATE)
        organizationRepository.delete(org)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateNonExistingProject() {
        val databaseSizeBeforeUpdate = projectRepository.findAll().size

        // Create the Project
        val projectDto = projectMapper.projectToProjectDTO(project)

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restProjectMockMvc.perform(
            MockMvcRequestBuilders.put("/api/projects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(projectDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Project in the database
        val projectList = projectRepository.findAll()
        assertThat(projectList).hasSize(databaseSizeBeforeUpdate + 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteProject() {
        // Initialize the database
        projectRepository.saveAndFlush(project)
        val databaseSizeBeforeDelete = projectRepository.findAll().size

        // Get the project
        restProjectMockMvc.perform(
            MockMvcRequestBuilders.delete("/api/projects/{projectName}", project.projectName)
                .accept(TestUtil.APPLICATION_JSON_UTF8)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the database is empty
        val projectList = projectRepository.findAll()
        assertThat(projectList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun equalsVerifier() {
        org.junit.jupiter.api.Assertions.assertTrue(TestUtil.equalsVerifier(Project::class.java))
    }

    companion object {
        private const val DEFAULT_PROJECT_NAME = "AAAAAAAAAA"
        private const val UPDATED_PROJECT_NAME = "BBBBBBBBBB"
        private const val DEFAULT_DESCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"
        private const val DEFAULT_ORGANIZATION = "main"
        private const val UPDATED_ORGANIZATION = "org1"
        private const val DEFAULT_LOCATION = "AAAAAAAAAA"
        private const val UPDATED_LOCATION = "BBBBBBBBBB"
        private val DEFAULT_START_DATE = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(0L), ZoneOffset.UTC
        )
        private val UPDATED_START_DATE = ZonedDateTime.now(
            ZoneId.systemDefault()
        ).withNano(0)
        private val DEFAULT_PROJECT_STATUS = ProjectStatus.PLANNING
        private val UPDATED_PROJECT_STATUS = ProjectStatus.ONGOING
        private val DEFAULT_END_DATE = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(0L), ZoneOffset.UTC
        )
        private val UPDATED_END_DATE = ZonedDateTime.now(
            ZoneId.systemDefault()
        ).withNano(0)

        /**
         * Create an entity for this test.
         *
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        fun createEntity(): Project {
            val organization = Organization()
            organization.id = 1L
            organization.name = DEFAULT_ORGANIZATION
            organization.description = "test"
            return Project()
                .projectName(DEFAULT_PROJECT_NAME)
                .description(DEFAULT_DESCRIPTION)
                .organizationName(DEFAULT_ORGANIZATION)
                .organization(organization)
                .location(DEFAULT_LOCATION)
                .startDate(DEFAULT_START_DATE)
                .projectStatus(DEFAULT_PROJECT_STATUS)
                .endDate(DEFAULT_END_DATE)
        }
    }
}
