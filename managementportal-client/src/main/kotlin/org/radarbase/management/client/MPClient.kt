/*
 * Copyright (c) 2020. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.radarcns.oauth.OAuth2AccessTokenDetails
import org.radarcns.oauth.OAuth2Client
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.MalformedURLException
import java.util.concurrent.TimeUnit

class MPClient private constructor(
    serverConfig: MPServerConfig,
    objectMapper: ObjectMapper? = null,
    httpClient: OkHttpClient? = null,
) {
    private val clientId: String = serverConfig.clientId
    private val clientSecret: String = serverConfig.clientSecret
    val baseUrl: HttpUrl = (serverConfig.url.toHttpUrlOrNull()
        ?: throw MalformedURLException("Cannot parse base URL ${serverConfig.url} as an URL"))
        .newBuilder()
        .addPathSegment("")
        .build()

    private val objectMapper: ObjectMapper = objectMapper ?: ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    var httpClient: OkHttpClient = httpClient ?: OkHttpClient().newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val projectListReader: ObjectReader by lazy { this.objectMapper.readerForListOf(MPProject::class.java) }
    private val subjectListReader: ObjectReader by lazy { this.objectMapper.readerForListOf(MPSubject::class.java) }
    private val clientListReader: ObjectReader by lazy { this.objectMapper.readerForListOf(MPOAuthClient::class.java) }

    private val oauth2Client = OAuth2Client.Builder()
        .httpClient(httpClient)
        .credentials(clientId, clientSecret)
        .endpoint(baseUrl.toUrl(), "oauth/token")
        .build()

    val validToken: OAuth2AccessTokenDetails
        get() = oauth2Client.validToken

    /** Read list of projects from ManagementPortal. */
    fun readProjects(
        page: Int = 0,
        size: Int = Int.MAX_VALUE,
    ): List<MPProject> {
        logger.debug("Requesting for projects")
        return request(projectListReader,
            {
                addPathSegments("api/projects")
                addQueryParameter("page", page.toString())
                addQueryParameter("size", size.toString())
            })
    }

    /** Read list of participants from ManagementPortal project. The [projectId] is the name that
     * the project is identified by. */
    fun readSubjects(
        projectId: String,
        page: Int = 0,
        size: Int = Int.MAX_VALUE,
    ): List<MPSubject> {
        return request<List<MPSubject>>(subjectListReader,
            {
                addPathSegments("api/projects/$projectId/subjects")
                addQueryParameter("page", page.toString())
                addQueryParameter("size", size.toString())
            })
            .map { it.copy(projectId = projectId) }
    }

    @Suppress("unused")
    fun readClients(): List<MPOAuthClient> {
        return request(clientListReader, { addPathSegments("api/oauth-clients") })
    }

    inline fun <T> request(
        reader: ObjectReader,
        urlBuilder: HttpUrl.Builder.() -> Unit,
        requestBuilder: (Request.Builder.() -> Unit) = { }
    ): T = request(urlBuilder, requestBuilder) { request, response ->
        val body = response.body ?: throw IOException("No response body to ${request.url}")
        reader.readValue(body.byteStream())
    }

    inline fun request(
        urlBuilder: HttpUrl.Builder.() -> Unit,
        requestBuilder: (Request.Builder.() -> Unit) = { },
    ) {
        request(urlBuilder, requestBuilder, { _, _ -> })
    }

    inline fun <T> request(
        urlBuilder: HttpUrl.Builder.() -> Unit,
        requestBuilder: Request.Builder.() -> Unit = { },
        responseHandler: (Request, Response) -> T,
    ): T {
        val request = Request.Builder().apply {
            url(baseUrl.newBuilder().apply {
                urlBuilder()
            }.build())
            header("Authorization", "Bearer $validToken")
            requestBuilder()
        }.build()

        return httpClient.newCall(request).execute().use {
            if (!it.isSuccessful) throw IOException("Request to ${request.url} failed (code ${it.code})")
            responseHandler(request, it)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MPClient::class.java)
    }

    data class MPServerConfig(
        val url: String,
        val clientId: String,
        val clientSecret: String,
    )
}
