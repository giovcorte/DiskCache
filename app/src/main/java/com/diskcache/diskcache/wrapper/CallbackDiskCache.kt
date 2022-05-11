package com.diskcache.diskcache.wrapper

import com.diskcache.diskcache.DiskCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

@Suppress("unused")
abstract class CallbackDiskCache<T>(
    private val diskCache: DiskCache,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val mutex = Mutex()

    fun put(key: String, value: T, callback: (success: Boolean) -> Unit) {
        coroutineScope.launch {
            mutex.withLock {
                diskCache.edit(key)?.let { editor ->
                    writeValueToFile(value, editor.file())
                    editor.commit()
                    callback(true)
                } ?: run {
                    callback(false)
                }
            }
        }
    }

    fun get(key: String, callback: (value: T?) -> Unit) {
        coroutineScope.launch {
            mutex.withLock {
                diskCache.get(key)?.let { snapshot ->
                    val result : T? = decodeValueFromFile(snapshot.file())
                    snapshot.close()
                    callback(result)
                } ?: run {
                    callback(null)
                }
            }
        }
    }

    fun contains(key: String) : Boolean {
        return diskCache.get(key) != null
    }

    fun clear(callback: (success: Boolean) -> Unit) {
        coroutineScope.launch {
            mutex.withLock {
                diskCache.evictAll()
                callback(true)
            }
        }
    }

    abstract fun writeValueToFile(value: T, file: File)

    abstract fun decodeValueFromFile(file: File) : T?
}