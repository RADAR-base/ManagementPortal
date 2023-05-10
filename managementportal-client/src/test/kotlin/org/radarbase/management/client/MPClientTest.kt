/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.http.entity.ContentType
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.ktor.auth.ClientCredentialsConfig
import org.radarbase.ktor.auth.OAuth2AccessToken
import org.radarbase.ktor.auth.clientCredentials
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

@OptIn(ExperimentalCoroutinesApi::class)
class MPClientTest {
    private lateinit var authStub: StubMapping
    private lateinit var wireMockServer: WireMockServer
    private lateinit var client: MPClient

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(9090)
        wireMockServer.start()

        wireMockServer.stubFor(
            get(anyUrl())
                .willReturn(aResponse()
                    .withStatus(HTTP_UNAUTHORIZED)))

        authStub = wireMockServer.stubFor(
            post(urlEqualTo("/oauth/token"))
                .willReturn(aResponse()
                    .withStatus(HTTP_OK)
                    .withHeader("content-type", ContentType.APPLICATION_JSON.toString())
                    .withBody("{\"access_token\":\"abcdef\"}")))

        client = mpClient {
            url = "http://localhost:9090/"
            auth {
                clientCredentials(
                    authConfig = ClientCredentialsConfig(
                        tokenUrl = "http://localhost:9090/oauth/token",
                        clientId = "testId",
                        clientSecret = "testSecret",
                    ),
                    targetHost = "localhost",
                )
            }
        }
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun testClients() = runBlocking {
        val body =
            """
            [{
                "clientId": "c",
                "scope": ["s1", "s2"],
                "resourceIds": ["r1"],
                "authorizedGrantTypes": null,
                "autoApproveScopes": null,
                "accessTokenValiditySeconds": 900,
                "refreshTokenValiditySeconds": null,
                "authorities": ["aud_managementPortal"],
                "registeredRedirectUri": null,
                "additionalInformation": null
            },
            {
                "clientId": "d",
                "scope": ["s3", "s2"],
                "resourceIds": ["r1", "r2"],
                "authorizedGrantTypes": ["a1", "a2"],
                "autoApproveScopes": ["a1", "a2"],
                "accessTokenValiditySeconds": 900,
                "refreshTokenValiditySeconds": 86400,
                "authorities": ["aud_managementPortal"],
                "registeredRedirectUri": ["http://localhost"],
                "additionalInformation": {"something": "other"}
            }]
            """.trimIndent()

        wireMockServer.stubFor(
            get(urlPathEqualTo("/api/oauth-clients"))
                .withHeader("Authorization", equalTo("Bearer abcdef"))
                .willReturn(aResponse()
                    .withStatus(HTTP_OK)
                    .withHeader("content-type", ContentType.APPLICATION_JSON.toString())
                    .withBody(body)))

        val clients = client.requestClients()

        assertThat(clients, hasSize(2))
        assertThat(clients, Matchers.equalTo(listOf(
            MPOAuthClient(
                id = "c",
                scope = listOf("s1", "s2"),
                resourceIds = listOf("r1"),
                authorizedGrantTypes = emptyList(),
                autoApproveScopes = emptyList(),
                accessTokenValiditySeconds = 900,
                refreshTokenValiditySeconds = null,
                authorities = listOf("aud_managementPortal"),
                registeredRedirectUri = emptyList(),
                additionalInformation = emptyMap(),
            ),
            MPOAuthClient(
                id = "d",
                scope = listOf("s3", "s2"),
                resourceIds = listOf("r1", "r2"),
                authorizedGrantTypes = listOf("a1", "a2"),
                autoApproveScopes = listOf("a1", "a2"),
                accessTokenValiditySeconds = 900,
                refreshTokenValiditySeconds = 86400,
                authorities = listOf("aud_managementPortal"),
                registeredRedirectUri = listOf("http://localhost"),
                additionalInformation = mapOf("something" to "other"),
            )
        )))

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/oauth/token"))
            .withRequestBody(EqualToPattern("grant_type=client_credentials&client_id=testId&client_secret=testSecret")))
        wireMockServer.verify(2, getRequestedFor(urlPathEqualTo("/api/oauth-clients")))
    }

    @Test
    fun testParseToken() {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val token = json.decodeFromString<OAuth2AccessToken?>("""{"access_token":"access token","token_type":"bearer","expires_in":899,"scope":"PROJECT.READ","iss":"ManagementPortal","grant_type":"client_credentials","iat":1600000000,"jti":"some token"}""")
        assertThat(token, Matchers.equalTo(
            OAuth2AccessToken(
                accessToken = "access token",
                expiresIn = 899,
                tokenType = "bearer",
                scope = "PROJECT.READ"
            )
        ))
    }

    @Test
    fun testProjects() = runTest {
        val body =
            """
            [{
                "id": 1,
                "projectName": "p",
                "humanReadableProjectName": null,
                "description": "d",
                "organization": null,
                "location": "u",
                "startDate": null,
                "projectStatus": "ONGOING",
                "endDate": null,
                "attributes": {},
                "persistentTokenTimeout": null
            },
            {
                "id": 2,
                "projectName": "p2",
                "humanReadableProjectName": "P2",
                "description": "d2",
                "organization": {"id": 1, "name": "Mixed"},
                "location": "here",
                "startDate": "2021-06-07T02:02:00Z",
                "projectStatus": "ONGOING",
                "endDate": "2022-06-07T02:02:00Z",
                "attributes": {
                    "External-project-id": "p2a",
                    "Human-readable-project-name": "P2"
                },
                "persistentTokenTimeout": null
            }]
            """.trimIndent()

        wireMockServer.stubFor(
            get(urlPathEqualTo("/api/projects"))
                .withHeader("Authorization", equalTo("Bearer abcdef"))
                .willReturn(aResponse()
                    .withStatus(HTTP_OK)
                    .withHeader("content-type", ContentType.APPLICATION_JSON.toString())
                    .withBody(body)))

        val projects = client.requestProjects()
        assertThat(projects, hasSize(2))
        assertThat(projects, Matchers.equalTo(listOf(
            MPProject(
                id = "p",
                name = null,
                description = "d",
                organization = null,
                location = "u",
                startDate = null,
                projectStatus = "ONGOING",
                endDate = null,
                attributes = emptyMap(),
            ),
            MPProject(
                id = "p2",
                name = "P2",
                description = "d2",
                organization = MPOrganization(id = "Mixed"),
                location = "here",
                startDate = "2021-06-07T02:02:00Z",
                projectStatus = "ONGOING",
                endDate = "2022-06-07T02:02:00Z",
                attributes = mapOf(
                    "External-project-id" to "p2a",
                    "Human-readable-project-name" to "P2",
                ),
            ),
        )))

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/oauth/token")))
        wireMockServer.verify(2, getRequestedFor(urlPathEqualTo("/api/projects")))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MPClientTest::class.java)
    }
}
