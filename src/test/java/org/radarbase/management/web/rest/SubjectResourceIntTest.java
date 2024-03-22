package org.radarbase.management.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.radarbase.management.service.SubjectServiceTest.DEFAULT_ENTERNAL_ID;
import static org.radarbase.management.service.SubjectServiceTest.DEFAULT_EXTERNAL_LINK;
import static org.radarbase.management.service.SubjectServiceTest.DEFAULT_REMOVED;
import static org.radarbase.management.service.SubjectServiceTest.DEFAULT_STATUS;
import static org.radarbase.management.service.SubjectServiceTest.MODEL;
import static org.radarbase.management.service.SubjectServiceTest.PRODUCER;
import static org.radarbase.management.service.SubjectServiceTest.UPDATED_ENTERNAL_ID;
import static org.radarbase.management.service.SubjectServiceTest.UPDATED_EXTERNAL_LINK;
import static org.radarbase.management.service.SubjectServiceTest.UPDATED_REMOVED;
import static org.radarbase.management.service.SubjectServiceTest.createEntityDTO;
import static org.radarbase.management.web.rest.TestUtil.commitTransactionAndStartNew;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.domain.Subject;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.security.JwtAuthenticationFilter;
import org.radarbase.management.service.SourceService;
import org.radarbase.management.service.SourceTypeService;
import org.radarbase.management.service.SubjectService;
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO;
import org.radarbase.management.service.dto.ProjectDTO;
import org.radarbase.management.service.dto.SourceDTO;
import org.radarbase.management.service.dto.SourceTypeDTO;
import org.radarbase.management.service.dto.SubjectDTO;
import org.radarbase.management.service.mapper.SubjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test class for the SubjectResource REST controller.
 *
 * @see SubjectResource
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
class SubjectResourceIntTest {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceTypeService sourceTypeService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RadarToken radarToken;

    private MockMvc restSubjectMockMvc;

    @BeforeEach
    public void setUp() throws ServletException {
        MockitoAnnotations.openMocks(this);
        SubjectResource subjectResource = new SubjectResource();
        ReflectionTestUtils.setField(subjectResource, "subjectService", subjectService);
        ReflectionTestUtils.setField(subjectResource, "subjectRepository", subjectRepository);
        ReflectionTestUtils.setField(subjectResource, "subjectMapper", subjectMapper);
        ReflectionTestUtils.setField(subjectResource, "projectRepository", projectRepository);
        ReflectionTestUtils.setField(subjectResource, "sourceTypeService", sourceTypeService);
        ReflectionTestUtils.setField(subjectResource, "token", radarToken);
        ReflectionTestUtils.setField(subjectResource, "sourceService", sourceService);

        JwtAuthenticationFilter filter = OAuthHelper.createAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restSubjectMockMvc = MockMvcBuilders.standaloneSetup(subjectResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter)
                .addFilter(filter)
                // add the oauth token by default to all requests for this mockMvc
                .defaultRequest(get("/").with(OAuthHelper.bearerToken())).build();
    }


    @Test
    @Transactional
    void createSubject() throws Exception {
        final int databaseSizeBeforeCreate = subjectRepository.findAll().size();

        // Create the Subject
        SubjectDTO subjectDto = createEntityDTO();
        restSubjectMockMvc.perform(post("/api/subjects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto)))
                .andExpect(status().isCreated());

        // Validate the Subject in the database
        List<Subject> subjectList = subjectRepository.findAll();
        assertThat(subjectList).hasSize(databaseSizeBeforeCreate + 1);
        Subject testSubject = subjectList.get(subjectList.size() - 1);
        assertThat(testSubject.getExternalLink()).isEqualTo(DEFAULT_EXTERNAL_LINK);
        assertThat(testSubject.getExternalId()).isEqualTo(DEFAULT_ENTERNAL_ID);
        assertThat(testSubject.isRemoved()).isEqualTo(DEFAULT_REMOVED);
        assertEquals(1, testSubject.getUser().getRoles().size());
    }

    @Test
    @Transactional
    void createSubjectWithExistingId() throws Exception {
        // Create a Subject
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO());
        final int databaseSizeBeforeCreate = subjectRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSubjectMockMvc.perform(post("/api/subjects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto)))
                .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Subject> subjectList = subjectRepository.findAll();
        assertThat(subjectList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllSubjects() throws Exception {
        // Initialize the database
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO());

        // Get all the subjectList
        restSubjectMockMvc.perform(get("/api/subjects?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(subjectDto.getId().intValue())))
                .andExpect(jsonPath("$.[*].externalLink").value(hasItem(DEFAULT_EXTERNAL_LINK)))
                .andExpect(jsonPath("$.[*].externalId").value(hasItem(DEFAULT_ENTERNAL_ID)))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    @Transactional
    void getSubject() throws Exception {
        // Initialize the database
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO());

        // Get the subject
        restSubjectMockMvc.perform(get("/api/subjects/{login}", subjectDto.getLogin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(subjectDto.getId().intValue()))
                .andExpect(jsonPath("$.externalLink").value(DEFAULT_EXTERNAL_LINK))
                .andExpect(jsonPath("$.externalId").value(DEFAULT_ENTERNAL_ID))
                .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingSubject() throws Exception {
        // Get the subject
        restSubjectMockMvc.perform(get("/api/subjects/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void updateSubject() throws Exception {
        // Initialize the database
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO());
        final int databaseSizeBeforeUpdate = subjectRepository.findAll().size();

        // Update the subject
        Subject updatedSubject = subjectRepository.findById(subjectDto.getId()).get();
        updatedSubject
                .externalLink(UPDATED_EXTERNAL_LINK)
                .externalId(UPDATED_ENTERNAL_ID)
                .removed(UPDATED_REMOVED);
        subjectDto = subjectMapper.subjectToSubjectDTO(updatedSubject);

        restSubjectMockMvc.perform(put("/api/subjects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto)))
                .andExpect(status().isOk());

        // Validate the Subject in the database
        List<Subject> subjectList = subjectRepository.findAll();
        assertThat(subjectList).hasSize(databaseSizeBeforeUpdate);
        Subject testSubject = subjectList.get(subjectList.size() - 1);
        assertThat(testSubject.getExternalLink()).isEqualTo(UPDATED_EXTERNAL_LINK);
        assertThat(testSubject.getExternalId()).isEqualTo(UPDATED_ENTERNAL_ID);
        assertThat(testSubject.isRemoved()).isEqualTo(UPDATED_REMOVED);
    }

    @Test
    @Transactional
    void updateSubjectWithNewProject() throws Exception {
        // Initialize the database
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO());
        final int databaseSizeBeforeUpdate = subjectRepository.findAll().size();

        // Update the subject
        Subject updatedSubject = subjectRepository.findById(subjectDto.getId()).get();


        updatedSubject
                .externalLink(UPDATED_EXTERNAL_LINK)
                .externalId(UPDATED_ENTERNAL_ID)
                .removed(UPDATED_REMOVED);

        subjectDto = subjectMapper.subjectToSubjectDTO(updatedSubject);
        ProjectDTO newProject = new ProjectDTO();
        newProject.setId(2L);
        newProject.setProjectName("RadarNew");
        newProject.setLocation("new location");
        subjectDto.setProject(newProject);

        restSubjectMockMvc.perform(put("/api/subjects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto)))
                .andExpect(status().isOk());

        // Validate the Subject in the database
        List<Subject> subjectList = subjectRepository.findAll();
        assertThat(subjectList).hasSize(databaseSizeBeforeUpdate);
        Subject testSubject = subjectList.get(subjectList.size() - 1);
        assertThat(testSubject.getExternalLink()).isEqualTo(UPDATED_EXTERNAL_LINK);
        assertThat(testSubject.getExternalId()).isEqualTo(UPDATED_ENTERNAL_ID);
        assertThat(testSubject.isRemoved()).isEqualTo(UPDATED_REMOVED);
        assertThat(testSubject.getUser().getRoles().size()).isEqualTo(2);
    }

    @Test
    @Transactional
    void updateNonExistingSubject() throws Exception {
        final int databaseSizeBeforeUpdate = subjectRepository.findAll().size();

        // Create the Subject
        SubjectDTO subjectDto = createEntityDTO();

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSubjectMockMvc.perform(put("/api/subjects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto)))
                .andExpect(status().isCreated());

        // Validate the Subject in the database
        List<Subject> subjectList = subjectRepository.findAll();
        assertThat(subjectList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    void deleteSubject() throws Exception {
        // Initialize the database
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO());
        final int databaseSizeBeforeDelete = subjectRepository.findAll().size();

        // Get the subject
        restSubjectMockMvc.perform(delete("/api/subjects/{login}", subjectDto.getLogin())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Subject> subjectList = subjectRepository.findAll();
        assertThat(subjectList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void equalsVerifier() throws Exception {
        assertTrue(TestUtil.equalsVerifier(Subject.class));
    }


    @Test
    @Transactional
    void dynamicSourceRegistrationWithId() throws Exception {
        final int databaseSizeBeforeCreate = subjectRepository.findAll().size();

        // Create the Subject
        SubjectDTO subjectDto = createEntityDTO();
        restSubjectMockMvc.perform(post("/api/subjects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto)))
                .andExpect(status().isCreated());

        // Validate the Subject in the database
        List<Subject> subjectList = subjectRepository.findAll();
        assertThat(subjectList).hasSize(databaseSizeBeforeCreate + 1);
        Subject testSubject = subjectList.get(subjectList.size() - 1);

        String subjectLogin = testSubject.getUser().getLogin();
        assertNotNull(subjectLogin);

        // Create a source description
        MinimalSourceDetailsDTO sourceRegistrationDto = createSourceWithSourceTypeId();

        restSubjectMockMvc.perform(post("/api/subjects/{login}/sources", subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceId").isNotEmpty());

        // A source can not be assigned twice to a subject, so this call must fail
        assertThat(sourceRegistrationDto.getSourceId()).isNull();
        restSubjectMockMvc.perform(post("/api/subjects/{login}/sources", subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    void dynamicSourceRegistrationWithoutId() throws Exception {
        final int databaseSizeBeforeCreate = subjectRepository.findAll().size();

        // Create the Subject
        SubjectDTO subjectDto = createEntityDTO();
        restSubjectMockMvc.perform(post("/api/subjects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto)))
                .andExpect(status().isCreated());

        // Validate the Subject in the database
        List<Subject> subjectList = subjectRepository.findAll();
        assertThat(subjectList).hasSize(databaseSizeBeforeCreate + 1);
        Subject testSubject = subjectList.get(subjectList.size() - 1);

        String subjectLogin = testSubject.getUser().getLogin();
        assertNotNull(subjectLogin);

        // Create a source description
        MinimalSourceDetailsDTO sourceRegistrationDto = createSourceWithoutSourceTypeId();

        restSubjectMockMvc.perform(post("/api/subjects/{login}/sources", subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceId").isNotEmpty());

        // A source can not be assigned twice to a subject, so this call must fail
        assertThat(sourceRegistrationDto.getSourceId()).isNull();
        restSubjectMockMvc.perform(post("/api/subjects/{login}/sources", subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto)))
                .andExpect(status().is4xxClientError());

        // Get all the subjectList
        restSubjectMockMvc
                .perform(get("/api/subjects/{login}/sources?sort=id,desc", subjectDto.getLogin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").isNotEmpty());

    }

    @Test
    @Transactional
    void dynamicSourceRegistrationWithoutDynamicRegistrationFlag() throws Exception {
        final int databaseSizeBeforeCreate = subjectRepository.findAll().size();

        // Create the Subject
        SubjectDTO subjectDto = createEntityDTO();
        restSubjectMockMvc.perform(post("/api/subjects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto)))
                .andExpect(status().isCreated());

        // Validate the Subject in the database
        List<Subject> subjectList = subjectRepository.findAll();
        assertThat(subjectList).hasSize(databaseSizeBeforeCreate + 1);
        Subject testSubject = subjectList.get(subjectList.size() - 1);

        String subjectLogin = testSubject.getUser().getLogin();
        assertNotNull(subjectLogin);

        // Create a source description
        MinimalSourceDetailsDTO sourceRegistrationDto =
                createSourceWithoutSourceTypeIdAndWithoutDynamicRegistration();

        restSubjectMockMvc.perform(post("/api/subjects/{login}/sources", subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @Transactional
    void getSubjectSources() throws Exception {
        // Initialize the database
        SubjectDTO subjectDtoToCreate = createEntityDTO();
        SourceDTO createdSource = sourceService.save(createSource());
        MinimalSourceDetailsDTO sourceDto = new MinimalSourceDetailsDTO()
                .id(createdSource.getId())
                .sourceName(createdSource.getSourceName())
                .sourceTypeId(createdSource.getSourceType().getId())
                .sourceId(createdSource.getSourceId());

        subjectDtoToCreate.setSources(Collections.singleton(sourceDto));

        assertNotNull(sourceDto.getId());
        SubjectDTO createdSubject = subjectService.createSubject(subjectDtoToCreate);
        assertFalse(createdSubject.getSources().isEmpty());

        // Get the subject
        restSubjectMockMvc.perform(get("/api/subjects/{login}/sources", createdSubject.getLogin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(createdSource.getId().intValue()))
                .andExpect(
                        jsonPath("$.[0].sourceId").value(createdSource.getSourceId().toString()));
    }

    @Test
    @Transactional
    void getSubjectSourcesWithQueryParam() throws Exception {
        // Initialize the database
        SubjectDTO subjectDtoToCreate = createEntityDTO();
        SourceDTO createdSource = sourceService.save(createSource());
        MinimalSourceDetailsDTO sourceDto = new MinimalSourceDetailsDTO()
                .id(createdSource.getId())
                .sourceName(createdSource.getSourceName())
                .sourceTypeId(createdSource.getSourceType().getId())
                .sourceId(createdSource.getSourceId());

        subjectDtoToCreate.setSources(Collections.singleton(sourceDto));

        assertNotNull(sourceDto.getId());
        SubjectDTO createdSubject = subjectService.createSubject(subjectDtoToCreate);
        commitTransactionAndStartNew();

        assertNotNull(createdSubject.getLogin());
        // Get the subject
        restSubjectMockMvc.perform(get("/api/subjects/{login}/sources?withInactiveSources=true",
                createdSubject.getLogin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(createdSource.getId().intValue()))
                .andExpect(
                        jsonPath("$.[*].sourceId").value(createdSource.getSourceId().toString()));
    }

    @Test
    @Transactional
    void getInactiveSubjectSourcesWithQueryParam() throws Exception {
        // Initialize the database
        SubjectDTO subjectDtoToCreate = createEntityDTO();
        SourceDTO createdSource = sourceService.save(createSource());
        MinimalSourceDetailsDTO sourceDto = new MinimalSourceDetailsDTO()
                .id(createdSource.getId())
                .sourceName(createdSource.getSourceName())
                .sourceTypeId(createdSource.getSourceType().getId())
                .sourceId(createdSource.getSourceId());

        subjectDtoToCreate.setSources(Collections.singleton(sourceDto));

        assertNotNull(sourceDto.getId());
        SubjectDTO createdSubject = subjectService.createSubject(subjectDtoToCreate);
        commitTransactionAndStartNew();

        createdSubject.setSources(Collections.emptySet());

        SubjectDTO updatedSubject = subjectService.updateSubject(createdSubject);
        commitTransactionAndStartNew();

        assertNotNull(updatedSubject.getLogin());
        // Get the subject
        restSubjectMockMvc.perform(get("/api/subjects/{login}/sources?withInactiveSources=true",
                updatedSubject.getLogin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(createdSource.getId().intValue()))
                .andExpect(
                        jsonPath("$.[*].sourceId").value(createdSource.getSourceId().toString()));

        // Get the subject
        restSubjectMockMvc
                .perform(get("/api/subjects/{login}/sources?withInactiveSources=false",
                        updatedSubject.getLogin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    private SourceDTO createSource() {
        SourceDTO sourceDto = new SourceDTO();
        sourceDto.setId(1L);
        sourceDto.setAssigned(false);
        sourceDto.setSourceId(UUID.randomUUID());
        sourceDto.setSourceType(sourceTypeService.findAll().get(0));
        sourceDto.setSourceName("something");
        return sourceDto;
    }

    private MinimalSourceDetailsDTO createSourceWithSourceTypeId() {
        List<SourceTypeDTO> sourceTypes = sourceTypeService.findAll().stream()
                .filter(SourceTypeDTO::getCanRegisterDynamically)
                .collect(Collectors.toList());
        assertThat(sourceTypes.size()).isPositive();
        SourceTypeDTO sourceType = sourceTypes.get(0);

        return getSource().sourceTypeId(sourceType.getId());
    }

    private MinimalSourceDetailsDTO createSourceWithoutSourceTypeId() {
        List<SourceTypeDTO> sourceTypes = sourceTypeService.findAll().stream()
                .filter(SourceTypeDTO::getCanRegisterDynamically)
                .collect(Collectors.toList());
        assertThat(sourceTypes.size()).isPositive();
        SourceTypeDTO sourceType = sourceTypes.get(0);
        return getSource()
                .sourceTypeCatalogVersion(sourceType.getCatalogVersion())
                .sourceTypeModel(sourceType.getModel())
                .sourceTypeProducer(sourceType.getProducer());
    }

    private MinimalSourceDetailsDTO createSourceWithoutSourceTypeIdAndWithoutDynamicRegistration() {
        List<SourceTypeDTO> sourceTypes = sourceTypeService.findAll().stream()
                .filter(it -> !it.getCanRegisterDynamically())
                .collect(Collectors.toList());
        assertThat(sourceTypes.size()).isPositive();
        SourceTypeDTO sourceType = sourceTypes.get(0);
        return getSource()
                .sourceTypeCatalogVersion(sourceType.getCatalogVersion())
                .sourceTypeModel(sourceType.getModel())
                .sourceTypeProducer(sourceType.getProducer());
    }

    private MinimalSourceDetailsDTO getSource() {
        MinimalSourceDetailsDTO sourceRegistrationDto = new MinimalSourceDetailsDTO()
                .sourceName(PRODUCER + "-" + MODEL)
                .attributes(Collections.singletonMap("something", "value"));
        assertThat(sourceRegistrationDto.getSourceId()).isNull();
        return sourceRegistrationDto;
    }

    @Test
    @Transactional
    void testDynamicRegistrationAndUpdateSourceAttributes() throws Exception {
        final int databaseSizeBeforeCreate = subjectRepository.findAll().size();

        // Create the Subject
        SubjectDTO subjectDto = createEntityDTO();
        restSubjectMockMvc.perform(post("/api/subjects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto)))
                .andExpect(status().isCreated());

        // Validate the Subject in the database
        List<Subject> subjectList = subjectRepository.findAll();
        assertThat(subjectList).hasSize(databaseSizeBeforeCreate + 1);
        Subject testSubject = subjectList.get(subjectList.size() - 1);

        String subjectLogin = testSubject.getUser().getLogin();
        assertNotNull(subjectLogin);

        // Create a source description
        MinimalSourceDetailsDTO sourceRegistrationDto = createSourceWithoutSourceTypeId();

        MvcResult result = restSubjectMockMvc.perform(post("/api/subjects/{login}/sources",
                subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto)))
                .andExpect(status().isOk())
                .andReturn();

        MinimalSourceDetailsDTO value = (MinimalSourceDetailsDTO)
                TestUtil.convertJsonStringToObject(result
                .getResponse().getContentAsString() , MinimalSourceDetailsDTO.class);

        assertNotNull(value.getSourceName());

        Map<String, String> attributes = new HashMap<>();
        attributes.put("TEST_KEY" , "Value");
        attributes.put("ANDROID_VERSION" , "something");
        attributes.put("Other" , "test");

        restSubjectMockMvc.perform(post(
                "/api/subjects/{login}/sources/{sourceName}", subjectLogin, value.getSourceName())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(attributes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributes").isNotEmpty());

    }
}
