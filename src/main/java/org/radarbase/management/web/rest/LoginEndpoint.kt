package org.radarbase.management.web.rest

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.radarbase.auth.exception.IdpException
import org.radarbase.management.config.ManagementPortalProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import java.time.Duration
import java.time.Instant

@RestController
@RequestMapping("/api")
class LoginEndpoint
    @Autowired
    constructor(
        private val managementPortalProperties: ManagementPortalProperties,
    ) {
        private val httpClient =
            HttpClient(CIO) {
                install(HttpTimeout) {
                    connectTimeoutMillis = Duration.ofSeconds(10).toMillis()
                    socketTimeoutMillis = Duration.ofSeconds(10).toMillis()
                    requestTimeoutMillis = Duration.ofSeconds(300).toMillis()
                }
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

        @GetMapping("/redirect/login")
        suspend fun loginRedirect(
            @RequestParam(required = false) code: String?,
        ): RedirectView {
            val redirectView = RedirectView()
            val config = managementPortalProperties
            val mpUrl = config.common.baseUrl

            if (code == null) {
                redirectView.url = buildAuthUrl(config, mpUrl)
            } else {
                val accessToken = fetchAccessToken(code, config)
                redirectView.url = "$mpUrl/#/?access_token=$accessToken"
            }
            return redirectView
        }

        @GetMapping("/redirect/account")
        fun settingsRedirect(): RedirectView {
            val redirectView = RedirectView()
            redirectView.url = "${managementPortalProperties.identityServer.loginUrl}/settings"
            return redirectView
        }

        private fun buildAuthUrl(config: ManagementPortalProperties, mpUrl: String): String {
            return "${config.authServer.serverUrl}/oauth2/auth?" +
                    "client_id=${config.frontend.clientId}&" +
                    "response_type=code&" +
                    "state=${Instant.now()}&" +
                    "audience=res_ManagementPortal&" +
                    "scope=offline&" +
                    "redirect_uri=$mpUrl/api/redirect/login"
        }

        private suspend fun fetchAccessToken(
            code: String,
            config: ManagementPortalProperties,
        ): String {
            val tokenUrl = "${config.authServer.serverUrl}/oauth2/token"
            val response =
                httpClient.post(tokenUrl) {
                    contentType(ContentType.Application.FormUrlEncoded)
                    accept(ContentType.Application.Json)
                    setBody(
                        Parameters
                            .build {
                                append("grant_type", "authorization_code")
                                append("code", code)
                                append("redirect_uri", "${config.common.baseUrl}/api/redirect/login")
                                append("client_id", config.frontend.clientId)
                            }.formUrlEncode(),
                    )
                }

            if (response.status.isSuccess()) {
                val responseMap = response.body<Map<String, JsonElement>>()
                return responseMap["access_token"]?.jsonPrimitive?.content
                    ?: throw IdpException("Access token not found in response")
            } else {
                throw IdpException("Unable to get access token")
            }
        }

        companion object {
            private val logger = LoggerFactory.getLogger(LoginEndpoint::class.java)
        }
    }
