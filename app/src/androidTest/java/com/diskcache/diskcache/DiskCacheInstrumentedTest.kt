package com.diskcache.diskcache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.diskcache.diskcache.wrapper.AndroidDiskCache
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileInputStream
import java.io.FileOutputStream

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
        diskCache.put("test") {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(it))
        }
        val cachedBitmap = diskCache.get("test") {
            BitmapFactory.decodeStream(FileInputStream(it))
        }
        assertNotEquals(diskCache.size(), 0)
        assertNotNull(cachedBitmap)

        for (i in 1..1000) {
            diskCache.put("test$i") {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(it))
            }
        }

        assert(diskCache.size() <= 10000)
    }
}