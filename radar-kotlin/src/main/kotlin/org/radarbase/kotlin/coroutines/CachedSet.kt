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

/**
 * Set of data that is cached for a duration of time.
 *
 * @param supplier How to update the cache.
 */
class CachedSet<T>(
    cacheConfig: CacheConfig = CacheConfig(),
    supplier: suspend () -> Set<T>,
): CachedValue<Set<T>>(cacheConfig, supplier) {
    /** Whether the cache contains [value]. If it does not contain the value and [CacheConfig.retryDuration]
     * has passed since the last try, it will update the cache and try once more. */
    suspend fun contains(value: T): Boolean = test { value in it }

    /**
     * Find a value matching [predicate].
     * If it does not contain the value and [CacheConfig.retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     * @return value if found and null otherwise
     */
    suspend fun find(predicate: (T) -> Boolean): T? = query({ it.find(predicate) }, { it != null }).value

    /**
     * Get the value.
     * If the cache is empty and [CacheConfig.retryDuration]
     * has passed since the last try, it will update the cache and try once more.
     */
    override suspend fun get(): Set<T> = get { it.isNotEmpty() }.value
}
