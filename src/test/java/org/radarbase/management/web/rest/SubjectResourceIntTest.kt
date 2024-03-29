package org.radarbase.management.web.rest

import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.Subject
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.service.SourceService
import org.radarbase.management.service.SourceTypeService
import org.radarbase.management.service.SubjectService
import org.radarbase.management.service.SubjectServiceTest
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.SourceDTO
import org.radarbase.management.service.dto.SourceTypeDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.mapper.SubjectMapper
import org.radarbase.management.web.rest.errors.ExceptionTranslator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockFilterConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Collectors
import javax.servlet.ServletException

/**
 * Test class for the SubjectResource REST controller.
 *
 * @see SubjectResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal class SubjectResourceIntTest(
    @Autowired private val subjectResource: SubjectResource,
    @Autowired private val subjectRepository: SubjectRepository,
    @Autowired private val subjectMapper: SubjectMapper,
    @Autowired private val subjectService: SubjectService,
    @Autowired private val sourceService: SourceService,
    @Autowired private val sourceTypeService: SourceTypeService,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val exceptionTranslator: ExceptionTranslator,
) {
    private lateinit var restSubjectMockMvc: MockMvc

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restSubjectMockMvc =
            MockMvcBuilders.standaloneSetup(subjectResource).setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator).setMessageConverters(jacksonMessageConverter)
                .addFilter<StandaloneMockMvcBuilder>(filter) // add the oauth token by default to all requests for this mockMvc
                .defaultRequest<StandaloneMockMvcBuilder>(
                    MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken())
                ).build()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createSubject() {
        val databaseSizeBeforeCreate = subjectRepository.findAll().size

        // Create the Subject
        val subjectDto: SubjectDTO = SubjectServiceTest.createEntityDTO()
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto))
        ).andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Subject in the database
        val subjectList = subjectRepository.findAll()
        Assertions.assertThat(subjectList).hasSize(databaseSizeBeforeCreate + 1)
        val testSubject = subjectList[subjectList.size - 1]
        Assertions.assertThat(testSubject.externalLink).isEqualTo(SubjectServiceTest.DEFAULT_EXTERNAL_LINK)
        Assertions.assertThat(testSubject.externalId).isEqualTo(SubjectServiceTest.DEFAULT_ENTERNAL_ID)
        Assertions.assertThat(testSubject.removed).isEqualTo(SubjectServiceTest.DEFAULT_REMOVED)
        Assertions.assertThat(testSubject.user!!.roles.size).isEqualTo(1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createSubjectWithExistingId() {
        // Create a Subject
        val subjectDto = subjectService.createSubject(SubjectServiceTest.createEntityDTO())
        val databaseSizeBeforeCreate = subjectRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest())

        // Validate the Alice in the database
        val subjectList = subjectRepository.findAll()
        Assertions.assertThat(subjectList).hasSize(databaseSizeBeforeCreate)
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun allSubjects() {
        // Initialize the database
        val subjectDto = subjectService.createSubject(SubjectServiceTest.createEntityDTO())

        // Get all the subjectList
        restSubjectMockMvc.perform(MockMvcRequestBuilders.get("/api/subjects?sort=id,desc"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)).andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].id").value<Iterable<Int?>>(
                    Matchers.hasItem(
                        subjectDto!!.id!!.toInt()
                    )
                )
            ).andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].externalLink")
                    .value<Iterable<String?>>(Matchers.hasItem(SubjectServiceTest.DEFAULT_EXTERNAL_LINK))
            ).andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].externalId")
                    .value<Iterable<String?>>(Matchers.hasItem(SubjectServiceTest.DEFAULT_ENTERNAL_ID))
            ).andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].status")
                    .value<Iterable<String?>>(Matchers.hasItem(SubjectServiceTest.DEFAULT_STATUS.toString()))
            )
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun subject() {
        // Initialize the database
        val subjectDto = subjectService.createSubject(SubjectServiceTest.createEntityDTO())

        // Get the subject
        restSubjectMockMvc.perform(MockMvcRequestBuilders.get("/api/subjects/{login}", subjectDto!!.login))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(subjectDto.id!!.toInt())).andExpect(
                MockMvcResultMatchers.jsonPath("$.externalLink").value(SubjectServiceTest.DEFAULT_EXTERNAL_LINK)
            ).andExpect(
                MockMvcResultMatchers.jsonPath("$.externalId").value(SubjectServiceTest.DEFAULT_ENTERNAL_ID)
            ).andExpect(
                MockMvcResultMatchers.jsonPath("$.status").value(SubjectServiceTest.DEFAULT_STATUS.toString())
            )
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun nonExistingSubject() {
        // Get the subject
        restSubjectMockMvc.perform(MockMvcRequestBuilders.get("/api/subjects/{id}", Long.MAX_VALUE))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateSubject() {
        // Initialize the database
        var subjectDto = subjectService.createSubject(SubjectServiceTest.createEntityDTO())
        val databaseSizeBeforeUpdate = subjectRepository.findAll().size

        // Update the subject
        val updatedSubject = subjectRepository.findById(subjectDto!!.id!!).get()
        updatedSubject.externalLink(SubjectServiceTest.UPDATED_EXTERNAL_LINK)
            .externalId(SubjectServiceTest.UPDATED_ENTERNAL_ID).removed = SubjectServiceTest.UPDATED_REMOVED
        subjectDto = subjectMapper.subjectToSubjectDTO(updatedSubject)
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.put("/api/subjects").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto))
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the Subject in the database
        val subjectList = subjectRepository.findAll()
        Assertions.assertThat(subjectList).hasSize(databaseSizeBeforeUpdate)
        val testSubject = subjectList[subjectList.size - 1]
        Assertions.assertThat(testSubject.externalLink).isEqualTo(SubjectServiceTest.UPDATED_EXTERNAL_LINK)
        Assertions.assertThat(testSubject.externalId).isEqualTo(SubjectServiceTest.UPDATED_ENTERNAL_ID)
        Assertions.assertThat(testSubject.removed).isEqualTo(SubjectServiceTest.UPDATED_REMOVED)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateSubjectWithNewProject() {
        // Initialize the database
        var subjectDto = subjectService.createSubject(SubjectServiceTest.createEntityDTO())
        val databaseSizeBeforeUpdate = subjectRepository.findAll().size

        // Update the subject
        val updatedSubject = subjectRepository.findById(subjectDto!!.id!!).get()
        updatedSubject.externalLink(SubjectServiceTest.UPDATED_EXTERNAL_LINK)
            .externalId(SubjectServiceTest.UPDATED_ENTERNAL_ID).removed = SubjectServiceTest.UPDATED_REMOVED
        subjectDto = subjectMapper.subjectToSubjectDTO(updatedSubject)
        val newProject = ProjectDTO()
        newProject.id = 2L
        newProject.projectName = "RadarNew"
        newProject.location = "new location"
        subjectDto!!.project = newProject
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.put("/api/subjects").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto))
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the Subject in the database
        val subjectList = subjectRepository.findAll()
        Assertions.assertThat(subjectList).hasSize(databaseSizeBeforeUpdate)
        val testSubject = subjectList[subjectList.size - 1]
        Assertions.assertThat(testSubject.externalLink).isEqualTo(SubjectServiceTest.UPDATED_EXTERNAL_LINK)
        Assertions.assertThat(testSubject.externalId).isEqualTo(SubjectServiceTest.UPDATED_ENTERNAL_ID)
        Assertions.assertThat(testSubject.removed).isEqualTo(SubjectServiceTest.UPDATED_REMOVED)
        Assertions.assertThat(testSubject.user!!.roles.size).isEqualTo(2)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateNonExistingSubject() {
        val databaseSizeBeforeUpdate = subjectRepository.findAll().size

        // Create the Subject
        val subjectDto: SubjectDTO = SubjectServiceTest.createEntityDTO()

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.put("/api/subjects").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto))
        ).andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Subject in the database
        val subjectList = subjectRepository.findAll()
        Assertions.assertThat(subjectList).hasSize(databaseSizeBeforeUpdate + 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteSubject() {
        // Initialize the database
        val subjectDto = subjectService.createSubject(SubjectServiceTest.createEntityDTO())
        val databaseSizeBeforeDelete = subjectRepository.findAll().size

        // Get the subject
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.delete("/api/subjects/{login}", subjectDto!!.login)
                .accept(TestUtil.APPLICATION_JSON_UTF8)
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // Validate the database is empty
        val subjectList = subjectRepository.findAll()
        Assertions.assertThat(subjectList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun equalsVerifier() {
        assertTrue(TestUtil.equalsVerifier(Subject::class.java))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun dynamicSourceRegistrationWithId() {
        val databaseSizeBeforeCreate = subjectRepository.findAll().size

        // Create the Subject
        val subjectDto: SubjectDTO = SubjectServiceTest.createEntityDTO()
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto))
        ).andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Subject in the database
        val subjectList = subjectRepository.findAll()
        Assertions.assertThat(subjectList).hasSize(databaseSizeBeforeCreate + 1)
        val testSubject = subjectList[subjectList.size - 1]
        val subjectLogin = testSubject.user!!.login
        assertNotNull(subjectLogin)

        // Create a source description
        val sourceRegistrationDto = createSourceWithSourceTypeId()
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects/{login}/sources", subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto))
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.sourceId").isNotEmpty())

        // A source can not be assigned twice to a subject, so this call must fail
        Assertions.assertThat(sourceRegistrationDto.sourceId).isNull()
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects/{login}/sources", subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto))
        ).andExpect(MockMvcResultMatchers.status().is4xxClientError())
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun dynamicSourceRegistrationWithoutId() {
        val databaseSizeBeforeCreate = subjectRepository.findAll().size

        // Create the Subject
        val subjectDto: SubjectDTO = SubjectServiceTest.createEntityDTO()
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto))
        ).andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Subject in the database
        val subjectList = subjectRepository.findAll()
        Assertions.assertThat(subjectList).hasSize(databaseSizeBeforeCreate + 1)
        val testSubject = subjectList[subjectList.size - 1]
        val subjectLogin = testSubject.user!!.login
        assertNotNull(subjectLogin)

        // Create a source description
        val sourceRegistrationDto = createSourceWithoutSourceTypeId()
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects/{login}/sources", subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto))
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.sourceId").isNotEmpty())

        // A source can not be assigned twice to a subject, so this call must fail
        Assertions.assertThat(sourceRegistrationDto.sourceId).isNull()
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects/{login}/sources", subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto))
        ).andExpect(MockMvcResultMatchers.status().is4xxClientError())

        // Get all the subjectList
        restSubjectMockMvc.perform(
                MockMvcRequestBuilders.get(
                    "/api/subjects/{login}/sources?sort=id,desc",
                    subjectDto.login
                )
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[*].id").isNotEmpty())
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun dynamicSourceRegistrationWithoutDynamicRegistrationFlag() {
        val databaseSizeBeforeCreate = subjectRepository.findAll().size

        // Create the Subject
        val subjectDto: SubjectDTO = SubjectServiceTest.createEntityDTO()
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto))
        ).andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Subject in the database
        val subjectList = subjectRepository.findAll()
        Assertions.assertThat(subjectList).hasSize(databaseSizeBeforeCreate + 1)
        val testSubject = subjectList[subjectList.size - 1]
        val subjectLogin = testSubject.user!!.login
        assertNotNull(subjectLogin)

        // Create a source description
        val sourceRegistrationDto = createSourceWithoutSourceTypeIdAndWithoutDynamicRegistration()
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects/{login}/sources", subjectLogin)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto))
        ).andExpect(MockMvcResultMatchers.status().is4xxClientError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").isNotEmpty())
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun subjectSources() {
        // Initialize the database
        val subjectDtoToCreate: SubjectDTO = SubjectServiceTest.createEntityDTO()
        val createdSource = sourceService.save(createSource())
        val sourceDto = MinimalSourceDetailsDTO().id(createdSource.id).sourceName(createdSource.sourceName)
            .sourceTypeId(createdSource.sourceType?.id).sourceId(createdSource.sourceId!!)
        subjectDtoToCreate.sources = setOf(sourceDto)
        assertNotNull(sourceDto.id)
        val createdSubject = subjectService.createSubject(subjectDtoToCreate)
        assertFalse(createdSubject!!.sources.isEmpty())

        // Get the subject
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/subjects/{login}/sources", createdSubject.login
            )
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(createdSource.id!!.toInt())).andExpect(
                MockMvcResultMatchers.jsonPath("$.[0].sourceId").value(createdSource.sourceId.toString())
            )
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun subjectSourcesWithQueryParam() {
        // Initialize the database
        val subjectDtoToCreate: SubjectDTO = SubjectServiceTest.createEntityDTO()
        val createdSource = sourceService.save(createSource())
        val sourceDto = MinimalSourceDetailsDTO().id(createdSource.id).sourceName(createdSource.sourceName)
            .sourceTypeId(createdSource.sourceType?.id).sourceId(createdSource.sourceId!!)
        subjectDtoToCreate.sources = setOf(sourceDto)
        assertNotNull(sourceDto.id)
        val createdSubject = subjectService.createSubject(subjectDtoToCreate)
        TestUtil.commitTransactionAndStartNew()
        assertNotNull(createdSubject!!.login)
        // Get the subject
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/subjects/{login}/sources?withInactiveSources=true", createdSubject.login
            )
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[*].id").value(createdSource.id!!.toInt())).andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].sourceId").value(createdSource.sourceId.toString())
            )
    }

    @Throws(Exception::class)
    @Transactional
    @Test
    fun inactiveSubjectSourcesWithQueryParam() {
        // Initialize the database
        val subjectDtoToCreate: SubjectDTO = SubjectServiceTest.createEntityDTO()
        val createdSource = sourceService.save(createSource())
        val sourceDto = MinimalSourceDetailsDTO().id(createdSource.id).sourceName(createdSource.sourceName)
            .sourceTypeId(createdSource.sourceType?.id).sourceId(createdSource.sourceId!!)
        subjectDtoToCreate.sources = setOf(sourceDto)
        assertNotNull(sourceDto.id)
        val createdSubject = subjectService.createSubject(subjectDtoToCreate)
        TestUtil.commitTransactionAndStartNew()
        createdSubject!!.sources = emptySet()
        val updatedSubject = subjectService.updateSubject(createdSubject)
        TestUtil.commitTransactionAndStartNew()
        assertNotNull(updatedSubject!!.login)
        // Get the subject
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.get(
                "/api/subjects/{login}/sources?withInactiveSources=true", updatedSubject.login
            )
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[*].id").value(createdSource.id!!.toInt())).andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].sourceId").value(createdSource.sourceId.toString())
            )

        // Get the subject
        restSubjectMockMvc.perform(
                MockMvcRequestBuilders.get(
                    "/api/subjects/{login}/sources?withInactiveSources=false", updatedSubject.login
                )
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)).andReturn()
    }

    private fun createSource(): SourceDTO {
        val sourceDto = SourceDTO()
        sourceDto.assigned = false
        sourceDto.sourceId = UUID.randomUUID()
        sourceDto.sourceType = sourceTypeService.findAll()[0]
        sourceDto.sourceName = "something" + UUID.randomUUID()
        return sourceDto
    }

    private fun createSourceWithSourceTypeId(): MinimalSourceDetailsDTO {
        val sourceTypes =
            sourceTypeService.findAll().stream().filter { obj: SourceTypeDTO -> obj.canRegisterDynamically }
                .collect(Collectors.toList())
        Assertions.assertThat(sourceTypes.size).isPositive()
        val sourceType = sourceTypes[0]
        return source.sourceTypeId(sourceType.id)
    }

    private fun createSourceWithoutSourceTypeId(): MinimalSourceDetailsDTO {
        val sourceTypes =
            sourceTypeService.findAll().stream().filter { obj: SourceTypeDTO -> obj.canRegisterDynamically }
                .collect(Collectors.toList())
        Assertions.assertThat(sourceTypes.size).isPositive()
        val sourceType = sourceTypes[0]
        return source.sourceTypeCatalogVersion(sourceType.catalogVersion).sourceTypeModel(sourceType.model)
            .sourceTypeProducer(sourceType.producer)
    }

    private fun createSourceWithoutSourceTypeIdAndWithoutDynamicRegistration(): MinimalSourceDetailsDTO {
        val sourceTypes =
            sourceTypeService.findAll().filter { it: SourceTypeDTO -> !it.canRegisterDynamically }
        Assertions.assertThat(sourceTypes.size).isPositive()
        val sourceType = sourceTypes[0]
        return source.sourceTypeCatalogVersion(sourceType.catalogVersion).sourceTypeModel(sourceType.model)
            .sourceTypeProducer(sourceType.producer)
    }

    private val source: MinimalSourceDetailsDTO
        get() {
            val sourceRegistrationDto =
                MinimalSourceDetailsDTO().sourceName(SubjectServiceTest.PRODUCER + "-" + SubjectServiceTest.MODEL)
                    .attributes(Collections.singletonMap("something", "value"))
            Assertions.assertThat(sourceRegistrationDto.sourceId).isNull()
            return sourceRegistrationDto
        }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testDynamicRegistrationAndUpdateSourceAttributes() {
        val databaseSizeBeforeCreate = subjectRepository.findAll().size

        // Create the Subject
        val subjectDto: SubjectDTO = SubjectServiceTest.createEntityDTO()
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post("/api/subjects").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subjectDto))
        ).andExpect(MockMvcResultMatchers.status().isCreated())

        // Validate the Subject in the database
        val subjectList = subjectRepository.findAll()
        Assertions.assertThat(subjectList).hasSize(databaseSizeBeforeCreate + 1)
        val testSubject = subjectList[subjectList.size - 1]
        val subjectLogin = testSubject.user!!.login
        assertNotNull(subjectLogin)

        // Create a source description
        val sourceRegistrationDto = createSourceWithoutSourceTypeId()
        val result = restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post(
                "/api/subjects/{login}/sources", subjectLogin
            ).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(sourceRegistrationDto))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        val value = TestUtil.convertJsonStringToObject(
            result.response.contentAsString, MinimalSourceDetailsDTO::class.java
        ) as MinimalSourceDetailsDTO
        assertNotNull(value.sourceName)
        val attributes: MutableMap<String, String> = HashMap()
        attributes["TEST_KEY"] = "Value"
        attributes["ANDROID_VERSION"] = "something"
        attributes["Other"] = "test"
        restSubjectMockMvc.perform(
            MockMvcRequestBuilders.post(
                "/api/subjects/{login}/sources/{sourceName}", subjectLogin, value.sourceName
            ).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(attributes))
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.attributes").isNotEmpty())
    }
}
