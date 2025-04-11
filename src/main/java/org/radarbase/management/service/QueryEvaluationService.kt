package org.radarbase.management.service
import org.radarbase.management.domain.*
import org.radarbase.management.domain.enumeration.QueryLogicType
import org.radarbase.management.domain.enumeration.QueryMetric
import org.radarbase.management.domain.enumeration.QueryTimeFrame
import org.radarbase.management.repository.*
import org.radarbase.management.service.dto.QueryDTO
import org.radarbase.management.service.dto.QueryGroupDTO
import org.radarbase.management.service.dto.QueryLogicDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

data class DataPoint(
    val month: String,
    val value: Double
)
data class UserData(
    val metrics: Map<String, List<DataPoint>>
)
@Service
@Transactional
public class QueryEValuationService(
    private val queryLogicRepository:  QueryLogicRepository,
    private val queryGroupRepository: QueryGroupRepository,
    private val queryEvaluationRepository: QueryEvaluationRepository,

    private val subjectRepository: SubjectRepository

) {
    fun evaluteQueryCondition(queryLogic: QueryLogic, userData: UserData, currentMonth: String) : Boolean {
        return when(queryLogic.type) {
               QueryLogicType.CONDITION -> evaluateSingleCondition(queryLogic, userData, currentMonth)
               QueryLogicType.LOGIC -> evaluateLogicalCondition(queryLogic, userData, currentMonth)
            else -> false;
        }
    }

    fun evaluateSingleCondition(queryLogic: QueryLogic, userData: UserData, currentMonth: String): Boolean {
           val metricValuesData = userData.metrics[queryLogic.query?.metric?.name]  ?: return false
           val timeFrame  = queryLogic.query?.time_frame ?: return false
           val timeframeMonths = extractTimeframeMonths(timeFrame, currentMonth);
           val relevantData = metricValuesData.filter { it.month in timeframeMonths};

            if (relevantData.isEmpty()) {
                return false
            }

           val comparisonOperator = queryLogic.query?.operator?.symbol ?: return false ;
           val expectedValue = queryLogic.query?.value ?: return false;

           val average = relevantData.map { it.value }.average();

           return when (comparisonOperator){
               ">" -> average  > expectedValue.toDouble()
               "<" -> average < expectedValue.toDouble()
               ">=" -> average  >= expectedValue.toDouble()
               "<=" -> average <= expectedValue.toDouble()
               "==" -> average == expectedValue.toDouble()
               "!=" -> average != expectedValue.toDouble()
               else -> false
           }
    }

    fun evaluateLogicalCondition(queryLogic: QueryLogic, userData: UserData, currentMonth: String) : Boolean {
        val children = queryLogic.children ?: return false;

        val results = children.map {
            evaluteQueryCondition(it, userData, currentMonth)
        }

        return when (queryLogic.logic_operator.toString()) {
            "AND" -> results.all { it }
            "OR" -> results.any { it }
            else -> false;
        }
    }

    fun extractTimeframeMonths(timeframe: QueryTimeFrame, currentMonth: String): List<String> {
        val currentDateFormatter = DateTimeFormatter.ofPattern("MMMM-yyyy-dd")

        val currentYear = LocalDate.now().year

       val currentDate = LocalDate.parse("$currentMonth-$currentYear-01", currentDateFormatter)

        var monthsBack = 0;
        when (timeframe.name) {
            "PAST_MONTH" -> monthsBack = 1
            "PAST_6_MONTH" -> monthsBack = 6
            "PAST_YEAR" -> monthsBack = 12
            else -> monthsBack = 1;
        }

        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

        var result =  (0 until monthsBack + 1).map {
            currentDate.minusMonths(it.toLong()).format(outputFormatter)
        }

        //TODO: replace with actual dates once out of testing phase
        return listOf("2024-01", "2024-02", "2024-03", "2024-04", "2024-05")
    }



    //TODO: this will be replaced by a real data and automatic worker
    fun testLogicEvaluation(queryGroupId: Long, subjectId: Long, customUserData: UserData?) : Boolean  {
        val subjectOpt = subjectRepository.findById(subjectId)
        val queryGroupOpt  = queryGroupRepository.findById(queryGroupId);

        if(subjectOpt.isPresent && queryGroupOpt.isPresent ) {
            val subject = subjectOpt.get();
            val queryGroup = queryGroupOpt.get();

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

            if(customUserData != null) {
                userData = customUserData;
            }

            val flatConditions = queryLogicRepository.findByQueryGroupId(queryGroupId)
            val root = buildLogicTree(flatConditions) ?: return false;

            val currentDate = LocalDate.now()
            val month = currentDate.month
            val monthName = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

            val result =   evaluteQueryCondition(root, userData, monthName);

            saveQueryEvaluationResult(result, subject, queryGroup)

            return result
        }

        return false;

    }

    fun saveQueryEvaluationResult(result: Boolean, subject: Subject, queryGroup: QueryGroup) {
        val queryEvaluationList = queryEvaluationRepository.findBySubjectAndQueryGroup(subject, queryGroup) ;

        if(queryEvaluationList.isNotEmpty()) {
            val existingQueryEvaluation = queryEvaluationList[0]

            if(existingQueryEvaluation.result == false && result) {
                existingQueryEvaluation.updatedDate = ZonedDateTime.now()
                existingQueryEvaluation.result = true
                queryEvaluationRepository.save(existingQueryEvaluation)
            }

        } else {
            val newQueryEvaluation = QueryEvaluation();

            newQueryEvaluation.queryGroup = queryGroup
            newQueryEvaluation.subject = subject
            newQueryEvaluation.createdDate = ZonedDateTime.now()
            newQueryEvaluation.result = result

            queryEvaluationRepository.save(newQueryEvaluation);
        }
        queryEvaluationRepository.flush();
    }
    fun buildLogicTree(conditions: List<QueryLogic>): QueryLogic? {
        val map = conditions.associateBy { it.id  }.toMutableMap()

        var root : QueryLogic? = null

        for(condition in conditions) {
            if (condition.parent?.id != null) {
                val parent = map[condition.parent?.id]
                parent?.children?.add(condition)
            }
             else {
                    root = condition;
                }
            }


        return root;
    }

    companion object {
        private val log = LoggerFactory.getLogger(QueryBuilderService::class.java)
    }
}
