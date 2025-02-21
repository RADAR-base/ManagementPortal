package org.radarbase.management.service

import org.slf4j.LoggerFactory
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.utils.IoUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.InputStream


import java.io.File;
class AWSService {
    private val log = LoggerFactory.getLogger(javaClass)
    private var s3AsyncClient: S3AsyncClient? = null
    private var bucketName: String? = null
    var region = Region.of("eu-west-2")

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


    fun useHttpUrlConnectionToGet(presignedUrlString: String?): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream() // Capture the response body to a byte array.
        try {
            val presignedUrl = URL(presignedUrlString)
            val connection = presignedUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.inputStream.use { content ->
                IoUtils.copy(
                    content,
                    byteArrayOutputStream
                )
            }
            log.info("HTTP response code is " + connection.responseCode)
        } catch (e: S3Exception) {
            log.error(e.message, e)
        } catch (e: IOException) {
            log.error(e.message, e)
        }
        return byteArrayOutputStream.toByteArray()
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
