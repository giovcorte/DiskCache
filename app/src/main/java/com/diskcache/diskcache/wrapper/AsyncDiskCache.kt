package com.diskcache.diskcache.wrapper

import com.diskcache.diskcache.DiskCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

@Suppress("unused")
abstract class AsyncDiskCache<T>(
    private val diskCache: DiskCache,
) {
    private val mutex = Mutex()

    suspend fun put(key: String, value: T) : Boolean {
        return mutex.withLock {
            diskCache.edit(key)?.let { editor ->
                writeValueToFile(value, editor.file())
                editor.commit()
                true
            } ?: run {
                false
            }
        }
    }

    suspend fun get(key: String) : T? {
        return mutex.withLock {
            diskCache.get(key)?.let { snapshot ->
                val result : T? = decodeValueFromFile(snapshot.file())
                snapshot.close()
                result
            } ?: run {
                null
            }
        }
    }

    fun contains(key: String) : Boolean {
        return diskCache.get(key) != null
    }

    suspend fun clear() {
        mutex.withLock {
            diskCache.evictAll()
        }
    }

    abstract fun writeValueToFile(value: T, file: File)

    abstract fun decodeValueFromFile(file: File) : T?
}