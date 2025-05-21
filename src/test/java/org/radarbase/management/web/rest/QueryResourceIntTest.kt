package org.radarbase.management.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.radarbase.management.service.dto.QueryGroupDTO
import org.radarbase.management.service.dto.QueryLogicDTO
import org.radarbase.management.service.dto.QueryDTO

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.web.rest.errors.ExceptionTranslator

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockFilterConfig

import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.transaction.annotation.Transactional
import javax.servlet.ServletException
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.radarbase.auth.token.RadarToken
import org.radarbase.management.domain.*
import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryLogicOperator
import org.radarbase.management.domain.enumeration.QueryMetric
import org.radarbase.management.domain.enumeration.QueryTimeFrame
import org.radarbase.management.repository.*
import org.radarbase.management.security.RadarAuthentication
import org.radarbase.management.service.*

import org.radarbase.management.service.dto.QueryParticipantDTO
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.ZonedDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal class QueryResourceTest(
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
    @Autowired private val queryEvaluationRepository: QueryEvaluationRepository

    ) {

    private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper()
    @Autowired private lateinit var mockUserService: UserService
    @Autowired private  lateinit  var queryBuilderService: QueryBuilderService

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
        query.queryMetric = QueryMetric.SLEEP_LENGTH
        query.queryGroup = queryGroup
        query.comparisonOperator = ComparisonOperator.LESS_THAN_OR_EQUALS
        query.timeFrame = QueryTimeFrame.LAST_7_DAYS
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

        val queryDTO = QueryDTO(QueryMetric.SLEEP_LENGTH,ComparisonOperator.LESS_THAN_OR_EQUALS,"80", QueryTimeFrame.LAST_7_DAYS)
        queryDTO.metric = QueryMetric.SLEEP_LENGTH
        queryDTO.value = "80"
        queryDTO.time_frame = QueryTimeFrame.LAST_7_DAYS
        queryDTO.operator = ComparisonOperator.LESS_THAN_OR_EQUALS

        queryLogicParentDTO.queryGroupId = queryGroup.id
        queryLogicParentDTO.logic_operator = QueryLogicOperator.AND

        val queryLogicChild = QueryLogicDTO();
        queryLogicChild.query = queryDTO


        queryLogicParentDTO.children = mutableListOf(queryLogicChild)


        val json = objectMapper.writeValueAsString(queryLogicParentDTO)

        mockMvc.perform(post("/api/query-builder/querylogic")
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

        mockMvc.perform(post("/api/query-builder/querygroups")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(json)))
            .andExpect(status().isOk)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun shouldGetQueryList() {

        createAndAddQueryToDB();

        mockMvc.perform(get("/api/query-builder/queries"))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].value").value<Iterable<String?>>(
                    Matchers.hasItem(
                        "80"
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].queryMetric").value<Iterable<String?>>(
                    Matchers.hasItem(
                        "SLEEP_LENGTH"
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].comparisonOperator").value<Iterable<String?>>(
                    Matchers.hasItem(
                        "LESS_THAN_OR_EQUALS"
                    )
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.[*].timeFrame").value<Iterable<String?>>(
                    Matchers.hasItem(
                        "LAST_7_DAYS"
                    )
                )
            )
    }




    @Test
    @Transactional
    @Throws(Exception::class)
    fun shouldGetQueryGroupList() {
    whenever(mockUserService.getUserWithAuthorities()).doReturn(user)
        val existingUser = userRepository.findAll()[0];

        val queryGroup = createQueryGroup(existingUser);
        queryGroup.id = queryGroupRepository.saveAndFlush(queryGroup).id;



    mockMvc.perform(get("/api/query-builder/querygroups"))
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

        mockMvc.perform(delete("/api/query-builder/querygroups/" + query.queryGroup?.id))
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

        mockMvc.perform(post("/api/query-builder/queryparticipant")
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

        mockMvc.perform(get("/api/query-builder/querygroups/subject/" + queryParticipant.subject?.id))
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


        mockMvc.perform(delete("/api/query-builder/querygroups/" + queryGroupId + "/subject/" + subjectId))
            .andExpect(status().isOk)

        Assertions.assertThat(queryParticipantRepository.findAll().size).isEqualTo(0)
    }

    @Test
    @Transactional
    fun shouldReturnActiveQueries() {
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

        mockMvc.perform(get("/api/query-builder/active/${subject.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.${queryGroup.id}").exists())
    }


}
