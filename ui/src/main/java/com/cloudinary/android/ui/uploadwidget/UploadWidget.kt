package com.cloudinary.android.ui.uploadwidget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import com.cloudinary.android.ui.uploadwidget.model.CropPoints
import com.cloudinary.android.ui.uploadwidget.view.UploadWidgetActivity
import com.cloudinary.android.uploader.request.UploadRequest
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Helper class to start the UploadWidget and preprocess its results.
 */
object UploadWidget {
    /**
     * The key used to pass upload widget result data back from [UploadWidgetActivity].
     */
    const val RESULT_EXTRA = "upload_widget_result_extra"

    /**
     * The key used to pass the uris to the upload widget.
     */
    const val URIS_EXTRA = "uris_extra"

    /**
     * Start the [UploadWidgetActivity]. Please make sure that you have declared it your manifest.
     *
     * @param activity    The activity which requested the upload widget.
     * @param requestCode A request code to start the upload widget with.
     * @param uris   Uris of the selected media files.
     */
    fun startActivity(
        activity: Activity,
        requestCode: Int,
        uris: ArrayList<Uri>
    ) {
        val intent = Intent(activity, UploadWidgetActivity::class.java).apply {
            putParcelableArrayListExtra(URIS_EXTRA, uris)
        }
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * Create a preprocessed list of [UploadRequest]s from the UploadWidget's results data.
     *
     * @param data Results data from the upload widget.
     * @return Preprocessed [UploadRequest]s.
     */
    fun preprocessResults(context: Context, data: Intent): ArrayList<UploadRequest>? {
        checkDataNotNull(data)
        val results: ArrayList<Result>? = data.getParcelableArrayListExtra(RESULT_EXTRA)

        return results?.let {
            val uploadRequests = ArrayList<UploadRequest>(it.size)
            for (result in it) {
                val uploadRequest = UploadWidgetResultProcessor.process(context, result)
                uploadRequests.add(uploadRequest)
            }

            uploadRequests
        }
    }

    /**
     * Create a new [UploadRequest] with the upload widget's preprocess results.
     *
     * @param result Result data from the upload widget.
     * @return Newly created [UploadRequest].
     */
    fun preprocessResult(context: Context, result: Result): UploadRequest =
        UploadWidgetResultProcessor.process(context, result)

    /**
     * Preprocess the `uploadRequest`'s with the upload widget results.
     *
     * @param uploadRequest Already constructed upload request.
     * @param result Result data from the upload widget.
     * @return Preprocessed [UploadRequest]
     * @throws IllegalStateException    if `uploadRequest` was already dispatched.
     */
    fun preprocessResult(
        context: Context,
        uploadRequest: UploadRequest,
        result: Result
    ): UploadRequest = UploadWidgetResultProcessor.process(context, uploadRequest, result)

    /**
     * Open the native android picker to choose a media file.
     *
     * @param activity    The activity that the native android picker was initiated from.
     * @param requestCode A request code to start the native android picker with.
     */
    fun openMediaChooser(activity: Activity, requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf("image/jpeg", "image/jpg", "image/png", "video/*")
                )

                "(*/*"
            } else {
                "image/*|video/*"
            }
        }
        activity.startActivityForResult(intent, requestCode)
    }

    private fun checkDataNotNull(data: Intent) =
        data.getParcelableArrayListExtra<Result>(RESULT_EXTRA)
            ?: throw IllegalArgumentException("Data must contain upload widget results")

    /**
     * Result data of the upload widget activity
     */
    @Parcelize
    data class Result(
        /**
         * Source uri.
         */
        var uri: Uri,

        /**
         * Pair of cropping points.
         */
        var cropPoints: CropPoints? = null,

        /**
         * Angle to rotate.
         */
        var rotationAngle: Int = 0
    ) : Parcelable
}