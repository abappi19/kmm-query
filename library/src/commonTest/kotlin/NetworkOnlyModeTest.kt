package io.github.abappi19.kmp_query

import io.github.abappi19.kmp_query.core.CacheMode
import io.github.abappi19.kmp_query.core.QueryClient
import io.github.abappi19.kmp_query.core.useQuery
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds


class NetworkOnlyModeTest {

    val client = QueryClient()

    @Test
    fun `NETWORK_ONLY - initial load`() = runTest {

        val query = client.useQuery(
            key = listOf("test"),
            fetcher = fetcher@{
                delay(100.milliseconds)
                "Hello World"
            },
            cacheMode = CacheMode.NETWORK_ONLY
        )


        val result = query.data.first { it != null }

        assertEquals("Hello World", result)



        assertEquals(
            "query.isLoading.value==false",
            if (query.isLoading.value) "query.isLoading.value==true" else "query.isLoading.value==false"
        )
        assertEquals(
            "query.isRefreshing.value==false",
            if (query.isRefreshing.value) "query.isRefreshing.value==true" else "query.isRefreshing.value==false"
        )


    }


    @Test
    fun `NETWORK_ONLY - refresh`() = runTest {
        var isFirst = MutableStateFlow(true)
        val query = client.useQuery(
            key = listOf("test"),
            fetcher = fetcher@{
                delay(100.milliseconds)
                if (!isFirst.value) return@fetcher "Hello World 2"

                isFirst.value = false
                "Hello World"
            },
            cacheMode = CacheMode.NETWORK_ONLY
        )

        query.data.first { it != null }

        assertEquals(
            "query.isLoading.value==false",
            if (query.isLoading.value) "query.isLoading.value==true" else "query.isLoading.value==false"
        )
        assertEquals(
            "query.isRefreshing.value==false",
            if (query.isRefreshing.value) "query.isRefreshing.value==true" else "query.isRefreshing.value==false"
        )

        query.refresh()

        query.isRefreshing.first { !it }
        query.data.first { it != "Hello World" }

        assertEquals("Hello World 2", query.data.value)

    }

    @Test
    fun `NETWORK_ONLY - offline`() = runTest {
        var isFirst = MutableStateFlow(true)
        val query = client.useQuery(
            key = listOf("test"),
            fetcher = fetcher@{
                throw Exception("No Internet")
            },
            cacheMode = CacheMode.NETWORK_ONLY
        )

        query.isRefreshing.first { !it }
        query.isLoading.first { !it }

        assertEquals(
            "query.isLoading.value==false",
            if (query.isLoading.value) "query.isLoading.value==true" else "query.isLoading.value==false"
        )
        assertEquals(
            "query.isRefreshing.value==false",
            if (query.isRefreshing.value) "query.isRefreshing.value==true" else "query.isRefreshing.value==false"
        )
        assertEquals(
            "query.data.value==null",
            if (query.data.value == null) "query.data.value==null" else "query.data.value!=null"
        )

        assertEquals(
            "query.error.value!=null",
            if (query.error.value != null) "query.error.value!=null" else "query.error.value==null"
        )

    }

    @Test
    fun `NETWORK_ONLY - unstableNetwork`() = runTest {
        var isFirst = MutableStateFlow(true)
        val query = client.useQuery(
            key = listOf("test"),
            fetcher = fetcher@{
                delay(100.milliseconds)
                if (isFirst.value) {
                    isFirst.value = false
                    return@fetcher "Hello World"
                }
                throw Exception("No Internet")
            },
            cacheMode = CacheMode.NETWORK_ONLY
        )

        query.isRefreshing.first { !it }
        query.isLoading.first { !it }

        assertEquals(
            "query.isLoading.value==false",
            if (query.isLoading.value) "query.isLoading.value==true" else "query.isLoading.value==false"
        )
        assertEquals(
            "query.isRefreshing.value==false",
            if (query.isRefreshing.value) "query.isRefreshing.value==true" else "query.isRefreshing.value==false"
        )

        assertEquals("Hello World", query.data.value)

        query.refresh()

        query.isRefreshing.first { !it }

        assertEquals(
            "query.data.value==null",
            if (query.data.value == null) "query.data.value==null" else "query.data.value!=null"
        )

        assertEquals(
            "query.error.value!=null",
            if (query.error.value == null) "query.error.value==null" else "query.error.value!=null"
        )

    }

}