package org.radarbase.management.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockitoAnnotations;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.domain.Project;
import org.radarbase.management.domain.Source;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.repository.SourceRepository;
import org.radarbase.management.security.JwtAuthenticationFilter;
import org.radarbase.management.service.SourceService;
import org.radarbase.management.service.SourceTypeService;
import org.radarbase.management.service.dto.SourceDTO;
import org.radarbase.management.service.dto.SourceTypeDTO;
import org.radarbase.management.service.mapper.SourceMapper;
import org.radarbase.management.service.mapper.SourceTypeMapper;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test class for the DeviceResource REST controller.
 *
 * @see SourceResource
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
class SourceResourceIntTest {

    private static final UUID DEFAULT_SOURCE_PHYSICAL_ID = UUID.randomUUID();
    private static final UUID UPDATED_SOURCE_PHYSICAL_ID = DEFAULT_SOURCE_PHYSICAL_ID;

    private static final String DEFAULT_SOURCE_NAME = "CCCCCCCCCC";

    private static final Boolean DEFAULT_ASSIGNED = false;
    private static final Boolean UPDATED_ASSIGNED = true;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SourceMapper sourceMapper;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceTypeService sourceTypeService;

    @Autowired
    private SourceTypeMapper sourceTypeMapper;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private HttpServletRequest servletRequest;

    @Autowired
    private ProjectRepository projectRepository;

    private MockMvc restDeviceMockMvc;

    private Source source;

    private Project project;

    @BeforeEach
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
        SourceResource sourceResource = new SourceResource();
        ReflectionTestUtils.setField(sourceResource, "servletRequest", servletRequest);
        ReflectionTestUtils.setField(sourceResource, "sourceService", sourceService);
        ReflectionTestUtils.setField(sourceResource, "sourceRepository", sourceRepository);

        JwtAuthenticationFilter filter = OAuthHelper.createAuthenticationFilter();
        filter.init(new MockFilterConfig());
        this.restDeviceMockMvc = MockMvcBuilders.standaloneSetup(sourceResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter)
                .addFilter(filter)
                .defaultRequest(get("/").with(OAuthHelper.bearerToken()))
                .alwaysDo(MockMvcResultHandlers.print()).build();
    }

    /**
     * Create an entity for this test.
     *
     * <p>This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.</p>
     */
    public static Source createEntity() {
        Source source = new Source()
                .assigned(DEFAULT_ASSIGNED)
                .sourceName(DEFAULT_SOURCE_NAME);
        return source;
    }

    @BeforeEach
    public void initTest() {
        source = createEntity();
        List<SourceTypeDTO> sourceTypeDtos = sourceTypeService.findAll();
        assertThat(sourceTypeDtos.size()).isPositive();
        source.setSourceType(sourceTypeMapper.sourceTypeDTOToSourceType(sourceTypeDtos.get(0)));
        project = projectRepository.findById(1L).get();
        source.project(project);
    }

    @Test
    @Transactional
    void createSource() throws Exception {
        int databaseSizeBeforeCreate = sourceRepository.findAll().size();

        // Create the Source
        SourceDTO sourceDto = sourceMapper.sourceToSourceDTO(source);
        restDeviceMockMvc.perform(post("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto)))
                .andExpect(status().isCreated());

        // Validate the Source in the database
        List<Source> sourceList = sourceRepository.findAll();
        assertThat(sourceList).hasSize(databaseSizeBeforeCreate + 1);
        Source testSource = sourceList.get(sourceList.size() - 1);
        assertThat(testSource.isAssigned()).isEqualTo(DEFAULT_ASSIGNED);
        assertThat(testSource.getSourceName()).isEqualTo(DEFAULT_SOURCE_NAME);
        assertThat(testSource.getProject().getProjectName()).isEqualTo(project.getProjectName());
    }

    @Test
    @Transactional
    void createSourceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = sourceRepository.findAll().size();

        // Create the Source with an existing ID
        source.setId(1L);
        SourceDTO sourceDto = sourceMapper.sourceToSourceDTO(source);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDeviceMockMvc.perform(post("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto)))
                .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Source> sourceList = sourceRepository.findAll();
        assertThat(sourceList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkSourcePhysicalIdIsGenerated() throws Exception {
        int databaseSizeBeforeTest = sourceRepository.findAll().size();
        // set the field null
        source.setSourceId(null);

        // Create the Source
        SourceDTO sourceDto = sourceMapper.sourceToSourceDTO(source);

        restDeviceMockMvc.perform(post("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto)))
                .andExpect(status().isCreated());

        List<Source> sourceList = sourceRepository.findAll();
        assertThat(sourceList).hasSize(databaseSizeBeforeTest + 1);

        // find our created source
        Source createdSource = sourceList.stream()
                .filter(s -> s.getSourceName().equals(DEFAULT_SOURCE_NAME))
                .findFirst()
                .orElse(null);
        assertThat(createdSource).isNotNull();

        // check source id
        assertThat(createdSource.getSourceId()).isNotNull();
    }

    @Test
    @Transactional
    void checkAssignedIsRequired() throws Exception {
        int databaseSizeBeforeTest = sourceRepository.findAll().size();
        // set the field null
        source.setAssigned(null);

        // Create the Source, which fails.
        SourceDTO sourceDto = sourceMapper.sourceToSourceDTO(source);

        restDeviceMockMvc.perform(post("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto)))
                .andExpect(status().isBadRequest());

        List<Source> sourceList = sourceRepository.findAll();
        assertThat(sourceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSources() throws Exception {
        // Initialize the database
        sourceRepository.saveAndFlush(source);

        // Get all the deviceList
        restDeviceMockMvc.perform(get("/api/sources?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(source.getId().intValue())))
                .andExpect(jsonPath("$.[*].sourceId").value(everyItem(notNullValue())))
                .andExpect(jsonPath("$.[*].assigned").value(
                        hasItem(DEFAULT_ASSIGNED.booleanValue())));
    }

    @Test
    @Transactional
    void getSource() throws Exception {
        // Initialize the database
        sourceRepository.saveAndFlush(source);

        // Get the source
        restDeviceMockMvc.perform(get("/api/sources/{sourceName}", source.getSourceName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(source.getId().intValue()))
                .andExpect(jsonPath("$.sourceId").value(notNullValue()))
                .andExpect(jsonPath("$.assigned").value(DEFAULT_ASSIGNED.booleanValue()));
    }

    @Test
    @Transactional
    void getNonExistingSource() throws Exception {
        // Get the source
        restDeviceMockMvc.perform(get("/api/sources/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void updateSource() throws Exception {
        // Initialize the database
        sourceRepository.saveAndFlush(source);
        int databaseSizeBeforeUpdate = sourceRepository.findAll().size();

        // Update the source
        Source updatedSource = sourceRepository.findById(source.getId()).get();
        updatedSource
                .sourceId(UPDATED_SOURCE_PHYSICAL_ID)
                .assigned(UPDATED_ASSIGNED);
        SourceDTO sourceDto = sourceMapper.sourceToSourceDTO(updatedSource);

        restDeviceMockMvc.perform(put("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto)))
                .andExpect(status().isOk());

        // Validate the Source in the database
        List<Source> sourceList = sourceRepository.findAll();
        assertThat(sourceList).hasSize(databaseSizeBeforeUpdate);
        Source testSource = sourceList.get(sourceList.size() - 1);
        assertThat(testSource.getSourceId()).isEqualTo(UPDATED_SOURCE_PHYSICAL_ID);
        assertThat(testSource.isAssigned()).isEqualTo(UPDATED_ASSIGNED);
    }

    @Test
    @Transactional
    void updateNonExistingSource() throws Exception {
        int databaseSizeBeforeUpdate = sourceRepository.findAll().size();

        // Create the Source
        SourceDTO sourceDto = sourceMapper.sourceToSourceDTO(source);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restDeviceMockMvc.perform(put("/api/sources")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceDto)))
                .andExpect(status().isCreated());

        // Validate the Source in the database
        List<Source> sourceList = sourceRepository.findAll();
        assertThat(sourceList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    void deleteSource() throws Exception {
        // Initialize the database
        sourceRepository.saveAndFlush(source);
        int databaseSizeBeforeDelete = sourceRepository.findAll().size();

        // Get the source
        restDeviceMockMvc.perform(delete("/api/sources/{sourceName}", source.getSourceName())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Source> sourceList = sourceRepository.findAll();
        assertThat(sourceList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void equalsVerifier() throws Exception {
        Assertions.assertTrue(TestUtil.equalsVerifier(Source.class));
    }
}
