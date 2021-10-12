package org.radarbase.management.web.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.radarbase.auth.authentication.OAuthHelper;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.domain.Group;
import org.radarbase.management.domain.Project;
import org.radarbase.management.repository.GroupRepository;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.security.JwtAuthenticationFilter;
import org.radarbase.management.service.GroupService;
import org.radarbase.management.service.dto.GroupDTO;
import org.radarbase.management.service.mapper.GroupMapper;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the ProjectResource REST controller.
 *
 * @see ProjectResource
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
class GroupResourceIntTest {

    private static final String DEFAULT_PROJECT_NAME = "AAAAAAAAAA";

    @Autowired
    private GroupService groupService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private HttpServletRequest servletRequest;

    private MockMvc restProjectMockMvc;

    private Group group;

    private Project project;

    @BeforeEach
    @Transactional
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
        GroupResource projectResource = new GroupResource();
        ReflectionTestUtils.setField(projectResource, "groupService", groupService);
        ReflectionTestUtils.setField(projectResource, "servletRequest", servletRequest);

        JwtAuthenticationFilter filter = OAuthHelper.createAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restProjectMockMvc = MockMvcBuilders.standaloneSetup(projectResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter)
                .addFilter(filter)
                .defaultRequest(get("/").with(OAuthHelper.bearerToken())).build();
        project = ProjectResourceIntTest.createEntity();
        projectRepository.save(project);
        group = createEntity();
    }

    @AfterEach
    @Transactional
    public void tearDown() {
        groupRepository.deleteAll();
        projectRepository.delete(project);
    }

    /**
     * Create an entity for this test.
     *
     * <p>This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.</p>
     */
    private Group createEntity() {
        Group group = new Group();
        group.setName("group1");
        group.setProject(project);
        return group;
    }

    @BeforeEach
    public void initTest() {
        group = createEntity();
    }

    @Test
    @Transactional
    void createGroup() throws Exception {
        assertThat(groupRepository.findAll().size()).isEqualTo(0);

        // Create the Project
        GroupDTO groupDto = groupMapper.groupToGroupDTO(group);
        restProjectMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isCreated());

        // Validate the Project in the database
        List<Group> groupList = groupRepository.findAll();
        assertThat(groupList).hasSize(1);
        Group testGroup = groupList.get(groupList.size() - 1);
        assertThat(testGroup.getProject().getId()).isEqualTo(project.getId());
        assertThat(testGroup.getName()).isEqualTo("group1");
    }


    @Test
    @Transactional
    void createGroupNonExistingProject() throws Exception {
        assertThat(groupRepository.findAll().size()).isEqualTo(0);
        projectRepository.delete(project);

        // Create the Project
        GroupDTO groupDto = groupMapper.groupToGroupDTO(group);
        restProjectMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isNotFound());

        // Validate the Project in the database
        assertThat(groupRepository.findAll()).hasSize(0);
    }

    @Test
    @Transactional
    void createGroupWithExistingName() throws Exception {
        assertThat(groupRepository.findAll().size()).isEqualTo(0);

        // Create the Project
        GroupDTO groupDto = groupMapper.groupToGroupDTO(group);
        restProjectMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isCreated());

        restProjectMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isConflict());

        // Validate the Alice in the database
        List<Group> groupList = groupRepository.findAll();
        assertThat(groupList).hasSize(1);
    }


    @Test
    @Transactional
    void createGroupWithExistingNameInDifferentProject() throws Exception {
        assertThat(groupRepository.findAll().size()).isEqualTo(0);

        Project project2 = ProjectResourceIntTest.createEntity()
                .projectName(project.getProjectName() + "2");


        projectRepository.saveAndFlush(project2);
        Group group2 = new Group();
        group2.setName(group.getName());
        group2.setProject(project2);
        groupRepository.saveAndFlush(group2);

        // Create the Project
        GroupDTO groupDto = groupMapper.groupToGroupDTO(group);
        restProjectMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isCreated());

        GroupDTO group2Dto = groupMapper.groupToGroupDTO(group2);
        restProjectMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project2.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(group2Dto)))
                .andExpect(status().isCreated());

        // Validate the Alice in the database
        List<Group> groupList = groupRepository.findAll();
        assertThat(groupList).hasSize(2);
        assertThat(groupList).hasOnlyOneElementSatisfying(g ->
                assertThat(g.getProject().getId()).isEqualTo(project.getId()));
        assertThat(groupList).hasOnlyOneElementSatisfying(g ->
                assertThat(g.getProject().getId()).isEqualTo(project2.getId()));
        assertThat(groupList).allSatisfy(g -> assertThat(g.getName()).isEqualTo(group.getName()));

        projectRepository.delete(project2);
    }

    @Test
    @Transactional
    void checkGroupNameIsRequired() throws Exception {
        assertThat(groupRepository.findAll().size()).isEqualTo(0);

        group.setName(null);

        // Create the Project
        GroupDTO groupDto = groupMapper.groupToGroupDTO(group);
        restProjectMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertThat(groupRepository.findAll()).hasSize(0);
    }

    @Test
    @Transactional
    void getAllProjects() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get all the projectList
        restProjectMockMvc.perform(get("/api/projects/{projectName}/groups",
                        project.getProjectName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(group.getId().intValue())))
                .andExpect(jsonPath("$.[*].projectId").value(hasItem(DEFAULT_PROJECT_NAME)))
                .andExpect(jsonPath("$.[*].name").value(hasItem("group1")));
    }

    @Test
    @Transactional
    void getProject() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get the project
        restProjectMockMvc.perform(get("/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName(), group.getName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(group.getId().intValue()))
                .andExpect(jsonPath("$.name").value("group1"))
                .andExpect(jsonPath("$.projectId").value(project.getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingGroup() throws Exception {
        // Get the project
        restProjectMockMvc.perform(get("/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName(), group.getName()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void deleteGroup() throws Exception {
        assertThat(groupRepository.findAll().size()).isEqualTo(0);

        // Initialize the database
        groupRepository.saveAndFlush(group);

        assertThat(groupRepository.findAll().size()).isEqualTo(1);

        // Get the project
        restProjectMockMvc.perform(delete(
                        "/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName(), group.getName())
                        .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        assertThat(groupRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    @Transactional
    void deleteGroupNonExisting() throws Exception {
        assertThat(groupRepository.findAll().size()).isEqualTo(0);

        // Initialize the database
        groupRepository.saveAndFlush(group);

        assertThat(groupRepository.findAll().size()).isEqualTo(1);

        // Get the project
        restProjectMockMvc.perform(delete(
                        "/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName(), group.getName() + "2")
                        .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

        // Validate the database is empty
        assertThat(groupRepository.findAll().size()).isEqualTo(1);
    }


    @Test
    @Transactional
    void deleteGroupNonExistingProject() throws Exception {
        assertThat(groupRepository.findAll().size()).isEqualTo(0);

        // Initialize the database
        groupRepository.saveAndFlush(group);

        assertThat(groupRepository.findAll().size()).isEqualTo(1);

        // Get the project
        restProjectMockMvc.perform(delete(
                        "/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName() + "2", group.getName())
                        .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

        // Validate the database is empty
        assertThat(groupRepository.findAll().size()).isEqualTo(1);
    }
}
