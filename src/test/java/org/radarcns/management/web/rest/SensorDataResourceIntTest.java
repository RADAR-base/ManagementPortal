package org.radarcns.management.web.rest;

import org.radarcns.management.ManagementPortalApp;

import org.radarcns.management.domain.SensorData;
import org.radarcns.management.repository.SensorDataRepository;
import org.radarcns.management.service.SensorDataService;
import org.radarcns.management.service.dto.SensorDataDTO;
import org.radarcns.management.service.mapper.SensorDataMapper;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.radarcns.management.domain.enumeration.DataType;
/**
 * Test class for the SensorDataResource REST controller.
 *
 * @see SensorDataResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalApp.class)
public class SensorDataResourceIntTest {

    private static final String DEFAULT_SENSOR_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_SENSOR_TYPE = "BBBBBBBBBB";

    private static final DataType DEFAULT_DATA_TYPE = DataType.RAW;
    private static final DataType UPDATED_DATA_TYPE = DataType.DERIVED;

    private static final String DEFAULT_DATA_FORMAT = "AAAAAAAAAA";
    private static final String UPDATED_DATA_FORMAT = "BBBBBBBBBB";

    private static final String DEFAULT_FREQUENCY = "AAAAAAAAAA";
    private static final String UPDATED_FREQUENCY = "BBBBBBBBBB";

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private SensorDataMapper sensorDataMapper;

    @Autowired
    private SensorDataService sensorDataService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restSensorDataMockMvc;

    private SensorData sensorData;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        SensorDataResource sensorDataResource = new SensorDataResource(sensorDataService);
        this.restSensorDataMockMvc = MockMvcBuilders.standaloneSetup(sensorDataResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SensorData createEntity(EntityManager em) {
        SensorData sensorData = new SensorData()
            .sensorType(DEFAULT_SENSOR_TYPE)
            .dataType(DEFAULT_DATA_TYPE)
            .dataFormat(DEFAULT_DATA_FORMAT)
            .frequency(DEFAULT_FREQUENCY);
        return sensorData;
    }

    @Before
    public void initTest() {
        sensorData = createEntity(em);
    }

    @Test
    @Transactional
    public void createSensorData() throws Exception {
        int databaseSizeBeforeCreate = sensorDataRepository.findAll().size();

        // Create the SensorData
        SensorDataDTO sensorDataDTO = sensorDataMapper.sensorDataToSensorDataDTO(sensorData);
        restSensorDataMockMvc.perform(post("/api/sensor-data")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sensorDataDTO)))
            .andExpect(status().isCreated());

        // Validate the SensorData in the database
        List<SensorData> sensorDataList = sensorDataRepository.findAll();
        assertThat(sensorDataList).hasSize(databaseSizeBeforeCreate + 1);
        SensorData testSensorData = sensorDataList.get(sensorDataList.size() - 1);
        assertThat(testSensorData.getSensorName()).isEqualTo(DEFAULT_SENSOR_TYPE);
        assertThat(testSensorData.getDataType()).isEqualTo(DEFAULT_DATA_TYPE);
        assertThat(testSensorData.getKeySchema()).isEqualTo(DEFAULT_DATA_FORMAT);
        assertThat(testSensorData.getFrequency()).isEqualTo(DEFAULT_FREQUENCY);
    }

    @Test
    @Transactional
    public void createSensorDataWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = sensorDataRepository.findAll().size();

        // Create the SensorData with an existing ID
        sensorData.setId(1L);
        SensorDataDTO sensorDataDTO = sensorDataMapper.sensorDataToSensorDataDTO(sensorData);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSensorDataMockMvc.perform(post("/api/sensor-data")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sensorDataDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<SensorData> sensorDataList = sensorDataRepository.findAll();
        assertThat(sensorDataList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkSensorTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = sensorDataRepository.findAll().size();
        // set the field null
        sensorData.setSensorName(null);

        // Create the SensorData, which fails.
        SensorDataDTO sensorDataDTO = sensorDataMapper.sensorDataToSensorDataDTO(sensorData);

        restSensorDataMockMvc.perform(post("/api/sensor-data")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sensorDataDTO)))
            .andExpect(status().isBadRequest());

        List<SensorData> sensorDataList = sensorDataRepository.findAll();
        assertThat(sensorDataList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSensorData() throws Exception {
        // Initialize the database
        sensorDataRepository.saveAndFlush(sensorData);

        // Get all the sensorDataList
        restSensorDataMockMvc.perform(get("/api/sensor-data?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sensorData.getId().intValue())))
            .andExpect(jsonPath("$.[*].sensorType").value(hasItem(DEFAULT_SENSOR_TYPE.toString())))
            .andExpect(jsonPath("$.[*].dataType").value(hasItem(DEFAULT_DATA_TYPE.toString())))
            .andExpect(jsonPath("$.[*].dataFormat").value(hasItem(DEFAULT_DATA_FORMAT.toString())))
            .andExpect(jsonPath("$.[*].frequency").value(hasItem(DEFAULT_FREQUENCY.toString())));
    }

    @Test
    @Transactional
    public void getSensorData() throws Exception {
        // Initialize the database
        sensorDataRepository.saveAndFlush(sensorData);

        // Get the sensorData
        restSensorDataMockMvc.perform(get("/api/sensor-data/{id}", sensorData.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(sensorData.getId().intValue()))
            .andExpect(jsonPath("$.sensorType").value(DEFAULT_SENSOR_TYPE.toString()))
            .andExpect(jsonPath("$.dataType").value(DEFAULT_DATA_TYPE.toString()))
            .andExpect(jsonPath("$.dataFormat").value(DEFAULT_DATA_FORMAT.toString()))
            .andExpect(jsonPath("$.frequency").value(DEFAULT_FREQUENCY.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSensorData() throws Exception {
        // Get the sensorData
        restSensorDataMockMvc.perform(get("/api/sensor-data/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSensorData() throws Exception {
        // Initialize the database
        sensorDataRepository.saveAndFlush(sensorData);
        int databaseSizeBeforeUpdate = sensorDataRepository.findAll().size();

        // Update the sensorData
        SensorData updatedSensorData = sensorDataRepository.findOne(sensorData.getId());
        updatedSensorData
            .sensorType(UPDATED_SENSOR_TYPE)
            .dataType(UPDATED_DATA_TYPE)
            .dataFormat(UPDATED_DATA_FORMAT)
            .frequency(UPDATED_FREQUENCY);
        SensorDataDTO sensorDataDTO = sensorDataMapper.sensorDataToSensorDataDTO(updatedSensorData);

        restSensorDataMockMvc.perform(put("/api/sensor-data")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sensorDataDTO)))
            .andExpect(status().isOk());

        // Validate the SensorData in the database
        List<SensorData> sensorDataList = sensorDataRepository.findAll();
        assertThat(sensorDataList).hasSize(databaseSizeBeforeUpdate);
        SensorData testSensorData = sensorDataList.get(sensorDataList.size() - 1);
        assertThat(testSensorData.getSensorName()).isEqualTo(UPDATED_SENSOR_TYPE);
        assertThat(testSensorData.getDataType()).isEqualTo(UPDATED_DATA_TYPE);
        assertThat(testSensorData.getKeySchema()).isEqualTo(UPDATED_DATA_FORMAT);
        assertThat(testSensorData.getFrequency()).isEqualTo(UPDATED_FREQUENCY);
    }

    @Test
    @Transactional
    public void updateNonExistingSensorData() throws Exception {
        int databaseSizeBeforeUpdate = sensorDataRepository.findAll().size();

        // Create the SensorData
        SensorDataDTO sensorDataDTO = sensorDataMapper.sensorDataToSensorDataDTO(sensorData);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSensorDataMockMvc.perform(put("/api/sensor-data")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sensorDataDTO)))
            .andExpect(status().isCreated());

        // Validate the SensorData in the database
        List<SensorData> sensorDataList = sensorDataRepository.findAll();
        assertThat(sensorDataList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteSensorData() throws Exception {
        // Initialize the database
        sensorDataRepository.saveAndFlush(sensorData);
        int databaseSizeBeforeDelete = sensorDataRepository.findAll().size();

        // Get the sensorData
        restSensorDataMockMvc.perform(delete("/api/sensor-data/{id}", sensorData.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<SensorData> sensorDataList = sensorDataRepository.findAll();
        assertThat(sensorDataList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SensorData.class);
    }
}
