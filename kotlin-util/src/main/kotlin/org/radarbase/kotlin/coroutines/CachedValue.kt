package org.radarbase.kotlin.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Semaphore
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal typealias DeferredCache<T> = CompletableDeferred<CachedValue.CacheContents<T>>

/**
 * Caches a value with full support for coroutines. The value that will be cached is computed by
 * [supplier].
 * Only one coroutine context will compute the value at a time, other coroutine contexts will wait
 * for it to finish.
 */
open class CachedValue<T>(
    private val config: CacheConfig,
    private val supplier: suspend () -> T,
) {
    private val cache = AtomicReference<CompletableDeferred<CacheContents<T>>>()
    private val semaphore: Semaphore? = if (config.maxSimultaneousCompute > 1) {
        Semaphore(config.maxSimultaneousCompute - 1)
    } else {
        null
    }

    /**
     * Query the cached value by running [transform] and return its result if valid. If
     * [evaluate] returns false on the result, the cache computation is reevaluated if
     * [CacheConfig.retryDuration] has been reached.
     */
    suspend fun <R> query(
        transform: suspend (T) -> R,
        evaluate: (R) -> Boolean = { true },
    ): CacheResult<R> {
        val deferredResult = raceForDeferred()
        val deferred = deferredResult.value

        return if (deferredResult is CacheMiss) {
            val result = deferred.computeAndCache()
            CacheMiss(transform(result))
        } else {
            val concurrentResult = deferred.concurrentComputeAndCache()
            if (concurrentResult != null) {
                CacheMiss(transform(concurrentResult))
            } else {
                deferred.awaitCache(transform, evaluate)
            }
        }
    }

    suspend fun isStale(): Boolean {
        val currentDeferred = cache.get() ?: return true
        if (!currentDeferred.isCompleted) return false
        return currentDeferred.await().isExpired { true }
    }

    /**
     * Get cached value. If the cache is expired, fetch it again. The first coroutine context
     * that reaches this method will call [computeAndCache], others coroutine contexts will use the
     * value computed by the first. The result is not computed more
     * often than [CacheConfig.retryDuration]. If the result was an exception, the exception is
     * rethrown from cache. It is recomputed if the [CacheConfig.exceptionCacheDuration] has passed.
     */
    open suspend fun get(): T = query({ it }) { false }.value

    /**
     * Get cached value. If the cache is expired, fetch it again. The first coroutine context
     * that reaches this method will call [computeAndCache], others coroutine contexts will use the
     * value computed by the first. If the value was retrieved from cache and [evaluate]
     * returns false for that value, the result is recomputed. The result is not computed more
     * often than [CacheConfig.retryDuration]. If the result was an exception, the exception is
     * rethrown from cache. It is recomputed if the [CacheConfig.exceptionCacheDuration] has passed.
     */
    suspend inline fun get(noinline evaluate: (T) -> Boolean): CacheResult<T> = query({ it }, evaluate)

    /**
     * Test the cached value by running [predicate] and return its result if true. If
     * [predicate] returns false on the result, the cache computation is reevaluated if
     * [CacheConfig.retryDuration] has been reached.
     */
    suspend inline fun test(noinline predicate: (T) -> Boolean): Boolean {
        return query(predicate) { it }.value
    }

    private suspend fun DeferredCache<T>.computeAndCache(): T {
        val result = try {
            val value = supplier()
            complete(CacheValue(value))
            value
        } catch (ex: Throwable) {
            complete(CacheError(ex))
            throw ex
        }
        return result
    }

    private suspend fun DeferredCache<T>.concurrentComputeAndCache(): T? {
        if (isCompleted) return null

        return semaphore?.tryWithPermitOrNull {
            if (isCompleted) {
                null
            } else {
                computeAndCache()
            }
        }
    }

    private suspend fun <R> DeferredCache<T>.awaitCache(
        transform: suspend (T) -> R,
        evaluate: (R) -> Boolean,
    ): CacheResult<R> {
        val result = await().map(transform)
        return if (result.isExpired(evaluate)) {
            // Either no new coroutine context had updated the cache value, then update it to
            // null. Otherwise, another suspend context is active and get() will await the
            // result from that context
            cache.compareAndSet(this, null)
            query(transform) { false }
        } else {
            val value = result.getOrThrow()
            CacheHit(value)
        }
    }

    /**
     * Race for the first suspend context to create a CompletableDeferred object. All other contexts
     * will use that context to read their values.
     *
     * @return a pair of a CompletableDeferred value and a boolean, if true this context is the
     *          winner, if false this should use the deferred to read its value.
     */
    private fun raceForDeferred(): CacheResult<DeferredCache<T>> {
        var result: CacheResult<DeferredCache<T>>

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

    private inline fun <R> CacheContents<R>.isExpired(evaluate: (R) -> Boolean): Boolean = when {
        this is CacheError -> exception is CancellationException ||
                isExpired(config.exceptionCacheDuration)
        this is CacheValue && !evaluate(value) -> isExpired(config.retryDuration)
        else -> isExpired(config.refreshDuration)
    }

    fun clear() {
        cache.set(null)
    }

    @OptIn(ExperimentalTime::class)
    internal sealed class CacheContents<T>(
        initialDuration: Duration? = null,
        time: TimeMark? = null,
    ) {
        protected val time: TimeMark

        init {
            var now = time ?: TimeSource.Monotonic.markNow()
            if (initialDuration != null) {
                now -= initialDuration
            }
            this.time = now
        }

        @Volatile
        private var isExpired = false

        fun isExpired(age: Duration): Boolean = when {
            isExpired -> true
            (time + age).hasPassedNow() -> {
                isExpired = true
                true
            }
            else -> false
        }

        abstract fun getOrThrow(): T

        @Suppress("UNCHECKED_CAST")
        abstract suspend fun <R> map(transform: suspend (T) -> R): CacheContents<R>
    }

    @OptIn(ExperimentalTime::class)
    internal class CacheError<T>(
        val exception: Throwable,
    ) : CacheContents<T>() {
        override fun getOrThrow(): T = throw exception
        @Suppress("UNCHECKED_CAST")
        override suspend fun <R> map(transform: suspend (T) -> R): CacheContents<R> = this as CacheError<R>
    }

    @OptIn(ExperimentalTime::class)
    internal class CacheValue<T>(
        val value: T,
        initialDuration: Duration? = null,
        time: TimeMark? = null,
    ) : CacheContents<T>(initialDuration, time) {
        override fun getOrThrow(): T = value

        override suspend fun <R> map(transform: suspend (T) -> R): CacheContents<R> = try {
            CacheValue(transform(value), time = time)
        } catch (ex: Throwable) {
            CacheError(ex)
        }
    }

    sealed interface CacheResult<T> {
        val value: T
    }

    data class CacheHit<T>(override val value: T) : CacheResult<T>
    data class CacheMiss<T>(override val value: T) : CacheResult<T>

    companion object {
        private val logger = LoggerFactory.getLogger(CachedValue::class.java)
    }
}
