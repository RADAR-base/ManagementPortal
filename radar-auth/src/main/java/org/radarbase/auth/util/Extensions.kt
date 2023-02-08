package org.radarbase.auth.util

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consume

internal suspend fun <T, R> Iterable<T>.forkJoin(convert: suspend (T) -> R): List<R> = coroutineScope {
    map { t -> async { convert(t) } }
        .awaitAll()
}

internal  suspend fun <T> consumeFirst(producer: suspend CoroutineScope.(SendChannel<T>) -> Unit): T = coroutineScope {
    val channel = Channel<T>()

    val producerJob = launch {
        producer(channel)
        channel.close()
    }

    val result = channel.consume { receive() }
    producerJob.cancel()
    result
}
