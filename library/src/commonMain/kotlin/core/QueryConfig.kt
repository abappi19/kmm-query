package io.github.abappi19.kmm_query.core

import io.github.abappi19.kmm_query.cache.DefaultQueryPersistor
import io.github.abappi19.kmm_query.cache.QueryPersistor
import kotlinx.coroutines.CoroutineScope

data class QueryConfig<T>(
    val key: List<Any>,
    val fetcher: suspend () -> T,
    val scope: CoroutineScope,
    val cacheTimeMillis: Long?,
    val staleTimeMillis: Long?,
    val retryCount: Int?,
    val persistor: QueryPersistor?
)


data class QueryManagerConfig(
    val cacheTimeMillis: Long?,
    val staleTimeMillis: Long?,
    val retryCount: Int? = 0,
    val persistor: QueryPersistor? = null
)