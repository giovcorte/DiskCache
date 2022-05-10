# DiskCache
Simple and readable disk cache for kotlin and android applications (with journaled lru strategy)

This is a simple lru disk cache, based on the idea of the original DiskLruCache but realized in modern kotlin and with some semplifications (not less functions!). 
It doesn't use any exernal library, and has a good recovery and fault tollerance. 

To use this library in your projects, two easy step are required, the first on your main build.gradle and the second in your module level build.gradle:

    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

    dependencies {
	        implementation 'com.github.giovcorte:DiskCache:Tag'
	}

Example of usages in a wrapper class for storing and retrieving bitmaps:

```kotlin
class ImageDiskCache(private val diskCache: DiskCache) {

    fun get(key: String): Bitmap? {
        val realKey = formatKey(key)
        var bitmap: Bitmap? = null
        diskCache.get(realKey)?.let { snapshot ->
            bitmap = BitmapFactory.decodeStream(FileInputStream(snapshot.file()))
            snapshot.close()
        }
        return bitmap
    }

    fun contains(key: String): Boolean {
        return diskCache.get(formatKey(key)) != null
    }

    fun put(key: String, bitmap: Bitmap) {
        diskCache.edit(formatKey(key))?.let { editor ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(editor.file()))
            editor.commit()
        }
    }

    fun clear() {
        diskCache.delete()
    }

    private fun formatKey(str: String?): String {
        val formatted = str!!.replace("[^a-zA-Z0-9]".toRegex(), "").lowercase()
        return formatted.substring(0, if (formatted.length >= 120) 110 else formatted.length)
    }

}
```

Like the original DiskLruCache, we have to open and close Editors for modifying a file and Snapshots for reading. You can open multiple Snapshots but only one Editor at time.
When you have finished to edit/read the file, you have to commit() the changes or close() the Sanpshot, so the entry can be saved or, in case of a last recentry used entry, deleted if
space cleanup is required. A difference form the original is that this cache expose the file you store, and not an InputStream/OutputStream. Also the keys must match the
[a-zA-Z0-9].
