package io.github.abappi19.kmpQuery.utils

import kotlinx.coroutines.delay
import org.kotlincrypto.hash.md.MD5

suspend fun <T> retryOperation(
    retryCount: Int,
    block: suspend () -> T,
): T {

    var lastError: Throwable? = null

    repeat(
        if (retryCount <= 0) 1 else retryCount
    ) { attempt ->
        try {
            return block()
        } catch (e: Throwable) {
            lastError = e
            delay(attempt * 100L)
        }
    }
    throw lastError ?: IllegalStateException("retryOperation failed without exception")
}

/**
* Generate MD5 hash of a string
* @param input String to hash
* @return MD5 hashed string of the input
 */
fun md5Hash(input: String): String {
    val md5 = MD5()
    val digest = md5.digest(input.encodeToByteArray())
    return digest.joinToString("") { byte ->
        byte.toUByte().toString(16).padStart(2, '0')
    }
}