/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey

import okhttp3.OkHttpClient
import okhttp3.Request
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.auth.jersey.mock.MockProjectService
import org.radarcns.auth.authentication.OAuthHelper
import org.radarcns.auth.authentication.TokenValidator
import java.net.URI
import javax.inject.Singleton

internal class RadarJerseyResourceEnhancerTest {
    private lateinit var client: OkHttpClient
    private lateinit var server: HttpServer

    @BeforeEach
    fun setUp() {
        val authConfig = AuthConfig(
                managementPortalUrl = "http://localhost:8080",
                jwtResourceName = "res_ManagementPortal")

        val radarEnhancer = RadarJerseyResourceEnhancer(authConfig)
        val mpEnhancer = ManagementPortalResourceEnhancer()

        val resourceConfig = object : ResourceConfig() {
            init {
                packages("org.radarbase.auth.jersey.mock.resource")
                packages(*radarEnhancer.packages)
                packages(*mpEnhancer.packages)
            }
        }

        resourceConfig.register(object : AbstractBinder() {
            override fun configure() {
                bind(MockProjectService(listOf("a", "b")))
                        .to(ProjectService::class.java)
                        .`in`(Singleton::class.java)

                bindFactory { OAuthHelper.createTokenValidator() }
                        .to(TokenValidator::class.java)

                radarEnhancer.enhance(this)
                mpEnhancer.enhance(this)
            }
        })

        server =  GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:9091"), resourceConfig)
        server.start()

        client = OkHttpClient()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testBasicGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"this\":\"that\"}"))
        }
    }

    @Test
    fun testAuthenticatedGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/user")
                .header("Authorization", "Bearer ${OAuthHelper.validEcToken}")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"accessToken\":\"${OAuthHelper.validEcToken}\"}"))
        }
    }


    @Test
    fun testUnauthenticatedGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/user")
                .header("Accept", "application/json")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(401))
            assertThat(response.body?.string(), equalTo("{\"error\":\"token_missing\",\"error_description\":\"No bearer token is provided in the request.\"}"))
        }
    }


    @Test
    fun testBadAuthenticationGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/user")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer abcdef")
                .build()).execute().use { response ->
            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(401))
            assertThat(response.body?.string(), equalTo("{\"error\":\"token_unverified\",\"error_description\":\"Cannot verify token. It may have been rendered invalid.\"}"))
        }
    }

    @Test
    fun testExistingGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects/a/users/b")
                .header("Authorization", "Bearer ${OAuthHelper.validEcToken}")
                .build()).execute().use { response ->

            assertThat(response.isSuccessful, `is`(true))
            assertThat(response.body?.string(), equalTo("{\"projectId\":\"a\",\"userId\":\"b\"}"))
        }
    }


    @Test
    fun testNonExistingGet() {
        client.newCall(Request.Builder()
                .url("http://localhost:9091/projects/c/users/b")
                .header("Authorization", "Bearer ${OAuthHelper.validEcToken}")
                .header("Accept", "application/json")
                .build()).execute().use { response ->

            assertThat(response.isSuccessful, `is`(false))
            assertThat(response.code, `is`(404))
            assertThat(response.body?.string(), equalTo("{\"error\":\"project_not_found\",\"error_description\":null}"))
        }
    }

    @Test
    fun testNonExistingGetHtml() {
        val response = client.newCall(Request.Builder()
                .url("http://localhost:9091/projects/c/users/b")
                .header("Authorization", "Bearer ${OAuthHelper.validEcToken}")
                .header("Accept", "text/html,application/json")
                .build()).execute()

        assertThat(response.isSuccessful, `is`(false))
        assertThat(response.code, `is`(404))

        val body = response.body?.string()

        assertThat(body, containsString("<h1>Bad request (status code 404)</h1>"))
    }


    companion object {
        @BeforeAll
        @JvmStatic
        fun setUpClass() {
            OAuthHelper.setUp()
        }
    }
}
