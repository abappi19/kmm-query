package io.github.abappi19.kmpQuery.core

import io.github.abappi19.kmpQuery.cache.getObject
import io.github.abappi19.kmpQuery.cache.setObject
import io.github.abappi19.kmpQuery.io.github.abappi19.kmp_query.utils.md5Hash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.times


/**
 * Defines a reactive query interface for managing asynchronous data fetching with caching strategies.
 * 
 * @param T The type of data being queried, must be serializable for caching
 */
interface Query<T> {
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
    val data: StateFlow<T?>

    /**
     * Observable stream of query errors
     */
    val error: StateFlow<Throwable?>

    /**
     * Loading state indicator (true during initial fetch)
     */
    val isLoading: StateFlow<Boolean>

    /**
     * Refreshing state indicator (true during manual refreshes)
     */
    val isRefreshing: StateFlow<Boolean>
}

/**
 * Creates a managed query instance with caching and retry logic.
 * 
 * @param key Unique identifier for the query (used for caching)
 * @param fetcher Async function to fetch fresh data
 * @param cacheTimeMillis Duration to keep data in cache (0 = no caching)
 * @param staleTimeMillis Duration before considering cached data stale
 * @param retryCount Number of automatic retry attempts on failure
 * @param cacheMode Strategy for cache/network interaction
 * @param enabled Control whether the query enabled or not
 * 
 * @return Configured Query instance managing the data lifecycle
 */
@OptIn(ExperimentalTime::class)
inline fun <reified T : @Serializable Any> QueryClient.createQuery(
    key: List<Any?>,
    crossinline fetcher: suspend () -> T,
    cacheTimeMillis: Long = queryManagerConfig.cacheTimeMillis ?: 0,
    staleTimeMillis: Long = queryManagerConfig.staleTimeMillis ?: 0,
    retryCount: Int = queryManagerConfig.retryCount ?: 0,
    cacheMode: CacheMode = queryManagerConfig.cacheMode ?: CacheMode.CACHE_FIRST,
    enabled: Boolean = true,
): Query<T> {

    val queryKey = md5Hash(key.joinToString(":"))
    val lastQueryTimeKey = "${queryKey}_lastQueryTime"

    var lastQueryTime: Long = persistor.getObject(lastQueryTimeKey)
        ?: Clock.System.now().toEpochMilliseconds()

    val isCacheValid: Boolean = cacheMode == CacheMode.CACHE_ONLY
            || (cacheMode == CacheMode.CACHE_FIRST &&
            (cacheTimeMillis == Long.MAX_VALUE
                    || (Clock.System.now()
                .toEpochMilliseconds() < lastQueryTime + cacheTimeMillis)
                    )
            )

    val isLoading = MutableStateFlow(false)
    val isRefreshing = MutableStateFlow(false)

    val data = MutableStateFlow(
        if (isCacheValid) {
            persistor.getObject<T>(queryKey)
        } else {
            persistor.removeItem(queryKey)
            null
        }
    )
    val error = MutableStateFlow<Throwable?>(null)

    val resetLoadingState = {
        isLoading.value = false
        isRefreshing.value = false
    }


    val refresh = refresh@{
        if (
            !enabled
            || isRefreshing.value
        ) return@refresh

        CoroutineScope(Dispatchers.Default).launch {
            try {
                isRefreshing.value = true
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()

                val isDataFresh =
                    staleTimeMillis == Long.MAX_VALUE || (currentTimeMillis < lastQueryTime + staleTimeMillis)

                val shouldSkip = when (cacheMode) {
                    CacheMode.CACHE_ONLY -> data.value != null // if cache exists, skip
                    CacheMode.CACHE_FIRST -> isDataFresh && data.value != null //  if data is fresh, skip
                    CacheMode.NETWORK_ONLY -> false // never skip
                    CacheMode.NETWORK_FIRST -> false // never skip
                }

                if (shouldSkip) {
                    resetLoadingState()

                    return@launch
                }


                var err: Throwable?

                repeat(
                    if (retryCount <= 0) 1 else retryCount
                ) { attempt ->
                    try {
                        fetcher().also {
                            data.value = it

                            resetLoadingState()

                            // in network only mode, do not persist cache and query time
                            if (cacheMode == CacheMode.NETWORK_ONLY) return@launch

                            lastQueryTime = Clock.System.now().toEpochMilliseconds()
                            persistor.setObject(queryKey, it)
                            persistor.setObject(lastQueryTimeKey, lastQueryTime)

                            return@launch
                        }
                    } catch (e: Throwable) {
                        err = e
                        delay(attempt * 100.milliseconds)
                    }

                    throw err
                }

            } catch (e: Exception) {
                val isCacheValid: Boolean = cacheMode == CacheMode.CACHE_ONLY
                        || (cacheMode == CacheMode.CACHE_FIRST &&
                        (cacheTimeMillis == Long.MAX_VALUE
                                || (Clock.System.now()
                            .toEpochMilliseconds() < lastQueryTime + cacheTimeMillis)
                                )
                        )

                val shouldSkip = when (cacheMode) {
                    CacheMode.CACHE_ONLY -> data.value != null
                    CacheMode.CACHE_FIRST -> isCacheValid && data.value != null
                    CacheMode.NETWORK_ONLY -> false
                    CacheMode.NETWORK_FIRST -> data.value != null
                }

                if (shouldSkip) {
                    resetLoadingState()
                    return@launch
                }
                data.value = null
                error.value = e

                resetLoadingState()

            }
        }
        Unit
    }

    val invalidate = invalidate@{
        if (enabled != true) return@invalidate
        data.value = null
        refresh()
    }

    // Fetch data initially

    if (enabled == true) {
        isLoading.value = true
        refresh()
    }

    return object : Query<T> {
        override val data = data as StateFlow<T?>
        override fun refresh() = refresh()
        override fun invalidate() = invalidate()
        override val error = error
        override val isLoading = isLoading
        override val isRefreshing = isRefreshing
    }
}


