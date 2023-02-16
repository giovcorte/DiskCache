package com.diskcache.diskcache.wrapper

import com.diskcache.diskcache.DiskCache
import kotlinx.coroutines.*
import java.io.Closeable
import java.io.File
import java.io.Flushable

@Suppress("unused")
class AndroidDiskCache(private val diskCache: DiskCache) : Closeable, Flushable {

    object Builder {
        private var folder: File? = null
        private var appVersion: Int = 1
        private var dispatcher: CoroutineDispatcher = Dispatchers.IO
        private var maxSize: Long = 1024000000

        fun folder(folder: File) = apply { this.folder = folder }

        fun appVersion(version: Int) = apply { this.appVersion = version }

        fun dispatcher(dispatcher: CoroutineDispatcher) = apply { this.dispatcher = dispatcher }

        fun maxSize(maxSize: Long) = apply { this.maxSize = maxSize }

        fun build() = folder?.let { cacheFolder ->
                AndroidDiskCache(
                    DiskCache(
                        folder = cacheFolder,
                        maxSize = maxSize,
                        appVersion = appVersion,
                        cleanupDispatcher = dispatcher))
            } ?: throw IllegalStateException("you must provide a folder to initialize KDiskCache")
    }

    fun put(key: String, writeToFile: (file: File) -> Unit) {
        diskCache.edit(key)?.let { editor ->
            writeToFile(editor.file())
            editor.commit()
        }
    }

    fun <T> get(key: String, readFromFile: (file: File) -> T) : T? {
        diskCache.get(key)?.let { snapshot ->
            val result : T? = readFromFile(snapshot.file())
            snapshot.close()
            return result
        } ?: run {
            return null
        }
    }

    fun contains(key: String) : Boolean {
        return diskCache.get(key) != null
    }

    fun remove(key: String) : Boolean {
        return diskCache.remove(key)
    }

    fun clear() {
        diskCache.evictAll()
    }

    fun size() : Long {
        return diskCache.size()
    }

    override fun close() {
        diskCache.close()
    }

    override fun flush() {
        diskCache.flush()
    }
}