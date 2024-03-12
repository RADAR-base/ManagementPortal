package org.radarbase.management.web.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.radarbase.auth.authentication.OAuthHelper;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.domain.SourceData;
import org.radarbase.management.repository.SourceDataRepository;
import org.radarbase.management.security.JwtAuthenticationFilter;
import org.radarbase.management.service.SourceDataService;
import org.radarbase.management.service.dto.SourceDataDTO;
import org.radarbase.management.service.mapper.SourceDataMapper;
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
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the SourceDataResource REST controller.
 *
 * @see SourceDataResource
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
class SourceDataResourceIntTest {

    private static final String DEFAULT_SOURCE_DATA_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_SOURCE_DATA_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_SOURCE_DATA_NAME = "AAAAAAAAAAAAA";
    private static final String UPDATED_SOURCE_DATA_NAME = "BBBBBBBBBBAAA";

    private static final String DEFAULT_PROCESSING_STATE = "RAW";
    private static final String UPDATED_PROCESSING_STATE = "DERIVED";

    private static final String DEFAULT_KEY_SCHEMA = "AAAAAAAAAAC";
    private static final String UPDATED_KEY_SCHEMA = "BBBBBBBBBBC";

    private static final String DEFAULT_VALUE_SCHEMA = "AAAAAAAAAAA";
    private static final String UPDATED_VALUE_SCHEMA = "BBBBBBBBBBB";

    private static final String DEFAULT_FREQUENCY = "AAAAAAAAAAAA";
    private static final String UPDATED_FREQUENCY = "BBBBBBBBBBBB";

    private static final String DEFAULT_UNTI = "AAAAAAAAAAAAAAC";
    private static final String UPDATED_UNIT = "BBBBBBBBBBBBBBC";

    private static final String DEFAULT_TOPIC = "AAAAAAAAAAAAAAA";
    private static final String UPDATED_TOPIC = "BBBBBBBBBBBBBBB";

    @Autowired
    private SourceDataRepository sourceDataRepository;

    @Autowired
    private SourceDataMapper sourceDataMapper;

    @Autowired
    private SourceDataService sourceDataService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private RadarToken radarToken;

    private MockMvc restSourceDataMockMvc;

    private SourceData sourceData;

    @BeforeEach
    public void setUp() throws ServletException {
        MockitoAnnotations.openMocks(this);
        SourceDataResource sourceDataResource = new SourceDataResource();
        ReflectionTestUtils.setField(sourceDataResource, "sourceDataService", sourceDataService);
        ReflectionTestUtils.setField(sourceDataResource, "token", radarToken);

        JwtAuthenticationFilter filter = OAuthHelper.createAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restSourceDataMockMvc = MockMvcBuilders.standaloneSetup(sourceDataResource)
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
    public static SourceData createEntity(EntityManager em) {
        SourceData sourceData = new SourceData()
                .sourceDataType(DEFAULT_SOURCE_DATA_TYPE)
                .sourceDataName(DEFAULT_SOURCE_DATA_NAME)
                .processingState(DEFAULT_PROCESSING_STATE)
                .keySchema(DEFAULT_KEY_SCHEMA)
                .valueSchema(DEFAULT_VALUE_SCHEMA)
                .topic(DEFAULT_TOPIC)
                .unit(DEFAULT_UNTI)
                .frequency(DEFAULT_FREQUENCY);

        return sourceData;
    }

    @BeforeEach
    public void initTest() {
        sourceData = createEntity(em);
    }

    @Test
    @Transactional
    void createSourceData() throws Exception {
        int databaseSizeBeforeCreate = sourceDataRepository.findAll().size();

        // Create the SourceData
        SourceDataDTO sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);
        restSourceDataMockMvc.perform(post("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto)))
                .andExpect(status().isCreated());

        // Validate the SourceData in the database
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeCreate + 1);
        SourceData testSourceData = sourceDataList.get(sourceDataList.size() - 1);
        assertThat(testSourceData.getSourceDataType()).isEqualTo(DEFAULT_SOURCE_DATA_TYPE);
        assertThat(testSourceData.getSourceDataName()).isEqualTo(DEFAULT_SOURCE_DATA_NAME);
        assertThat(testSourceData.getProcessingState()).isEqualTo(DEFAULT_PROCESSING_STATE);
        assertThat(testSourceData.getKeySchema()).isEqualTo(DEFAULT_KEY_SCHEMA);
        assertThat(testSourceData.getValueSchema()).isEqualTo(DEFAULT_VALUE_SCHEMA);
        assertThat(testSourceData.getFrequency()).isEqualTo(DEFAULT_FREQUENCY);
        assertThat(testSourceData.getTopic()).isEqualTo(DEFAULT_TOPIC);
        assertThat(testSourceData.getUnit()).isEqualTo(DEFAULT_UNTI);
    }

    @Test
    @Transactional
    void createSourceDataWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = sourceDataRepository.findAll().size();

        // Create the SourceData with an existing ID
        sourceData.setId(1L);
        SourceDataDTO sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSourceDataMockMvc.perform(post("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto)))
                .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkSourceDataTypeIsNotRequired() throws Exception {
        final int databaseSizeBeforeTest = sourceDataRepository.findAll().size();
        // set the field null
        sourceData.setSourceDataType(null);

        // Create the SourceData, which fails.
        SourceDataDTO sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);

        restSourceDataMockMvc.perform(post("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto)))
                .andExpect(status().isCreated());

        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeTest + 1);
    }


    @Test
    @Transactional
    void checkSourceDataTypeOrTopicIsRequired() throws Exception {
        final int databaseSizeBeforeTest = sourceDataRepository.findAll().size();
        // set the field null
        sourceData.setSourceDataType(null);
        sourceData.setTopic(null);

        // Create the SourceData, which fails.
        SourceDataDTO sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);
        restSourceDataMockMvc.perform(post("/api/source-data")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(sourceDataDto)))
                .andExpect(status().isBadRequest());

        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSourceData() throws Exception {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData);

        // Get all the sourceDataList
        restSourceDataMockMvc.perform(get("/api/source-data?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(sourceData.getId().intValue())))
                .andExpect(jsonPath("$.[*].sourceDataType").value(
                        hasItem(DEFAULT_SOURCE_DATA_TYPE)))
                .andExpect(jsonPath("$.[*].sourceDataName").value(
                        hasItem(DEFAULT_SOURCE_DATA_NAME)))
                .andExpect(jsonPath("$.[*].processingState").value(
                        hasItem(DEFAULT_PROCESSING_STATE)))
                .andExpect(jsonPath("$.[*].keySchema").value(hasItem(DEFAULT_KEY_SCHEMA)))
                .andExpect(jsonPath("$.[*].valueSchema").value(hasItem(DEFAULT_VALUE_SCHEMA)))
                .andExpect(jsonPath("$.[*].unit").value(hasItem(DEFAULT_UNTI)))
                .andExpect(jsonPath("$.[*].topic").value(hasItem(DEFAULT_TOPIC)))
                .andExpect(jsonPath("$.[*].frequency").value(hasItem(DEFAULT_FREQUENCY)));
    }

    @Test
    @Transactional
    void getAllSourceDataWithPagination() throws Exception {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData);

        // Get all the sourceDataList
        restSourceDataMockMvc.perform(get("/api/source-data?page=0&size=5&sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(sourceData.getId().intValue())))
                .andExpect(jsonPath("$.[*].sourceDataType").value(
                        hasItem(DEFAULT_SOURCE_DATA_TYPE)))
                .andExpect(jsonPath("$.[*].sourceDataName").value(
                        hasItem(DEFAULT_SOURCE_DATA_NAME)))
                .andExpect(jsonPath("$.[*].processingState").value(
                        hasItem(DEFAULT_PROCESSING_STATE)))
                .andExpect(jsonPath("$.[*].keySchema").value(hasItem(DEFAULT_KEY_SCHEMA)))
                .andExpect(jsonPath("$.[*].valueSchema").value(hasItem(DEFAULT_VALUE_SCHEMA)))
                .andExpect(jsonPath("$.[*].unit").value(hasItem(DEFAULT_UNTI)))
                .andExpect(jsonPath("$.[*].topic").value(hasItem(DEFAULT_TOPIC)))
                .andExpect(jsonPath("$.[*].frequency").value(hasItem(DEFAULT_FREQUENCY)));
    }

    @Test
    @Transactional
    void getSourceData() throws Exception {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData);

        // Get the sourceData
        restSourceDataMockMvc.perform(get("/api/source-data/{sourceDataName}",
                sourceData.getSourceDataName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(sourceData.getId().intValue()))
                .andExpect(jsonPath("$.sourceDataType").value(DEFAULT_SOURCE_DATA_TYPE))
                .andExpect(jsonPath("$.sourceDataName").value(DEFAULT_SOURCE_DATA_NAME))
                .andExpect(jsonPath("$.processingState").value(DEFAULT_PROCESSING_STATE))
                .andExpect(jsonPath("$.keySchema").value(DEFAULT_KEY_SCHEMA))
                .andExpect(jsonPath("$.valueSchema").value(DEFAULT_VALUE_SCHEMA))
                .andExpect(jsonPath("$.unit").value(DEFAULT_UNTI))
                .andExpect(jsonPath("$.topic").value(DEFAULT_TOPIC))
                .andExpect(jsonPath("$.frequency").value(DEFAULT_FREQUENCY));
    }

    @Test
    @Transactional
    void getNonExistingSourceData() throws Exception {
        // Get the sourceData
        restSourceDataMockMvc.perform(get("/api/source-data/{sourceDataName}",
                DEFAULT_SOURCE_DATA_NAME + DEFAULT_SOURCE_DATA_NAME))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void updateSourceData() throws Exception {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData);
        int databaseSizeBeforeUpdate = sourceDataRepository.findAll().size();

        // Update the sourceData
        SourceData updatedSourceData = sourceDataRepository.findById(sourceData.getId()).get();
        updatedSourceData
                .sourceDataType(UPDATED_SOURCE_DATA_TYPE)
                .sourceDataName(UPDATED_SOURCE_DATA_NAME)
                .processingState(UPDATED_PROCESSING_STATE)
                .keySchema(UPDATED_KEY_SCHEMA)
                .valueSchema(UPDATED_VALUE_SCHEMA)
                .topic(UPDATED_TOPIC)
                .unit(UPDATED_UNIT)
                .frequency(UPDATED_FREQUENCY);
        SourceDataDTO sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(updatedSourceData);

        restSourceDataMockMvc.perform(put("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto)))
                .andExpect(status().isOk());

        // Validate the SourceData in the database
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeUpdate);
        SourceData testSourceData = sourceDataList.get(sourceDataList.size() - 1);
        assertThat(testSourceData.getSourceDataType()).isEqualTo(UPDATED_SOURCE_DATA_TYPE);
        assertThat(testSourceData.getSourceDataName()).isEqualTo(UPDATED_SOURCE_DATA_NAME);
        assertThat(testSourceData.getProcessingState()).isEqualTo(UPDATED_PROCESSING_STATE);
        assertThat(testSourceData.getKeySchema()).isEqualTo(UPDATED_KEY_SCHEMA);
        assertThat(testSourceData.getValueSchema()).isEqualTo(UPDATED_VALUE_SCHEMA);
        assertThat(testSourceData.getTopic()).isEqualTo(UPDATED_TOPIC);
        assertThat(testSourceData.getUnit()).isEqualTo(UPDATED_UNIT);
        assertThat(testSourceData.getFrequency()).isEqualTo(UPDATED_FREQUENCY);
    }

    @Test
    @Transactional
    void updateNonExistingSourceData() throws Exception {
        int databaseSizeBeforeUpdate = sourceDataRepository.findAll().size();

        // Create the SourceData
        SourceDataDTO sourceDataDto = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSourceDataMockMvc.perform(put("/api/source-data")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDataDto)))
                .andExpect(status().isCreated());

        // Validate the SourceData in the database
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    void deleteSourceData() throws Exception {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData);
        int databaseSizeBeforeDelete = sourceDataRepository.findAll().size();

        // Get the sourceData
        restSourceDataMockMvc.perform(delete("/api/source-data/{sourceDataName}",
                sourceData.getSourceDataName())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void equalsVerifier() throws Exception {
        Assertions.assertTrue(TestUtil.equalsVerifier(SourceData.class));
    }
}
