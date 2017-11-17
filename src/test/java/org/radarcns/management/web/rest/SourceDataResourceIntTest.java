package org.radarcns.management.web.rest;

import org.radarcns.management.ManagementPortalApp;

import org.radarcns.management.domain.SourceData;
import org.radarcns.management.repository.SourceDataRepository;
import org.radarcns.management.security.JwtAuthenticationFilter;
import org.radarcns.management.service.SourceDataService;
import org.radarcns.management.service.dto.SourceDataDTO;
import org.radarcns.management.service.mapper.SourceDataMapper;
import org.radarcns.management.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.radarcns.management.domain.enumeration.DataType;
/**
 * Test class for the SourceDataResource REST controller.
 *
 * @see SourceDataResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalApp.class)
public class SourceDataResourceIntTest {

    private static final String DEFAULT_SENSOR_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SENSOR_NAME = "BBBBBBBBBB";

    private static final DataType DEFAULT_DATA_TYPE = DataType.RAW;
    private static final DataType UPDATED_DATA_TYPE = DataType.DERIVED;

    private static final String DEFAULT_KEY_SCHEMA = "AAAAAAAAAA";
    private static final String UPDATED_KEY_SCHEMA = "BBBBBBBBBB";

    private static final String DEFAULT_FREQUENCY = "AAAAAAAAAA";
    private static final String UPDATED_FREQUENCY = "BBBBBBBBBB";

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
    private HttpServletRequest servletRequest;

    private MockMvc restSourceDataMockMvc;

    private SourceData sourceData;

    @Before
    public void setup() throws ServletException {
        MockitoAnnotations.initMocks(this);
        SourceDataResource sourceDataResource = new SourceDataResource();
        ReflectionTestUtils.setField(sourceDataResource, "sourceDataService", sourceDataService);
        ReflectionTestUtils.setField(sourceDataResource, "servletRequest", servletRequest);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
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
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SourceData createEntity(EntityManager em) {
        SourceData sourceData = new SourceData()
            .sensorName(DEFAULT_SENSOR_NAME)
            .dataType(DEFAULT_DATA_TYPE)
            .keySchema(DEFAULT_KEY_SCHEMA)
            .frequency(DEFAULT_FREQUENCY);
        return sourceData;
    }

    @Before
    public void initTest() {
        sourceData = createEntity(em);
    }

    @Test
    @Transactional
    public void createSourceData() throws Exception {
        int databaseSizeBeforeCreate = sourceDataRepository.findAll().size();

        // Create the SourceData
        SourceDataDTO sourceDataDTO = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);
        restSourceDataMockMvc.perform(post("/api/source-data")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceDataDTO)))
            .andExpect(status().isCreated());

        // Validate the SourceData in the database
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeCreate + 1);
        SourceData testSourceData = sourceDataList.get(sourceDataList.size() - 1);
        assertThat(testSourceData.getSensorName()).isEqualTo(DEFAULT_SENSOR_NAME);
        assertThat(testSourceData.getDataType()).isEqualTo(DEFAULT_DATA_TYPE);
        assertThat(testSourceData.getKeySchema()).isEqualTo(DEFAULT_KEY_SCHEMA);
        assertThat(testSourceData.getFrequency()).isEqualTo(DEFAULT_FREQUENCY);
    }

    @Test
    @Transactional
    public void createSourceDataWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = sourceDataRepository.findAll().size();

        // Create the SourceData with an existing ID
        sourceData.setId(1L);
        SourceDataDTO sourceDataDTO = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSourceDataMockMvc.perform(post("/api/source-data")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceDataDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkSensorTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = sourceDataRepository.findAll().size();
        // set the field null
        sourceData.setSensorName(null);

        // Create the SourceData, which fails.
        SourceDataDTO sourceDataDTO = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);

        restSourceDataMockMvc.perform(post("/api/source-data")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceDataDTO)))
            .andExpect(status().isBadRequest());

        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSourceData() throws Exception {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData);

        // Get all the sourceDataList
        restSourceDataMockMvc.perform(get("/api/source-data?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sourceData.getId().intValue())))
            .andExpect(jsonPath("$.[*].sensorName").value(hasItem(DEFAULT_SENSOR_NAME.toString())))
            .andExpect(jsonPath("$.[*].dataType").value(hasItem(DEFAULT_DATA_TYPE.toString())))
            .andExpect(jsonPath("$.[*].keySchema").value(hasItem(DEFAULT_KEY_SCHEMA.toString())))
            .andExpect(jsonPath("$.[*].frequency").value(hasItem(DEFAULT_FREQUENCY.toString())));
    }

    @Test
    @Transactional
    public void getSourceData() throws Exception {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData);

        // Get the sourceData
        restSourceDataMockMvc.perform(get("/api/source-data/{sensorName}", sourceData.getSensorName()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(sourceData.getId().intValue()))
            .andExpect(jsonPath("$.sensorName").value(DEFAULT_SENSOR_NAME.toString()))
            .andExpect(jsonPath("$.dataType").value(DEFAULT_DATA_TYPE.toString()))
            .andExpect(jsonPath("$.keySchema").value(DEFAULT_KEY_SCHEMA.toString()))
            .andExpect(jsonPath("$.frequency").value(DEFAULT_FREQUENCY.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSourceData() throws Exception {
        // Get the sourceData
        restSourceDataMockMvc.perform(get("/api/source-data/{sensorName}", DEFAULT_SENSOR_NAME +
            DEFAULT_SENSOR_NAME))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSourceData() throws Exception {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData);
        int databaseSizeBeforeUpdate = sourceDataRepository.findAll().size();

        // Update the sourceData
        SourceData updatedSourceData = sourceDataRepository.findOne(sourceData.getId());
        updatedSourceData
            .sensorName(UPDATED_SENSOR_NAME)
            .dataType(UPDATED_DATA_TYPE)
            .keySchema(UPDATED_KEY_SCHEMA)
            .frequency(UPDATED_FREQUENCY);
        SourceDataDTO sourceDataDTO = sourceDataMapper.sourceDataToSourceDataDTO(updatedSourceData);

        restSourceDataMockMvc.perform(put("/api/source-data")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceDataDTO)))
            .andExpect(status().isOk());

        // Validate the SourceData in the database
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeUpdate);
        SourceData testSourceData = sourceDataList.get(sourceDataList.size() - 1);
        assertThat(testSourceData.getSensorName()).isEqualTo(UPDATED_SENSOR_NAME);
        assertThat(testSourceData.getDataType()).isEqualTo(UPDATED_DATA_TYPE);
        assertThat(testSourceData.getKeySchema()).isEqualTo(UPDATED_KEY_SCHEMA);
        assertThat(testSourceData.getFrequency()).isEqualTo(UPDATED_FREQUENCY);
    }

    @Test
    @Transactional
    public void updateNonExistingSourceData() throws Exception {
        int databaseSizeBeforeUpdate = sourceDataRepository.findAll().size();

        // Create the SourceData
        SourceDataDTO sourceDataDTO = sourceDataMapper.sourceDataToSourceDataDTO(sourceData);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSourceDataMockMvc.perform(put("/api/source-data")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceDataDTO)))
            .andExpect(status().isCreated());

        // Validate the SourceData in the database
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteSourceData() throws Exception {
        // Initialize the database
        sourceDataRepository.saveAndFlush(sourceData);
        int databaseSizeBeforeDelete = sourceDataRepository.findAll().size();

        // Get the sourceData
        restSourceDataMockMvc.perform(delete("/api/source-data/{sensorName}", sourceData.getSensorName())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SourceData.class);
    }
}
