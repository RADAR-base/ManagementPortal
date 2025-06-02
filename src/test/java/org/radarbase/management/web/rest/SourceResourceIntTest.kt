package org.radarbase.management.web.rest

import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.config.BasePostgresIntegrationTest
import org.radarbase.management.domain.Project
import org.radarbase.management.domain.Source
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.repository.SourceRepository
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.SourceService
import org.radarbase.management.service.SourceTypeService
import org.radarbase.management.service.mapper.SourceMapper
import org.radarbase.management.service.mapper.SourceTypeMapper
import org.radarbase.management.web.rest.errors.ExceptionTranslator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockFilterConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.servlet.ServletException

/**
 * Test class for the DeviceResource REST controller.
 *
 * @see SourceResource
 */

internal class SourceResourceIntTest (
    @Autowired private val sourceResource: SourceResource,

    @Autowired private val sourceRepository: SourceRepository,
    @Autowired private val sourceMapper: SourceMapper,
    @Autowired private val sourceTypeService: SourceTypeService,
    @Autowired private val sourceTypeMapper: SourceTypeMapper,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val exceptionTranslator: ExceptionTranslator,
    @Autowired private val projectRepository: ProjectRepository,
) : BasePostgresIntegrationTest() {
    private lateinit var restDeviceMockMvc: MockMvc
    private lateinit var source: Source
    private lateinit var project: Project

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restDeviceMockMvc = MockMvcBuilders.standaloneSetup(sourceResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter<StandaloneMockMvcBuilder>(filter)
            .defaultRequest<StandaloneMockMvcBuilder>(MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken()))
            .alwaysDo<StandaloneMockMvcBuilder>(MockMvcResultHandlers.print()).build()
    }

    @BeforeEach
    fun initTest() {
        source = createEntity()
        val sourceTypeDtos = sourceTypeService.findAll()
        Assertions.assertThat(sourceTypeDtos.size).isPositive()
        source.sourceType = sourceTypeMapper.sourceTypeDTOToSourceType(sourceTypeDtos[0])
        project = projectRepository.findById(1L).get()
        source.project(project)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createSource() {
        val databaseSizeBeforeCreate = sourceRepository.findAll().size

        // Create the Source
        val sourceDto = sourceMapper.sourceToSourceDTO(source)
        restDeviceMockMvc.perform(
            MockMvcRequestBuilders.post("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        Assertions.assertThat(sourceList).hasSize(databaseSizeBeforeCreate + 1)
        val testSource = sourceList[sourceList.size - 1]
        Assertions.assertThat(testSource.assigned).isEqualTo(DEFAULT_ASSIGNED)
        Assertions.assertThat(testSource.sourceName).isEqualTo(DEFAULT_SOURCE_NAME)
        Assertions.assertThat(testSource.project!!.projectName).isEqualTo(project.projectName)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createSourceWithExistingId() {
        val databaseSizeBeforeCreate = sourceRepository.findAll().size

        // Create the Source with an existing ID
        source.id = 1L
        val sourceDto = sourceMapper.sourceToSourceDTO(source)

        // An entity with an existing ID cannot be created, so this API call must fail
        restDeviceMockMvc.perform(
            MockMvcRequestBuilders.post("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())

        // Validate the Alice in the database
        val sourceList = sourceRepository.findAll()
        Assertions.assertThat(sourceList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkSourcePhysicalIdIsGenerated() {
        val databaseSizeBeforeTest = sourceRepository.findAll().size
        // set the field null
        source.sourceId = null

        // Create the Source
        val sourceDto = sourceMapper.sourceToSourceDTO(source)
        restDeviceMockMvc.perform(
            MockMvcRequestBuilders.post("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())
        val sourceList = sourceRepository.findAll()
        Assertions.assertThat(sourceList).hasSize(databaseSizeBeforeTest + 1)

        // find our created source
        val createdSource = sourceList.stream()
            .filter { s: Source? -> s!!.sourceName == DEFAULT_SOURCE_NAME }
            .findFirst()
            .orElse(null)
        Assertions.assertThat(createdSource).isNotNull()

        // check source id
        Assertions.assertThat(createdSource!!.sourceId).isNotNull()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkAssignedIsRequired() {
        val databaseSizeBeforeTest = sourceRepository.findAll().size
        // set the field null
        source.assigned = null

        // Create the Source, which fails.
        val sourceDto = sourceMapper.sourceToSourceDTO(source)
        restDeviceMockMvc.perform(
            MockMvcRequestBuilders.post("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
        val sourceList = sourceRepository.findAll()
        Assertions.assertThat(sourceList).hasSize(databaseSizeBeforeTest)
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun allSources() {
            // Initialize the database
            sourceRepository.saveAndFlush(source)

            // Get all the deviceList
            restDeviceMockMvc.perform(MockMvcRequestBuilders.get("/api/sources?sort=id,desc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].id").value<Iterable<Int?>>(
                        Matchers.hasItem(
                            source.id!!.toInt()
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].sourceId").value(Matchers.everyItem(Matchers.notNullValue()))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].assigned").value<Iterable<Boolean?>>(
                        Matchers.hasItem(DEFAULT_ASSIGNED)
                    )
                )
        }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getSource() {
        // Initialize the database
        sourceRepository.saveAndFlush(source)

        // Get the source
        restDeviceMockMvc.perform(MockMvcRequestBuilders.get("/api/sources/{sourceName}", source.sourceName))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(source.id!!.toInt()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sourceId").value(Matchers.notNullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.assigned").value(DEFAULT_ASSIGNED))
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun nonExistingSource() {
            // Get the source
            restDeviceMockMvc.perform(MockMvcRequestBuilders.get("/api/sources/{id}", Long.MAX_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
        }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateSource() {
        // Initialize the database
        sourceRepository.saveAndFlush(source)
        val databaseSizeBeforeUpdate = sourceRepository.findAll().size

        // Update the source
        val updatedSource = sourceRepository.findById(source.id!!).get()
        updatedSource
            .sourceId(UPDATED_SOURCE_PHYSICAL_ID)
            .assigned = UPDATED_ASSIGNED
        val sourceDto = sourceMapper.sourceToSourceDTO(updatedSource)
        restDeviceMockMvc.perform(
            MockMvcRequestBuilders.put("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        Assertions.assertThat(sourceList).hasSize(databaseSizeBeforeUpdate)
        val testSource = sourceList[sourceList.size - 1]
        Assertions.assertThat(testSource.sourceId).isEqualTo(UPDATED_SOURCE_PHYSICAL_ID)
        Assertions.assertThat(testSource.assigned).isEqualTo(UPDATED_ASSIGNED)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateNonExistingSource() {
        val databaseSizeBeforeUpdate = sourceRepository.findAll().size

        // Create the Source
        val sourceDto = sourceMapper.sourceToSourceDTO(source)

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restDeviceMockMvc.perform(
            MockMvcRequestBuilders.put("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        Assertions.assertThat(sourceList).hasSize(databaseSizeBeforeUpdate + 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteSource() {
        // Initialize the database
        sourceRepository.saveAndFlush(source)
        val databaseSizeBeforeDelete = sourceRepository.findAll().size

        // Get the source
        restDeviceMockMvc.perform(
            MockMvcRequestBuilders.delete("/api/sources/{sourceName}", source.sourceName)
                .accept(TestUtil.APPLICATION_JSON_UTF8)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the database is empty
        val sourceList = sourceRepository.findAll()
        Assertions.assertThat(sourceList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun equalsVerifier() {
        org.junit.jupiter.api.Assertions.assertTrue(TestUtil.equalsVerifier(Source::class.java))
    }

    companion object {
        private val DEFAULT_SOURCE_PHYSICAL_ID = UUID.randomUUID()
        private val UPDATED_SOURCE_PHYSICAL_ID = DEFAULT_SOURCE_PHYSICAL_ID
        private const val DEFAULT_SOURCE_NAME = "CCCCCCCCCC"
        private const val DEFAULT_ASSIGNED = false
        private const val UPDATED_ASSIGNED = true

        /**
         * Create an entity for this test.
         *
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        fun createEntity(): Source {
            val s = Source()
                .sourceName(DEFAULT_SOURCE_NAME)

            s.assigned = DEFAULT_ASSIGNED
            return s
        }
    }
}
