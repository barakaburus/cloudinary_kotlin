package com.cloudinary.android.ui.uploadwidget.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap

/**
 * Get the media type of the Uri.
 * @param context Android context.
 * @param uri Uri of a media file.
 * @return The media type of the file.
 */
internal fun getMediaType(context: Context, uri: Uri): MediaType? {
    val mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        val cr = context.applicationContext.contentResolver
        cr.getType(uri)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
    }

    return mimeType?.let {
        when (mimeType.split("/").toTypedArray()[0]) {
            "image" -> MediaType.IMAGE
            "video" -> MediaType.VIDEO
            else -> null
        }
    }
}

/**
 * Get the first frame of a video
 * @param context Android context.
 * @param uri Uri of the video file.
 * @return First frame of the video.
 */
internal fun getVideoThumbnail(context: Context, uri: Uri): Bitmap =
    MediaMetadataRetriever().apply { setDataSource(context, uri) }.getFrameAtTime(1)