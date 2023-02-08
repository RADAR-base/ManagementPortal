package org.radarbase.auth.authentication

import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.interfaces.DecodedJWT
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consume
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.RadarToken
import org.radarbase.auth.util.CachedValue
import org.radarbase.auth.util.consumeFirst
import org.radarbase.auth.util.forkJoin
import org.slf4j.LoggerFactory
import java.time.Duration

private typealias TokenVerifierCache = CachedValue<List<TokenVerifier>>

/**
 * Validates JWT token signed by the Management Portal. It may be used from multiple threads.
 * If the status of the public key should be checked immediately, call
 * [.refresh] directly after creating this validator. It currently does not check this, so
 * that the validator can be used even if a remote ManagementPortal is not reachable during
 * construction.
 */
class TokenValidator
@JvmOverloads
constructor(
    /** Loaders for token verifiers to use in the token authenticator. */
    verifierLoaders: List<TokenVerifierLoader>,
    /** Minimum fetch timeout before a token is attempted to be fetched again. */
    fetchTimeout: Duration = Duration.ofMinutes(1),
    /** Maximum time that the token verifier does not need to be fetched. */
    maxAge: Duration = Duration.ofDays(1),
) {
    private val algorithmLoaders: List<TokenVerifierCache> = verifierLoaders.map { loader ->
        CachedValue(
            minAge = fetchTimeout,
            maxAge = maxAge,
        ) {
            loader.fetch()
        }
    }

    /**
     * Validates an access token and returns the token as a [RadarToken] object.
     *
     * If we have not yet fetched the JWT public key, this method will fetch it. If a signature can
     * not be verified, this method will fetch the JWT public key again, as it might have been
     * changed, and re-check the token. However, this fetching of the public key will only be
     * performed at most once every `fetchTimeout` seconds, to prevent (malicious)
     * clients from making us call the token endpoint too frequently.
     *
     * @param token The access token
     * @return The decoded access token
     * @throws TokenValidationException If the token can not be validated.
     */
    @Throws(TokenValidationException::class)
    fun authenticateBlocking(token: String): RadarToken = runBlocking {
        authenticate(token)
    }

    /**
     * Validates an access token and returns the decoded JWT as a [DecodedJWT] object.
     *
     * If we have not yet fetched the JWT public key, this method will fetch it. If a signature can
     * not be verified, this method will fetch the JWT public key again, as it might have been
     * changed, and re-check the token. However, this fetching of the public key will only be
     * performed at most once every `fetchTimeout` seconds, to prevent (malicious)
     * clients from making us call the token endpoint too frequently.
     *
     * @param token The access token
     * @return The decoded access token
     * @throws TokenValidationException If the token can not be validated.
     */
    @Throws(TokenValidationException::class)
    suspend fun authenticate(token: String): RadarToken {
        val result: Result<RadarToken> = consumeFirst { channel ->
            val errors = algorithmLoaders
                .forkJoin { cache ->
                    val result = cache.verify(token)
                    // short-circuit to return the first successful result
                    if (result.isSuccess) channel.send(result)
                    result
                }
                .mapNotNull { it.exceptionOrNull() }
                .flatMap { it.suppressedExceptions }

            val suppressedMessage = errors.joinToString { it.message ?: it.javaClass.simpleName }
            channel.send(
                TokenValidationException("No registered validator in could authenticate this token: $suppressedMessage")
                    .toFailure(errors)
            )
        }

        return result.getOrThrow()
    }

    /** Refresh the token verifiers from cache on the next validation. */
    fun refresh() {
        algorithmLoaders.forEach { it.clear() }
    }

    /**
     * Verify the token using the TokenVerifier lists from cache.
     * If verification fails and the TokenVerifier list was retrieved from cache
     * try to reload the TokenVerifier list and verify again.
     * If none of the verifications succeed, return a result of TokenValidationException
     * with suppressed exceptions all the exceptions returned from a TokenVerifier.
     */
    private suspend fun TokenVerifierCache.verify(token: String): Result<RadarToken> {
        val verifiers = getOrEmpty(false)

        var results = verifiers.value.map { it.runCatching { verify(token) } }
        results.find { it.isSuccess }?.let { return it }

        // already fetched a new value, no need to fetch it again
        if (verifiers is CachedValue.CacheMiss) return results.toValidationExceptionResult()

        val refreshedVerifiers = getOrEmpty(true)
        if (refreshedVerifiers == verifiers) return results.toValidationExceptionResult()

        results = refreshedVerifiers.value.map { it.runCatching { verify(token) } }
        results.find { it.isSuccess }?.let { return it }
        return results.toValidationExceptionResult()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenValidator::class.java)

        private suspend fun TokenVerifierCache.getOrEmpty(
            refresh: Boolean
        ): CachedValue.CacheResult<List<TokenVerifier>> =
            try {
                get(refresh)
            } catch (ex: Throwable) {
                logger.warn("Failed to load authentication algorithm keys: {}", ex.message)
                CachedValue.CacheMiss(emptyList())
            }

        private fun List<Result<RadarToken>>.toValidationExceptionResult(): Result<RadarToken> {
            val exceptions = mapNotNull { result ->
                result.exceptionOrNull()
                    ?.takeIf { it !is AlgorithmMismatchException }
            }

            return TokenValidationException("Failed to validate token")
                .toFailure(exceptions)
        }

        private fun <T> Throwable.toFailure(causes: Iterable<Throwable> = emptyList()): Result<T> {
            causes.forEach { addSuppressed(it) }
            return Result.failure(this)
        }
    }
}
