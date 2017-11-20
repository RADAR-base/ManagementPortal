package org.radarcns.management.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.radarcns.management.ManagementPortalApp;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.domain.SourceData;
import org.radarcns.management.domain.enumeration.SourceTypeScope;
import org.radarcns.management.repository.SourceTypeRepository;
import org.radarcns.management.repository.SourceDataRepository;
import org.radarcns.management.security.JwtAuthenticationFilter;
import org.radarcns.management.service.SourceTypeService;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.service.mapper.SourceTypeMapper;
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
 * Test class for the SourceTypeResource REST controller.
 *
 * @see SourceTypeResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalApp.class)
public class SourceTypeResourceIntTest {

    private static final String DEFAULT_DEVICE_PRODUCER = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_PRODUCER = "BBBBBBBBBB";

    private static final String DEFAULT_DEVICE_MODEL = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_MODEL = "BBBBBBBBBB";

    private static final String DEFAULT_DEVICE_VERSION = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_VERSION = "AAAAAAAAAA";

    private static final SourceTypeScope DEFAULT_SOURCE_TYPE_SCOPE = SourceTypeScope.ACTIVE;
    private static final SourceTypeScope UPDATED_SOURCE_TYPE_SCOPE = SourceTypeScope.PASSIVE;

    @Autowired
    private SourceTypeRepository sourceTypeRepository;

    @Autowired
    private SourceTypeMapper sourceTypeMapper;

    @Autowired
    private SourceTypeService sourceTypeService;

    @Autowired
    private SourceDataRepository sourceDataRepository;

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

    private MockMvc restSourceTypeMockMvc;

    private SourceType sourceType;

    @Before
    public void setup() throws ServletException {
        MockitoAnnotations.initMocks(this);
        SourceTypeResource sourceTypeResource = new SourceTypeResource();
        ReflectionTestUtils.setField(sourceTypeResource, "sourceTypeService" , sourceTypeService);
        ReflectionTestUtils.setField(sourceTypeResource, "sourceTypeRepository" , sourceTypeRepository);
        ReflectionTestUtils.setField(sourceTypeResource, "servletRequest", servletRequest);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restSourceTypeMockMvc = MockMvcBuilders.standaloneSetup(sourceTypeResource)
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
    public static SourceType createEntity(EntityManager em) {
        SourceType sourceType = new SourceType()
            .deviceProducer(DEFAULT_DEVICE_PRODUCER)
            .deviceModel(DEFAULT_DEVICE_MODEL)
            .deviceVersion(DEFAULT_DEVICE_VERSION)
            .sourceTypeScope(DEFAULT_SOURCE_TYPE_SCOPE);
        return sourceType;
    }

    @Before
    public void initTest() {
        sourceType = createEntity(em);
    }

    @Test
    @Transactional
    public void createSourceType() throws Exception {
        int databaseSizeBeforeCreate = sourceTypeRepository.findAll().size();

        // Create the SourceType
        SourceTypeDTO sourceTypeDTO = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType);
        restSourceTypeMockMvc.perform(post("/api/source-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceTypeDTO)))
            .andExpect(status().isCreated());

        // Validate the SourceType in the database
        List<SourceType> sourceTypeList = sourceTypeRepository.findAll();
        assertThat(sourceTypeList).hasSize(databaseSizeBeforeCreate + 1);
        SourceType testSourceType = sourceTypeList.get(sourceTypeList.size() - 1);
        assertThat(testSourceType.getDeviceProducer()).isEqualTo(DEFAULT_DEVICE_PRODUCER);
        assertThat(testSourceType.getDeviceModel()).isEqualTo(DEFAULT_DEVICE_MODEL);
        assertThat(testSourceType.getSourceTypeScope()).isEqualTo(DEFAULT_SOURCE_TYPE_SCOPE);
        assertThat(testSourceType.getCatalogVersion()).isEqualTo(DEFAULT_DEVICE_VERSION);
    }

    @Test
    @Transactional
    public void createSourceTypeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = sourceTypeRepository.findAll().size();

        // Create the SourceType with an existing ID
        sourceType.setId(1L);
        SourceTypeDTO sourceTypeDTO = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSourceTypeMockMvc.perform(post("/api/source-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceTypeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<SourceType> sourceTypeList = sourceTypeRepository.findAll();
        assertThat(sourceTypeList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkDeviceModelIsRequired() throws Exception {
        int databaseSizeBeforeTest = sourceTypeRepository.findAll().size();
        // set the field null
        sourceType.setDeviceModel(null);

        // Create the SourceType, which fails.
        SourceTypeDTO sourceTypeDTO = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType);

        restSourceTypeMockMvc.perform(post("/api/source-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceTypeDTO)))
            .andExpect(status().isBadRequest());

        List<SourceType> sourceTypeList = sourceTypeRepository.findAll();
        assertThat(sourceTypeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSourceTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = sourceTypeRepository.findAll().size();
        // set the field null
        sourceType.setSourceTypeScope(null);

        // Create the SourceType, which fails.
        SourceTypeDTO sourceTypeDTO = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType);

        restSourceTypeMockMvc.perform(post("/api/source-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceTypeDTO)))
            .andExpect(status().isBadRequest());

        List<SourceType> sourceTypeList = sourceTypeRepository.findAll();
        assertThat(sourceTypeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = sourceTypeRepository.findAll().size();
        // set the field null
        sourceType.deviceVersion(null);

        // Create the SourceType, which fails.
        SourceTypeDTO sourceTypeDTO = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType);

        restSourceTypeMockMvc.perform(post("/api/source-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceTypeDTO)))
            .andExpect(status().isBadRequest());

        List<SourceType> sourceTypeList = sourceTypeRepository.findAll();
        assertThat(sourceTypeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSourceTypes() throws Exception {
        // Initialize the database
        sourceTypeRepository.saveAndFlush(sourceType);

        // Get all the sourceTypeList
        restSourceTypeMockMvc.perform(get("/api/source-types?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sourceType.getId().intValue())))
            .andExpect(jsonPath("$.[*].deviceProducer").value(hasItem(DEFAULT_DEVICE_PRODUCER.toString())))
            .andExpect(jsonPath("$.[*].deviceModel").value(hasItem(DEFAULT_DEVICE_MODEL.toString())))
            .andExpect(jsonPath("$.[*].sourceTypeScope").value(hasItem(DEFAULT_SOURCE_TYPE_SCOPE.toString())));
    }

    @Test
    @Transactional
    public void getSourceType() throws Exception {
        // Initialize the database
        sourceTypeRepository.saveAndFlush(sourceType);

        // Get the sourceType
        restSourceTypeMockMvc.perform(get("/api/source-types/{prodcuer}/{model}/{version}",
            sourceType.getDeviceProducer(), sourceType.getDeviceModel(), sourceType.getCatalogVersion()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(sourceType.getId().intValue()))
            .andExpect(jsonPath("$.deviceProducer").value(DEFAULT_DEVICE_PRODUCER.toString()))
            .andExpect(jsonPath("$.deviceModel").value(DEFAULT_DEVICE_MODEL.toString()))
            .andExpect(jsonPath("$.sourceTypeScope").value(DEFAULT_SOURCE_TYPE_SCOPE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSourceType() throws Exception {
        // Get the sourceType
        restSourceTypeMockMvc.perform(get("/api/source-types/{prodcuer}/{model}/{version}",
            "does", "not", "exist"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSourceType() throws Exception {
        // Initialize the database
        sourceTypeRepository.saveAndFlush(sourceType);
        int databaseSizeBeforeUpdate = sourceTypeRepository.findAll().size();

        // Update the sourceType
        SourceType updatedSourceType = sourceTypeRepository.findOne(sourceType.getId());
        updatedSourceType
            .deviceProducer(UPDATED_DEVICE_PRODUCER)
            .deviceModel(UPDATED_DEVICE_MODEL)
            .sourceTypeScope(UPDATED_SOURCE_TYPE_SCOPE);
        SourceTypeDTO sourceTypeDTO = sourceTypeMapper.sourceTypeToSourceTypeDTO(updatedSourceType);

        restSourceTypeMockMvc.perform(put("/api/source-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceTypeDTO)))
            .andExpect(status().isOk());

        // Validate the SourceType in the database
        List<SourceType> sourceTypeList = sourceTypeRepository.findAll();
        assertThat(sourceTypeList).hasSize(databaseSizeBeforeUpdate);
        SourceType testSourceType = sourceTypeList.get(sourceTypeList.size() - 1);
        assertThat(testSourceType.getDeviceProducer()).isEqualTo(UPDATED_DEVICE_PRODUCER);
        assertThat(testSourceType.getDeviceModel()).isEqualTo(UPDATED_DEVICE_MODEL);
        assertThat(testSourceType.getSourceTypeScope()).isEqualTo(UPDATED_SOURCE_TYPE_SCOPE);
    }

    @Test
    @Transactional
    public void updateNonExistingSourceType() throws Exception {
        int databaseSizeBeforeUpdate = sourceTypeRepository.findAll().size();

        // Create the SourceType
        SourceTypeDTO sourceTypeDTO = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSourceTypeMockMvc.perform(put("/api/source-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceTypeDTO)))
            .andExpect(status().isCreated());

        // Validate the SourceType in the database
        List<SourceType> sourceTypeList = sourceTypeRepository.findAll();
        assertThat(sourceTypeList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteSourceType() throws Exception {
        // Initialize the database
        sourceTypeRepository.saveAndFlush(sourceType);
        int databaseSizeBeforeDelete = sourceTypeRepository.findAll().size();

        // Get the sourceType
        restSourceTypeMockMvc.perform(delete("/api/source-types/{prodcuer}/{model}/{version}",
            sourceType.getDeviceProducer(), sourceType.getDeviceModel(), sourceType.getCatalogVersion())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<SourceType> sourceTypeList = sourceTypeRepository.findAll();
        assertThat(sourceTypeList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SourceType.class);
    }

    @Test
    @Transactional
    public void idempotentPutWithoutId() throws Exception {
        int databaseSizeBeforeUpdate = sourceTypeRepository.findAll().size();
        int sensorsSizeBeforeUpdate = sourceDataRepository.findAll().size();

        sourceType.setSourceData(Collections.singleton(SourceDataResourceIntTest.createEntity(em)));
        // Create the SourceType
        SourceTypeDTO sourceTypeDTO = sourceTypeMapper.sourceTypeToSourceTypeDTO(sourceType);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSourceTypeMockMvc.perform(put("/api/source-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceTypeDTO)))
            .andExpect(status().isCreated());

        // Validate the SourceType in the database
        List<SourceType> sourceTypeList = sourceTypeRepository.findAll();
        assertThat(sourceTypeList).hasSize(databaseSizeBeforeUpdate + 1);

        // Validate the SourceData in the database
        List<SourceData> sourceDataList = sourceDataRepository.findAll();
        assertThat(sourceDataList).hasSize(sensorsSizeBeforeUpdate + 1);

        // Test doing a put with only producer and model, no id, does not create a new source-type
        // assert that the id is still unset
        assertThat(sourceTypeDTO.getId()).isNull();
        restSourceTypeMockMvc.perform(put("/api/source-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(sourceTypeDTO)))
            .andExpect(status().is(HttpStatus.CONFLICT.value()));

        // Validate no change in database size
        sourceTypeList = sourceTypeRepository.findAll();
        assertThat(sourceTypeList).hasSize(databaseSizeBeforeUpdate + 1);

//        // Validate no change in sourceData database size
//        sourceDataList = sourceDataRepository.findAll();
//        assertThat(sourceDataList).hasSize(sensorsSizeBeforeUpdate + 1);
    }
}
