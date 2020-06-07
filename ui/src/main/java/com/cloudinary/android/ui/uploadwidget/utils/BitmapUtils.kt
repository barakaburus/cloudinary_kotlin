package com.cloudinary.android.ui.uploadwidget.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.cloudinary.android.ui.uploadwidget.model.Dimensions
import java.io.FileNotFoundException
import java.io.InputStream
import kotlin.math.max

/**
 * Decode a sampled bitmap to the required width and height.
 *
 * @param context Android context.
 * @param uri Bitmap's source uri.
 * @param reqWidth Required width for the bitmap to be adjusted to.
 * @param reqHeight Required height for the bitmap to be adjusted to.
 * @return The decoded bitmap.
 * @throws FileNotFoundException If cannot locate the bitmap's source uri.
 */
@Throws(FileNotFoundException::class)
internal fun decodeSampledBitmapFromUri(
    context: Context,
    uri: Uri,
    reqWidth: Int,
    reqHeight: Int
): Bitmap? {
    var bitmap: Bitmap? = null
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

    getUriInputStream(context, uri).use {
        BitmapFactory.decodeStream(it, null, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
    }
    getUriInputStream(context, uri).use {
        val sampledBitmap = BitmapFactory.decodeStream(it, null, options)
        bitmap = getScaledBitmap(sampledBitmap!!, reqWidth, reqHeight)
    }

    return bitmap
}

/**
 * Get bitmap's dimensions.
 *
 * @param context Android context.
 * @param uri Bitmap's source uri.
 * @return Dimensions of the bitmap.
 * @throws FileNotFoundException If cannot locate the bitmap's source uri.
 */
@Throws(FileNotFoundException::class)
internal fun getBitmapDimensions(context: Context, uri: Uri): Dimensions {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

    getUriInputStream(context, uri).use {
        BitmapFactory.decodeStream(it, null, options)
    }

    return Dimensions(options.outWidth, options.outHeight)
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight
            && halfWidth / inSampleSize >= reqWidth
        ) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

@Throws(FileNotFoundException::class)
private fun getUriInputStream(context: Context, uri: Uri): InputStream? {
    return context.contentResolver.openInputStream(uri)
}

private fun getScaledBitmap(bitmap: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap? {
    if (reqWidth > 0 && reqHeight > 0) {
        val resized: Bitmap?
        val width = bitmap.width
        val height = bitmap.height
        val scale = max(
            width / reqWidth.toFloat(),
            height / reqHeight.toFloat()
        )
        resized = Bitmap.createScaledBitmap(
            bitmap,
            (width / scale).toInt(),
            (height / scale).toInt(),
            false
        )
        resized?.let {
            if (resized != bitmap) {
                bitmap.recycle()
            }
            return resized
        }
    }
    return bitmap
}