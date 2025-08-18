package io.github.abappi19.kmm_query.core

import io.github.abappi19.kmm_query.cache.QueryPersistor
import kotlinx.coroutines.CoroutineScope

/**
 * Configuration container for individual query instances.
 *
 * @param key Unique identifier for the query, used for caching and deduplication.
 *            Should be stable across component re-renders.
 * @param fetcher Suspending function that executes the data fetching operation
 * @param scope Coroutine scope for managing query execution lifecycle
 * @param cacheMode Override caching strategy for this query (defaults to manager's setting)
 * @param cacheTimeMillis Maximum duration (ms) to keep unused data in cache before garbage collection
 * @param staleTimeMillis Duration (ms) after which fresh data is considered stale
 * @param retryCount Number of automatic retry attempts on network errors
 * @param persistor Persistent storage adapter for cross-session caching
 *
 * @see QueryManagerConfig For global configuration defaults
 * @see CacheMode For available caching strategies
 */
data class QueryConfig<T>(
    val key: List<Any>,
    val fetcher: suspend () -> T,
    val scope: CoroutineScope,
    val cacheMode: CacheMode?,
    val cacheTimeMillis: Long?,
    val staleTimeMillis: Long?,
    val retryCount: Int?,
    val persistor: QueryPersistor?,
)

/**
 * Global configuration defaults for all queries managed by a QueryManager.
 *
 * @property cacheTimeMillis Default: 5 minutes (300,000 ms)
 * @property staleTimeMillis Default: 0 ms (immediately stale)
 * @property retryCount Default: 0 (no automatic retries)
 * @property cacheMode Default: CACHE_FIRST
 * @property persistor Default in-memory cache (no persistence)
 */
data class QueryManagerConfig(
    val cacheTimeMillis: Long?,
    val staleTimeMillis: Long?,
    val retryCount: Int? = 0,
    val cacheMode: CacheMode? = CacheMode.CACHE_FIRST,
    val persistor: QueryPersistor? = null,
)