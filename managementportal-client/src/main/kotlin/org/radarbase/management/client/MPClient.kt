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
import org.radarbase.oauth.OAuth2AccessTokenDetails
import org.radarbase.oauth.OAuth2Client
import java.io.IOException
import java.net.MalformedURLException
import java.util.concurrent.TimeUnit

/**
 * Client for the ManagementPortal REST API.
 */
@Suppress("unused")
class MPClient(
    /** Server configuration of the ManagementPortal API. */
    serverConfig: MPServerConfig,
    /** ObjectMapper to use for all requests. */
    objectMapper: ObjectMapper? = null,
    /** HTTP client to make requests with. */
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

    /** HTTP client to make requests with. */
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

    /**
     * Valid access token for the ManagementPortal REST API.
     * @throws org.radarbase.exception.TokenException if a new access token could not be fetched
     */
    val validToken: OAuth2AccessTokenDetails
        get() = oauth2Client.validToken

    /** Request list of projects from ManagementPortal. */
    fun requestProjects(
        page: Int = 0,
        size: Int = Int.MAX_VALUE,
    ): List<MPProject> = request(
        projectListReader,
        {
            addPathSegments("api/projects")
            addQueryParameter("page", page.toString())
            addQueryParameter("size", size.toString())
        })

    /**
     * Request list of subjects from ManagementPortal project. The [projectId] is the name that
     * the project is identified by.
     */
    fun requestSubjects(
        projectId: String,
        page: Int = 0,
        size: Int = Int.MAX_VALUE,
    ): List<MPSubject> = request<List<MPSubject>>(
        subjectListReader,
        {
            addPathSegments("api/projects/$projectId/subjects")
            addQueryParameter("page", page.toString())
            addQueryParameter("size", size.toString())
        })
        .map { it.copy(projectId = projectId) }

    /**
     * Request list of OAuth 2.0 clients from ManagementPortal.
     */
    fun requestClients(
        page: Int = 0,
        size: Int = Int.MAX_VALUE,
    ): List<MPOAuthClient> = request(
        clientListReader,
        {
            addPathSegments("api/oauth-clients")
            addQueryParameter("page", page.toString())
            addQueryParameter("size", size.toString())
        })

    /**
     * Make a request and parse the result as JSON. The response body is parsed by [reader]. The
     * url to query is constructed using [urlBuilder]. In [urlBuilder], use
     * [HttpUrl.Builder.addPathSegments] over [HttpUrl.Builder.encodedPath] to preserve the correct
     * base URL. The request can optionally be modified with [requestBuilder], for example to POST
     * content or add headers.
     * @throws IOException if the request fails, has a unsuccessful status code or if the response
     *      cannot be read.
     */
    inline fun <T> request(
        reader: ObjectReader,
        urlBuilder: HttpUrl.Builder.() -> Unit,
        requestBuilder: (Request.Builder.() -> Unit) = { }
    ): T = request(urlBuilder, requestBuilder) { request, response ->
        if (!response.isSuccessful) {
            throw IOException("Request to ${request.url} failed (code ${response.code})")
        }
        val body = response.body ?: throw IOException("No response body to ${request.url}")
        reader.readValue(body.byteStream())
    }

    /**
     * Make a request without any response processing. The
     * url to query is constructed using [urlBuilder]. In [urlBuilder], use
     * [HttpUrl.Builder.addPathSegments] over [HttpUrl.Builder.encodedPath] to preserve the correct
     * base URL. The request can optionally be modified with [requestBuilder], for example to POST
     * content or add headers.
     * @throws IOException if the request fails or has a unsuccessful status code.
     */
    inline fun request(
        urlBuilder: HttpUrl.Builder.() -> Unit,
        requestBuilder: (Request.Builder.() -> Unit) = { },
    ) {
        request(urlBuilder, requestBuilder, { request, response ->
            if (!response.isSuccessful) {
                throw IOException("Request to ${request.url} failed (code ${response.code})")
            }
        })
    }

    /**
     * Make a request without custom response processing. The
     * url to query is constructed using [urlBuilder]. In [urlBuilder], use
     * [HttpUrl.Builder.addPathSegments] over [HttpUrl.Builder.encodedPath] to preserve the correct
     * base URL. The request can optionally be modified with [requestBuilder], for example to POST
     * content or add headers. The response can be handled with [responseHandler], e.g., by
     * evaluating the status code or the body with a custom body reader.
     */
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
            responseHandler(request, it)
        }
    }

    data class MPServerConfig(
        val url: String,
        val clientId: String,
        val clientSecret: String,
    )
}
