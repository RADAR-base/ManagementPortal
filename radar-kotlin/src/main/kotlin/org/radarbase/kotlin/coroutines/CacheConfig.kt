package org.radarbase.kotlin.coroutines

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class CacheConfig(
    /** Duration after which the cache is considered stale and should be refreshed. */
    val refreshDuration: Duration = 30.minutes,
    /** Duration after which the cache may be refreshed if the cache does not fulfill a certain
     * requirement. This should be shorter than [refreshDuration] to have effect. */
    val retryDuration: Duration = 1.minutes,
    /** Time until the result may be recomputed when an exception is set for the cache. */
    val exceptionCacheDuration: Duration = 10.seconds,
    /**
     * Number of simultaneous computations that may occur. Increase if the time to computation
     * is very variable.
     */
    val maxSimultaneousCompute: Int = 1,
) {
    init {
        require(retryDuration > Duration.ZERO) { "Cache fetch duration $retryDuration must be positive" }
        require(refreshDuration >= retryDuration) { "Cache maximum age $refreshDuration must be at least fetch timeout $retryDuration" }
        require(maxSimultaneousCompute > 0) { "At least one context must be able to compute the result" }
    }
}
