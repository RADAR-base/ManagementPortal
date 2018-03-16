package org.radarcns.management.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.security.JwtAuthenticationFilter;
import org.radarcns.management.service.SourceTypeService;
import org.radarcns.management.service.SubjectService;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.web.rest.errors.ExceptionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test class for the SubjectResource REST controller.
 *
 * @see SubjectResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
public class SubjectResourceIntTest {

    private static final String DEFAULT_EXTERNAL_LINK = "AAAAAAAAAA";
    private static final String UPDATED_EXTERNAL_LINK = "BBBBBBBBBB";

    private static final String DEFAULT_ENTERNAL_ID = "AAAAAAAAAA";
    private static final String UPDATED_ENTERNAL_ID = "BBBBBBBBBB";

    private static final Boolean DEFAULT_REMOVED = false;
    private static final Boolean UPDATED_REMOVED = true;

    private static final SubjectDTO.SubjectStatus DEFAULT_STATUS =
            SubjectDTO.SubjectStatus.ACTIVATED;

    private static final String MODEL = "App";
    private static final String PRODUCER = "THINC-IT";

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SourceTypeService sourceTypeService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private HttpServletRequest servletRequest;

    private MockMvc restSubjectMockMvc;

    @Before
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
        SubjectResource subjectResource = new SubjectResource();
        ReflectionTestUtils.setField(subjectResource, "subjectService" , subjectService);
        ReflectionTestUtils.setField(subjectResource, "subjectRepository" , subjectRepository);
        ReflectionTestUtils.setField(subjectResource, "subjectMapper" , subjectMapper);
        ReflectionTestUtils.setField(subjectResource, "projectRepository" , projectRepository);
        ReflectionTestUtils.setField(subjectResource, "sourceTypeService", sourceTypeService);
        ReflectionTestUtils.setField(subjectResource, "servletRequest", servletRequest);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restSubjectMockMvc = MockMvcBuilders.standaloneSetup(subjectResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter(filter)
            // add the oauth token by default to all requests for this mockMvc
            .defaultRequest(get("/").with(OAuthHelper.bearerToken())).build();
    }

    /**
     * Create an entity for this test.
     *
     * <p>This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.</p>
     */
    public static SubjectDTO createEntityDTO(EntityManager em) {
        SubjectDTO subject = new SubjectDTO();
        subject.setExternalLink(DEFAULT_EXTERNAL_LINK);
        subject.setExternalId(DEFAULT_ENTERNAL_ID);
        subject.setStatus(SubjectDTO.SubjectStatus.ACTIVATED);
        ProjectDTO projectDto = new ProjectDTO();
        projectDto.setId(1L);
        projectDto.setProjectName("Radar");
        projectDto.setLocation("SOMEWHERE");
        projectDto.setDescription("test");
        subject.setProject(projectDto);
        return subject;
    }

    @Test
    @Transactional
    public void createSubject() throws Exception {
        final int databaseSizeBeforeCreate = subjectRepository.findAll().size();

        // Create the Subject
        SubjectDTO subjectDto = createEntityDTO(em);
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
        assertEquals(testSubject.getUser().getRoles().size(),1);
    }

    @Test
    @Transactional
    public void createSubjectWithExistingId() throws Exception {
        // Create a Subject
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO(em));
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
    public void getAllSubjects() throws Exception {
        // Initialize the database
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO(em));

        // Get all the subjectList
        restSubjectMockMvc.perform(get("/api/subjects?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(subjectDto.getId().intValue())))
            .andExpect(jsonPath("$.[*].externalLink").value(hasItem(DEFAULT_EXTERNAL_LINK)))
            .andExpect(jsonPath("$.[*].externalId").value(hasItem(DEFAULT_ENTERNAL_ID)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    @Transactional
    public void getSubject() throws Exception {
        // Initialize the database
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO(em));

        // Get the subject
        restSubjectMockMvc.perform(get("/api/subjects/{login}", subjectDto.getLogin()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(subjectDto.getId().intValue()))
            .andExpect(jsonPath("$.externalLink").value(DEFAULT_EXTERNAL_LINK))
            .andExpect(jsonPath("$.externalId").value(DEFAULT_ENTERNAL_ID))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSubject() throws Exception {
        // Get the subject
        restSubjectMockMvc.perform(get("/api/subjects/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSubject() throws Exception {
        // Initialize the database
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO(em));
        final int databaseSizeBeforeUpdate = subjectRepository.findAll().size();

        // Update the subject
        Subject updatedSubject = subjectRepository.findOne(subjectDto.getId());
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
    public void updateSubjectWithNewProject() throws Exception {
        // Initialize the database
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO(em));
        final int databaseSizeBeforeUpdate = subjectRepository.findAll().size();

        // Update the subject
        Subject updatedSubject = subjectRepository.findOne(subjectDto.getId());


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
    public void updateNonExistingSubject() throws Exception {
        final int databaseSizeBeforeUpdate = subjectRepository.findAll().size();

        // Create the Subject
        SubjectDTO subjectDto = createEntityDTO(em);

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
    public void deleteSubject() throws Exception {
        // Initialize the database
        SubjectDTO subjectDto = subjectService.createSubject(createEntityDTO(em));
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
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Subject.class);
    }


    @Test
    @Transactional
    public void dynamicSourceRegistrationWithId() throws Exception {
        final int databaseSizeBeforeCreate = subjectRepository.findAll().size();

        // Create the Subject
        SubjectDTO subjectDto = createEntityDTO(em);
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
        MinimalSourceDetailsDTO sourceRegistrationDto = createSourceWithDeviceId();

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
    public void dynamicSourceRegistrationWithoutId() throws Exception {
        final int databaseSizeBeforeCreate = subjectRepository.findAll().size();

        // Create the Subject
        SubjectDTO subjectDto = createEntityDTO(em);
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
        MinimalSourceDetailsDTO sourceRegistrationDto = createSourceWithoutDeviceId();

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

    private MinimalSourceDetailsDTO createSourceWithDeviceId() {
        // Create a source description
        MinimalSourceDetailsDTO sourceRegistrationDto = new MinimalSourceDetailsDTO();
        sourceRegistrationDto.setSourceName(PRODUCER + "-" + MODEL);
        sourceRegistrationDto.getAttributes().put("some", "value");

        List<SourceTypeDTO> sourceTypes = sourceTypeService.findAll().stream()
                .filter(dt -> dt.getCanRegisterDynamically())
                .collect(Collectors.toList());

        assertThat(sourceTypes.size()).isGreaterThan(0);
        SourceTypeDTO sourceType = sourceTypes.get(0);
        sourceRegistrationDto.setSourceTypeId(sourceType.getId());

        assertThat(sourceRegistrationDto.getSourceId()).isNull();
        return sourceRegistrationDto;
    }

    private MinimalSourceDetailsDTO createSourceWithoutDeviceId() {
        // Create a source description
        MinimalSourceDetailsDTO sourceRegistrationDto = new MinimalSourceDetailsDTO();
        sourceRegistrationDto.setSourceName(PRODUCER + "-" + MODEL);
        sourceRegistrationDto.getAttributes().put("some", "value");

        List<SourceTypeDTO> sourceTypes = sourceTypeService.findAll().stream()
                .filter(dt -> dt.getCanRegisterDynamically())
                .collect(Collectors.toList());

        assertThat(sourceTypes.size()).isGreaterThan(0);
        SourceTypeDTO sourceType = sourceTypes.get(0);
        sourceRegistrationDto.setSourceTypeCatalogVersion(sourceType.getCatalogVersion());
        sourceRegistrationDto.setSourceTypeModel(sourceType.getModel());
        sourceRegistrationDto.setSourceTypeProducer(sourceType.getProducer());

        assertThat(sourceRegistrationDto.getSourceId()).isNull();
        return sourceRegistrationDto;
    }
}
