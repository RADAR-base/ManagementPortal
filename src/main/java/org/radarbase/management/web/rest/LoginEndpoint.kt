package org.radarbase.management.web.rest

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.radarbase.auth.exception.IdpException
import org.radarbase.management.config.ManagementPortalProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api")
class LoginEndpoint @Autowired constructor(
    private val managementPortalProperties: ManagementPortalProperties,
) {

    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 10_000
            requestTimeoutMillis = 300_000
        }
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    @GetMapping("/redirect/login")
    suspend fun loginRedirect(@RequestParam(required = false) code: String?): RedirectView {
        val redirectView = RedirectView()

        if (code == null) {
            redirectView.url = buildAuthUrl()
        } else {
            try {
                val accessToken = fetchAccessToken(code)
                redirectView.url = "${managementPortalProperties.common.managementPortalBaseUrl}/#/?access_token=$accessToken"
            } catch (e: IdpException) {
                // Log the error, provide fallback
                redirectView.url = "/error?message=Unable%20to%20authenticate"
            }
        }
        return redirectView
    }

    @GetMapping("/redirect/account")
    fun settingsRedirect(): RedirectView {
        val redirectView = RedirectView()
        redirectView.url = "${managementPortalProperties.identityServer.loginUrl}/settings"
        return redirectView
    }

    private fun buildAuthUrl(): String {
        val state = UUID.randomUUID().toString() // Generate a random state
        if (managementPortalProperties.authServer.internal) {
            return "${managementPortalProperties.common.managementPortalBaseUrl}/oauth/authorize?" +
                    "client_id=${managementPortalProperties.frontend.clientId}&" +
                    "response_type=code&" +
                    "redirect_uri=${managementPortalProperties.common.managementPortalBaseUrl}/api/redirect/login"
        }
        return "${managementPortalProperties.authServer.loginUrl}/oauth2/auth?" +
                "client_id=${managementPortalProperties.frontend.clientId}&" +
                "response_type=code&" +
                "state=$state&" +
                "audience=res_ManagementPortal&" +
                "scope=SOURCEDATA.CREATE SOURCETYPE.UPDATE SOURCETYPE.DELETE AUTHORITY.UPDATE MEASUREMENT.DELETE PROJECT.READ AUDIT.CREATE USER.DELETE AUTHORITY.DELETE SUBJECT.DELETE MEASUREMENT.UPDATE SOURCEDATA.UPDATE SUBJECT.READ USER.UPDATE SOURCETYPE.CREATE AUTHORITY.READ USER.CREATE SOURCE.CREATE SOURCE.READ SUBJECT.CREATE ROLE.UPDATE ROLE.READ MEASUREMENT.READ PROJECT.UPDATE PROJECT.DELETE ROLE.DELETE SOURCE.DELETE SOURCETYPE.READ ROLE.CREATE SOURCEDATA.DELETE SUBJECT.UPDATE SOURCE.UPDATE PROJECT.CREATE AUDIT.READ MEASUREMENT.CREATE AUDIT.DELETE AUDIT.UPDATE AUTHORITY.CREATE USER.READ ORGANIZATION.READ ORGANIZATION.CREATE ORGANIZATION.UPDATE SOURCEDATA.READ&" +
                "redirect_uri=${managementPortalProperties.common.managementPortalBaseUrl}/api/redirect/login"
    }

    suspend fun fetchAccessToken(code: String): String {
        val tokenUrl = if (managementPortalProperties.authServer.internal) {
            "${managementPortalProperties.common.managementPortalBaseUrl}/oauth/token"
        } else {
            "${managementPortalProperties.authServer.serverUrl}/oauth2/token"
        }
        val clientId = managementPortalProperties.frontend.clientId
        val clientSecret = managementPortalProperties.frontend.clientSecret
        val authHeader = "Basic " + Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())

        try {
            val response = httpClient.post(tokenUrl) {
                headers { append(HttpHeaders.Authorization, authHeader) }
                contentType(ContentType.Application.FormUrlEncoded)
                accept(ContentType.Application.Json)
                setBody(
                    Parameters.build {
                        append("grant_type", "authorization_code")
                        append("code", code)
                        append("redirect_uri", "${managementPortalProperties.common.managementPortalBaseUrl}/api/redirect/login")
                        append("client_id", clientId)
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
        } catch (e: Exception) {
            throw IdpException("Error fetching access token: ${e.message}", e)
        }
    }
}
