package io.github.abappi19.kmm_query.core

import io.github.abappi19.kmm_query.cache.DefaultQueryPersistor
import io.github.abappi19.kmm_query.cache.getObject
import kotlinx.serialization.Serializable

import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
class QueryManager(
    val queryManagerConfig: QueryManagerConfig = QueryManagerConfig(
        cacheTimeMillis = 0,
        staleTimeMillis = 0,
        retryCount = 0
    ),
) {
    val persistor = queryManagerConfig.persistor ?: DefaultQueryPersistor()

}



