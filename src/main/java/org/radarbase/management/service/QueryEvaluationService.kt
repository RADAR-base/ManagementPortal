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
    val value: Double? = null,
    val histogramDataPoints: Map<String, Int> = mapOf()

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
    private val queryParticipantRepository: QueryParticipantRepository,
    private val awsService: AWSService

) {
    fun evaluteQueryCondition(queryLogic: QueryLogic, userData: MutableMap<String, DataSummaryCategory>, currentMonth: String) : Boolean {
        return when(queryLogic.type) {
               QueryLogicType.CONDITION -> evaluateSingleCondition(queryLogic, userData, currentMonth)
               QueryLogicType.LOGIC -> evaluateLogicalCondition(queryLogic, userData, currentMonth)
            else -> false;
        }
    }

    private fun evaluateAgainstHistogramData(aggregatedData: MutableMap<String, Int>, expectedValue: String) : Boolean {
        if(aggregatedData.isEmpty()) {
            return false
        }
        val maxRange = aggregatedData.maxByOrNull { it.value }
        val result = maxRange != null && maxRange.key == expectedValue
        return result
    }


    private fun evaluateAgainstAveragedData(relevantData: List<Double>, expectedValue: String, comparsionOperator: String ): Boolean {
        if(relevantData.isEmpty()) {
            return false
        }

        val average = relevantData.average();

        return when (comparsionOperator){
            ">" -> average  > expectedValue.toDouble()
            "<" -> average < expectedValue.toDouble()
            ">=" -> average  >= expectedValue.toDouble()
            "<=" -> average <= expectedValue.toDouble()
            "=" -> average == expectedValue.toDouble()
            "!=" -> average != expectedValue.toDouble()
            else -> false
        }
    }

    private fun aggregateDataForHistogramEvaluation(metric: String, currentTimeFrame: String, userData: MutableMap<String, DataSummaryCategory>) : MutableMap<String, Int>  {
        var questionnaireHistogramData :  MutableMap<String, Int>?  = mutableMapOf()
         val aggregatedData = mutableMapOf<String, Int>()

        when(metric.lowercase()) {
            "social" -> questionnaireHistogramData = userData[currentTimeFrame]?.questionnaire_histogram?.social
            "whereabouts"  -> questionnaireHistogramData = userData[currentTimeFrame]?.questionnaire_histogram?.whereabouts
            "sleep" -> questionnaireHistogramData = userData[currentTimeFrame]?.questionnaire_histogram?.sleep
        }

        if(questionnaireHistogramData != null) {
            for((range, count) in questionnaireHistogramData){
                aggregatedData[range] = aggregatedData.getOrDefault(range, 0) + count
            }
        }

        return aggregatedData
    }

    private fun getRelevantDataForAveragedEvaluation(entity: String, metric:String, summary: DataSummaryCategory ) : MutableList<Double> {
        val physicalData = summary.physical
        val sliderData = summary.questionnaire_slider
        val avgEvalData = mutableListOf<Double>()

        val value = when (entity.lowercase()) {
            "physical" -> physicalData[metric.lowercase()]
            "questionnaire_slider" -> sliderData[metric.lowercase()]
            else -> null
        }

        if (value != null) {
            avgEvalData += value
        }

        return avgEvalData
    }

    fun evaluateSingleCondition(
        queryLogic: QueryLogic,
        userData: MutableMap<String, DataSummaryCategory>,
        currentMonth: String
    ): Boolean {
        val query = queryLogic.query ?: return false

        val comparisonOperator = query.operator?.symbol ?: return false
        val entity = query.entity ?: return false
        val metric = query.field ?: return false
        val expectedValue = query.value ?: return false
        val timeFrame = query.timeFrame ?: return false
        val timeframeMonths = extractTimeframeMonths(timeFrame, currentMonth)

        var avgEvalData = mutableListOf<Double>()
        var histogramEvalData = mutableMapOf<String, Int>()

        for (month in timeframeMonths) {
            val summary = userData[month] ?: continue
            if (comparisonOperator == "IS") {
                histogramEvalData = aggregateDataForHistogramEvaluation(metric, month, userData)
            } else {
                avgEvalData = getRelevantDataForAveragedEvaluation(entity, metric, summary)

            }
        }

        return if (comparisonOperator == "IS") {
            evaluateAgainstHistogramData(histogramEvalData, expectedValue)
        } else {
            evaluateAgainstAveragedData(avgEvalData, expectedValue, comparisonOperator)
        }
    }
    fun evaluateLogicalCondition(queryLogic: QueryLogic, userData:  MutableMap<String, DataSummaryCategory>, currentMonth: String) : Boolean {
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


        val sleepHistogram =   (0L until 12L).map { monthsAgo ->
            val month = currentMonth.minusMonths(monthsAgo).format(formatter)
            val value =  mapOf("6-8" to 2, "8-10" to 18, "4-6" to 2, "10-12" to 5)
            DataPoint(month, null, value)
        }.reversed()

        return UserData(
            metrics = mapOf(
                "HEART_RATE" to heartRateData,
                "SLEEP_LENGTH" to sleepData,
                "HRV" to HRV,
                "SLEEP_5" to sleepHistogram)
        )
    }
    //TODO: this will be replaced by a real data and automatic worker
    fun testLogicEvaluation(subject: Subject, project: String,  customUserData: UserData?) : MutableMap<String, Boolean>?  {
        val subjectLogin = subject.user?.login!!
        val subjectId = subject.id!!;

        val processedData = awsService.startProcessing(project, subjectLogin, DataSource.CLASSPATH)?.data ?: return mutableMapOf()

        val subjectOpt = subjectRepository.findById(subjectId)
        val queryParticipant = queryParticipantRepository.findBySubjectId(subjectId);
        val results: MutableMap<String, Boolean> = mutableMapOf()


        if(subjectOpt.isPresent && queryParticipant.isNotEmpty()) {
            val subject = subjectOpt.get();

            for (queryParticipant: QueryParticipant in queryParticipant) {
                val queryGroup = queryParticipant.queryGroup ?: continue
                val queryGroupId = queryGroup.id ?: continue

                val flatConditions = queryLogicRepository.findByQueryGroupId(queryGroupId)
                val root = buildLogicTree(flatConditions) ?: return results;


                val currentDate = LocalDate.now()
                val month = currentDate.month
                val monthName = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

                val result =  evaluteQueryCondition(root, processedData, monthName);

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

            val content = queryContentService.findAllByQueryGroupId(queryGroupId)

            result[queryGroupId] = content;
        }

        return result
    }



    companion object {
        private val log = LoggerFactory.getLogger(QueryBuilderService::class.java)
    }
}
