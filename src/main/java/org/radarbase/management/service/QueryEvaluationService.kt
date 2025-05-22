package org.radarbase.management.service
import org.radarbase.management.domain.*
import org.radarbase.management.domain.enumeration.QueryLogicType
import org.radarbase.management.domain.enumeration.QueryTimeFrame
import org.radarbase.management.repository.*
import org.radarbase.management.service.dto.QueryContentDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
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
    private val queryContentService: QueryContentService,
    private val queryGroupRepository: QueryGroupRepository,
    private val queryEvaluationRepository: QueryEvaluationRepository,
    private val subjectRepository: SubjectRepository,
    private val queryParticipantRepository: QueryParticipantRepository

) {
    fun evaluteQueryCondition(queryLogic: QueryLogic, userData: UserData, currentMonth: String) : Boolean {
        return when(queryLogic.type) {
               QueryLogicType.CONDITION -> evaluateSingleCondition(queryLogic, userData, currentMonth)
               QueryLogicType.LOGIC -> evaluateLogicalCondition(queryLogic, userData, currentMonth)
            else -> false;
        }
    }

    fun evaluateSingleCondition(queryLogic: QueryLogic, userData: UserData, currentMonth: String): Boolean {
           val metricValuesData = userData.metrics[queryLogic.query?.queryMetric?.name]  ?: return false
           val timeFrame  = queryLogic.query?.timeFrame ?: return false
           val timeframeMonths = extractTimeframeMonths(timeFrame, currentMonth);
           val relevantData = metricValuesData.filter { it.month in timeframeMonths};

            if (relevantData.isEmpty()  || relevantData.size != timeframeMonths.size) {
                return false
            }

           val comparisonOperator = queryLogic.query?.comparisonOperator?.symbol ?: return false ;
           val expectedValue = queryLogic.query?.value ?: return false;

           val average = relevantData.map { it.value }.average();

           return when (comparisonOperator){
               ">" -> average  > expectedValue.toDouble()
               "<" -> average < expectedValue.toDouble()
               ">=" -> average  >= expectedValue.toDouble()
               "<=" -> average <= expectedValue.toDouble()
               "=" -> average == expectedValue.toDouble()
               "!=" -> average != expectedValue.toDouble()
               else -> false
           }
    }

    fun evaluateLogicalCondition(queryLogic: QueryLogic, userData: UserData, currentMonth: String) : Boolean {
        val children = queryLogic.children ?: return false;

        val results = children.map {
            evaluteQueryCondition(it, userData, currentMonth)
        }

        return when (queryLogic.logicOperator.toString()) {
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


        var result =  (1 until   monthsBack + 1).map {
            currentDate.minusMonths(it.toLong()).format(outputFormatter)
        }

        log.info("[QUERY-CONTENT] months {}", result)

        return result;

    }


    //TODO: delete later, only added  for Sandra evaluation purposes
    private fun generateUserData(valueHeartRate: Double, valueSleep: Long, HRV: Long)  : UserData{
        val currentMonth = YearMonth.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")

        val heartRateData =
            (0L until 12L).map { monthsAgo ->
                val month = currentMonth.minusMonths(monthsAgo).format(formatter)
                val value = valueHeartRate
                DataPoint(month, value.toDouble())
            }.reversed()


        val sleepData =   (0L until 12L).map { monthsAgo ->
            val month = currentMonth.minusMonths(monthsAgo).format(formatter)
            val value = valueSleep
            DataPoint(month, value.toDouble())
        }.reversed()


        val HRV =   (0L until 12L).map { monthsAgo ->
            val month = currentMonth.minusMonths(monthsAgo).format(formatter)
            val value = HRV
            DataPoint(month, value.toDouble())
        }.reversed()

        return UserData(
            metrics = mapOf(
                "HEART_RATE" to heartRateData,
                "SLEEP_LENGTH" to sleepData,
                "HRV" to HRV)
        )
    }
    //TODO: this will be replaced by a real data and automatic worker
    fun testLogicEvaluation(subjectId: Long, customUserData: UserData?) : MutableMap<String, Boolean>  {
        val subjectOpt = subjectRepository.findById(subjectId)
        val queryParticipant = queryParticipantRepository.findBySubjectId(subjectId);
        val results: MutableMap<String, Boolean> = mutableMapOf()


        if(subjectOpt.isPresent && queryParticipant.isNotEmpty()) {
            val subject = subjectOpt.get();

            var userData = generateUserData(87.0,8, 55)

            if(customUserData != null) {
                userData = customUserData;
            }

            for (queryParticipant: QueryParticipant in queryParticipant) {
                val queryGroup = queryParticipant.queryGroup ?: continue
                val queryGroupId = queryGroup.id ?: continue

                val flatConditions = queryLogicRepository.findByQueryGroupId(queryGroupId)
                val root = buildLogicTree(flatConditions) ?: return results;


                val currentDate = LocalDate.now()
                val month = currentDate.month
                val monthName = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

                val result =   evaluteQueryCondition(root, userData, monthName);

                saveQueryEvaluationResult(result, subject, queryGroup)

                results[queryGroup.name!!] = result;
            }
        }
        return results;
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

    fun getActiveQueryContentForParticipant(participantId: Long): Map<Long, List<QueryContentDTO>> {
        val result = mutableMapOf<Long, List<QueryContentDTO>>()

        val queryParticipantList = queryParticipantRepository.findBySubjectId(participantId)
        if (queryParticipantList.isEmpty()) return result

        val subjectOpt = subjectRepository.findById(participantId)
        if (!subjectOpt.isPresent) return result

        val subject = subjectOpt.get()

        // Fetch all successful evaluations for the subject
        val evaluations = queryEvaluationRepository.findBySubject(subject)
            .filter { it.result == true }

        for (evaluation in evaluations) {
            val queryGroup = evaluation.queryGroup ?: continue
            val queryGroupId = queryGroup.id ?: continue

            val content = queryContentService.getContentByQueryGroupId(queryGroupId)

            result[queryGroupId] = content;
        }

        return result
    }

    private fun getQueryGroupContent(queryGroupId: Long): List<Map<String, Any>> {
        return listOf(
            mapOf("type" to "TITLE", "text_value" to "How to sleep better"),
            mapOf("type" to "PARAGRAPH", "text_value" to "paragraph content"),
            mapOf("type" to "HEADING", "text_value" to "test heading"),
            mapOf("type" to "VIDEO", "text_value" to "https://www.youtube.com/embed/ff5Dc_M1ycw"),
            mapOf("type" to "IMAGE", "image" to "https://picsum.photos/200/300")
        )
    }



    companion object {
        private val log = LoggerFactory.getLogger(QueryBuilderService::class.java)
    }
}
