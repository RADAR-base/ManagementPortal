package org.radarbase.management.web.rest

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.Group
import org.radarbase.management.domain.Project
import org.radarbase.management.domain.Subject
import org.radarbase.management.repository.GroupRepository
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.repository.RoleRepository
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.GroupService
import org.radarbase.management.service.SubjectService
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.dto.SubjectDTO.SubjectStatus
import org.radarbase.management.service.mapper.GroupMapper
import org.radarbase.management.service.mapper.ProjectMapper
import org.radarbase.management.web.rest.errors.ExceptionTranslator
import org.radarbase.management.web.rest.vm.GroupPatchOperation
import org.radarbase.management.web.rest.vm.GroupPatchOperation.SubjectPatchValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockFilterConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.util.*
import javax.servlet.ServletException

/**
 * Test class for the GroupResource REST controller.
 *
 * @see GroupResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal class GroupResourceIntTest(
    @Autowired private val groupService: GroupService,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val projectMapper: ProjectMapper,
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val roleRepository: RoleRepository,
    @Autowired private val subjectRepository: SubjectRepository,
    @Autowired private val subjectService: SubjectService,
    @Autowired private val groupMapper: GroupMapper,
    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val exceptionTranslator: ExceptionTranslator,
    @Autowired private val groupRepository: GroupRepository,
    private var restGroupMockMvc: MockMvc,
    private var group: Group,
    private var project: Project
) {

    @Autowired
    private val authService: AuthService? = null

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val groupResource = GroupResource()
        ReflectionTestUtils.setField(groupResource, "groupService", groupService)
        ReflectionTestUtils.setField(groupResource, "authService", authService)
        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restGroupMockMvc =
            MockMvcBuilders.standaloneSetup(groupResource).setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator).setMessageConverters(jacksonMessageConverter)
                .addFilter<StandaloneMockMvcBuilder>(filter).defaultRequest<StandaloneMockMvcBuilder>(
                    MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken())
                ).build()
        project = ProjectResourceIntTest.Companion.createEntity()
        projectRepository.save(project)
        group = createEntity()
    }

    @AfterEach
    fun tearDown() {
        groupRepository.delete(group)
        val roles = roleRepository.findAllRolesByProjectName(
            project.projectName!!
        )
        roleRepository.deleteAll(roles)
        projectRepository.delete(project)
    }

    /**
     * Create an entity for this test.
     */
    private fun createEntity(): Group {
        val group = Group()
        group.name = "group1"
        group.project = project
        return group
    }

    @Test
    @Throws(Exception::class)
    fun createGroup() {
        // Create the Group
        val groupDto = groupMapper.groupToGroupDTO(group)
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.post(
                "/api/projects/{projectName}/groups", project.projectName
            ).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(groupDto))
        ).andExpect(MockMvcResultMatchers.status().isCreated())
        val savedGroup: Group? = groupRepository.findByProjectNameAndName(
            project.projectName, groupDto.name
        )

        // Validate the Group in the database
        assertThat(savedGroup?.project?.id).isEqualTo(project.id)
        assertThat(savedGroup?.name).isEqualTo("group1")
    }

    @Test
    @Throws(Exception::class)
    fun createGroupNonExistingProject() {
        projectRepository.delete(project)

        // Create the Group
        val groupDto = groupMapper.groupToGroupDTO(group)
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.post(
                "/api/projects/{projectName}/groups", project.projectName
            ).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(groupDto))
        ).andExpect(MockMvcResultMatchers.status().isNotFound())
    }

    @Test
    @Throws(Exception::class)
    fun createGroupWithExistingName() {
        // Create the Group
        val groupDto = groupMapper.groupToGroupDTO(group)
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.post(
                "/api/projects/{projectName}/groups", project.projectName
            ).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(groupDto))
        ).andExpect(MockMvcResultMatchers.status().isCreated())
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.post(
                "/api/projects/{projectName}/groups", project.projectName
            ).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(groupDto))
        ).andExpect(MockMvcResultMatchers.status().isConflict())
    }

    @Test
    @Throws(Exception::class)
    fun createGroupWithExistingNameInDifferentProject() {
        val project2: Project = ProjectResourceIntTest.Companion.createEntity().projectName(project.projectName + "2")
        projectRepository.saveAndFlush(project2)
        val group2 = Group()
        group2.name = group.name
        group2.project = project2

        // Create the Group
        val groupDto = groupMapper.groupToGroupDTO(group)
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.post(
                "/api/projects/{projectName}/groups", project.projectName
            ).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(groupDto))
        ).andExpect(MockMvcResultMatchers.status().isCreated())
        val group2Dto = groupMapper.groupToGroupDTO(group2)
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.post(
                "/api/projects/{projectName}/groups", project2.projectName
            ).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(group2Dto))
        ).andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate groups are saved for both projects
        val savedGroup1: Group? = groupRepository.findByProjectNameAndName(
            project.projectName, group.name
        )
        val savedGroup2: Group? = groupRepository.findByProjectNameAndName(
            project2.projectName, group2.name
        )
        val groupList = listOf(savedGroup1, savedGroup2)
        assertThat(groupList).hasSize(2)
        assertThat(groupList).haveAtLeastOne(
            Condition(
                { g -> project.id == g?.project?.id }, "use project 1"
            )
        )
        assertThat(groupList).haveAtLeastOne(
            Condition(
                { g -> project2.id == g?.project?.id }, "use project 2"
            )
        )
        assertThat(groupList).allSatisfy { g -> assertThat(g?.name).isEqualTo(group.name) }
        projectRepository.delete(project2)
    }

    @Test
    @Throws(Exception::class)
    fun checkGroupNameIsRequired() {
        group.name = null

        // Create the Group
        val groupDto = groupMapper.groupToGroupDTO(group)
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.post(
                "/api/projects/{projectName}/groups", project.projectName
            ).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(groupDto))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest())
    }

    @get:Throws(Exception::class)
    @get:Test
    val allGroups: Unit
        get() {
            // Initialize the database
            groupRepository.saveAndFlush<Group>(group)

            // Get all the groups
            restGroupMockMvc.perform(
                MockMvcRequestBuilders.get(
                    "/api/projects/{projectName}/groups", project.projectName
                )
            ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)).andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].projectId").value<Iterable<Int?>>(
                        Matchers.hasItem(project.id!!.toInt())
                    )
                ).andExpect(
                    MockMvcResultMatchers.jsonPath("$.[*].name").value<Iterable<String?>>(Matchers.hasItem("group1"))
                )
        }

    @Test
    @Throws(Exception::class)
    fun getGroup() {
        // Initialize the database
        groupRepository.saveAndFlush<Group>(group)

        // Get the Group
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/projects/{projectName}/groups/{groupName}", project.projectName, group.name
            )
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("group1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.projectId").value(project.id!!.toInt()))
    }

    @get:Throws(Exception::class)
    @get:Test
    val nonExistingGroup: Unit
        get() {
            // Get the Group
            restGroupMockMvc.perform(
                MockMvcRequestBuilders.get(
                    "/api/projects/{projectName}/groups/{groupName}", project.projectName, group.name
                )
            ).andExpect(MockMvcResultMatchers.status().isNotFound())
        }

    @Test
    @Throws(Exception::class)
    fun deleteGroup() {
        // Initialize the database
        groupRepository.saveAndFlush<Group>(group)

        // Get the Group
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/projects/{projectName}/groups/{groupName}", project.projectName, group.name
            ).accept(TestUtil.APPLICATION_JSON_UTF8)
        ).andExpect(MockMvcResultMatchers.status().isNoContent())

        // Validate the Group is not present in the database
        val savedGroup = groupRepository.findByProjectNameAndName(
            project.projectName, group.name
        )
        assertThat(savedGroup).isNull()
    }

    @Test
    @Throws(Exception::class)
    fun deleteGroupWithSubjects() {
        // Initialize the database
        groupRepository.saveAndFlush<Group>(group)
        val projectDto = projectMapper.projectToProjectDTO(project)
        val subjectDto = SubjectDTO()
        subjectDto.externalLink = "exLink1"
        subjectDto.externalId = "exId1"
        subjectDto.status = SubjectStatus.ACTIVATED
        subjectDto.project = projectDto
        subjectDto.group = group.name
        val savedSubject = subjectService.createSubject(subjectDto)

        // Try to delete the Group (and fail)
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/projects/{projectName}/groups/{groupName}", project.projectName, group.name
            ).accept(TestUtil.APPLICATION_JSON_UTF8)
        ).andExpect(MockMvcResultMatchers.status().isConflict())

        // Delete the Group (and unlink the subjects)
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/projects/{projectName}/groups/{groupName}", project.projectName, group.name
            ).param("unlinkSubjects", "true").accept(TestUtil.APPLICATION_JSON_UTF8)
        ).andExpect(MockMvcResultMatchers.status().isNoContent())

        // Validate the Group is not present in the database
        val savedGroup = groupRepository.findByProjectNameAndName(
            project.projectName, group.name
        )
        Assertions.assertThat(savedGroup).isNull()
        val storedSubject = subjectRepository.getOne(savedSubject!!.id!!)
        subjectRepository.delete(storedSubject)
    }

    @Test
    @Throws(Exception::class)
    fun deleteGroupNonExisting() {
        // Initialize the database
        groupRepository.saveAndFlush<Group>(group)

        // Get the Group
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/projects/{projectName}/groups/{groupName}", project.projectName, group.name + "2"
            ).accept(TestUtil.APPLICATION_JSON_UTF8)
        ).andExpect(MockMvcResultMatchers.status().isNotFound())

        // Validate the database still contains the group
        Assertions.assertThat(
            groupRepository.findById(
                group.id!!
            )
        ).isNotEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun deleteGroupNonExistingProject() {
        // Initialize the database
        groupRepository.saveAndFlush<Group>(group)

        // Get the Group
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/projects/{projectName}/groups/{groupName}", project.projectName + "2", group.name
            ).accept(TestUtil.APPLICATION_JSON_UTF8)
        ).andExpect(MockMvcResultMatchers.status().isNotFound())

        // Validate the database still contains the group
        Assertions.assertThat(
            groupRepository.findById(
                group.id!!
            )
        ).isNotEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun addSubjectsToGroup() {
        // Initialize the database
        groupRepository.saveAndFlush<Group>(group)
        val projectDto = projectMapper.projectToProjectDTO(project)
        val sub1 = SubjectDTO()
        sub1.externalLink = "exLink1"
        sub1.externalId = "exId1"
        sub1.status = SubjectStatus.ACTIVATED
        sub1.project = projectDto
        val sub2 = SubjectDTO()
        sub2.externalLink = "exLink2"
        sub2.externalId = "exId2"
        sub2.status = SubjectStatus.ACTIVATED
        sub2.project = projectDto
        val savedSub1 = subjectService.createSubject(sub1)
        val savedSub2 = subjectService.createSubject(sub2)
        val sub1Patch = SubjectPatchValue()
        sub1Patch.id = savedSub1!!.id
        val sub2Patch = SubjectPatchValue()
        sub2Patch.login = savedSub2!!.login
        val patchOp = GroupPatchOperation()
        patchOp.op = "add"
        val patchValue = ArrayList<SubjectPatchValue>()
        patchValue.add(sub1Patch)
        patchValue.add(sub2Patch)
        patchOp.value = patchValue
        val body: MutableList<GroupPatchOperation> = ArrayList()
        body.add(patchOp)

        // Get the Group
        restGroupMockMvc.perform(
            MockMvcRequestBuilders.patch(
                "/api/projects/{projectName}/groups/{groupName}/subjects", project.projectName, group.name
            ).contentType(TestUtil.APPLICATION_JSON_PATCH).content(TestUtil.convertObjectToJsonBytes(body))
        ).andExpect(MockMvcResultMatchers.status().isNoContent())

        // Validate that the group was set for both subjects
        val subjectLogins = listOf(savedSub1.login!!, savedSub2.login!!)
        val subjects = subjectRepository.findAllBySubjectLogins(subjectLogins)
        Assertions.assertThat(subjects).hasSize(2)
        Assertions.assertThat(subjects).allSatisfy { s: Subject ->
            Assertions.assertThat(
                s.group!!.id
            ).isEqualTo(group.id)
        }
        subjectRepository.deleteAll(subjects)
    }
}
