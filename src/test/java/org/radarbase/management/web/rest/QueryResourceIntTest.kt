package org.radarbase.management.web.rest


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.auth.token.RadarToken
import org.radarbase.management.config.BasePostgresIntegrationTest
import org.radarbase.management.domain.*
import org.radarbase.management.domain.enumeration.*
import org.radarbase.management.repository.*
import org.radarbase.management.security.RadarAuthentication
import org.radarbase.management.service.*
import org.radarbase.management.service.dto.*
import org.radarbase.management.web.rest.errors.ExceptionTranslator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockFilterConfig
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import javax.servlet.ServletException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue


internal class QueryResourceIntTest(
    @Autowired private val queryResource : QueryResource,
    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val exceptionTranslator: ExceptionTranslator,
    @Autowired private val radarToken: RadarToken,
    @Autowired private val passwordService: PasswordService,
    @Autowired private val queryRepository: QueryRepository,
    @Autowired private val queryGroupRepository: QueryGroupRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val subjectRepository: SubjectRepository,
    @Autowired private val queryParticipantRepository: QueryParticipantRepository,
    @Autowired private val queryLogicRepository: QueryLogicRepository,
    @Autowired private val queryEvaluationRepository: QueryEvaluationRepository,
    @Autowired private val queryContentRepository: QueryContentRepository,
    @Autowired private val queryContentService: QueryContentService,
    @Autowired private val queryParticipantContentRepository: QueryParticipantContentRepository,
    @Autowired private val queryContentGroupRepository: QueryContentGroupRepository

    ) : BasePostgresIntegrationTest() {

    private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper()
    @Autowired private lateinit var mockUserService: UserService
    @Autowired private  lateinit  var queryBuilderService: QueryBuilderService

    private val imageBlob = "/9j/4AAQSkZJRgABAQAAAQABAAD/4gHYSUNDX1BST0ZJTEUAAQEAAAHIAAAAAAQwAABtbnRyUkdCIFhZWiAH4AABAAEAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAACRyWFlaAAABFAAAABRnWFlaAAABKAAAABRiWFlaAAABPAAAABR3dHB0AAABUAAAABRyVFJDAAABZAAAAChnVFJDAAABZAAAAChiVFJDAAABZAAAAChjcHJ0AAABjAAAADxtbHVjAAAAAAAAAAEAAAAMZW5VUwAAAAgAAAAcAHMAUgBHAEJYWVogAAAAAAAAb6IAADj1AAADkFhZWiAAAAAAAABimQAAt4UAABjaWFlaIAAAAAAAACSgAAAPhAAAts9YWVogAAAAAAAA9tYAAQAAAADTLXBhcmEAAAAAAAQAAAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABtbHVjAAAAAAAAAAEAAAAMZW5VUwAAACAAAAAcAEcAbwBvAGcAbABlACAASQBuAGMALgAgADIAMAAxADb/2wBDAAoHBwgHBgoICAgLCgoLDhgQDg0NDh0VFhEYIx8lJCIfIiEmKzcvJik0KSEiMEExNDk7Pj4+JS5ESUM8SDc9Pjv/2wBDAQoLCw4NDhwQEBw7KCIoOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozv/wAARCAAUABQDASIAAhEBAxEB/8QAGgABAAIDAQAAAAAAAAAAAAAAAAMEAQIFB//EACQQAAICAgEDBAMAAAAAAAAAAAECAwQAIRESMUEFExQiUVJh/8QAFwEAAwEAAAAAAAAAAAAAAAAAAAECA//EABkRAQEBAAMAAAAAAAAAAAAAAAABESExQf/aAAwDAQACEQMRAD8A9hsCc15BWZFm6foZASoP94ytV9Qa1ZWOONXi9kO8wfjhidAL3IOzz21klupWlb5EyMWSNkBVyD0nuNHzlCG6ayWbl1pBHS5id2hKAKACXH7D8ka1oDfNeM7cvNx2cZpFIk0SSxsGR1DKw8g9sZLRHap17qIliISLHIsigk6ZTyDkxAIII5B8YxgWTtnGMYG//9k="

    private val baseURL = "/api/query-builder/"

    private lateinit var user: User

    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        mockUserService = mock()
        queryBuilderService = mock()


        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())

        SecurityContextHolder.getContext().authentication = RadarAuthentication(radarToken)


//        val queryResource = QueryResource(queryBuilderService, mockUserService)


        mockMvc = MockMvcBuilders.standaloneSetup(queryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter<StandaloneMockMvcBuilder>(filter)
            .defaultRequest<StandaloneMockMvcBuilder>(MockMvcRequestBuilders.get("/").with(OAuthHelper.bearerToken()))
            .build()
    }



    private fun createQueryGroup(user: User) : QueryGroup {
        val queryGroup = QueryGroup()

        queryGroup.name = "Name"
        queryGroup.description = "desc"
        queryGroup.createdDate = ZonedDateTime.now();
        queryGroup.createdBy = user;

        return queryGroup;
    }


    private fun createQuery(queryGroup: QueryGroup) : Query {
        val query = Query()
        query.field = QueryMetric.SLEEP_LENGTH.toString()
        query.queryGroup = queryGroup
        query.operator = ComparisonOperator.LESS_THAN_OR_EQUALS
        query.timeFrame = QueryTimeFrame.LAST_7_DAYS
        query.entity = "physical"
        query.value = "80"

        return query;
    }


    private fun createAndAddQueryGroupToDB() : QueryGroup {
        val user = userRepository.findAll()[0]
        val queryGroup = createQueryGroup(user);

        return queryGroupRepository.saveAndFlush(queryGroup)
    }



    private fun createAndAddQueryToDB() : Query {
        val queryGroup = createAndAddQueryGroupToDB()

        val query = createQuery(queryGroup)

        return queryRepository.saveAndFlush(query);
    }

    private fun createAndAddQueryParticipantToDB() : QueryParticipant {
        val queryGroup = createAndAddQueryGroupToDB()
        val subject = subjectRepository.findAll()[0]
        val user = userRepository.findAll()[0]

        val queryParticipant = QueryParticipant();
        queryParticipant.queryGroup = queryGroup;
        queryParticipant.subject = subject;
        queryParticipant.createdDate = ZonedDateTime.now()
        queryParticipant.createdBy = user;

        return queryParticipantRepository.saveAndFlush(queryParticipant);
    }


    private fun addContent(queryGroup: QueryGroup, subject: Subject) : QueryContentGroup  {
        var contentGroup = QueryContentGroup()

        contentGroup.queryGroup = queryGroup
        contentGroup.contentGroupName = "Content Group Name"
        contentGroup.createdDate = ZonedDateTime.now()
        contentGroup.updatedDate = ZonedDateTime.now()
        contentGroup = queryContentGroupRepository.saveAndFlush(contentGroup);

        var content =  QueryContent()
        content.queryGroup = queryGroup
        content.queryContentGroup = contentGroup
        content.value = "value"
        content.type = ContentType.PARAGRAPH
        content.heading = "heading1"
        content = queryContentRepository.saveAndFlush(content)

        val participantContent = QueryParticipantContent()
        participantContent.queryGroup = queryGroup
        participantContent.queryContentGroup = contentGroup
        participantContent.subject = subject
        participantContent.createdDate = ZonedDateTime.now()
        participantContent.isArchived = false;

        queryParticipantContentRepository.saveAndFlush(participantContent)

        return contentGroup
    }



    @BeforeEach
    fun initTest() {
        runBlocking {
            user = UserServiceIntTest.createEntity(passwordService)
        }
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun shouldSaveQueryLogic() {
        val queryGroup = createAndAddQueryGroupToDB()
        val queryLogicParentDTO = QueryLogicDTO()

        val queryDTO = QueryDTO(QueryMetric.SLEEP_LENGTH.toString(), ComparisonOperator.LESS_THAN_OR_EQUALS, "80", QueryTimeFrame.LAST_7_DAYS, "domain")
        queryDTO.value = "80"
        queryDTO.timeFrame = QueryTimeFrame.LAST_7_DAYS
        queryDTO.operator = ComparisonOperator.LESS_THAN_OR_EQUALS

        queryLogicParentDTO.queryGroupId = queryGroup.id
        queryLogicParentDTO.logic_operator = QueryLogicOperator.AND

        val queryLogicChild = QueryLogicDTO();
        queryLogicChild.query = queryDTO


        queryLogicParentDTO.children = mutableListOf(queryLogicChild)


        val json = objectMapper.writeValueAsString(queryLogicParentDTO)

        mockMvc.perform(post(baseURL + "querylogic")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(json)))
            .andExpect(status().isOk)

        val querySizeAfter = queryRepository.findAll().size
        val queryLogicSizeAfter = queryLogicRepository.findAll().size

        Assertions.assertThat(queryLogicSizeAfter).isEqualTo(2)
        Assertions.assertThat(querySizeAfter).isEqualTo(1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun shouldCreateQueryGroup() {
        whenever(mockUserService.getUserWithAuthorities()).doReturn(user)

        val queryGroupDTO = QueryGroupDTO()
        queryGroupDTO.name = "Name"
        queryGroupDTO.description = "desc"
        val json = objectMapper.writeValueAsString(queryGroupDTO)

        mockMvc.perform(post(baseURL + "querygroups")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(json)))
            .andExpect(status().isOk)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun shouldGetQueryList() {

        createAndAddQueryToDB();

        val returnValue = mockMvc.perform(get("/api/query-builder/queries"))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1)).andReturn()

        val content: String = returnValue.getResponse().getContentAsString()


        val objectMapper = jacksonObjectMapper()

        val queryList: MutableList<Query> = objectMapper.readValue(content)

        val query = queryList[0]

        Assertions.assertThat(query.field).isEqualTo("SLEEP_LENGTH")
        Assertions.assertThat(query.value).isEqualTo("80")
        Assertions.assertThat(query.timeFrame).isEqualTo(QueryTimeFrame.LAST_7_DAYS)
        Assertions.assertThat(query.operator).isEqualTo(ComparisonOperator.LESS_THAN_OR_EQUALS)

    }




    @Test
    @Transactional
    @Throws(Exception::class)
    fun shouldGetQueryGroupList() {
    whenever(mockUserService.getUserWithAuthorities()).doReturn(user)
        val existingUser = userRepository.findAll()[0];

        val queryGroup = createQueryGroup(existingUser);
        queryGroup.id = queryGroupRepository.saveAndFlush(queryGroup).id;



    mockMvc.perform(get(baseURL + "querygroups"))
        .andExpect(status().isOk)
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.[*].name").value<Iterable<String?>>(
                Matchers.hasItem(
                    "Name"
                )
            )
        )
}

    @Test
    @Transactional
    @Throws(Exception::class)
    fun shouldDeleteQueryGroupAndAllRelatedEntities() {
        val query = createAndAddQueryToDB();

        mockMvc.perform(delete(baseURL + "querygroups/" + query.queryGroup?.id))
            .andExpect(status().isOk)

    }
//
@Test
@Transactional
@Throws(Exception::class)
    fun shouldAssignQueryGroupToParticipant() {
        val queryGroup = createAndAddQueryGroupToDB()
        val subject = subjectRepository.findAll()[0]

        val dto = QueryParticipantDTO()
        dto.queryGroupId = queryGroup.id
        dto.subjectId = subject.id

        val json = objectMapper.writeValueAsString(dto)

        mockMvc.perform(post(baseURL + "queryparticipant")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(json)))
            .andExpect(status().isOk)


        val allQueryParticipantRows  = queryParticipantRepository.findAll()
        val queryParticipantRow = allQueryParticipantRows[0]

        Assertions.assertThat(allQueryParticipantRows.size).isEqualTo(1)
        Assertions.assertThat(queryParticipantRow.queryGroup?.id).isEqualTo(queryGroup.id)
        Assertions.assertThat(queryParticipantRow.subject?.id).isEqualTo(subject.id)
}
//
@Test
@Transactional
@Throws(Exception::class)
    fun shouldGetAssignedQueriesBySubject() {
        val queryParticipant = createAndAddQueryParticipantToDB();

        mockMvc.perform(get(baseURL + "querygroups/subject/" + queryParticipant.subject?.id))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].name").value<Iterable<String?>>(
                    Matchers.hasItem(
                        "Name"
                    )
                )
            )
}


    @Test
    @Transactional
    @Throws(Exception::class)
    fun shouldDeleteAssignedQueryGroup() {
        val queryParticipant = createAndAddQueryParticipantToDB();

        val queryGroupId = queryParticipant.queryGroup?.id
        val subjectId = queryParticipant.subject?.id


        mockMvc.perform(delete(baseURL + "querygroups/" + queryGroupId + "/subject/" + subjectId))
            .andExpect(status().isOk)

        Assertions.assertThat(queryParticipantRepository.findAll().size).isEqualTo(0)
    }

    @Test
    @Transactional
    fun shouldReturnContentItems() {
        val queryParticipant = createAndAddQueryParticipantToDB()
        val subject = queryParticipant.subject!!
        val queryGroup = queryParticipant.queryGroup!!

        val evaluation = QueryEvaluation().apply {
            this.subject = subject
            this.queryGroup = queryGroup
            this.result = true
            this.createdDate = ZonedDateTime.now()
        }
        queryEvaluationRepository.saveAndFlush(evaluation)

        val contentGroup = addContent(queryGroup, subject)

        mockMvc.perform(get("/api/query-builder/content-groups/${contentGroup.id}/participants/${subject.id}/content"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].value").value<Iterable<String?>>(
                    Matchers.hasItem(
                        "value"
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].value").value<Iterable<String?>>(
                    Matchers.hasItem(
                        "value"
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].heading").value<Iterable<String?>>(
                    Matchers.hasItem(
                        "heading1"
                    )
                )
            )

    }


    @Test
    @Transactional
    fun shouldReturnContentGroupsMappedByQueryGroups() {
        val queryParticipant = createAndAddQueryParticipantToDB()
        val subject = queryParticipant.subject!!
        val queryGroup = queryParticipant.queryGroup!!

        val evaluation = QueryEvaluation().apply {
            this.subject = subject
            this.queryGroup = queryGroup
            this.result = true
            this.createdDate = ZonedDateTime.now()
        }
        queryEvaluationRepository.saveAndFlush(evaluation)

        val contentGroup = addContent(queryGroup, subject)
        addContent(queryGroup, subject)

        val returnValue = mockMvc.perform(get("/api/query-builder/participants/${subject.id}/content-groups"))
            .andExpect(status().isOk).andReturn()

        val content: String = returnValue.getResponse().getContentAsString()
        val objectMapper = jacksonObjectMapper()
        val queryList: Map<String, List<QueryContentGroupDTO>>  = objectMapper.readValue(content)

        Assertions.assertThat(queryList.keys.size).isEqualTo(1)
        Assertions.assertThat(queryList[queryGroup.name]!!.size).isEqualTo(2)


        val item1 = queryList[queryGroup.name]!![0]
        Assertions.assertThat(item1.contentGroupName).isEqualTo(contentGroup.contentGroupName)
    }

    fun createContentGroup(name: String, queryGroup: QueryGroup): QueryContentGroup {
        return QueryContentGroup().apply {
            contentGroupName = name
            this.queryGroup = queryGroup
            createdDate = ZonedDateTime.now()
            updatedDate = ZonedDateTime.now()
        }.also {
            queryContentGroupRepository.save(it)
        }
    }

    fun createQueryContentGroupDTO(queryGroup: QueryGroup, heading: String, paragraph: String,videoLink: String,title: String): QueryContentGroupDTO{
        var blob = imageBlob
        var mockContentGroup = createContentGroup("Test Content Group", queryGroup)
        val mockDTO = QueryContentGroupDTO().apply {
            contentGroupName = mockContentGroup.contentGroupName
            queryGroupId = queryGroup.id
            id = mockContentGroup.id
            queryContentDTOList = listOf(
                QueryContentDTO().apply {
                    type = ContentType.TITLE
                    value = title
                    queryGroupId = queryGroup.id
                    queryContentGroupId = mockContentGroup.id
                },
                QueryContentDTO().apply {
                    type = ContentType.PARAGRAPH
                    value = paragraph
                    queryGroupId = queryGroup.id
                    queryContentGroupId = mockContentGroup.id
                },
                QueryContentDTO().apply {
                    type = ContentType.IMAGE
                    imageBlob = blob
                    queryGroupId = queryGroup.id
                    queryContentGroupId = mockContentGroup.id
                },
                QueryContentDTO().apply {
                    type = ContentType.HEADING
                    value = heading
                    queryGroupId = queryGroup.id
                    queryContentGroupId = mockContentGroup.id
                }
            )
        }
        return mockDTO;
    }

    @Test
    @Transactional
    fun shouldRetrieveQueryContent() {
        val heading = "heading";
        val paragraph = "this is a paragraph"
        val videoLink = "video-link"
        val title = "this is a title"


        val queryParticipant = createAndAddQueryParticipantToDB()
        val queryGroup = queryParticipant.queryGroup!!

        val contentGroup = createContentGroup("Test content group", queryGroup)

        var queryContentList : List<QueryContent> = mutableListOf();

        val paragraphContent = QueryContent();
        paragraphContent.queryGroup = queryGroup
        paragraphContent.type = ContentType.PARAGRAPH;
        paragraphContent.heading = heading
        paragraphContent.value = paragraph
        paragraphContent.queryContentGroup = contentGroup

        queryContentRepository.save(paragraphContent);

        val videoContent = QueryContent();
        videoContent.queryGroup = queryGroup
        videoContent.type = ContentType.VIDEO
        videoContent.value = videoLink;
        videoContent.queryContentGroup = contentGroup
        queryContentRepository.save(videoContent);


        val imageContent = QueryContent();
        imageContent.queryGroup = queryGroup
        imageContent.type = ContentType.IMAGE;
        imageContent.imageBlob = queryContentService.convertImgStringToByteArray(imageBlob);
        imageContent.queryContentGroup = contentGroup
        queryContentRepository.save(imageContent);

        val titleContent = QueryContent();
        titleContent.queryGroup = queryGroup
        titleContent.type = ContentType.TITLE;
        titleContent.value = title;
        titleContent.queryContentGroup = contentGroup
        queryContentRepository.save(titleContent);


        val returnedValue = mockMvc.perform(get(baseURL + "querycontent/querygroup/${queryGroup.id}")
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andReturn()

        val mapper = ObjectMapper()


        val queryContentGroupDTOList: List<QueryContentGroupDTO> = mapper.readValue(
            returnedValue.getResponse().getContentAsByteArray(),
            object : TypeReference<List<QueryContentGroupDTO>>() {}
        )
//
       Assertions.assertThat(queryContentGroupDTOList[0].queryContentDTOList!!.size).isEqualTo(4)


        for(queryContentDTO in queryContentGroupDTOList[0].queryContentDTOList!!) {

            if(queryContentDTO.type == ContentType.PARAGRAPH) {
                queryContentDTO.value = paragraph
                queryContentDTO.heading = heading
            }

            if(queryContentDTO.type == ContentType.VIDEO) {
                queryContentDTO.value = videoLink
            }

            if(queryContentDTO.type == ContentType.TITLE) {
                queryContentDTO.value = title
            }


            if(queryContentDTO.type == ContentType.IMAGE) {
                queryContentDTO.imageBlob = imageBlob
            }
        }


    }
    @Test
    @Transactional
    fun shouldDeleteQueryEvaluationContent() {
        val queryParticipant = createAndAddQueryParticipantToDB()
        val queryGroup = queryParticipant.queryGroup!!
        val subject = queryParticipant.subject!!

        val evaluation = QueryEvaluation().apply {
            this.queryGroup = queryGroup
            this.subject = subject
            this.result = true
            this.createdDate = ZonedDateTime.now()
        }
        queryEvaluationRepository.saveAndFlush(evaluation)

        Assertions.assertThat(queryEvaluationRepository.findAll().size).isEqualTo(1)

        mockMvc.perform(
            delete(baseURL + "queryevaluation/querygroup/${queryGroup.id}/subject/${subject.id}")
        )
            .andExpect(status().isOk)

        Assertions.assertThat(
            queryEvaluationRepository.findAll().any {
                it.queryGroup?.id == queryGroup.id && it.subject?.id == subject.id
            }
        ).isFalse()
    }

    @Test
    @Transactional
    fun shouldSaveOrUpdateContentGroup() {
        val heading = "heading"
        val paragraph = "this is a paragraph"
        val videoLink = "video-link"
        val title = "this is a title"

        val sizeBefore = queryContentRepository.findAll().size

        val queryParticipant = createAndAddQueryParticipantToDB()
        val queryGroup = queryParticipant.queryGroup!!

        val contentGroupRequest = QueryContentGroupDTO().apply {
            contentGroupName = "Test Group"
            queryGroupId = queryGroup.id!!
            queryContentDTOList = listOf()
            id = null
        }

        val contentGroupResult = mockMvc.perform(
            post("$baseURL/querycontentgroup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentGroupRequest))
        )
            .andExpect(status().isOk)
            .andReturn()


        var mockQueryContentGroupDTO = createQueryContentGroupDTO(queryGroup, heading, paragraph,videoLink,title);


        mockMvc.perform(
            post("$baseURL/querycontentgroup/")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mockQueryContentGroupDTO))
        )
            .andExpect(status().isOk)

        val sizeAfter = queryContentRepository.findAll().size
        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 4)

        val newContentList = queryContentRepository.findAll()

        for (content in newContentList) {
            when (content.type) {
                ContentType.HEADING -> {
                    Assertions.assertThat(content.value).isEqualTo(heading)
                }
                ContentType.PARAGRAPH -> {
                    Assertions.assertThat(content.value).isEqualTo(paragraph)
                }
                ContentType.IMAGE -> {
                    Assertions.assertThat(content.imageBlob).isNotNull()
                }
                ContentType.VIDEO -> {
                    Assertions.assertThat(content.value).isEqualTo(videoLink)
                }
                ContentType.TITLE -> {
                    Assertions.assertThat(content.value).isEqualTo(title)
                }
                else -> {}
            }
        }
    }

    @Test
    @Transactional
    fun shouldDeleteContentGroup() {
        var mockContentGroup = createContentGroup("test", createAndAddQueryGroupToDB())
        var contentGroupID = mockContentGroup.id
        mockMvc.perform(delete(baseURL+"querycontentgroup/"+ contentGroupID))
            .andExpect(status().isOk)
        Assertions.assertThat(queryContentGroupRepository.findAll().size).isEqualTo(0)
    }


}
