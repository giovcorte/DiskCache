package com.diskcache.diskcache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.diskcache.diskcache.wrapper.AndroidDiskCache
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DiskCacheInstrumentedTest {
    @Test
    fun saveAndRetrieve() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val diskCache = AndroidDiskCache.Builder.folder(appContext.cacheDir).maxSize(8192).build()
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        diskCache.put("test", byteArray)
        val cachedBitmap = diskCache.get("test") {
            BitmapFactory.decodeStream(ByteArrayInputStream(it))
        }
        assertNotEquals(diskCache.size(), 0)
        assertNotNull(cachedBitmap)

        for (i in 1..1000) {
            diskCache.put("test$i", byteArray)
        }

        assert(diskCache.size() <= 10000)
        Log.w("DISKCACHE2", diskCache.size().toString())

        diskCache.close()

        val diskCache2 = AndroidDiskCache.Builder.folder(appContext.cacheDir).maxSize(8192).build()
        val cachedBitmap2 = diskCache2.get("test998") {
            BitmapFactory.decodeStream(ByteArrayInputStream(it))
        }
        Log.w("DISKCACHE2", diskCache2.size().toString())
        assertNotNull(cachedBitmap2)
        assertNotEquals(diskCache2.size(), 0)
    }
}