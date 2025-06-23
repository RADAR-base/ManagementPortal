package org.radarbase.management.service


import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.config.BasePostgresIntegrationTest
import org.radarbase.management.domain.*
import org.radarbase.management.domain.enumeration.*
import org.radarbase.management.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random


/**
 * Test class for the SubjectService class.
 *
 * @see SubjectService
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@Transactional
class QueryEvaluationServiceTest(
    @Autowired private val queryEValuationService: QueryEValuationService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val subjectRepository: SubjectRepository,
    @Autowired private val queryGroupRepository: QueryGroupRepository,
    @Autowired private val queryParticipantRepository: QueryParticipantRepository,
    @Autowired private val queryEvaluationRepository: QueryEvaluationRepository,
    @Autowired private val queryRepository: QueryRepository

) : BasePostgresIntegrationTest() {
      lateinit var userData: UserData

    fun generateUserData(valueHeartRate: Double, valueSleep: Long, HRV: Long)  : UserData{
        val currentMonth = YearMonth.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")

        val heartRateData =
            (1L until 8L).map { monthsAgo ->
                val month = currentMonth.minusMonths(monthsAgo).format(formatter)
                val value = valueHeartRate
                DataPoint(month, value.toDouble())
            }.reversed()


        val sleepData =   (1L until 8L).map { monthsAgo ->
            val month = currentMonth.minusMonths(monthsAgo).format(formatter)
            val value = valueSleep
            DataPoint(month, value.toDouble())
        }.reversed()


        val HRV =   (1L until 8L ).map { monthsAgo ->
            val month = currentMonth.minusMonths(monthsAgo).format(formatter)
            val value = valueSleep
            DataPoint(month, value.toDouble())
        }.reversed()

        return UserData(
            metrics = mapOf(
                "HEART_RATE" to heartRateData,
                "SLEEP_LENGTH" to sleepData,
                "HRV" to HRV)
        )
    }
    @BeforeEach
    fun initTest() {
        userData = generateUserData(64.2,8, 50)
//         userData = UserData(
//            metrics = mapOf(
//                "HEART_RATE" to listOf(
//                    DataPoint("2024-01", 56.0),
//                    DataPoint("2024-02", 60.0),
//                    DataPoint("2024-03", 65.0),
//                    DataPoint("2024-04", 70.0),
//                    DataPoint("2024-05", 70.0)
//                ),
//                "SLEEP_LENGTH" to listOf(
//                    DataPoint("2024-01", 8.0),
//                    DataPoint("2024-02", 8.0),
//                    DataPoint("2024-03", 8.0),
//                    DataPoint("2024-04", 8.0),
//                    DataPoint("2024-05", 8.0)
//                ),
//                "HRV" to listOf(
//                    DataPoint("2024-01", 50.0),
//                    DataPoint("2024-02", 45.0),
//                    DataPoint("2024-03", 47.0),
//                    DataPoint("2024-04", 45.0),
//                    DataPoint("2024-05", 42.0)
//                )
//            )
//        )
    }
    fun createQueryGroup(): QueryGroup {
        val user = userRepository.findAll()[0];

        val queryGroup = QueryGroup();
        queryGroup.name = "TestQueryGroup"
        queryGroup.description = "description"
        queryGroup.createdBy = user;
        queryGroup.createdDate = ZonedDateTime.now();

        return queryGroup
    }

    fun createQuery(queryGroup: QueryGroup?, queryMetric: QueryMetric, queryOperator: ComparisonOperator, timeframe: QueryTimeFrame, value: String)  : Query {
        var query = Query();

        query.queryGroup = queryGroup
        query.field = queryMetric.toString()
        query.operator = queryOperator
        query.value = value
        query.timeFrame = timeframe

        return query
    }

    fun createQueryLogic(queryGroup: QueryGroup?, type: QueryLogicType, logicOperator: QueryLogicOperator?, query: Query?, parentQueryLogic : QueryLogic? ) : QueryLogic {
        val queryLogic = QueryLogic() ;
        queryLogic.id =  Random.nextLong()
        queryLogic.queryGroup = queryGroup
        queryLogic.type = type
        queryLogic.logicOperator = logicOperator
        queryLogic.query = query;
        queryLogic.parent = parentQueryLogic

        return queryLogic
    }


    //64.2


//    GREATER_THAN_OR_EQUALS(">="),

    @Test
    @Transactional
    fun testEvaluateSingleConditionGreaterThan() {
        val hrQueyr  = createQuery(null, QueryMetric.HEART_RATE, ComparisonOperator.GREATER_THAN, QueryTimeFrame.PAST_6_MONTH, "60");
        val queryLogic1 = createQueryLogic(null, QueryLogicType.CONDITION, null, hrQueyr, null );
        val result = queryEValuationService.evaluateSingleCondition(queryLogic1, userData, "April") ;

        Assertions.assertTrue(result);

    }
    @Test
    @Transactional
    fun testEvaluateSingleConditionGreaterThanOrEquals() {
        val hrQueyr  = createQuery(null, QueryMetric.HEART_RATE, ComparisonOperator.GREATER_THAN_OR_EQUALS, QueryTimeFrame.PAST_6_MONTH, "64.2");
        val queryLogic1 = createQueryLogic(null, QueryLogicType.CONDITION, null, hrQueyr, null );
        val result = queryEValuationService.evaluateSingleCondition(queryLogic1, userData, "April") ;

        Assertions.assertTrue(result);

    }

    @Test
    @Transactional
    fun testEvaluateSingleConditionEquals() {
        val hrQueyr  = createQuery(null, QueryMetric.HEART_RATE, ComparisonOperator.EQUALS, QueryTimeFrame.PAST_6_MONTH, "64.2");
        val queryLogic1 = createQueryLogic(null, QueryLogicType.CONDITION, null, hrQueyr, null );
        val result = queryEValuationService.evaluateSingleCondition(queryLogic1, userData, "April") ;

        Assertions.assertTrue(result);

    }

    @Test
    @Transactional
    fun testEvaluateSingleConditionNotEqual() {
        val hrQueyr  = createQuery(null, QueryMetric.HEART_RATE, ComparisonOperator.NOT_EQUALS, QueryTimeFrame.PAST_6_MONTH, "65.2");
        val queryLogic1 = createQueryLogic(null, QueryLogicType.CONDITION, null, hrQueyr, null );
        val result = queryEValuationService.evaluateSingleCondition(queryLogic1, userData, "April") ;

        Assertions.assertTrue(result);

    }

    @Test
    @Transactional
    fun testEvaluateSingleConditionLessThan() {
        val hrQueyr  = createQuery(null, QueryMetric.HEART_RATE, ComparisonOperator.LESS_THAN, QueryTimeFrame.PAST_6_MONTH, "64.3");
        val queryLogic1 = createQueryLogic(null, QueryLogicType.CONDITION, null, hrQueyr, null );
        val result = queryEValuationService.evaluateSingleCondition(queryLogic1, userData, "April") ;

        Assertions.assertTrue(result);
    }

    @Test
    @Transactional
    fun testEvaluateSingleConditionLessThanOrEquals() {
        val hrQueyr  = createQuery(null, QueryMetric.HEART_RATE, ComparisonOperator.LESS_THAN_OR_EQUALS, QueryTimeFrame.PAST_6_MONTH, "64.2");
        val queryLogic1 = createQueryLogic(null, QueryLogicType.CONDITION, null, hrQueyr, null );
        val result = queryEValuationService.evaluateSingleCondition(queryLogic1, userData, "April") ;

        Assertions.assertTrue(result);
    }

    @Test
    @Transactional
    fun buildLogicTreeShouldReturnTopRootLogicGroup() {
        val hrQueyr  = createQuery(null, QueryMetric.HEART_RATE, ComparisonOperator.GREATER_THAN, QueryTimeFrame.PAST_6_MONTH, "60");
        val sleepQuery  = createQuery(null, QueryMetric.SLEEP_LENGTH, ComparisonOperator.GREATER_THAN, QueryTimeFrame.PAST_6_MONTH, "60");
        val hrvQuery = createQuery(null, QueryMetric.HRV, ComparisonOperator.GREATER_THAN, QueryTimeFrame.PAST_6_MONTH, "60");


        val rootLogicQuery = createQueryLogic(null, QueryLogicType.LOGIC, QueryLogicOperator.AND, null, null);
        val queryLogic1 = createQueryLogic(null, QueryLogicType.CONDITION, null, hrQueyr, rootLogicQuery );

        val innerRootLogicQuery = createQueryLogic(null, QueryLogicType.LOGIC, QueryLogicOperator.OR, null, rootLogicQuery);
        val sleepCondition = createQueryLogic(null, QueryLogicType.CONDITION, null, sleepQuery, innerRootLogicQuery );
        val hrvCondition = createQueryLogic(null, QueryLogicType.CONDITION, null, hrvQuery, innerRootLogicQuery );

        val list : List<QueryLogic> = listOf(
            rootLogicQuery,
            queryLogic1,
            innerRootLogicQuery,
            sleepCondition,
            hrvCondition
        )

        val result = queryEValuationService.buildLogicTree(list);

        Assertions.assertEquals(result?.id,rootLogicQuery.id)
    }



    @Test
    @Transactional
    fun testEvaluteQueryCondition() {

        var queryList: Map<String, Query> = mapOf(
           "HR" to createQuery(null, QueryMetric.HEART_RATE, ComparisonOperator.GREATER_THAN, QueryTimeFrame.PAST_6_MONTH, "60"),
            "SLEEP" to createQuery(null, QueryMetric.SLEEP_LENGTH, ComparisonOperator.EQUALS, QueryTimeFrame.PAST_6_MONTH, "8"),
            "HRV" to createQuery(null, QueryMetric.HRV, ComparisonOperator.LESS_THAN, QueryTimeFrame.PAST_6_MONTH, "50")
        )


        var root =  getRoot(queryList, QueryLogicOperator.AND, QueryLogicOperator.AND)


        var result = queryEValuationService.evaluteQueryCondition(root!!, userData, "April");

        // should be true
        Assertions.assertTrue(result);

         queryList  = mapOf(
            "HR" to createQuery(null, QueryMetric.HEART_RATE, ComparisonOperator.GREATER_THAN, QueryTimeFrame.PAST_6_MONTH, "60"),
            "SLEEP" to createQuery(null, QueryMetric.SLEEP_LENGTH, ComparisonOperator.EQUALS, QueryTimeFrame.PAST_6_MONTH, "8"),
            "HRV" to createQuery(null, QueryMetric.HRV, ComparisonOperator.GREATER_THAN, QueryTimeFrame.PAST_6_MONTH, "50")
        )

         root =  getRoot(queryList, QueryLogicOperator.AND, QueryLogicOperator.AND)
         result = queryEValuationService.evaluteQueryCondition(root!!, userData, "April");

        // should be false
         Assertions.assertFalse(result);


        queryList  = mapOf(
            "HR" to createQuery(null, QueryMetric.HEART_RATE, ComparisonOperator.GREATER_THAN, QueryTimeFrame.PAST_6_MONTH, "60"),
            "SLEEP" to createQuery(null, QueryMetric.SLEEP_LENGTH, ComparisonOperator.EQUALS, QueryTimeFrame.PAST_6_MONTH, "8"),
            "HRV" to createQuery(null, QueryMetric.HRV, ComparisonOperator.GREATER_THAN, QueryTimeFrame.PAST_6_MONTH, "50")
        )

        root =  getRoot(queryList, QueryLogicOperator.AND, QueryLogicOperator.OR)
        result = queryEValuationService.evaluteQueryCondition(root!!, userData, "April");

        // should be false
        Assertions.assertTrue(result);

    }





    private fun getRoot(listQueries:  Map<String, Query>, rootLogic: QueryLogicOperator, innerRootLogic: QueryLogicOperator): QueryLogic? {

        val rootLogicQuery = createQueryLogic(null, QueryLogicType.LOGIC, rootLogic, null, null);
        val queryLogic1 = createQueryLogic(null, QueryLogicType.CONDITION, null, listQueries["HR"], rootLogicQuery );

        val innerRootLogicQuery = createQueryLogic(null, QueryLogicType.LOGIC, innerRootLogic, null, rootLogicQuery);
        val sleepCondition = createQueryLogic(null, QueryLogicType.CONDITION, null, listQueries["SLEEP"], innerRootLogicQuery );
        val hrvCondition = createQueryLogic(null, QueryLogicType.CONDITION, null, listQueries["HRV"], innerRootLogicQuery );

        val list : List<QueryLogic> = listOf(
            rootLogicQuery,
            queryLogic1,
            innerRootLogicQuery,
            sleepCondition,
            hrvCondition
        )

        val root = queryEValuationService.buildLogicTree(list);

        return root;
    }


}
