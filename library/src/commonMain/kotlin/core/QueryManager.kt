package io.github.abappi19.kmm_query.core

import io.github.abappi19.kmm_query.cache.DefaultQueryPersistor
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
class QueryManager(
    val queryManagerConfig: QueryManagerConfig = QueryManagerConfig(
        cacheTimeMillis = 0,
        staleTimeMillis = 0,
        cacheMode = CacheMode.CACHE_FIRST,
        retryCount = 0
    ),
) {
    val persistor = queryManagerConfig.persistor ?: DefaultQueryPersistor()
}
