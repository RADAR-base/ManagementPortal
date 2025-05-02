package org.radarbase.management.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.radarbase.auth.exception.IdpException
import org.radarbase.management.config.ManagementPortalProperties
import org.springframework.stereotype.Service
import java.util.*

@Service
class LoginService(
    private val managementPortalProperties: ManagementPortalProperties
) {
    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 10_000
            requestTimeoutMillis = 300_000
        }
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    fun buildAuthUrl(): String {
        val state = UUID.randomUUID().toString()
        
        val commonParams = mapOf(
            "client_id" to managementPortalProperties.frontend.clientId,
            "response_type" to "code",
            "redirect_uri" to "${managementPortalProperties.common.managementPortalBaseUrl}/api/redirect/login"
        )

        val (baseUrl, specificParams) = when (managementPortalProperties.authServer.internal) {
            true -> Pair(
                "${managementPortalProperties.common.managementPortalBaseUrl}/oauth/authorize",
                emptyMap()
            )
            false -> Pair(
                "${managementPortalProperties.authServer.loginUrl}/oauth2/auth",
                mapOf(
                    "state" to state,
                    "audience" to managementPortalProperties.frontend.audience,
                    "scope" to managementPortalProperties.frontend.scopes.joinToString(" ")
                )
            )
        }

        val params = commonParams + specificParams
        return "$baseUrl?${params.entries.joinToString("&") { "${it.key}=${it.value}" }}"
    }

    suspend fun fetchAccessToken(code: String): String {
        val config = getTokenRequestConfig()
        val authHeader = "Basic " + Base64.getEncoder().encodeToString("${config.clientId}:${config.clientSecret}".toByteArray())

        try {
            val requestBody = Parameters.build {
                append("grant_type", "authorization_code")
                append("code", code)
                append("redirect_uri", config.redirectUri)
            }.formUrlEncode()

            val response = httpClient.post(config.tokenUrl) {
                headers { 
                    append(HttpHeaders.Authorization, authHeader)
                    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                }
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                val responseMap = response.body<Map<String, JsonElement>>()
                return responseMap["access_token"]?.jsonPrimitive?.content
                    ?: throw IdpException("Access token not found in response")
            } else {
                val errorBody = response.body<String>()
                throw IdpException("Unable to get access token: ${response.status} - $errorBody")
            }
        } catch (e: Exception) {
            throw IdpException("Error fetching access token: ${e.message}", e)
        }
    }

    private data class TokenRequestConfig(
        val tokenUrl: String,
        val clientId: String,
        val clientSecret: String,
        val redirectUri: String
    )

    private fun getTokenRequestConfig(): TokenRequestConfig {
        val tokenUrl = if (managementPortalProperties.authServer.internal) {
            "${managementPortalProperties.common.managementPortalBaseUrl}/oauth/token"
        } else {
            "${managementPortalProperties.authServer.serverUrl}/oauth2/token"
        }

        return TokenRequestConfig(
            tokenUrl = tokenUrl,
            clientId = managementPortalProperties.frontend.clientId,
            clientSecret = managementPortalProperties.frontend.clientSecret,
            redirectUri = "${managementPortalProperties.common.managementPortalBaseUrl}/api/redirect/login"
        )
    }
} 