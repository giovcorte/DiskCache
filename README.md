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
	        implementation 'com.github.giovcorte:DiskCache:1.1'
	}

Example of usages in a wrapper class for storing and retrieving bitmaps:

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
When you have finished to edit/read the file, you have to commit() the changes or close() the Sanpshot, so the entry can be saved or, in case of a last recentry used entry, deleted if
space cleanup is required. A difference form the original is that this cache expose the file you store, and not an InputStream/OutputStream. Also the keys must match the [a-zA-Z0-9] regex. In the Utilsclass you will find a method to format your keys.

If you dont want to use directly the DiskCache there are availables three tipes of cache wrappers: AsyncDiskCache<T>, based on susped coroutine functions; SyncDiskCache<T>, with synchronous operations; and CallbackDiskCache<T>, where you provide the function callback for the operation. You can implement each of those abstract classes giving two methods, one for decoding the T type variable from a file and another to write your T value to it. Example implementation for images:

```kotlin
class AsyncImageDiskCache(diskCache: DiskCache): AsyncDiskCache<Bitmap>(diskCache) {

    override fun writeValueToFile(value: Bitmap, file: File) {
        value.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(file))
    }

    override fun decodeValueFromFile(file: File): Bitmap? {
        return BitmapFactory.decodeStream(FileInputStream(file))
    }

}
```
Example of AsyncDiskCache usage:
```kotlin

val value: T? = async { 
    cache.get(key)
}
value.await()
	
```

Example of CallbackDiskCache usage:
	
```kotlin
cache.get(key) { tValue ->
	// use your value               
}
	
cache.put(key, value) { booleanSuccess ->
	// use the operation result
}
```
