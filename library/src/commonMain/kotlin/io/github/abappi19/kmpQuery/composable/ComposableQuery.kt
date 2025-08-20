package io.github.abappi19.kmpQuery.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.abappi19.kmpQuery.core.CacheMode
import io.github.abappi19.kmpQuery.core.QueryClient
import io.github.abappi19.kmpQuery.core.createQuery
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * Defines a reactive query interface for managing asynchronous data fetching with caching strategies.
 *
 * @param T The type of data being queried, must be serializable for caching
 */
interface ComposableQuery<T> {
    /**
     * Force a refresh of the data, bypassing any caching logic
     */
    fun refresh(): Unit

    /**
     * Mark the data as stale and trigger a background refresh
     */
    fun invalidate(): Unit

    /**
     * Observable stream of query results
     */
    val data: T?

    /**
     * Observable stream of query errors
     */
    val error: Throwable?

    /**
     * Loading state indicator (true during initial fetch)
     */
    val isLoading: Boolean

    /**
     * Refreshing state indicator (true during manual refreshes)
     */
    val isRefreshing: Boolean
}


@Composable
inline fun <reified T : @Serializable Any> QueryClient.rememberCreateQuery(
    key: List<Any?>,
    crossinline fetcher: suspend () -> T,
    cacheTimeMillis: Long = queryManagerConfig.cacheTimeMillis ?: 0,
    staleTimeMillis: Long = queryManagerConfig.staleTimeMillis ?: 0,
    retryCount: Int = queryManagerConfig.retryCount ?: 0,
    cacheMode: CacheMode = queryManagerConfig.cacheMode ?: CacheMode.CACHE_FIRST,
    enabled: Boolean = true,

    ): ComposableQuery<T> {
    val query = remember(this) {
        this.createQuery(
            key,
            fetcher,
            cacheTimeMillis,
            staleTimeMillis,
            retryCount,
            cacheMode,
            enabled
        )
    }

    val data by query.data.collectAsState(initial = null)
    val error by query.error.collectAsState(initial = null)

    val isRefreshing by query.isRefreshing.collectAsState(initial = false)
    val isLoading by query.isLoading.collectAsState(initial = false)






    return object : ComposableQuery<T> {
        override fun refresh() = query.refresh()

        override fun invalidate() = query.invalidate()

        override val data: T?
            get() = data
        override val error: Throwable?
            get() = error
        override val isLoading: Boolean
            get() = isLoading
        override val isRefreshing: Boolean
            get() = isRefreshing

    }

}