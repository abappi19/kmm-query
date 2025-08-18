package io.github.abappi19.kmm_query.cache

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


/**
 * Persistent storage interface for query caching system.
 *
 * Provides basic CRUD operations for serialized query data with platform-agnostic
 * interface. Implementations should handle:
 * - Thread-safe access to storage
 * - Data persistence across application restarts
 * - Efficient key-based lookups
 *
 * @see DefaultQueryPersistor For in-memory reference implementation
 */
interface QueryPersistor {
    /**
     * Retrieve raw string value for given key
     * @return Null if key not found, throws on read errors
     */
    fun getItem(key: String): String?

    /**
     * Persist string value with given key
     * @throws Exception On write failures
     */
    fun setItem(key: String, value: String)

    /**
     * Remove entry for given key
     * @throws Exception On deletion failures
     */
    fun removeItem(key: String)

    /**
     * Clear all stored entries
     * @throws Exception On clear operation failures
     */
    fun clear()
}

/**
 * Serialization extensions for [QueryPersistor] using Kotlinx Serialization.
 * Requires `@Serializable` annotation on data classes.
 *
 * @param T Serializable data type
 * @see Json For configuration of serialization format
 */
inline fun <reified T : @Serializable Any> QueryPersistor.getObject(key: String): T? {
    val cachedResponse = getItem(key) ?: return null
    return Json.decodeFromString(cachedResponse)
}

inline fun <reified T : @Serializable Any> QueryPersistor.setObject(key: String, value: T?) {
    if (value == null) {
        removeItem(key)
        return
    }
    setItem(key, Json.encodeToString(value))
}

/**
 * In-memory implementation of [QueryPersistor] for temporary storage.
 *
 * Note: Data is not persisted between application sessions.
 * Suitable for:
 * - Development/testing environments
 * - Ephemeral caching needs
 *
 * For persistent storage, implement [QueryPersistor] using platform-specific
 * storage mechanisms (e.g., SharedPreferences on Android, UserDefaults on iOS)
 */
class DefaultQueryPersistor : QueryPersistor {
    private var cache: MutableMap<String, String?> = mutableMapOf()

    override fun getItem(key: String): String? {
        return cache[key]
    }

    override fun setItem(key: String, value: String) {
        cache[key] = value
    }

    override fun removeItem(key: String) {
        cache.remove(key)
    }

    override fun clear() {
        cache.clear()
    }
}