package org.radarbase.management.web.rest;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.radarbase.auth.authentication.OAuthHelper;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.domain.Group;
import org.radarbase.management.domain.Project;
import org.radarbase.management.repository.GroupRepository;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.repository.RoleRepository;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.security.JwtAuthenticationFilter;
import org.radarbase.management.service.GroupService;
import org.radarbase.management.service.SubjectService;
import org.radarbase.management.service.dto.GroupDTO;
import org.radarbase.management.service.dto.SubjectDTO;
import org.radarbase.management.service.mapper.GroupMapper;
import org.radarbase.management.service.mapper.ProjectMapper;
import org.radarbase.management.web.rest.errors.ExceptionTranslator;
import org.radarbase.management.web.rest.vm.GroupPatchOperation;
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

import javax.servlet.ServletException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.radarbase.management.service.dto.SubjectDTO.SubjectStatus.ACTIVATED;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the GroupResource REST controller.
 *
 * @see GroupResource
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@WithMockUser
class GroupResourceIntTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RadarToken token;

    private MockMvc restGroupMockMvc;

    private Group group;

    private Project project;

    @BeforeEach
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
        var groupResource = new GroupResource();
        ReflectionTestUtils.setField(groupResource, "groupService", groupService);
        ReflectionTestUtils.setField(groupResource, "token", token);

        JwtAuthenticationFilter filter = OAuthHelper.createAuthenticationFilter();
        filter.init(new MockFilterConfig());

        this.restGroupMockMvc = MockMvcBuilders.standaloneSetup(groupResource)
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
    public void tearDown() {
        groupRepository.delete(group);
        var roles = roleRepository.findAllRolesByProjectName(project.getProjectName());
        roleRepository.deleteAll(roles);
        projectRepository.delete(project);
    }

    /**
     * Create an entity for this test.
     */
    private Group createEntity() {
        Group group = new Group();
        group.setName("group1");
        group.setProject(project);
        return group;
    }

    @Test
    void createGroup() throws Exception {
        // Create the Group
        var groupDto = groupMapper.groupToGroupDTO(group);
        restGroupMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isCreated());

        var savedGroup = groupRepository.findByProjectNameAndName(
                project.getProjectName(), groupDto.getName()).get();

        // Validate the Group in the database
        assertThat(savedGroup.getProject().getId()).isEqualTo(project.getId());
        assertThat(savedGroup.getName()).isEqualTo("group1");
    }


    @Test
    void createGroupNonExistingProject() throws Exception {
        projectRepository.delete(project);

        // Create the Group
        var groupDto = groupMapper.groupToGroupDTO(group);
        restGroupMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createGroupWithExistingName() throws Exception {
        // Create the Group
        var groupDto = groupMapper.groupToGroupDTO(group);
        restGroupMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isCreated());

        restGroupMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isConflict());
    }


    @Test
    void createGroupWithExistingNameInDifferentProject() throws Exception {
        Project project2 = ProjectResourceIntTest.createEntity()
                .projectName(project.getProjectName() + "2");

        projectRepository.saveAndFlush(project2);
        Group group2 = new Group();
        group2.setName(group.getName());
        group2.setProject(project2);

        // Create the Group
        var groupDto = groupMapper.groupToGroupDTO(group);
        restGroupMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isCreated());

        var group2Dto = groupMapper.groupToGroupDTO(group2);
        restGroupMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project2.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(group2Dto)))
                .andExpect(status().isCreated());

        // Validate groups are saved for both projects
        var savedGroup1 = groupRepository.findByProjectNameAndName(
                project.getProjectName(), group.getName()).get();
        var savedGroup2 = groupRepository.findByProjectNameAndName(
                project2.getProjectName(), group2.getName()).get();
        var groupList = Arrays.asList(savedGroup1, savedGroup2);
        assertThat(groupList).hasSize(2);
        assertThat(groupList).haveAtLeastOne(new Condition<>(
                g -> project.getId().equals(g.getProject().getId()), "use project 1"));
        assertThat(groupList).haveAtLeastOne(new Condition<>(
                g -> project2.getId().equals(g.getProject().getId()), "use project 2"));
        assertThat(groupList).allSatisfy(
                g -> assertThat(g.getName()).isEqualTo(group.getName()));

        projectRepository.delete(project2);
    }

    @Test
    void checkGroupNameIsRequired() throws Exception {
        group.setName(null);

        // Create the Group
        GroupDTO groupDto = groupMapper.groupToGroupDTO(group);
        restGroupMockMvc.perform(post("/api/projects/{projectName}/groups",
                        project.getProjectName())
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(groupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllGroups() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get all the groups
        restGroupMockMvc.perform(get("/api/projects/{projectName}/groups",
                        project.getProjectName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].projectId").value(
                        hasItem(project.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem("group1")));
    }

    @Test
    void getGroup() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get the Group
        restGroupMockMvc.perform(get("/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName(), group.getName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("group1"))
                .andExpect(jsonPath("$.projectId").value(project.getId().intValue()));
    }

    @Test
    void getNonExistingGroup() throws Exception {
        // Get the Group
        restGroupMockMvc.perform(get("/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName(), group.getName()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteGroup() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get the Group
        restGroupMockMvc.perform(delete(
                        "/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName(), group.getName())
                        .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNoContent());

        // Validate the Group is not present in the database
        var savedGroup = groupRepository.findByProjectNameAndName(
                project.getProjectName(), group.getName());
        assertThat(savedGroup).isEmpty();
    }

    @Test
    void deleteGroupWithSubjects() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        var projectDto = projectMapper.projectToProjectDTO(project);

        var subjectDto = new SubjectDTO();
        subjectDto.setExternalLink("exLink1");
        subjectDto.setExternalId("exId1");
        subjectDto.setStatus(ACTIVATED);
        subjectDto.setProject(projectDto);
        subjectDto.setGroup(group.getName());
        var savedSubject = subjectService.createSubject(subjectDto);

        // Try to delete the Group (and fail)
        restGroupMockMvc.perform(delete(
                        "/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName(), group.getName())
                        .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isConflict());

        // Delete the Group (and unlink the subjects)
        restGroupMockMvc.perform(delete(
                        "/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName(), group.getName())
                        .param("unlinkSubjects", "true")
                        .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNoContent());

        // Validate the Group is not present in the database
        var savedGroup = groupRepository.findByProjectNameAndName(
                project.getProjectName(), group.getName());
        assertThat(savedGroup).isEmpty();

        var storedSubject = subjectRepository.getOne(savedSubject.getId());
        subjectRepository.delete(storedSubject);
    }

    @Test
    void deleteGroupNonExisting() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get the Group
        restGroupMockMvc.perform(delete(
                        "/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName(), group.getName() + "2")
                        .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

        // Validate the database still contains the group
        assertThat(groupRepository.findById(group.getId())).isNotEmpty();
    }


    @Test
    void deleteGroupNonExistingProject() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        // Get the Group
        restGroupMockMvc.perform(delete(
                        "/api/projects/{projectName}/groups/{groupName}",
                        project.getProjectName() + "2", group.getName())
                        .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

        // Validate the database still contains the group
        assertThat(groupRepository.findById(group.getId())).isNotEmpty();
    }

    @Test
    void addSubjectsToGroup() throws Exception {
        // Initialize the database
        groupRepository.saveAndFlush(group);

        var projectDto = projectMapper.projectToProjectDTO(project);

        var sub1 = new SubjectDTO();
        sub1.setExternalLink("exLink1");
        sub1.setExternalId("exId1");
        sub1.setStatus(ACTIVATED);
        sub1.setProject(projectDto);

        var sub2 = new SubjectDTO();
        sub2.setExternalLink("exLink2");
        sub2.setExternalId("exId2");
        sub2.setStatus(ACTIVATED);
        sub2.setProject(projectDto);

        var savedSub1 = subjectService.createSubject(sub1);
        var savedSub2 = subjectService.createSubject(sub2);

        var sub1Patch = new GroupPatchOperation.SubjectPatchValue();
        sub1Patch.setId(savedSub1.getId());
        var sub2Patch = new GroupPatchOperation.SubjectPatchValue();
        sub2Patch.setLogin(savedSub2.getLogin());

        var patchOp = new GroupPatchOperation();
        patchOp.setOp("add");
        var patchValue = new ArrayList<GroupPatchOperation.SubjectPatchValue>();
        patchValue.add(sub1Patch);
        patchValue.add(sub2Patch);
        patchOp.setValue(patchValue);

        List<GroupPatchOperation> body = new ArrayList<>();
        body.add(patchOp);

        // Get the Group
        restGroupMockMvc.perform(patch(
                        "/api/projects/{projectName}/groups/{groupName}/subjects",
                        project.getProjectName(), group.getName())

                        .contentType(TestUtil.APPLICATION_JSON_PATCH)
                        .content(TestUtil.convertObjectToJsonBytes(body)))
                .andExpect(status().isNoContent());

        // Validate that the group was set for both subjects
        var subjectLogins = Arrays.asList(savedSub1.getLogin(), savedSub2.getLogin());
        var subjects = subjectRepository.findAllBySubjectLogins(subjectLogins);
        assertThat(subjects).hasSize(2);
        assertThat(subjects).allSatisfy(
                s -> assertThat(s.getGroup().getId()).isEqualTo(group.getId()));

        subjectRepository.deleteAll(subjects);
    }
}
