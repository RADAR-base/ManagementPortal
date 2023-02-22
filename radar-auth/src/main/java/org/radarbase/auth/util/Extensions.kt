package org.radarbase.auth.util

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlin.coroutines.CoroutineContext

/**
 * Transform each value in the iterable in a separate coroutine and await termination.
 */
internal suspend inline fun <T, R> Iterable<T>.forkJoin(
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
internal suspend inline fun <T> consumeFirst(
    coroutineContext: CoroutineContext = Dispatchers.Default,
    crossinline producer: suspend CoroutineScope.(emit: suspend (T) -> Unit) -> Unit
): T = coroutineScope {
    val channel = Channel<T>()

    val producerJob = launch(coroutineContext) {
        producer(channel::send)
        channel.close()
    }

    val result = channel.consume { receive() }
    producerJob.cancel()
    result
}

internal operator fun <T> Set<T>.plus(elements: Set<T>): Set<T> = when {
    isEmpty() -> elements
    elements.isEmpty() -> this
    else -> buildSet(size + elements.size) {
        addAll(this)
        addAll(elements)
    }
}
