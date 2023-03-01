@file:Suppress("unused")

package org.radarbase.kotlin.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

/**
 * Try to acquire a semaphore permit, and run [block] if successful.
 * If this cannot be achieved without blocking, return null.
 * @return result of [block] or null if no permit could be acquired.
 */
suspend fun <T> Semaphore.tryWithPermitOrNull(block: suspend () -> T): T? {
    if (!tryAcquire()) return null
    return try {
        block()
    } finally {
        release()
    }
}

/**
 * Get a future value via coroutine suspension.
 * The future is evaluated in context [Dispatchers.IO].
 */
suspend fun <T> Future<T>.suspendGet(
    duration: Duration? = null,
): T = coroutineScope {
    val channel = Channel<Unit>()
    launch {
        try {
            channel.receive()
        } catch (ex: CancellationException) {
            cancel(true)
        }
    }
    try {
        withContext(Dispatchers.IO) {
            if (duration != null) {
                get(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            } else {
                get()
            }
        }
    } catch (ex: InterruptedException) {
        throw CancellationException("Future was interrupted", ex)
    } finally {
        channel.send(Unit)
    }
}

/**
 * Transform each value in the iterable in a separate coroutine and await termination.
 */
suspend inline fun <T, R> Iterable<T>.forkJoin(
    coroutineContext: CoroutineContext = Dispatchers.Default,
    crossinline transform: suspend CoroutineScope.(T) -> R
): List<R> = coroutineScope {
    map { t -> async(coroutineContext) { transform(t) } }
        .awaitAll()
}

/**
 * Consume the first value produced by the producer on its provided channel. Once a value is sent
 * by the producer, its coroutine is cancelled.
 * @throws kotlinx.coroutines.channels.ClosedReceiveChannelException if the producer does not
 *         produce any values.
 */
suspend inline fun <T> consumeFirst(
    coroutineContext: CoroutineContext = Dispatchers.Default,
    crossinline producer: suspend CoroutineScope.(emit: suspend (T) -> Unit) -> Unit
): T = coroutineScope {
    val channel = Channel<T>()

    val producerJob = launch(coroutineContext) {
        try {
            producer(channel::send)
        } finally {
            channel.close()
        }
    }

    val result = channel.consume { receive() }
    producerJob.cancel()
    result
}

/**
 * Transforms each value with [transform] and returns the first value where [predicate] returns
 * true. Each value is transformed and evaluated in its own async context. If no transformed value
 * satisfies predicate, null is returned.
 */
suspend fun <T, R>  Iterable<T>.forkFirstOfOrNull(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    transform: suspend CoroutineScope.(T) -> R,
    predicate: suspend CoroutineScope.(R) -> Boolean,
): R? = consumeFirst(coroutineContext) { emit ->
    forkJoin(coroutineContext) { t ->
        val result = transform(t)
        if (predicate(result)) {
            emit(result)
        }
    }
    emit(null)
}

suspend fun <T, R>  Iterable<T>.forkFirstOfNotNullOrNull(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    transform: suspend CoroutineScope.(T) -> R?
): R? = forkFirstOfOrNull(coroutineContext, transform) { it != null }

/**
 * Returns true as soon as [predicate] returns true on a value, or false if [predicate] does
 * not return true on any of the values. All values are evaluated in a separate async context using
 * [forkJoin].
 */
suspend fun <T> Iterable<T>.forkAny(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    predicate: suspend CoroutineScope.(T) -> Boolean
): Boolean = forkFirstOfOrNull(coroutineContext, predicate) { it } ?: false

operator fun <T> Set<T>.plus(elements: Set<T>): Set<T> = when {
    isEmpty() -> elements
    elements.isEmpty() -> this
    else -> buildSet(size + elements.size) {
        addAll(this)
        addAll(elements)
    }
}
