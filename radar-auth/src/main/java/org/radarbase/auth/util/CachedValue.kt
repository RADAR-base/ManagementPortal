package org.radarbase.auth.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

/**
 * Caches a value with full support for coroutines.
 * Only one coroutine context will compute the value at a time, other coroutine contexts will wait
 * for it to finish.
 */
class CachedValue<T>(
    private val minAge: Duration = Duration.ofMinutes(1),
    private val maxAge: Duration = Duration.ofHours(3),
    private val compute: suspend () -> T,
) {
    init {
        require(minAge > Duration.ZERO) { "Cache fetch duration $minAge must be positive" }
        require(maxAge >= minAge) { "Cache maximum age $maxAge must be at least fetch timeout $minAge" }
    }

    private val cache = AtomicReference<CompletableDeferred<CacheContents<T>>>()

    /**
     * Get cached value. If the cache is expired, fetch it again. The first coroutine context
     * that reaches this method will call [compute], others coroutine contexts will use the value
     * computed by the first.
     */
    suspend fun get(refresh: Boolean = false): CacheResult<T> {
        val deferred = raceForDeferred()

        val result: CacheContents<T>

        if (deferred is CacheMiss) {
            result = try {
                CacheValue(compute())
            } catch (ex: Throwable) {
                CacheError(ex)
            }
            deferred.value.complete(result)
        } else {
            result = deferred.value.await()
            // If the result is expired, refetch.
            if (result.isExpired(refresh)) {
                // Either no new coroutine context had updated the cache value, then update it to
                // null. Otherwise, another suspend context is active and get() will await the
                // result from that context
                cache.compareAndSet(deferred.value, null)
                return get(refresh = false)
            }
        }

        return when (result) {
            is CacheValue -> if (deferred is CacheMiss) CacheMiss(result.value) else CacheHit(result.value)
            is CacheError -> throw result.exception
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

    private fun CacheContents<*>.isExpired(refresh: Boolean): Boolean = when {
        this is CacheError && exception is CancellationException -> true
        refresh || this is CacheError -> isExpired(minAge)
        else -> isExpired(maxAge)
    }

    fun clear() {
        cache.set(null)
    }

    private sealed class CacheContents<T> {
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
