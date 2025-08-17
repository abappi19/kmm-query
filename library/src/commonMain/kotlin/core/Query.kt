package io.github.abappi19.kmm_query.core

import io.github.abappi19.kmm_query.cache.getObject
import io.github.abappi19.kmm_query.cache.setObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


interface Query<T> {
    fun refresh(): Unit
    fun invalidate(): Unit
    val data: StateFlow<T?>
}


@OptIn(ExperimentalTime::class)
inline fun <reified T : @Serializable Any> QueryManager.useQuery(
    key: List<String>,
    crossinline fetcher: suspend () -> T,
    cacheTimeMillis: Long = queryManagerConfig.cacheTimeMillis ?: 0,
    staleTimeMillis: Long = queryManagerConfig.staleTimeMillis ?: 0,
    retryCount: Int = queryManagerConfig.retryCount ?: 0,
    enabled: Boolean = true,
): Query<T> {

    val queryKey = key.joinToString(":")

    var lastQueryTime: Long = Clock.System.now().toEpochMilliseconds()

    val isLoading = MutableStateFlow(false)
    val isRefreshing = MutableStateFlow(false)

    val data = MutableStateFlow(persistor.getObject<T>(queryKey))
    val error = MutableStateFlow<Throwable?>(null)

    val refresh = refresh@{
        if (!enabled) return@refresh
        CoroutineScope(Dispatchers.Default).launch {
            try {
                isRefreshing.value = true
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()

                val isDataFresh = staleTimeMillis == Long.MAX_VALUE || (currentTimeMillis < lastQueryTime + staleTimeMillis)
                val isCacheValid = cacheTimeMillis == Long.MAX_VALUE || (currentTimeMillis < lastQueryTime + cacheTimeMillis)

                if (isDataFresh && isCacheValid && data.value != null) return@launch


                val fetcherData = fetcher()
                data.value = fetcherData
                persistor.setObject(queryKey, fetcherData)
                lastQueryTime = Clock.System.now().toEpochMilliseconds()
            } catch (e: Exception) {
                error.value = e
            } finally {
                isLoading.value = false
                isRefreshing.value = false
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

    if (enabled == true) refresh()

    return object : Query<T> {
        override val data = data as StateFlow<T?>
        override fun refresh() = refresh()
        override fun invalidate() = invalidate()
    }
}
