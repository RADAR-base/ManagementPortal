package org.radarbase.management.service
import software.amazon.awssdk.services.s3.model.S3Object

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.LoggerFactory
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import com.fasterxml.jackson.module.kotlin.readValue
import org.radarbase.management.domain.MetricAverage
import org.radarbase.management.domain.enumeration.AggregationPeriod
import org.radarbase.management.domain.enumeration.AggregationType
import org.radarbase.management.repository.MetricAveragesRepository
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import java.io.*
import java.lang.IllegalArgumentException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

enum class DataSource {
    S3, CLASSPATH
}
@JsonIgnoreProperties(ignoreUnknown = true)
data class S3JsonData(
    val patient_id: String,
    val site: String,
    val data_summary: DataSummary,
    val feature_statistics: Map<String, FeatureStatistics>,
    val questionnaire_responses: QuestionnaireResponses
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DataSummary(val start_date: String?, val end_date: String?, val total_days_with_data: Int)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeatureStatistics(val mean: Double?, val total_responses: Double?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class QuestionnaireResponses(
    val total_responses: Int,
    val days_with_responses: Int,
    val slider: Map<String, SliderResponses>,
    val histogram:  Histogram)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SliderResponses(
    val total_entries: Double,
    val mean: Double)


// OUTPUT
data class DataSummaryResult(
    var data: MutableMap<String, DataSummaryCategory>,
    val allSlider: MutableList<String>,
    val allHistogram: MutableList<String>,
    val allPhysical: MutableList<String>,

)

data class DataSummaryCategory(
    var physical:  MutableMap<String, Double>,
    var questionnaire_total:  Double,
    var questionnaire_slider: MutableMap<String,  Double>,
    var questionnaire_histogram: HistogramResponse,


)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Histogram(
    val social: Map<String, Map<String, Int>> = emptyMap(),
    val whereabouts: Map<String, Map<String, Int>> = emptyMap(),
    val sleep: Map<String, Map<String, Int>> = emptyMap())


@JsonIgnoreProperties(ignoreUnknown = true)
data class HistogramResponse(
    val social: MutableMap<String,  Int>,
    val whereabouts: MutableMap<String,  Int>,
    val sleep: MutableMap<String,  Int>)



@Service
@Transactional
class AWSService(
    private val metricAveragesRepository: MetricAveragesRepository,
    private val subjectService: SubjectService,
    private val metricAverageService: MetricAverageService


) {
    private val log = LoggerFactory.getLogger(javaClass)
    private var s3AsyncClient: S3AsyncClient? = null
    private var bucketName: String? = null
    var region = Region.of("eu-west-2")


//Map<String, Map<String, Double>>
    fun startProcessing(projectName: String, login: String, dataSource: DataSource) : DataSummaryResult? {
         log.info("[PDF-EXPORT] start processing")

        val dataSource = dataSource  // Change this to DataSource.CLASSPATH to load from resources

        val keyName = "output/" + projectName + "/" + login + "/export/"
          log.info("[PDF-EXPORT] key ${keyName}")

        val bucketName = "connect-output-storage"
        val folderPath = keyName
        val region = Region.EU_WEST_2 // Change to your AWS region
        val resourceFolderPath = "export/" // Folder inside resources


        val s3Client = S3Client.builder()
            .region(region)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()


        val files = if (dataSource == DataSource.S3) {
            listS3JsonFiles(s3Client, bucketName, folderPath)
        } else {
            listClasspathJsonFiles(resourceFolderPath)
        }


            if(files.size == 0) {
                return null;
            }

        val monthlyFeatureStats = processJsonFiles(s3Client, bucketName, files, dataSource)

        return monthlyFeatureStats;

    }

    fun listS3JsonFiles(s3Client: S3Client, bucket: String, prefix: String): List<String> {
        val request = ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build()
        val response = s3Client.listObjectsV2(request)
        return response.contents().map(S3Object::key)
            .filter { it != prefix } // Exclude the folder itself
    }
    fun listClasspathJsonFiles(resourceFolderPath: String) : List<String> {

        val classLoader =  Thread.currentThread().contextClassLoader
        val resource = classLoader.getResource(resourceFolderPath) ?: return emptyList()
        val file = java.io.File(resource.toURI())

        return file.list()?.map  {
            "$resourceFolderPath$it"
        }  ?: emptyList()


    }



    fun extractMonthFromFilename(filename: String): String {
        val regex = """_(\d{4}-\d{2})""".toRegex() // Looks for _YYYY-MM in filename
        return regex.find(filename)?.groupValues?.get(1) ?: "unknown"
    }

    fun downloadS3Json(s3Client: S3Client, bucket: String, key: String): String {
        val request = GetObjectRequest.builder().bucket(bucket).key(key).build()
        s3Client.getObject(request).use { objStream ->
            return BufferedReader(InputStreamReader(objStream)).readText()
        }
    }


    fun readClassPathJson(filePath: String) : String {
        val classLoader = Thread.currentThread().contextClassLoader
        val inputStream = classLoader.getResourceAsStream(filePath)
            ?: throw IllegalArgumentException("File not found: $filePath")

        return inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    }


    fun processJsonFiles(
        client: S3Client,
        bucket: String,
        fileKeys: List<String>,
        dataSource: DataSource
    ): DataSummaryResult {
        val jsonMapper = jacksonObjectMapper()
        val dataSummaryResult = DataSummaryResult(
            data = mutableMapOf(),
            allHistogram = mutableListOf(),
            allSlider = mutableListOf(),
            allPhysical = mutableListOf()
        )

        for (key in fileKeys) {
            if (key == "export/.DS_Store") continue

            val jsonString = when (dataSource) {
                DataSource.S3 -> downloadS3Json(client, bucket, key)
                DataSource.CLASSPATH -> readClassPathJson(key)
            }

            val jsonData: S3JsonData = jsonMapper.readValue(jsonString)
            val month = extractMonthFromFilename(key)

            val dataSummaryCategory = dataSummaryResult.data.getOrPut(month) {
                DataSummaryCategory(
                    physical = mutableMapOf(),
                    questionnaire_total = 0.0,
                    questionnaire_slider = mutableMapOf(),
                    questionnaire_histogram = HistogramResponse(
                        social = mutableMapOf(),
                        whereabouts = mutableMapOf(),
                        sleep = mutableMapOf()
                    )
                )
            }


            jsonData.feature_statistics.forEach { (feature, stats) ->
                val mean = stats.mean ?: stats.total_responses ?: 0.0
                dataSummaryCategory.physical[feature] = mean

                if (feature !in dataSummaryResult.allPhysical) {
                    dataSummaryResult.allPhysical.add(feature)
                }
            }


            dataSummaryCategory.questionnaire_total = jsonData.questionnaire_responses.days_with_responses.toDouble()


            jsonData.questionnaire_responses.slider.forEach { (feature, stats) ->
                dataSummaryCategory.questionnaire_slider[feature] = stats.mean

                if (feature !in dataSummaryResult.allSlider) {
                    dataSummaryResult.allSlider.add(feature)
                }
            }


            processHistogramCategory(
                jsonData.questionnaire_responses.histogram.social["social_1"],
                dataSummaryCategory.questionnaire_histogram.social
            )

            processHistogramCategory(
                jsonData.questionnaire_responses.histogram.whereabouts["whereabouts_1"],
                dataSummaryCategory.questionnaire_histogram.whereabouts
            )

            processHistogramCategory(
                jsonData.questionnaire_responses.histogram.sleep["sleep_5"],
                dataSummaryCategory.questionnaire_histogram.sleep
            )
        }

        return dataSummaryResult
    }

    private fun processHistogramCategory(
        source: Map<String, Int>?,
        target: MutableMap<String, Int>
    ) {
        source?.forEach { (feature, value) ->
            val normalizedKey = feature.toDoubleOrNull()?.toInt()?.toString() ?: feature
            target.merge(normalizedKey, value, Int::plus)
        }
    }



    ///// old stuff

    private fun configureS3Builder() {
        bucketName = "voicein-ethics-upload"
        s3AsyncClient = S3AsyncClient.builder().region(region).build()
    }

    fun createPresignedGetUrl(bucketName: String?, keyName: String?): String? {
        S3Presigner.create().use { presigner ->
            val objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build()
            val presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // The URL will expire in 10 minutes.
                .getObjectRequest(objectRequest)
                .build()
            val presignedRequest = presigner.presignGetObject(presignRequest)
            log.info("Presigned URL: [{}]", presignedRequest.url().toString())
            log.info("HTTP method: [{}]", presignedRequest.httpRequest().method())
            return presignedRequest.url().toExternalForm()
        }
    }


    fun useHttpUrlConnectionToGetDataAsInputStream(presignedUrlString: String?): InputStream? {
        val byteArrayOutputStream = ByteArrayOutputStream() // Capture the response body to a byte array.
        var inputStreamTest : InputStream = ByteArrayInputStream.nullInputStream()

        try {
            val presignedUrl = URL(presignedUrlString)
            val connection = presignedUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.inputStream.use { content ->

                inputStreamTest = content
                content.close()
            }
            log.info("HTTP response code is " + connection.responseCode)
        } catch (e: S3Exception) {
            log.error(e.message, e)
        } catch (e: IOException) {
            log.error(e.message, e)
        }

        val byteArray = byteArrayOutputStream.toByteArray();
        val inputStream : InputStream =  ByteArrayInputStream(byteArray);

        return inputStream
    }

    fun readLocalFile()  : Map<String, List<Double>> {
        val featureStatisticsMap = mutableMapOf<String, MutableList<Double>>()
        try {
            val objectMapper = jacksonObjectMapper()
            val inputStream: InputStream = ClassPathResource("monthly_health_data.json").inputStream
            val data: List<Map<String, Any>> = objectMapper.readValue(inputStream)

            for (monthData in data) {
                val featureStatistics = monthData["feature_statistics"] as Map<String, Any>

                featureStatistics.forEach { (feature, stats) ->
                    val meanValue = (stats as Map<String, Any>)["mean"] as? Double ?: (stats["mean_duration"] as? Double)
                    if (meanValue != null) {
                        featureStatisticsMap.getOrPut(feature) { mutableListOf() }.add(meanValue)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return featureStatisticsMap
    }

}
