package org.radarbase.auth.kratos

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.radarbase.auth.exception.IdpException
import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * Service class for managing identities.
 */
class SessionService(private val serverUrl: String) {
    private val httpClient = HttpClient(CIO).config {
        install(HttpTimeout) {
            connectTimeoutMillis = Duration.ofSeconds(10).toMillis()
            socketTimeoutMillis = Duration.ofSeconds(10).toMillis()
            requestTimeoutMillis = Duration.ofSeconds(300).toMillis()
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    /** Get a [KratosSessionDTO] for a given session token. Returns the generated [KratosSessionDTO] */
    @Throws(IdpException::class)
    suspend fun getSession(token: String): KratosSessionDTO {
        val kratosSession: KratosSessionDTO

        val cookie = "ory_kratos_session=" + token

        withContext(Dispatchers.IO) {
            val response = httpClient.get {
                header("Cookie", cookie)
                url("$serverUrl/sessions/whoami")
                accept(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                kratosSession = response.body<KratosSessionDTO>()
                log.debug("session retrieved: ${kratosSession}")
            } else {
                throw IdpException("couldn't get kratos session ${token} at " + serverUrl)
            }
        }

        return kratosSession
    }

    /** Get a [KratosSessionDTO] for a given session token. Returns the generated [KratosSessionDTO] */
    @Throws(IdpException::class)
    suspend fun getLogoutUrl(token: String): String {
        val cookie = "ory_kratos_session=" + token
        val logOutResponse: LogoutResponse

        withContext(Dispatchers.IO) {
            val response = httpClient.get {
                header("Cookie", cookie)
                url("$serverUrl/self-service/logout/browser")
                accept(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                logOutResponse = response.body<LogoutResponse>()
            } else {
                throw IdpException("couldn't get logout url at " + serverUrl)
            }
        }

        return logOutResponse.logout_url ?: throw IdpException("could not get logoutUrl")
    }

    @Serializable
    class LogoutResponse (
        val logout_url: String?,
        val logout_token: String?,
    )

    companion object {
        private val log = LoggerFactory.getLogger(SessionService::class.java)
    }
}
