package org.radarbase.management.web.rest

import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.SourceData
import org.radarbase.management.repository.SourceDataRepository
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.SourceDataService
import org.radarbase.management.service.mapper.SourceDataMapper
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.servlet.ServletException

/**
 * Test class for the SourceDataResource REST controller.
 *
 * @see SourceDataResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal open class SourceDataResourceIntTest(
    @Autowired private val sourceDataRepository: SourceDataRepository,
    @Autowired private val sourceDataMapper: SourceDataMapper,
    @Autowired private val sourceDataService: SourceDataService,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val exceptionTranslator: ExceptionTranslator,
    @Autowired private val em: EntityManager,
    private var restSourceDataMockMvc: MockMvc,
    private var sourceData: SourceData,
    @Autowired private val authService: AuthService
) {

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val sourceDataResource = SourceDataResource(
            sourceDataService,
            authService
        )
        ReflectionTestUtils.setField(sourceDataResource, "sourceDataService", sourceDataService)
        ReflectionTestUtils.setField(sourceDataResource, "authService", authService)
        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restSourceDataMockMvc = MockMvcBuilders.standaloneSetup(sourceDataResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter<StandaloneMockMvcBuilder>(filter)
            .defaultRequest<StandaloneMockMvcBuilder>(MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken()))
            .build()
    }

    @BeforeEach
    fun initTest() {
        sourceData = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun createSourceData() {
        val databaseSizeBeforeCreate = sourceDataRepository.findAll().size

        // Create the SourceData
        val sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(sourceData)
        restSourceDataMockMvc.perform(
            MockMvcRequestBuilders.post("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the SourceData in the database
        val sourceDataList = sourceDataRepository.findAll()
        Assertions.assertThat(sourceDataList).hasSize(databaseSizeBeforeCreate + 1)
        val testSourceData = sourceDataList[sourceDataList.size - 1]
        Assertions.assertThat(testSourceData.sourceDataType).isEqualTo(DEFAULT_SOURCE_DATA_TYPE)
        Assertions.assertThat(testSourceData.sourceDataName).isEqualTo(DEFAULT_SOURCE_DATA_NAME)
        Assertions.assertThat(testSourceData.processingState).isEqualTo(DEFAULT_PROCESSING_STATE)
        Assertions.assertThat(testSourceData.keySchema).isEqualTo(DEFAULT_KEY_SCHEMA)
        Assertions.assertThat(testSourceData.valueSchema).isEqualTo(DEFAULT_VALUE_SCHEMA)
        Assertions.assertThat(testSourceData.frequency).isEqualTo(DEFAULT_FREQUENCY)
        Assertions.assertThat(testSourceData.topic).isEqualTo(DEFAULT_TOPIC)
        Assertions.assertThat(testSourceData.unit).isEqualTo(DEFAULT_UNTI)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun createSourceDataWithExistingId() {
        val databaseSizeBeforeCreate = sourceDataRepository.findAll().size

        // Create the SourceData with an existing ID
        sourceData.id = 1L
        val sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(sourceData)

        // An entity with an existing ID cannot be created, so this API call must fail
        restSourceDataMockMvc.perform(
            MockMvcRequestBuilders.post("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())

        // Validate the Alice in the database
        val sourceDataList = sourceDataRepository.findAll()
        Assertions.assertThat(sourceDataList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun checkSourceDataTypeIsNotRequired() {
        val databaseSizeBeforeTest = sourceDataRepository.findAll().size
        // set the field null
        sourceData.sourceDataType = null

        // Create the SourceData, which fails.
        val sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(sourceData)
        restSourceDataMockMvc.perform(
            MockMvcRequestBuilders.post("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())
        val sourceDataList = sourceDataRepository.findAll()
        Assertions.assertThat(sourceDataList).hasSize(databaseSizeBeforeTest + 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun checkSourceDataTypeOrTopicIsRequired() {
        val databaseSizeBeforeTest = sourceDataRepository.findAll().size
        // set the field null
        sourceData.sourceDataType = null
        sourceData.topic = null

        // Create the SourceData, which fails.
        val sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(sourceData)
        restSourceDataMockMvc.perform(
            MockMvcRequestBuilders.post("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
        val sourceDataList = sourceDataRepository.findAll()
        Assertions.assertThat(sourceDataList).hasSize(databaseSizeBeforeTest)
    }

    @get:Throws(Exception::class)
    @get:Transactional
    @get:Test
    open val allSourceData: Unit
        get() {
            // Initialize the database
            sourceDataRepository.saveAndFlush(sourceData)

            // Get all the sourceDataList
            restSourceDataMockMvc.perform(MockMvcRequestBuilders.get("/api/source-data?sort=id,desc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].id").value<Iterable<Int?>>(
                        Matchers.hasItem(
                            sourceData.id!!.toInt()
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].sourceDataType").value<Iterable<String?>>(
                        Matchers.hasItem(DEFAULT_SOURCE_DATA_TYPE)
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].sourceDataName").value<Iterable<String?>>(
                        Matchers.hasItem(DEFAULT_SOURCE_DATA_NAME)
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].processingState").value<Iterable<String?>>(
                        Matchers.hasItem(DEFAULT_PROCESSING_STATE)
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].keySchema").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_KEY_SCHEMA
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].valueSchema").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_VALUE_SCHEMA
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].unit").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_UNTI
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].topic").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_TOPIC
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].frequency").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_FREQUENCY
                        )
                    )
                )
        }

    @get:Throws(Exception::class)
    @get:Transactional
    @get:Test
    open val allSourceDataWithPagination: Unit
        get() {
            // Initialize the database
            sourceDataRepository.saveAndFlush(sourceData)

            // Get all the sourceDataList
            restSourceDataMockMvc.perform(MockMvcRequestBuilders.get("/api/source-data?page=0&size=5&sort=id,desc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].id").value<Iterable<Int?>>(
                        Matchers.hasItem(
                            sourceData.id!!.toInt()
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].sourceDataType").value<Iterable<String?>>(
                        Matchers.hasItem(DEFAULT_SOURCE_DATA_TYPE)
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].sourceDataName").value<Iterable<String?>>(
                        Matchers.hasItem(DEFAULT_SOURCE_DATA_NAME)
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].processingState").value<Iterable<String?>>(
                        Matchers.hasItem(DEFAULT_PROCESSING_STATE)
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].keySchema").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_KEY_SCHEMA
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].valueSchema").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_VALUE_SCHEMA
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].unit").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_UNTI
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].topic").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_TOPIC
                        )
                    )
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].frequency").value<Iterable<String?>>(
                        Matchers.hasItem(
                            DEFAULT_FREQUENCY
                        )
                    )
                )
        }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun getSourceData() {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData)

        // Get the sourceData
        restSourceDataMockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/source-data/{sourceDataName}",
                sourceData.sourceDataName
            )
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(sourceData.id!!.toInt()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sourceDataType").value(DEFAULT_SOURCE_DATA_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sourceDataName").value(DEFAULT_SOURCE_DATA_NAME))
            .andExpect(MockMvcResultMatchers.jsonPath("$.processingState").value(DEFAULT_PROCESSING_STATE))
            .andExpect(MockMvcResultMatchers.jsonPath("$.keySchema").value(DEFAULT_KEY_SCHEMA))
            .andExpect(MockMvcResultMatchers.jsonPath("$.valueSchema").value(DEFAULT_VALUE_SCHEMA))
            .andExpect(MockMvcResultMatchers.jsonPath("$.unit").value(DEFAULT_UNTI))
            .andExpect(MockMvcResultMatchers.jsonPath("$.topic").value(DEFAULT_TOPIC))
            .andExpect(MockMvcResultMatchers.jsonPath("$.frequency").value(DEFAULT_FREQUENCY))
    }

    @get:Throws(Exception::class)
    @get:Transactional
    @get:Test
    open val nonExistingSourceData: Unit
        get() {
            // Get the sourceData
            restSourceDataMockMvc.perform(
                MockMvcRequestBuilders.get(
                    "/api/source-data/{sourceDataName}",
                    DEFAULT_SOURCE_DATA_NAME + DEFAULT_SOURCE_DATA_NAME
                )
            )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
        }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun updateSourceData() {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData)
        val databaseSizeBeforeUpdate = sourceDataRepository.findAll().size

        // Update the sourceData
        val updatedSourceData = sourceDataRepository.findById(sourceData.id).get()
        updatedSourceData
            .sourceDataType(UPDATED_SOURCE_DATA_TYPE)
            .sourceDataName(UPDATED_SOURCE_DATA_NAME)
            .processingState(UPDATED_PROCESSING_STATE)
            .keySchema(UPDATED_KEY_SCHEMA)
            .valueSchema(UPDATED_VALUE_SCHEMA)
            .topic(UPDATED_TOPIC)
            .unit(UPDATED_UNIT)
            .frequency(UPDATED_FREQUENCY)
        val sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(updatedSourceData)
        restSourceDataMockMvc.perform(
            MockMvcRequestBuilders.put("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the SourceData in the database
        val sourceDataList = sourceDataRepository.findAll()
        Assertions.assertThat(sourceDataList).hasSize(databaseSizeBeforeUpdate)
        val testSourceData = sourceDataList[sourceDataList.size - 1]
        Assertions.assertThat(testSourceData.sourceDataType).isEqualTo(UPDATED_SOURCE_DATA_TYPE)
        Assertions.assertThat(testSourceData.sourceDataName).isEqualTo(UPDATED_SOURCE_DATA_NAME)
        Assertions.assertThat(testSourceData.processingState).isEqualTo(UPDATED_PROCESSING_STATE)
        Assertions.assertThat(testSourceData.keySchema).isEqualTo(UPDATED_KEY_SCHEMA)
        Assertions.assertThat(testSourceData.valueSchema).isEqualTo(UPDATED_VALUE_SCHEMA)
        Assertions.assertThat(testSourceData.topic).isEqualTo(UPDATED_TOPIC)
        Assertions.assertThat(testSourceData.unit).isEqualTo(UPDATED_UNIT)
        Assertions.assertThat(testSourceData.frequency).isEqualTo(UPDATED_FREQUENCY)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun updateNonExistingSourceData() {
        val databaseSizeBeforeUpdate = sourceDataRepository.findAll().size

        // Create the SourceData
        val sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(sourceData)

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSourceDataMockMvc.perform(
            MockMvcRequestBuilders.put("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the SourceData in the database
        val sourceDataList = sourceDataRepository.findAll()
        Assertions.assertThat(sourceDataList).hasSize(databaseSizeBeforeUpdate + 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun deleteSourceData() {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData)
        val databaseSizeBeforeDelete = sourceDataRepository.findAll().size

        // Get the sourceData
        restSourceDataMockMvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/source-data/{sourceDataName}",
                sourceData.sourceDataName
            )
                .accept(TestUtil.APPLICATION_JSON_UTF8)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the database is empty
        val sourceDataList = sourceDataRepository.findAll()
        Assertions.assertThat(sourceDataList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    open fun equalsVerifier() {
        org.junit.jupiter.api.Assertions.assertTrue(TestUtil.equalsVerifier(SourceData::class.java))
    }

    companion object {
        private const val DEFAULT_SOURCE_DATA_TYPE = "AAAAAAAAAA"
        private const val UPDATED_SOURCE_DATA_TYPE = "BBBBBBBBBB"
        private const val DEFAULT_SOURCE_DATA_NAME = "AAAAAAAAAAAAA"
        private const val UPDATED_SOURCE_DATA_NAME = "BBBBBBBBBBAAA"
        private const val DEFAULT_PROCESSING_STATE = "RAW"
        private const val UPDATED_PROCESSING_STATE = "DERIVED"
        private const val DEFAULT_KEY_SCHEMA = "AAAAAAAAAAC"
        private const val UPDATED_KEY_SCHEMA = "BBBBBBBBBBC"
        private const val DEFAULT_VALUE_SCHEMA = "AAAAAAAAAAA"
        private const val UPDATED_VALUE_SCHEMA = "BBBBBBBBBBB"
        private const val DEFAULT_FREQUENCY = "AAAAAAAAAAAA"
        private const val UPDATED_FREQUENCY = "BBBBBBBBBBBB"
        private const val DEFAULT_UNTI = "AAAAAAAAAAAAAAC"
        private const val UPDATED_UNIT = "BBBBBBBBBBBBBBC"
        private const val DEFAULT_TOPIC = "AAAAAAAAAAAAAAA"
        private const val UPDATED_TOPIC = "BBBBBBBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        fun createEntity(em: EntityManager?): SourceData {
            return SourceData()
                .sourceDataType(DEFAULT_SOURCE_DATA_TYPE)
                .sourceDataName(DEFAULT_SOURCE_DATA_NAME)
                .processingState(DEFAULT_PROCESSING_STATE)
                .keySchema(DEFAULT_KEY_SCHEMA)
                .valueSchema(DEFAULT_VALUE_SCHEMA)
                .topic(DEFAULT_TOPIC)
                .unit(DEFAULT_UNTI)
                .frequency(DEFAULT_FREQUENCY)
        }
    }
}
