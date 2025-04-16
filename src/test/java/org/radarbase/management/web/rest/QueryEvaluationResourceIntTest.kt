package org.radarbase.management.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.radarbase.auth.authentication.OAuthHelper
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.*
import org.radarbase.management.domain.enumeration.*
import org.radarbase.management.repository.*
import org.radarbase.management.service.DataPoint
import org.radarbase.management.service.PasswordService
import org.radarbase.management.service.UserData
import org.radarbase.management.service.UserServiceIntTest
import org.radarbase.management.web.rest.errors.ExceptionTranslator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
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
import java.lang.Boolean
import java.time.ZonedDateTime
import javax.servlet.ServletException
import kotlin.Exception
import kotlin.String
import kotlin.Throws
import kotlin.to
import com.fasterxml.jackson.core.type.TypeReference
/**
 * Test class for the ProjectResource REST controller.
 *
 * @see ProjectResource
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@WithMockUser
internal class QueryEvaluationResourceIntTest(
    @Autowired private val queryResource: QueryResource,

    @Autowired private val projectRepository: ProjectRepository,

    @Autowired private val queryRepository: QueryRepository,
    @Autowired private val queryGroupRepository: QueryGroupRepository,
    @Autowired private val queryLogicRepository: QueryLogicRepository,
    @Autowired private val queryEvaluationRepository: QueryEvaluationRepository,
    @Autowired private val queryParticipantRepository: QueryParticipantRepository,

    @Autowired private val userRepository: UserRepository ,
    @Autowired private val subjectRepository: SubjectRepository,

    @Autowired private val pageableArgumentResolver: PageableHandlerMethodArgumentResolver,
    @Autowired private val jacksonMessageConverter: MappingJackson2HttpMessageConverter,
    @Autowired private val exceptionTranslator: ExceptionTranslator,
    @Autowired private val passwordService: PasswordService,
    ) {
    private lateinit var restQueryMockMvc: MockMvc
    private lateinit var user: User


    @BeforeEach
    @Throws(ServletException::class)
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val filter = OAuthHelper.createAuthenticationFilter()
        filter.init(MockFilterConfig())
        restQueryMockMvc = MockMvcBuilders.standaloneSetup(queryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .addFilter<StandaloneMockMvcBuilder>(filter)
            .defaultRequest<StandaloneMockMvcBuilder>(MockMvcRequestBuilders.get("/query-builder").with(OAuthHelper.bearerToken()))
            .build()
    }

    @BeforeEach
    fun initTest() {
        runBlocking {
            user = UserServiceIntTest.createEntity(passwordService)
        }
    }



    fun createQueryGroup(): QueryGroup {
        val user = userRepository.findAll()[0];

        val queryGroup = QueryGroup();
        queryGroup.name = "TestQueryGroup"
        queryGroup.description = "description"
        queryGroup.createdBy = user;
        queryGroup.createdDate = ZonedDateTime.now();

        return queryGroupRepository.saveAndFlush(queryGroup) ;
    }

    fun createQuery(queryGroup: QueryGroup, queryMetric: QueryMetric, queryOperator: ComparisonOperator, timeframe: QueryTimeFrame, value: String)  : Query {
        var query = Query();

        query.queryGroup = queryGroup
        query.queryMetric = queryMetric
        query.comparisonOperator = queryOperator
        query.value = value
        query.timeFrame = timeframe

        return queryRepository.saveAndFlush(query);
    }


    fun createQueryLogic(queryGroup: QueryGroup, type: QueryLogicType, logicOperator: QueryLogicOperator?,  query:Query?, parentQueryLogic : QueryLogic? ) : QueryLogic {
        val queryLogic = QueryLogic() ;
        queryLogic.queryGroup = queryGroup
        queryLogic.type = type
        queryLogic.logicOperator = logicOperator
        queryLogic.query = query;
        queryLogic.parent = parentQueryLogic

        return queryLogicRepository.saveAndFlush(queryLogic)
    }

    fun createQueryParticipantAssignment(queryGroup: QueryGroup) {
        val user = userRepository.findAll()[0];
        val subject = subjectRepository.findAll()[0];

        val queryParticipant = QueryParticipant();
        queryParticipant.queryGroup = queryGroup;
        queryParticipant.createdBy = user;
        queryParticipant.subject = subject;
        queryParticipant.createdDate = ZonedDateTime.now();

        queryParticipantRepository.saveAndFlush(queryParticipant);
    }

    fun convertStringToBoolean(content: String, queryGroupName: String) : kotlin.Boolean {
        val objectMapper = ObjectMapper()
        val resultMap: MutableMap<String, Boolean> = objectMapper.readValue(
            content,
            object : TypeReference<MutableMap<String, Boolean>>() {}
        )
        val result = Boolean.parseBoolean(resultMap[queryGroupName]!!.toString())
        return result;
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun evaluateQuery() {

       var queryEvaluationSize = queryEvaluationRepository.findAll().size;

        var subject = subjectRepository.findAll()[0]

        val queryGroup = createQueryGroup();

        val query = createQuery(queryGroup, QueryMetric.HEART_RATE, ComparisonOperator.EQUALS, QueryTimeFrame.PAST_6_MONTH, "55");
        val query1 = createQuery(queryGroup, QueryMetric.SLEEP_LENGTH, ComparisonOperator.EQUALS, QueryTimeFrame.PAST_6_MONTH, "8");

        val parentQueryLogic = createQueryLogic(queryGroup,QueryLogicType.LOGIC, QueryLogicOperator.AND, null,null);
        createQueryLogic(queryGroup,QueryLogicType.CONDITION, null, query,parentQueryLogic);
        createQueryLogic(queryGroup,QueryLogicType.CONDITION, null, query1,parentQueryLogic);


        // create participant assignemnt

        createQueryParticipantAssignment(queryGroup);


        var userData = UserData(
            metrics = mapOf(
                "HEART_RATE" to listOf(
                    DataPoint("2024-01", 55.0),
                    DataPoint("2024-02", 55.0),
                    DataPoint("2024-03", 55.0),
                    DataPoint("2024-04", 55.0),
                    DataPoint("2024-05", 55.0)
                ),
                "SLEEP_LENGTH" to listOf(
                    DataPoint("2024-01", 7.0),
                    DataPoint("2024-02", 8.0),
                    DataPoint("2024-03", 8.0),
                    DataPoint("2024-04", 8.0),
                    DataPoint("2024-05", 8.0)
                ),
                "HRV" to listOf(
                    DataPoint("2024-01", 50.0),
                    DataPoint("2024-02", 45.0),
                    DataPoint("2024-03", 47.0),
                    DataPoint("2024-04", 45.0),
                    DataPoint("2024-05", 42.0)
                )
            )
        )


        val returnValue = restQueryMockMvc.perform(
            MockMvcRequestBuilders.post("/api/query-builder/evaluate/" + subject.id)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userData))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val content: String = returnValue.getResponse().getContentAsString()

        val result = convertStringToBoolean(content, queryGroup.name!!)

        assertFalse(result)

        var queryEvaluationNewSize = queryEvaluationRepository.findAll().size;
       assertEquals(queryEvaluationSize + 1, queryEvaluationNewSize)


       var queryEvaluation =   queryEvaluationRepository.findAll()[0]
        assertEquals(false, queryEvaluation.result)


        userData = UserData(
            metrics = mapOf(
                "HEART_RATE" to listOf(
                    DataPoint("2024-01", 55.0),
                    DataPoint("2024-02", 55.0),
                    DataPoint("2024-03", 55.0),
                    DataPoint("2024-04", 55.0),
                    DataPoint("2024-05", 55.0)
                ),
                "SLEEP_LENGTH" to listOf(
                    DataPoint("2024-01", 8.0),
                    DataPoint("2024-02", 8.0),
                    DataPoint("2024-03", 8.0),
                    DataPoint("2024-04", 8.0),
                    DataPoint("2024-05", 8.0)
                ),
                "HRV" to listOf(
                    DataPoint("2024-01", 50.0),
                    DataPoint("2024-02", 45.0),
                    DataPoint("2024-03", 47.0),
                    DataPoint("2024-04", 45.0),
                    DataPoint("2024-05", 42.0)
                )
            )
        )

        val returnValue1 = restQueryMockMvc.perform(
            MockMvcRequestBuilders.post("/api/query-builder/evaluate/" + subject.id)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userData))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val content1: String = returnValue1.getResponse().getContentAsString()

        val result1 = convertStringToBoolean(content1, queryGroup.name!!)

        assertTrue(result1)

         queryEvaluationNewSize = queryEvaluationRepository.findAll().size;
        assertEquals(queryEvaluationSize + 1, queryEvaluationNewSize)


         queryEvaluation =   queryEvaluationRepository.findAll()[0]
        assertEquals(true, queryEvaluation.result)


    }

}
