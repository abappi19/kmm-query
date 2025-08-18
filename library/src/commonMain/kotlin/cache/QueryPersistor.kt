package io.github.abappi19.kmm_query.cache

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


interface QueryPersistor {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
    fun clear()
}


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

