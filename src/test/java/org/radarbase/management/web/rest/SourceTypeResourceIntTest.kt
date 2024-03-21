package org.radarbase.management.web.rest

import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.SourceType
import org.radarbase.management.repository.SourceDataRepository
import org.radarbase.management.repository.SourceTypeRepository
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.SourceTypeService
import org.radarbase.management.service.mapper.SourceDataMapper
import org.radarbase.management.service.mapper.SourceTypeMapper
import org.radarbase.management.web.rest.errors.ExceptionTranslator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.HttpStatus
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
import jakarta.servlet.ServletException

/**
 * Test class for the SourceTypeResource REST controller.
 *
 * @see SourceTypeResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal class SourceTypeResourceIntTest(
    @Autowired private val sourceTypeRepository: SourceTypeRepository,
    @Autowired private val sourceTypeMapper: SourceTypeMapper,
    @Autowired private val sourceTypeService: SourceTypeService,
    @Autowired private val sourceDataMapper: SourceDataMapper,
    @Autowired private val sourceDataRepository: SourceDataRepository,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val exceptionTranslator: ExceptionTranslator,
    @Autowired private val authService: AuthService
) {
    private lateinit var restSourceTypeMockMvc: MockMvc
    private lateinit var sourceType: SourceType

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val sourceTypeResource = SourceTypeResource(
            sourceTypeService,
            sourceTypeRepository,
            authService
        )

        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restSourceTypeMockMvc = MockMvcBuilders.standaloneSetup(sourceTypeResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter<StandaloneMockMvcBuilder>(filter)
            .defaultRequest<StandaloneMockMvcBuilder>(MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken()))
            .build()
    }

    @BeforeEach
    fun initTest() {
        sourceType = createEntity()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createSourceType() {
        val databaseSizeBeforeCreate = sourceTypeRepository.findAll().size

        // Create the SourceType
        val sourceTypeDto = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType)
        val sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(
            SourceDataResourceIntTest.Companion.createEntity()
        )
        val sourceData = sourceTypeDto.sourceData
        sourceData.add(sourceDataDto!!)
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.post("/api/source-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceTypeDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the SourceType in the database
        val sourceTypeList = sourceTypeRepository.findAll()
        Assertions.assertThat(sourceTypeList).hasSize(databaseSizeBeforeCreate + 1)
        val testSourceType = sourceTypeList[sourceTypeList.size - 1]
        Assertions.assertThat(testSourceType.producer).isEqualTo(DEFAULT_PRODUCER)
        Assertions.assertThat(testSourceType.model).isEqualTo(DEFAULT_MODEL)
        Assertions.assertThat(testSourceType.sourceTypeScope).isEqualTo(DEFAULT_SOURCE_TYPE_SCOPE)
        Assertions.assertThat(testSourceType.catalogVersion).isEqualTo(DEFAULT_DEVICE_VERSION)
        Assertions.assertThat(testSourceType.sourceData).hasSize(1)
        val testSourceData = testSourceType.sourceData.iterator().next()
        Assertions.assertThat(testSourceData.sourceDataType).isEqualTo(sourceDataDto.sourceDataType)
        Assertions.assertThat(testSourceData.sourceDataName).isEqualTo(sourceDataDto.sourceDataName)
        Assertions.assertThat(testSourceData.processingState).isEqualTo(
            sourceDataDto.processingState
        )
        Assertions.assertThat(testSourceData.keySchema).isEqualTo(sourceDataDto.keySchema)
        Assertions.assertThat(testSourceData.frequency).isEqualTo(sourceDataDto.frequency)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createSourceTypeWithExistingId() {
        val databaseSizeBeforeCreate = sourceTypeRepository.findAll().size

        // Create the SourceType with an existing ID
        sourceType.id = 1L
        val sourceTypeDto = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType)

        // An entity with an existing ID cannot be created, so this API call must fail
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.post("/api/source-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceTypeDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())

        // Validate the Alice in the database
        val sourceTypeList = sourceTypeRepository.findAll()
        Assertions.assertThat(sourceTypeList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkModelIsRequired() {
        val databaseSizeBeforeTest = sourceTypeRepository.findAll().size
        // set the field null
        sourceType.model = null

        // Create the SourceType, which fails.
        val sourceTypeDto = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType)
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.post("/api/source-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceTypeDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
        val sourceTypeList = sourceTypeRepository.findAll()
        Assertions.assertThat(sourceTypeList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkSourceTypeIsRequired() {
        val databaseSizeBeforeTest = sourceTypeRepository.findAll().size
        // set the field null
        sourceType.sourceTypeScope = null

        // Create the SourceType, which fails.
        val sourceTypeDto = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType)
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.post("/api/source-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceTypeDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
        val sourceTypeList = sourceTypeRepository.findAll()
        Assertions.assertThat(sourceTypeList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkVersionIsRequired() {
        val databaseSizeBeforeTest = sourceTypeRepository.findAll().size
        // set the field null
        sourceType.catalogVersion(null)

        // Create the SourceType, which fails.
        val sourceTypeDto = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType)
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.post("/api/source-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceTypeDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
        val sourceTypeList = sourceTypeRepository.findAll()
        Assertions.assertThat(sourceTypeList).hasSize(databaseSizeBeforeTest)
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun allSourceTypes() {
            // Initialize the database
            sourceTypeRepository.saveAndFlush(sourceType)

            // Get all the sourceTypeList
            restSourceTypeMockMvc.perform(MockMvcRequestBuilders.get("/api/source-types"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].id").value<Iterable<Int?>>(
                        Matchers.hasItem(
                            sourceType.id!!.toInt()
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].producer").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_PRODUCER
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].model").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_MODEL
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].catalogVersion").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_DEVICE_VERSION
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].sourceTypeScope").value<Iterable<String?>>(
                        Matchers.hasItem(DEFAULT_SOURCE_TYPE_SCOPE)
                    )
                )
        }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun allSourceTypesWithPagination() {
            // Initialize the database
            sourceTypeRepository.saveAndFlush(sourceType)

            // Get all the sourceTypeList
            restSourceTypeMockMvc.perform(MockMvcRequestBuilders.get("/api/source-types?page=0&size=5&sort=id,desc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].id").value<Iterable<Int?>>(
                        Matchers.hasItem(
                            sourceType.id!!.toInt()
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].producer").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_PRODUCER
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].model").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_MODEL
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].catalogVersion").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_DEVICE_VERSION
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].sourceTypeScope").value<Iterable<String?>>(
                        Matchers.hasItem(DEFAULT_SOURCE_TYPE_SCOPE)
                    )
                )
        }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getSourceType() {
        // Initialize the database
        sourceTypeRepository.saveAndFlush(sourceType)

        // Get the sourceType
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/source-types/{prodcuer}/{model}/{version}",
                sourceType.producer, sourceType.model, sourceType.catalogVersion
            )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(sourceType.id!!.toInt()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.producer").value(DEFAULT_PRODUCER))
            .andExpect(MockMvcResultMatchers.jsonPath("$.model").value(DEFAULT_MODEL))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.sourceTypeScope").value(
                    DEFAULT_SOURCE_TYPE_SCOPE
                )
            )
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun nonExistingSourceType() {
            // Get the sourceType
            restSourceTypeMockMvc.perform(
                MockMvcRequestBuilders.get(
                    "/api/source-types/{prodcuer}/{model}/{version}",
                    "does", "not", "exist"
                )
            )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
        }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateSourceType() {
        // Initialize the database
        sourceTypeRepository.saveAndFlush(sourceType)
        val databaseSizeBeforeUpdate = sourceTypeRepository.findAll().size

        // Update the sourceType
        val updatedSourceType = sourceTypeRepository.findById(sourceType.id!!).get()
        updatedSourceType
            .producer(UPDATED_PRODUCER)
            .model(UPDATED_MODEL)
            .catalogVersion(UPDATED_DEVICE_VERSION)
            .sourceTypeScope(UPDATED_SOURCE_TYPE_SCOPE)
        val sourceTypeDto = sourceTypeMapper.sourceTypeToSourceTypeDTO(updatedSourceType)
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.put("/api/source-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceTypeDto))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the SourceType in the database
        val sourceTypeList = sourceTypeRepository.findAll()
        Assertions.assertThat(sourceTypeList).hasSize(databaseSizeBeforeUpdate)
        val testSourceType = sourceTypeList[sourceTypeList.size - 1]
        Assertions.assertThat(testSourceType.producer).isEqualTo(UPDATED_PRODUCER)
        Assertions.assertThat(testSourceType.model).isEqualTo(UPDATED_MODEL)
        Assertions.assertThat(testSourceType.catalogVersion).isEqualTo(UPDATED_DEVICE_VERSION)
        Assertions.assertThat(testSourceType.sourceTypeScope).isEqualTo(UPDATED_SOURCE_TYPE_SCOPE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateNonExistingSourceType() {
        val databaseSizeBeforeUpdate = sourceTypeRepository.findAll().size

        // Create the SourceType
        val sourceTypeDto = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType)

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.put("/api/source-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceTypeDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the SourceType in the database
        val sourceTypeList = sourceTypeRepository.findAll()
        Assertions.assertThat(sourceTypeList).hasSize(databaseSizeBeforeUpdate + 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteSourceType() {
        // Initialize the database
        sourceTypeRepository.saveAndFlush(sourceType)
        val databaseSizeBeforeDelete = sourceTypeRepository.findAll().size

        // Get the sourceType
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/source-types/{prodcuer}/{model}/{version}",
                sourceType.producer, sourceType.model, sourceType.catalogVersion
            )
                .accept(TestUtil.APPLICATION_JSON_UTF8)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the database is empty
        val sourceTypeList = sourceTypeRepository.findAll()
        Assertions.assertThat(sourceTypeList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun equalsVerifier() {
        org.junit.jupiter.api.Assertions.assertTrue(TestUtil.equalsVerifier(SourceType::class.java))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun idempotentPutWithoutId() {
        val databaseSizeBeforeUpdate = sourceTypeRepository.findAll().size
        val sensorsSizeBeforeUpdate = sourceDataRepository.findAll().size
        sourceType.sourceData = setOf(SourceDataResourceIntTest.Companion.createEntity())
        // Create the SourceType
        val sourceTypeDto = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType)

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.put("/api/source-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceTypeDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the SourceType in the database
        var sourceTypeList = sourceTypeRepository.findAll()
        Assertions.assertThat(sourceTypeList).hasSize(databaseSizeBeforeUpdate + 1)

        // Validate the SourceData in the database
        var sourceDataList = sourceDataRepository.findAll()
        Assertions.assertThat(sourceDataList).hasSize(sensorsSizeBeforeUpdate + 1)

        // Test doing a put with only producer and model, no id, does not create a new source-type
        // assert that the id is still unset
        Assertions.assertThat(sourceTypeDto.id).isNull()
        restSourceTypeMockMvc.perform(
            MockMvcRequestBuilders.put("/api/source-types")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceTypeDto))
        )
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.CONFLICT.value()))

        // Validate no change in database size
        sourceTypeList = sourceTypeRepository.findAll()
        Assertions.assertThat(sourceTypeList).hasSize(databaseSizeBeforeUpdate + 1)

        // Validate no change in sourceData database size
        sourceDataList = sourceDataRepository.findAll()
        Assertions.assertThat(sourceDataList).hasSize(sensorsSizeBeforeUpdate + 1)
    }

    companion object {
        private const val DEFAULT_PRODUCER = "AAAAA AAAAA"
        private const val UPDATED_PRODUCER = "BBBBBBBBBB"
        private const val DEFAULT_MODEL = "AAAAA AAAAA"
        private const val UPDATED_MODEL = "BBBBBBBBBB"
        private const val DEFAULT_DEVICE_VERSION = "AAAAAAAAAA"
        private const val UPDATED_DEVICE_VERSION = "AAAAAAAAAA"
        private const val DEFAULT_SOURCE_TYPE_SCOPE = "ACTIVE"
        private const val UPDATED_SOURCE_TYPE_SCOPE = "PASSIVE"

        /**
         * Create an entity for this test.
         *
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        fun createEntity(): SourceType {
            return SourceType()
                .producer(DEFAULT_PRODUCER)
                .model(DEFAULT_MODEL)
                .catalogVersion(DEFAULT_DEVICE_VERSION)
                .sourceTypeScope(DEFAULT_SOURCE_TYPE_SCOPE)
        }
    }
}
