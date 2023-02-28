/*
 *  Copyright 2020 The Hyve
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.radarbase.kotlin.coroutines

/** Set of data that is cached for a duration of time. */
class CachedMap<K,V>(
    cacheConfig: CacheConfig = CacheConfig(),
    supplier: suspend () -> Map<K,V>,
): CachedValue<Map<K, V>>(cacheConfig, supplier) {
    /** Whether the cache contains [key]. If it does not contain the value and [CacheConfig.retryDuration]
     * has passed since the last try, it will update the cache and try once more. */
    suspend fun contains(key: K): Boolean = test { key in it }

    /**
     * Find a pair matching [predicate].
     * If it does not contain the value and [CacheConfig.retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     * @return value if found and null otherwise
     */
    suspend fun find(predicate: (K, V) -> Boolean): Pair<K, V>? = query(
        { map ->
            map.entries
                .find { (k, v) -> predicate(k, v) }
                ?.toPair()
        },
        { it != null },
    ).value

    /**
     * Find a pair matching [predicate].
     * If it does not contain the value and [CacheConfig.retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     * @return value if found and null otherwise
     */
    suspend fun findValue(predicate: (V) -> Boolean): V? = query(
        { map -> map.values.find { predicate(it) } },
        { it != null },
    ).value

    /**
     * Get the value.
     * If the cache is empty and [CacheConfig.retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     */
    override suspend fun get(): Map<K, V> = get { it.isNotEmpty() }.value

    /**
     * Get the value.
     * If the cache is empty and [CacheConfig.retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     */
    suspend fun get(key: K): V? = query({ it[key] }, { it != null }).value
}
