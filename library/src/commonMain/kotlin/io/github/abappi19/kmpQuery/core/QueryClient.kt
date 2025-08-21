package io.github.abappi19.kmpQuery.core

import io.github.abappi19.kmpQuery.cache.DefaultQueryPersistor
import kotlin.time.ExperimentalTime

/**
 * Client for managing queries with configurable settings and persistence.
 *
 * @param queryManagerConfig Configuration settings for query management
 */

@OptIn(ExperimentalTime::class)
class QueryClient(
    val queryManagerConfig: QueryManagerConfig = QueryManagerConfig(
        cacheTimeMillis = 0,
        staleTimeMillis = 0,
        refetchOnLaunch = true,
        cacheMode = CacheMode.CACHE_FIRST,
        retryCount = 0
    ),
) {
    val persistor = queryManagerConfig.persistor ?: DefaultQueryPersistor()
}