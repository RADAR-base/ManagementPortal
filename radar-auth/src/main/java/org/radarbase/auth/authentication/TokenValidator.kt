package org.radarbase.auth.authentication

import com.auth0.jwt.exceptions.AlgorithmMismatchException
import kotlinx.coroutines.*
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.RadarToken
import org.radarbase.kotlin.coroutines.CacheConfig
import org.radarbase.kotlin.coroutines.CachedValue
import org.radarbase.kotlin.coroutines.consumeFirst
import org.radarbase.kotlin.coroutines.forkJoin
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.time.toKotlinDuration

private typealias TokenVerifierCache = CachedValue<List<TokenVerifier>>

/**
 * Validates JWT token signed by the Management Portal. It may be used from multiple coroutine
 * contexts.
 */
class TokenValidator
@JvmOverloads
constructor(
    /** Loaders for token verifiers to use in the token authenticator. */
    verifierLoaders: List<TokenVerifierLoader>?,
    /** Minimum fetch timeout before a token is attempted to be fetched again. */
    fetchTimeout: Duration = Duration.ofMillis(1),
    /** Maximum time that the token verifier does not need to be fetched. */
    maxAge: Duration = Duration.ofDays(1),
) {
    private val algorithmLoaders: List<TokenVerifierCache>?

    init {
        val config = CacheConfig(
            retryDuration = fetchTimeout.toKotlinDuration(),
            refreshDuration = maxAge.toKotlinDuration(),
            maxSimultaneousCompute = 2,
        )
        algorithmLoaders = verifierLoaders?.map { loader ->
            CachedValue(config, supplier = loader::fetch)
        }
    }

    /**
     * Validates an access token and returns the token as a [RadarToken] object.
     *
     * This will load all the verifiers. If a token cannot be verified, this method will fetch
     * the verifiers again, as the source may have changed. It will then and re-check the token.
     * However, the public key will not be fetched more than once every `fetchTimeout`,
     * to prevent (malicious) clients from loading external token verifiers too frequently.
     *
     * This implementation calls [runBlocking]. If calling from Kotlin, prefer to use [validate]
     * with coroutines instead.
     *
     * @param token The access token
     * @return The decoded access token
     * @throws TokenValidationException If the token can not be validated.
     */
    @Throws(TokenValidationException::class)
    fun validateBlocking(token: String): RadarToken = runBlocking {
        validate(token)
    }

    /**
     * Validates an access token and returns the token as a [RadarToken] object.
     *
     * This will load all the verifiers. If a token cannot be verified, this method will fetch
     * the verifiers again, as the source may have changed. It will then and re-check the token.
     * However, the public key will not be fetched more than once every `fetchTimeout`,
     * to prevent (malicious) clients from loading external token verifiers too frequently.
     *
     * @param token The access token
     * @return The decoded access token
     * @throws TokenValidationException If the token can not be validated.
     */
    @Throws(TokenValidationException::class)
    suspend fun validate(token: String): RadarToken {
        val result: Result<RadarToken> = consumeFirst { emit ->
            val causes = algorithmLoaders
                ?.forkJoin { cache ->
                    val result = cache.verify(token)
                    // short-circuit to return the first successful result
                    if (result.isSuccess) emit(result)
                    result
                }
                ?.flatMap {
                    it.exceptionOrNull()
                        ?.suppressedExceptions
                        ?: emptyList()
                } ?: emptyList()

            val message = if (causes.isEmpty()) {
                "No registered validator in could authenticate this token"
            } else {
                val suppressedMessage = causes.joinToString { it.message ?: it.javaClass.simpleName }
                "No registered validator in could authenticate this token: $suppressedMessage"
            }
            emit(TokenValidationException(message).toFailure(causes))
        }

        return result.getOrThrow()
    }

    /** Refresh the token verifiers from cache on the next validation. */
    fun refresh() {
        algorithmLoaders?.forEach { it.clear() }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenValidator::class.java)

        /**
         * Verify the token using the TokenVerifier lists from cache.
         * If verification fails and the TokenVerifier list was retrieved from cache
         * try to reload the TokenVerifier list and verify again.
         * If none of the verifications succeed, return a result of TokenValidationException
         * with suppressed exceptions all the exceptions returned from a TokenVerifier.
         */
        private suspend fun TokenVerifierCache.verify(token: String): Result<RadarToken> {
            val verifiers = getOrEmpty { false }

            val firstResult = verifiers.value.anyVerify(token)
            if (
                firstResult.isSuccess ||
                // already fetched new verifiers, no need to fetch it again
                verifiers is CachedValue.CacheMiss
            ) {
                return firstResult
            }

            val refreshedVerifiers = getOrEmpty { true }
            return if (refreshedVerifiers != verifiers) {
                refreshedVerifiers.value.anyVerify(token)
            } else {
                // The verifiers didn't change, so the result won't change
                firstResult
            }
        }

        private suspend fun List<TokenVerifier>.anyVerify(token: String): Result<RadarToken> {
            var exceptions: MutableList<Throwable>? = null

            forEach { verifier ->
                try {
                    val radarToken = verifier.verify(token)
                    return Result.success(radarToken)
                } catch (ex: Throwable) {
                    if (ex !is AlgorithmMismatchException) {
                        if (exceptions == null) {
                            exceptions = mutableListOf()
                        }
                        exceptions!!.add(ex)
                    }
                }
            }

            return TokenValidationException("Failed to validate token")
                .toFailure(exceptions ?: emptyList())
        }

        private suspend fun TokenVerifierCache.getOrEmpty(
            refresh: (List<TokenVerifier>) -> Boolean
        ): CachedValue.CacheResult<List<TokenVerifier>> =
            try {
                get(refresh)
            } catch (ex: Throwable) {
                logger.warn("Failed to load authentication algorithm keys: {}", ex.message)
                CachedValue.CacheMiss(emptyList())
            }

        private fun <T> Throwable.toFailure(causes: Iterable<Throwable> = emptyList()): Result<T> {
            causes.forEach { addSuppressed(it) }
            return Result.failure(this)
        }
    }
}
