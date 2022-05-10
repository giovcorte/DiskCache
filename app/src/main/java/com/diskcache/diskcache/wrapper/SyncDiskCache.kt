package com.diskcache.diskcache.wrapper

import com.diskcache.diskcache.DiskCache
import java.io.File

@Suppress("unused")
abstract class SyncDiskCache<T>(private val diskCache: DiskCache) {

    @Synchronized
    fun get(key: String) : T? {
        return diskCache.get(key)?.let { snapshot ->
            val result : T? = decodeValueFromFile(snapshot.file())
            snapshot.close()
            result
        } ?: run {
            null
        }
    }

    @Synchronized
    fun put(key: String, value: T) : Boolean {
        return diskCache.edit(key)?.let { editor ->
            writeValueToFile(value, editor.file())
            editor.commit()
            true
        } ?: run {
            false
        }
    }

    @Synchronized
    fun contains(key: String) : Boolean = diskCache.get(key) != null

    abstract fun writeValueToFile(value: T, file: File)

    abstract fun decodeValueFromFile(file: File) : T?
}