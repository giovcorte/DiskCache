# DiskCache
Simple and readable disk cache for Kotlin and android applications (with journaled last recently used strategy)

This is a simple disk cache, based on the idea of the original DiskLruCache but realized in modern Kotlin and with some simplifications (not less functions!). 
It has a really good low level and high level APIs, doesn't use any external library or dependency, and has a strong recovery and fault tolerance. 

To use this library in your projects two easy step are required, the first on your main build.gradle and the second in your module level build.gradle:

    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

    dependencies {
	        implementation 'com.github.giovcorte:DiskCache:1.4'
	}

Example of the usage of the low-level APIs in a wrapper class for storing and retrieving bitmaps:

```kotlin
class ImageDiskCache(private val diskCache: DiskCache) {

    fun get(key: String): Bitmap? {
        var bitmap: Bitmap? = null
        diskCache.get(key)?.let { snapshot ->
            bitmap = BitmapFactory.decodeStream(FileInputStream(snapshot.file()))
            snapshot.close()
        }
        return bitmap
    }

    fun contains(key: String): Boolean {
        return diskCache.get(key) != null
    }

    fun put(key: String, bitmap: Bitmap) {
        diskCache.edit(key)?.let { editor ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(editor.file()))
            editor.commit()
        }
    }

    fun clear() {
        diskCache.evictAll()
    }

}
```

Like the original DiskLruCache, we have to open and close Editors for modifying a file and Snapshots for reading. You can open multiple Snapshots but only one Editor at time.
When you have finished to edit/read the file, you have to commit() the changes or close() the Sanpshot, so the entry can be saved or, in case of a last recentry used entry, deleted if space cleanup is required. A difference form the original is that this cache expose the file you store, and not an InputStream/OutputStream. Also the keys must match the [a-zA-Z0-9] regex. In the Utils class you will find a method to format your keys.

If you dont want to use directly the DiskCache there are available a simple wrapper, that encapsulate a DiskCache instance and expose the methods with inline functions for the sake of simplicity and clean code!
You can easily use these examples in a coroutineScope to run non-blocking disk cache operations:

```kotlin
val diskCache = AndroidDiskCache.Builder.folder(cacheFolder).appVersion(1).maxSize(1024).build()

diskCache.put(imageUrl) { cachedFile ->
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(cachedFile))
}

val bitmap: Bitmap? diskCache.get(imageUrl) { cachedFile ->
    BitmapFactory.decodeStream(FileInputStream(cachedFile))
}

val exist: Boolean = diskCache.contains(imageUrl)

val removed: Boolean = diskCache.remove(imageUrl)

val size: Long = diskCache.size()

diskCache.clear()
```
