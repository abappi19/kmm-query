package io.github.abappi19.kmm_query.core

/**
 * Defines caching strategies for network operations.
 */
enum class CacheMode {
    /**
     * Bypasses cache completely, always fetches from network.
     * Use this for data that must be fresh or shouldn't be cached.
     * `staleTimeMillis` and `cacheTimeMillis` are not considered.
     */
    NETWORK_ONLY,

    /**
     * Uses only cached data, only makes network request if cache is empty.
     * Suitable for offline-first experiences or when staleness is acceptable.
     * `staleTimeMillis` and `cacheTimeMillis` are not considered.
     */
    CACHE_ONLY,

    /**
     * Attempts network request first, falls back to cache if:
     * - Network fails
     * - Network returns error
     * - Device is offline
     * `staleTimeMillis` and `cacheTimeMillis` are not considered.
     */
    NETWORK_FIRST,

    /**
     * Returns cached data immediately if available, then updates from network.
     * Provides quick response while ensuring eventual freshness.
     * You need to modify `staleTimeMillis` and `cacheTimeMillis` accordingly.
     */
    CACHE_FIRST,
}
