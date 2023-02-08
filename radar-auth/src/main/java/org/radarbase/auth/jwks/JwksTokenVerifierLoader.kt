package org.radarbase.auth.jwks

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.authentication.TokenVerifierLoader
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.jwt.JwtRadarToken
import org.radarbase.auth.jwt.JwtTokenVerifier
import org.slf4j.LoggerFactory
import java.time.Duration

class JwksTokenVerifierLoader(
    private val url: String,
    private val resourceName: String,
    private val algorithmParser: JwkParser,
) : TokenVerifierLoader {
    private val httpClient = HttpClient(CIO).config {
        install(HttpTimeout) {
            connectTimeoutMillis = Duration.ofSeconds(10).toMillis()
            socketTimeoutMillis = Duration.ofSeconds(10).toMillis()
            requestTimeoutMillis = Duration.ofSeconds(30).toMillis()
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        defaultRequest {
            url(this@JwksTokenVerifierLoader.url)
            accept(ContentType.Application.Json)
        }
    }

    override suspend fun fetch(): List<TokenVerifier> {
        val keySet = try {
            fetchPublicKeyInfo()
        } catch (ex: Exception) {
            logger.warn("Failed to fetch token for {}: {}", url, ex.message)
            return listOf()
        }
        return buildList(keySet.keys.size) {
            keySet.keys.forEach { key ->
                try {
                    add(
                        algorithmParser.parse(key)
                            .toTokenVerifier(resourceName)
                    )
                } catch (ex: Exception) {
                    logger.error("Failed to parse key from {}: {}", url, ex.message)
                }
            }
        }
    }

    private suspend fun fetchPublicKeyInfo(): JsonWebKeySet = withContext(Dispatchers.IO) {
        logger.info("Getting the JWT public key at {}", url)
        val response = httpClient.request()

        if (!response.status.isSuccess()) {
            throw TokenValidationException("Cannot fetch token keys (${response.status}) - ${response.bodyAsText()}")
        }

        response.body()
    }

    override fun toString(): String = "MPTokenKeyAlgorithmKeyLoader<url=$url>"

    companion object {
        @JvmStatic
        fun Algorithm.toTokenVerifier(resourceName: String): JwtTokenVerifier {
            val verifier = JWT.require(this).run {
                withClaimPresence(JwtRadarToken.SCOPE_CLAIM)
                withAudience(resourceName)
                build()
            }
            return JwtTokenVerifier(name, verifier)
        }

        private val logger = LoggerFactory.getLogger(JwksTokenVerifierLoader::class.java)
    }
}
