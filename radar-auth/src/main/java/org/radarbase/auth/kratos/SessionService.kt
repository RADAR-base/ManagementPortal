package org.radarbase.auth.kratos

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.radarbase.auth.exception.IdpException
import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * Service class for handling Kratos sessions but may be extended in the future.
 */
class SessionService(
    private val serverUrl: String,
) {
    /** Get a [KratosSessionDTO] for a given session token. Returns the generated [KratosSessionDTO] */
    @Throws(IdpException::class)
    suspend fun getSession(token: String): KratosSessionDTO {
        val kratosSession: KratosSessionDTO

        val cookie = "ory_kratos_session=" + token

        val address = "$serverUrl/sessions/whoami"
        log.debug("requesting session at $address")

        withContext(Dispatchers.IO) {
            val response =
                httpClient.get {
                    header("Cookie", cookie)
                    url(address)
                    accept(ContentType.Application.Json)
                }

            if (response.status.isSuccess()) {
                kratosSession = response.body<KratosSessionDTO>()
                log.debug("session retrieved: {}", kratosSession)
            } else {
                throw IdpException("couldn't get kratos session $token at $address", token = token)
            }
        }

        return kratosSession
    }

    /** Get a [KratosSessionDTO] for a given session token. Returns the generated [KratosSessionDTO] */
    @Throws(IdpException::class)
    suspend fun getLogoutUrl(token: String): String {
        val cookie = "ory_kratos_session=$token"
        val logOutResponse: LogoutResponse
        val address = "$serverUrl/self-service/logout/browser"
        log.debug("requesting logout url at $address")
        withContext(Dispatchers.IO) {
            val response =
                httpClient.get {
                    header("Cookie", cookie)
                    url(address)
                    accept(ContentType.Application.Json)
                }

            if (response.status.isSuccess()) {
                logOutResponse = response.body<LogoutResponse>()
            } else {
                throw IdpException("couldn't get logout url at $address", token)
            }
        }

        return logOutResponse.logout_url ?: throw IdpException("could not get logoutUrl", token)
    }

    @Serializable
    class LogoutResponse(
        val logout_url: String?,
    )

    companion object {
        private val log = LoggerFactory.getLogger(SessionService::class.java)

        private val httpClient =
            HttpClient(CIO).config {
                install(HttpTimeout) {
                    connectTimeoutMillis = Duration.ofSeconds(10).toMillis()
                    socketTimeoutMillis = Duration.ofSeconds(10).toMillis()
                    requestTimeoutMillis = Duration.ofSeconds(300).toMillis()
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                        },
                    )
                }
            }
    }
}
