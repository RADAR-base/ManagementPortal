package org.radarbase.auth.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Semaphore
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

internal typealias DeferredCache<T> = CompletableDeferred<CachedValue.CacheContents<T>>

/**
 * Caches a value with full support for coroutines.
 * Only one coroutine context will compute the value at a time, other coroutine contexts will wait
 * for it to finish.
 */
class CachedValue<T>(
    /** Duration after which the cache is considered stale and should be refreshed. */
    val refreshDuration: Duration = Duration.ofMinutes(30),
    /** Duration after which the cache may be refreshed if the cache does not fulfill a certain
     * requirement. This should be shorter than [refreshDuration] to have effect. */
    val retryDuration: Duration = Duration.ofMinutes(1),
    /** Time to wait for a lock to come free when an exception is set for the cache. */
    val exceptionLockDuration: Duration = Duration.ofSeconds(10),
    /**
     * Number of simultaneous computations that may occur. Increase if the time to computation
     * is very variable.
     */
    val maxSimultaneousCompute: Int = 1,
    private val compute: suspend () -> T,
) {

    private val cache = AtomicReference<CompletableDeferred<CacheContents<T>>>()
    private val semaphore: Semaphore? = if (maxSimultaneousCompute > 1) {
        Semaphore(maxSimultaneousCompute - 1)
    } else {
        null
    }

    init {
        require(retryDuration > Duration.ZERO) { "Cache fetch duration $retryDuration must be positive" }
        require(refreshDuration >= retryDuration) { "Cache maximum age $refreshDuration must be at least fetch timeout $retryDuration" }
        require(maxSimultaneousCompute > 0) { "At least one context must be able to compute the result" }
    }

    /**
     * Get cached value. If the cache is expired, fetch it again. The first coroutine context
     * that reaches this method will call [computeAndCache], others coroutine contexts will use the value
     * computed by the first.
     */
    suspend fun get(retryCondition: (T) -> Boolean = { false }): CacheResult<T> {
        val deferredResult = raceForDeferred()
        val deferred = deferredResult.value

        return if (deferredResult is CacheMiss) {
            deferred.computeAndCache()
        } else {
            deferred.concurrentComputeAndCache()
                ?: deferred.awaitCache(retryCondition)
        }
    }

    private suspend fun DeferredCache<T>.computeAndCache(): CacheResult<T> {
        val result = try {
            val value = compute()
            complete(CacheValue(value))
            value
        } catch (ex: Throwable) {
            complete(CacheError(ex))
            throw ex
        }
        return CacheMiss(result)
    }

    private suspend fun DeferredCache<T>.awaitCache(retry: (T) -> Boolean): CacheResult<T> {
        val result = await()
        return if (result.isExpired(retry)) {
            // Either no new coroutine context had updated the cache value, then update it to
            // null. Otherwise, another suspend context is active and get() will await the
            // result from that context
            cache.compareAndSet(this, null)
            get()
        } else {
            val value = result.getOrThrow()
            CacheHit(value)
        }
    }

    private suspend fun DeferredCache<T>.concurrentComputeAndCache(): CacheResult<T>? {
        semaphore ?: return null
        if (isCompleted || !semaphore.tryAcquire()) return null

        return try {
            if (isCompleted) {
                null
            } else {
                computeAndCache()
            }
        } finally {
            semaphore.release()
        }
    }

    /**
     * Race for the first suspend context to create a CompletableDeferred object. All other contexts
     * will use that context to read their values.
     *
     * @return a pair of a CompletableDeferred value and a boolean, if true this context is the
     *          winner, if false this should use the deferred to read its value.
     */
    private fun raceForDeferred(): CacheResult<CompletableDeferred<CacheContents<T>>> {
        var result: CacheResult<CompletableDeferred<CacheContents<T>>>

        do {
            val previousDeferred = cache.get()
            result = if (previousDeferred == null) {
                CacheMiss(CompletableDeferred())
            } else {
                CacheHit(previousDeferred)
            }
        } while (!cache.compareAndSet(previousDeferred, result.value))

        return result
    }

    private fun CacheContents<T>.isExpired(retry: (T) -> Boolean): Boolean = when {
        this is CacheError -> exception is CancellationException || isExpired(exceptionLockDuration)
        this is CacheValue && retry(value) -> isExpired(retryDuration)
        else -> isExpired(refreshDuration)
    }

    fun clear() {
        cache.set(null)
    }

    internal sealed class CacheContents<T> {
        val time: Instant = Instant.now()

        @Volatile
        private var isExpired = false

        fun isExpired(age: Duration): Boolean = when {
            isExpired -> true
            Instant.now() > time + age -> {
                isExpired = true
                true
            }
            else -> false
        }

        fun getOrThrow() = when (this) {
            is CacheValue -> value
            is CacheError -> throw exception
        }
    }

    private class CacheError<T>(
        val exception: Throwable,
    ): CacheContents<T>()

    private class CacheValue<T>(
        val value: T,
    ): CacheContents<T>()

    sealed interface CacheResult<T> {
        val value: T
    }
    data class CacheHit<T>(override val value: T): CacheResult<T>
    data class CacheMiss<T>(override val value: T): CacheResult<T>
}
