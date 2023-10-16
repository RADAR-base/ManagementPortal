/*
 * Copyright (c) 2020. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.radarbase.ktor.auth.OAuth2AccessToken
import java.util.*
import kotlin.time.Duration.Companion.seconds

fun mpClient(config: MPClient.Config.() -> Unit): MPClient {
    return MPClient(MPClient.Config().apply(config))
}

/**
 * Client for the ManagementPortal REST API.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class MPClient(config: Config) {
    lateinit var token: Flow<OAuth2AccessToken?>

    private val url: String = requireNotNull(config.url) {
        "Missing server URL"
    }.trimEnd('/') + '/'

            /** HTTP client to make requests with. */
    private val originalHttpClient: HttpClient? = config.httpClient
    private val auth: Auth.() -> Flow<OAuth2AccessToken?> = config.auth

    val httpClient = (originalHttpClient ?: HttpClient(CIO)).config {
        install(HttpTimeout) {
            connectTimeoutMillis = 10.seconds.inWholeMilliseconds
            socketTimeoutMillis = 10.seconds.inWholeMilliseconds
            requestTimeoutMillis = 30.seconds.inWholeMilliseconds
        }
        install(ContentNegotiation) {
            json(json)
        }
        install(Auth) {
            token = auth()
        }
        defaultRequest {
            url(this@MPClient.url)
        }
    }

    /** Request list of organizations from ManagementPortal */
    suspend fun requestOrganizations(
        page: Int = 0,
        size: Int = Int.MAX_VALUE,
    ): List<MPOrganization> = request {
        url("api/organizations")
        with(url.parameters) {
            append("page", page.toString())
            append("size", size.toString())
        }
    }

    /** Request list of projects from ManagementPortal. */
    suspend fun requestProjects(
        page: Int = 0,
        size: Int = Int.MAX_VALUE,
    ): List<MPProject> {
        val body = requestText {
            url("api/projects")
            with(url.parameters) {
                append("page", page.toString())
                append("size", size.toString())
            }
        }
        return json.decodeFromString(ListSerializer(MPProjectSerializer), body)
    }

    /**
     * Request list of subjects from ManagementPortal project. The [projectId] is the name that
     * the project is identified by.
     */
    suspend fun requestSubjects(
        projectId: String,
        page: Int = 0,
        size: Int = Int.MAX_VALUE,
    ): List<MPSubject> = request<List<MPSubject>> {
        url("api/projects/$projectId/subjects")
        with(url.parameters) {
            append("page", page.toString())
            append("size", size.toString())
        }
    }
        .map { it.copy(projectId = projectId) }

    /**
     * Request list of OAuth 2.0 clients from ManagementPortal.
     */
    suspend fun requestClients(
        page: Int = 0,
        size: Int = Int.MAX_VALUE,
    ): List<MPOAuthClient> = request {
        url("api/oauth-clients")
        with(url.parameters) {
            append("page", page.toString())
            append("size", size.toString())
        }
    }

    suspend inline fun <reified T> request(
        crossinline block: HttpRequestBuilder.() -> Unit,
    ): T = withContext(Dispatchers.IO) {
        with(httpClient.request(block)) {
            if (!status.isSuccess()) {
                throw HttpStatusException(status, "Request to ${request.url} failed (code $status)")
            }
            body()
        }
    }

    suspend inline fun requestText(
        crossinline block: HttpRequestBuilder.() -> Unit,
    ): String = withContext(Dispatchers.IO) {
        with(httpClient.request(block)) {
            if (!status.isSuccess()) {
                throw HttpStatusException(status, "Request to ${request.url} failed (code $status)")
            }
            bodyAsText()
        }
    }

    fun config(config: Config.() -> Unit): MPClient {
        val oldConfig = toConfig()
        val newConfig = toConfig().apply(config)
        return if (oldConfig != newConfig) MPClient(newConfig) else this
    }

    private fun toConfig(): Config = Config().apply {
        httpClient = this@MPClient.originalHttpClient
        url = this@MPClient.url
        auth = this@MPClient.auth
    }

    class Config {
        internal var auth: Auth.() -> Flow<OAuth2AccessToken?> = { MutableStateFlow(null) }

        /** HTTP client to make requests with. */
        var httpClient: HttpClient? = null

        var url: String? = null

        fun auth(install: Auth.() -> Flow<OAuth2AccessToken?>) {
            auth = install
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Config

            return httpClient == other.httpClient
                    && url == other.url
                    && auth == other.auth
        }

        override fun hashCode(): Int = Objects.hash(httpClient, url)
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
