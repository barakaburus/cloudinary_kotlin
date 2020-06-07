package com.cloudinary.android.ui.uploadwidget.model

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.collection.LruCache
import com.cloudinary.android.ui.uploadwidget.utils.*
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Asynchronous bitmap manager that loads and saves bitmaps. The manager uses a [LruCache] to cache the bitmaps
 * for better performance.
 */
internal object BitmapManager {
    private lateinit var memoryCache: LruCache<String, Bitmap?>
    private val executor: ExecutorService = Executors.newFixedThreadPool(4)
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    init {
        initMemoryCache()
    }

    /**
     * Load the uri and downsample the bitmap, adjusting it to the specified dimensions.
     *
     * @param context  Android context
     * @param uri      Uri of the image to be loaded
     * @param width    Width for the output bitmap to be adjusted to (not necessarily exact fit).
     * @param height   Height for the output bitmap to be adjusted to (not necessarily exact fit).
     * @param callback The callback to be called when loading the bitmap.
     */
    fun load(
        context: Context,
        uri: Uri,
        width: Int,
        height: Int,
        callback: LoadCallback?
    ) {
        executor.execute {
            try {
                val hash = getHash(uri.toString() + width + height)
                var bitmap = memoryCache[hash]
                if (bitmap == null) {
                    bitmap = decodeSampledBitmapFromUri(context, uri, width, height)
                    memoryCache.put(hash, bitmap!!)
                }
                val dimensions: Dimensions = getBitmapDimensions(context, uri)
                onLoadSuccess(bitmap, dimensions, callback)
            } catch (e: Exception) {
                onLoadFailed(callback)
            }
        }
    }

    /**
     * Get a video file's thumbnail.
     * @param context Android context.
     * @param uri Uri of the video file.
     * @param width Thumbnail's width.
     * @param height Thumbnail's height.
     * @param callback The callback to be called when loading the thumbnail.
     */
    fun thumbnail(
        context: Context,
        uri: Uri,
        width: Int,
        height: Int,
        callback: LoadCallback?
    ) {
        executor.execute {
            try {
                if (getMediaType(context, uri) === MediaType.VIDEO) {
                    val hash = getHash(uri.toString() + width + height)
                    var bitmap = memoryCache[hash]
                    if (bitmap == null) {
                        bitmap = getVideoThumbnail(context, uri)
                        memoryCache.put(hash, bitmap)
                    }
                    val dimensions: Dimensions = getBitmapDimensions(context, uri)
                    onLoadSuccess(bitmap, dimensions, callback)
                } else {
                    onLoadFailed(callback)
                }
            } catch (e: Exception) {
                onLoadFailed(callback)
            }
        }
    }

    /**
     * Save the bitmap into a file
     * @param context Android context.
     * @param bitmap Bitmap to save.
     * @param callback the callback to be called when saving the bitmap.
     */
    fun save(context: Context, bitmap: Bitmap, callback: SaveCallback?) {
        executor.execute {
            var fos: FileOutputStream? = null
            val fileName = UUID.randomUUID().toString()
            try {
                fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                val bitmapUri =
                    Uri.fromFile(context.getFileStreamPath(fileName))
                onSaveSuccess(bitmapUri, callback)
            } catch (e: Exception) {
                onSaveFailed(callback)
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                        if (fileName.isBlank()) {
                            // failed, delete the file just in case it's there:
                            context.deleteFile(fileName)
                        }
                    } catch (ignored: IOException) {
                    }
                }
            }
        }
    }

    private fun initMemoryCache() {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap?>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
    }

    private fun onLoadSuccess(bitmap: Bitmap, dimensions: Dimensions, callback: LoadCallback?) {
        mainThreadHandler.post { callback?.onSuccess(bitmap, dimensions) }
    }

    private fun onLoadFailed(callback: LoadCallback?) {
        mainThreadHandler.post { callback?.onFailure() }
    }

    private fun onSaveSuccess(resultUri: Uri, callback: SaveCallback?) {
        mainThreadHandler.post { callback?.onSuccess(resultUri) }
    }

    private fun onSaveFailed(callback: SaveCallback?) {
        mainThreadHandler.post { callback?.onFailure() }
    }

    private fun getHash(plaintext: String): String {
        var hash = ""
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(plaintext.toByteArray())
            hash = String(digest.digest())
        } catch (ignored: Exception) {
        }
        return hash
    }
    /**
     * Callback for loading a bitmap.
     */
    interface LoadCallback {

        /**
         * Called when the bitmap is loaded successfully.
         *
         * @param bitmap             The loaded bitmap.
         * @param originalDimensions The original bitmap's dimensions.
         */
        fun onSuccess(bitmap: Bitmap, originalDimensions: Dimensions)
        /**
         * Called when failed to load the bitmap.
         */
        fun onFailure()

    }
    /**
     * Callback for saving a bitmap into a file.
     */
    interface SaveCallback {

        /**
         * Called when the bitmap was saved successfully.
         *
         * @param resultUri Result's file uri.
         */
        fun onSuccess(resultUri: Uri)
        /**
         * Called when failed to save the bitmap.
         */
        fun onFailure()

    }
}