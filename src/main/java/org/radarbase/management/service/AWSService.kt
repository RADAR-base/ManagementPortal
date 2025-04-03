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
import org.springframework.core.io.ClassPathResource
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import java.io.*
import java.lang.IllegalArgumentException
import java.nio.charset.StandardCharsets

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
    var histogram: HistogramResponse,


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

class AWSService {
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

        val bucketName = "connect-dev-output"
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
    log.info("[PDF-EXPORT] file size ${files.size}")

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


    fun processJsonFiles(
       Client: S3Client,
        bucket: String,
        fileKeys: List<String>,
        dataSource: DataSource
    ): DataSummaryResult {
        val jsonMapper = jacksonObjectMapper()
        val monthlyAverages = mutableMapOf<String, MutableMap<String, MutableList<Double>>>()
        val dataSummaryResult = DataSummaryResult(
            data = mutableMapOf(),
            allHistogram = mutableListOf(),
            allSlider = mutableListOf(),
            allPhysical = mutableListOf()
        )

        for (key in fileKeys) {
            log.info("current key is ${key}")


            if(key == "export/.DS_Store") {
                continue
            }

            val jsonString = when (dataSource) {
                DataSource.S3 -> downloadS3Json(Client, bucket, key)
                DataSource.CLASSPATH -> readClassPathJson(key)
            }


            // gets the JSON from the file and reads it into a variable
            val jsonData: S3JsonData = jsonMapper.readValue(jsonString)
            val month = extractMonthFromFilename(key) // Extract month from filename


            // creates an empty object that will be filled up with datat
            val dataSummaryCategory = DataSummaryCategory(
                physical = mutableMapOf<String,  Double>(),
                questionnaire_total = 0.0,
                questionnaire_slider = mutableMapOf(),
                histogram = HistogramResponse(
                    social = mutableMapOf(),
                    whereabouts = mutableMapOf(),
                    sleep = mutableMapOf()


                ),

            )

            // puts a month in based on the file name
            dataSummaryResult.data
                .getOrPut(month) { dataSummaryCategory }


            // goes through the physical statistics (heart_rate , steps etc) and gets the mean value
            // and saves it
            jsonData.feature_statistics.forEach { (feature, stats) ->
                var mean : Double = 0.0;
                if(stats.mean != null) {
                    mean = stats.mean
                } else if (stats.total_responses != null) {
                    mean = stats.total_responses
                }


                //monthlyAverages not used anymore I think ?
                monthlyAverages
                    .getOrPut(month) { mutableMapOf() }
                    .getOrPut(feature) { mutableListOf() }
                    .add(mean)

                // this is where it puts steps: 3.5 as an exmaple
                dataSummaryCategory.physical
                             .getOrPut(feature){ mean }

            }

            // gets the questionnare_total per month
            dataSummaryCategory.questionnaire_total = jsonData.questionnaire_responses.days_with_responses.toDouble()


            // gets the questionnaire categories ( same principle as for physical ones)
            jsonData.questionnaire_responses.slider.forEach{ (feature, stats) ->
                val totalNumber =  stats.mean;

                dataSummaryCategory.questionnaire_slider
                    .getOrPut(feature){ totalNumber }
            }


            // the next three is hardcoded to get the histograms

            val social = jsonData.questionnaire_responses.histogram.social.get("social_1")
             if (social != null) {

                 social.forEach { (feature, stats) ->

                     val key = feature.toDouble().toInt()
                     var value = dataSummaryCategory.histogram.social.get(key.toString())

                     if (value == null) {
                         dataSummaryCategory.histogram.social.put(key.toString(), stats)
                     } else {
                         value += stats
                         dataSummaryCategory.histogram.social.put(key.toString(), value)
                     }
                 }
             }

            val whereabouts = jsonData.questionnaire_responses.histogram.whereabouts["whereabouts_1"]
            if (whereabouts != null) {

                whereabouts.forEach { (feature, stats) ->
                    val key = feature.toDouble().toInt()
                    var value = dataSummaryCategory.histogram.whereabouts[key.toString()]

                    if (value == null) {
                        dataSummaryCategory.histogram.whereabouts.put(key.toString(), stats)
                    } else {
                        value += stats
                        dataSummaryCategory.histogram.whereabouts.put(key.toString(), value)
                    }
                }
            }

            val sleep = jsonData.questionnaire_responses.histogram.sleep["sleep_5"]
            if (sleep != null) {

                sleep.forEach { (feature, stats) ->

                    var value = dataSummaryCategory.histogram.sleep[feature]

                    if (value == null) {
                        dataSummaryCategory.histogram.sleep.put(feature, stats)
                    } else {
                        value += stats
                        dataSummaryCategory.histogram.sleep.put(feature, value)
                    }
                }
            }

            for ((summaryKey, summaryValue) in dataSummaryResult.data) {

                for((sliderKey, sliderValue) in summaryValue.questionnaire_slider) {
                   if(sliderKey !in dataSummaryResult.allSlider) {
                       dataSummaryResult.allSlider.add(sliderKey)
                   }
                }

                for((sliderKey, sliderValue) in summaryValue.physical) {
                    if(sliderKey !in dataSummaryResult.allPhysical) {
                        dataSummaryResult.allPhysical.add(sliderKey)
                    }
                }
            }

            // histogram

     //       jsonData.questionnaire_responses.

//            log.info("questionnaire data is ${jsonData.questionnaire_responses.total_responses}")
//            monthlyAverages
//                .getOrPut(month) { mutableMapOf() }
//                .getOrPut("questionnaire_responses") { mutableListOf() }
//                .add(jsonData.questionnaire_responses.total_responses.toDouble())

        }

//        return monthlyAverages.mapValues { (_, features) ->
//            features.mapValues { (_, means) -> means.average() }
//        }

        return dataSummaryResult
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
