package org.radarcns.management.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.radarcns.management.ManagementPortalApp;
import org.radarcns.management.domain.DeviceType;
import org.radarcns.management.domain.SensorData;
import org.radarcns.management.domain.enumeration.SourceType;
import org.radarcns.management.repository.DeviceTypeRepository;
import org.radarcns.management.repository.SensorDataRepository;
import org.radarcns.management.security.JwtAuthenticationFilter;
import org.radarcns.management.service.DeviceTypeService;
import org.radarcns.management.service.dto.DeviceTypeDTO;
import org.radarcns.management.service.mapper.DeviceTypeMapper;
import org.radarcns.management.web.rest.errors.ExceptionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
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
import java.util.Collections;
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
 * Test class for the DeviceTypeResource REST controller.
 *
 * @see DeviceTypeResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalApp.class)
public class DeviceTypeResourceIntTest {

    private static final String DEFAULT_DEVICE_PRODUCER = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_PRODUCER = "BBBBBBBBBB";

    private static final String DEFAULT_DEVICE_MODEL = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_MODEL = "BBBBBBBBBB";

    private static final String DEFAULT_DEVICE_VERSION = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_VERSION = "AAAAAAAAAA";

    private static final SourceType DEFAULT_SOURCE_TYPE = SourceType.ACTIVE;
    private static final SourceType UPDATED_SOURCE_TYPE = SourceType.PASSIVE;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private DeviceTypeMapper deviceTypeMapper;

    @Autowired
    private DeviceTypeService deviceTypeService;

    @Autowired
    private SensorDataRepository sensorDataRepository;

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

    private MockMvc restDeviceTypeMockMvc;

    private DeviceType deviceType;

    @Before
    public void setup() throws ServletException {
        MockitoAnnotations.initMocks(this);
        DeviceTypeResource deviceTypeResource = new DeviceTypeResource();
        ReflectionTestUtils.setField(deviceTypeResource, "deviceTypeService" , deviceTypeService);
        ReflectionTestUtils.setField(deviceTypeResource, "deviceTypeRepository" , deviceTypeRepository);
        ReflectionTestUtils.setField(deviceTypeResource, "servletRequest", servletRequest);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restDeviceTypeMockMvc = MockMvcBuilders.standaloneSetup(deviceTypeResource)
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
    public static DeviceType createEntity(EntityManager em) {
        DeviceType deviceType = new DeviceType()
            .deviceProducer(DEFAULT_DEVICE_PRODUCER)
            .deviceModel(DEFAULT_DEVICE_MODEL)
            .deviceVersion(DEFAULT_DEVICE_VERSION)
            .sourceType(DEFAULT_SOURCE_TYPE);
        return deviceType;
    }

    @Before
    public void initTest() {
        deviceType = createEntity(em);
    }

    @Test
    @Transactional
    public void createDeviceType() throws Exception {
        int databaseSizeBeforeCreate = deviceTypeRepository.findAll().size();

        // Create the DeviceType
        DeviceTypeDTO deviceTypeDTO = deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);
        restDeviceTypeMockMvc.perform(post("/api/device-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceTypeDTO)))
            .andExpect(status().isCreated());

        // Validate the DeviceType in the database
        List<DeviceType> deviceTypeList = deviceTypeRepository.findAll();
        assertThat(deviceTypeList).hasSize(databaseSizeBeforeCreate + 1);
        DeviceType testDeviceType = deviceTypeList.get(deviceTypeList.size() - 1);
        assertThat(testDeviceType.getDeviceProducer()).isEqualTo(DEFAULT_DEVICE_PRODUCER);
        assertThat(testDeviceType.getDeviceModel()).isEqualTo(DEFAULT_DEVICE_MODEL);
        assertThat(testDeviceType.getSourceType()).isEqualTo(DEFAULT_SOURCE_TYPE);
        assertThat(testDeviceType.getCatalogVersion()).isEqualTo(DEFAULT_DEVICE_VERSION);
    }

    @Test
    @Transactional
    public void createDeviceTypeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = deviceTypeRepository.findAll().size();

        // Create the DeviceType with an existing ID
        deviceType.setId(1L);
        DeviceTypeDTO deviceTypeDTO = deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDeviceTypeMockMvc.perform(post("/api/device-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceTypeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<DeviceType> deviceTypeList = deviceTypeRepository.findAll();
        assertThat(deviceTypeList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDeviceModelIsRequired() throws Exception {
        int databaseSizeBeforeTest = deviceTypeRepository.findAll().size();
        // set the field null
        deviceType.setDeviceModel(null);

        // Create the DeviceType, which fails.
        DeviceTypeDTO deviceTypeDTO = deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);

        restDeviceTypeMockMvc.perform(post("/api/device-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceTypeDTO)))
            .andExpect(status().isBadRequest());

        List<DeviceType> deviceTypeList = deviceTypeRepository.findAll();
        assertThat(deviceTypeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSourceTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = deviceTypeRepository.findAll().size();
        // set the field null
        deviceType.setSourceType(null);

        // Create the DeviceType, which fails.
        DeviceTypeDTO deviceTypeDTO = deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);

        restDeviceTypeMockMvc.perform(post("/api/device-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceTypeDTO)))
            .andExpect(status().isBadRequest());

        List<DeviceType> deviceTypeList = deviceTypeRepository.findAll();
        assertThat(deviceTypeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = deviceTypeRepository.findAll().size();
        // set the field null
        deviceType.deviceVersion(null);

        // Create the DeviceType, which fails.
        DeviceTypeDTO deviceTypeDTO = deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);

        restDeviceTypeMockMvc.perform(post("/api/device-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceTypeDTO)))
            .andExpect(status().isBadRequest());

        List<DeviceType> deviceTypeList = deviceTypeRepository.findAll();
        assertThat(deviceTypeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllDeviceTypes() throws Exception {
        // Initialize the database
        deviceTypeRepository.saveAndFlush(deviceType);

        // Get all the deviceTypeList
        restDeviceTypeMockMvc.perform(get("/api/device-types?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(deviceType.getId().intValue())))
            .andExpect(jsonPath("$.[*].deviceProducer").value(hasItem(DEFAULT_DEVICE_PRODUCER.toString())))
            .andExpect(jsonPath("$.[*].deviceModel").value(hasItem(DEFAULT_DEVICE_MODEL.toString())))
            .andExpect(jsonPath("$.[*].sourceType").value(hasItem(DEFAULT_SOURCE_TYPE.toString())));
    }

    @Test
    @Transactional
    public void getDeviceType() throws Exception {
        // Initialize the database
        deviceTypeRepository.saveAndFlush(deviceType);

        // Get the deviceType
        restDeviceTypeMockMvc.perform(get("/api/device-types/{id}", deviceType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(deviceType.getId().intValue()))
            .andExpect(jsonPath("$.deviceProducer").value(DEFAULT_DEVICE_PRODUCER.toString()))
            .andExpect(jsonPath("$.deviceModel").value(DEFAULT_DEVICE_MODEL.toString()))
            .andExpect(jsonPath("$.sourceType").value(DEFAULT_SOURCE_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingDeviceType() throws Exception {
        // Get the deviceType
        restDeviceTypeMockMvc.perform(get("/api/device-types/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDeviceType() throws Exception {
        // Initialize the database
        deviceTypeRepository.saveAndFlush(deviceType);
        int databaseSizeBeforeUpdate = deviceTypeRepository.findAll().size();

        // Update the deviceType
        DeviceType updatedDeviceType = deviceTypeRepository.findOne(deviceType.getId());
        updatedDeviceType
            .deviceProducer(UPDATED_DEVICE_PRODUCER)
            .deviceModel(UPDATED_DEVICE_MODEL)
            .sourceType(UPDATED_SOURCE_TYPE);
        DeviceTypeDTO deviceTypeDTO = deviceTypeMapper.deviceTypeToDeviceTypeDTO(updatedDeviceType);

        restDeviceTypeMockMvc.perform(put("/api/device-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceTypeDTO)))
            .andExpect(status().isOk());

        // Validate the DeviceType in the database
        List<DeviceType> deviceTypeList = deviceTypeRepository.findAll();
        assertThat(deviceTypeList).hasSize(databaseSizeBeforeUpdate);
        DeviceType testDeviceType = deviceTypeList.get(deviceTypeList.size() - 1);
        assertThat(testDeviceType.getDeviceProducer()).isEqualTo(UPDATED_DEVICE_PRODUCER);
        assertThat(testDeviceType.getDeviceModel()).isEqualTo(UPDATED_DEVICE_MODEL);
        assertThat(testDeviceType.getSourceType()).isEqualTo(UPDATED_SOURCE_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingDeviceType() throws Exception {
        int databaseSizeBeforeUpdate = deviceTypeRepository.findAll().size();

        // Create the DeviceType
        DeviceTypeDTO deviceTypeDTO = deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restDeviceTypeMockMvc.perform(put("/api/device-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceTypeDTO)))
            .andExpect(status().isCreated());

        // Validate the DeviceType in the database
        List<DeviceType> deviceTypeList = deviceTypeRepository.findAll();
        assertThat(deviceTypeList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteDeviceType() throws Exception {
        // Initialize the database
        deviceTypeRepository.saveAndFlush(deviceType);
        int databaseSizeBeforeDelete = deviceTypeRepository.findAll().size();

        // Get the deviceType
        restDeviceTypeMockMvc.perform(delete("/api/device-types/{id}", deviceType.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<DeviceType> deviceTypeList = deviceTypeRepository.findAll();
        assertThat(deviceTypeList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DeviceType.class);
    }

    @Test
    @Transactional
    public void idempotentPutWithoutId() throws Exception {
        int databaseSizeBeforeUpdate = deviceTypeRepository.findAll().size();
        int sensorsSizeBeforeUpdate = sensorDataRepository.findAll().size();

        deviceType.setSensorData(Collections.singleton(SensorDataResourceIntTest.createEntity(em)));
        // Create the DeviceType
        DeviceTypeDTO deviceTypeDTO = deviceTypeMapper.deviceTypeToDeviceTypeDTO(deviceType);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restDeviceTypeMockMvc.perform(put("/api/device-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceTypeDTO)))
            .andExpect(status().isCreated());

        // Validate the DeviceType in the database
        List<DeviceType> deviceTypeList = deviceTypeRepository.findAll();
        assertThat(deviceTypeList).hasSize(databaseSizeBeforeUpdate + 1);

        // Validate the SensorData in the database
        List<SensorData> sensorDataList = sensorDataRepository.findAll();
        assertThat(sensorDataList).hasSize(sensorsSizeBeforeUpdate + 1);

        // Test doing a put with only producer and model, no id, does not create a new device-type
        // assert that the id is still unset
        assertThat(deviceTypeDTO.getId()).isNull();
        restDeviceTypeMockMvc.perform(put("/api/device-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceTypeDTO)))
            .andExpect(status().is(HttpStatus.CONFLICT.value()));

        // Validate no change in database size
        deviceTypeList = deviceTypeRepository.findAll();
        assertThat(deviceTypeList).hasSize(databaseSizeBeforeUpdate + 1);

//        // Validate no change in sensordata database size
//        sensorDataList = sensorDataRepository.findAll();
//        assertThat(sensorDataList).hasSize(sensorsSizeBeforeUpdate + 1);
    }
}
