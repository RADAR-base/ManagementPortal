package org.radarbase.auth.jwks

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Verification
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.authentication.TokenVerifierLoader
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.jwt.JwtTokenVerifier
import org.radarbase.auth.jwt.JwtTokenVerifier.Companion.SCOPE_CLAIM
import org.slf4j.LoggerFactory
import java.time.Duration

class JwksTokenVerifierLoader(
    private val url: String,
    private val resourceName: String,
    private val algorithmParser: JwkParser,
) : TokenVerifierLoader {
    override suspend fun fetch(): List<TokenVerifier> {
        val keySet =
            try {
                fetchPublicKeyInfo()
            } catch (ex: Exception) {
                logger.warn("Failed to fetch token for {}: {}", url, ex.message)
                return listOf()
            }
        return buildList(keySet.keys.size) {
            keySet.keys.forEach { key ->
                try {
                    add(
                        algorithmParser
                            .parse(key)
                            .toTokenVerifier(resourceName),
                    )
                } catch (ex: Exception) {
                    logger.error("Failed to parse key from {}: {}", url, ex.message)
                }
            }
        }
    }

    private suspend fun fetchPublicKeyInfo(): JsonWebKeySet =
        withContext(Dispatchers.IO) {
            logger.info("Getting the JWT public key at {}", url)
            val response = httpClient.request(url)

            if (!response.status.isSuccess()) {
                throw TokenValidationException("Cannot fetch token keys (${response.status}) - ${response.bodyAsText()}")
            }

            response.body()
        }

    override fun toString(): String = "MPTokenKeyAlgorithmKeyLoader<url=$url>"

    companion object {
        @JvmStatic
        @JvmOverloads
        fun Algorithm.toTokenVerifier(
            resourceName: String,
            builder: Verification.() -> Unit = {},
        ): JwtTokenVerifier {
            val verifier =
                JWT.require(this).run {
                    withClaimPresence(SCOPE_CLAIM)
                    withAudience(resourceName)
                    builder()
                    build()
                }
            return JwtTokenVerifier(name, verifier)
        }

        private val logger = LoggerFactory.getLogger(JwksTokenVerifierLoader::class.java)

        private val httpClient =
            HttpClient(CIO).config {
                install(HttpTimeout) {
                    connectTimeoutMillis = Duration.ofSeconds(10).toMillis()
                    socketTimeoutMillis = Duration.ofSeconds(10).toMillis()
                    requestTimeoutMillis = Duration.ofSeconds(30).toMillis()
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                        },
                    )
                }
                defaultRequest {
                    accept(ContentType.Application.Json)
                }
            }
    }
}
