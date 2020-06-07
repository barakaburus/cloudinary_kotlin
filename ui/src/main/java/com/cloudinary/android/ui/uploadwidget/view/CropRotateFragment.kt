package com.cloudinary.android.ui.uploadwidget.view

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cloudinary.android.ui.R
import com.cloudinary.android.ui.uploadwidget.model.BitmapManager
import com.cloudinary.android.ui.uploadwidget.model.CropRotateResult
import com.cloudinary.android.ui.uploadwidget.view.imageview.UploadWidgetImageView
import com.cloudinary.android.ui.uploadwidget.utils.MediaType
import com.cloudinary.android.ui.uploadwidget.utils.getMediaType
import com.cloudinary.android.ui.uploadwidget.utils.getVideoThumbnail
import kotlinx.android.synthetic.main.aspect_ratio_menu_item.*
import kotlinx.android.synthetic.main.fragment_crop_rotate.*
import kotlinx.android.synthetic.main.fragment_crop_rotate.view.*

private const val URI_ARG = "uri_arg"

/**
 * Crops and rotates a media file
 */
internal class CropRotateFragment : Fragment() {
    private lateinit var uploadWidgetImageView: UploadWidgetImageView
    private lateinit var uri: Uri
    private var callback: Callback? = null

    companion object {
        /**
         * Instantiate a new [CropRotateFragment].
         *
         * @param uri Uri of the media file to crop and rotate.
         * @param callback Callback to be called when there is a result for the crop and rotate.
         */
        fun newInstance(uri: Uri?, callback: Callback?) = CropRotateFragment().apply {
            requireNotNull(uri) { "Uri must be provided" }
            arguments = Bundle().apply {
                putString(URI_ARG, uri.toString())
            }
            setCallback(callback)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = Uri.parse(arguments?.getString(URI_ARG))
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crop_rotate, container, false)
        uploadWidgetImageView = view.imageView

        val mediaType: MediaType? = getMediaType(context!!, uri)
        if (mediaType === MediaType.VIDEO) {
            val videoThumbnail: Bitmap = getVideoThumbnail(context!!, uri)
            BitmapManager.save(context!!, videoThumbnail, object : BitmapManager.SaveCallback {
                override fun onSuccess(resultUri: Uri) {
                    uploadWidgetImageView.setImageUri(resultUri)
                    uploadWidgetImageView.showCropOverlay()
                }

                override fun onFailure() {}
            })
        } else {
            uploadWidgetImageView.setImageUri(uri)
            uploadWidgetImageView.showCropOverlay()
        }

        view.doneButton.setOnClickListener {
            callback?.onCropRotateFinish(uri, result, uploadWidgetImageView.resultBitmap)
            onBackPressed()
        }
        view.cancelButton.setOnClickListener {
            callback?.onCropRotateCancel(uri)
            onBackPressed()
        }
        if (mediaType === MediaType.VIDEO) {
            view.rotateButton.visibility = View.GONE
        } else {
            view.rotateButton.setOnClickListener { uploadWidgetImageView.rotateImage() }
        }

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP) {
                callback?.onCropRotateCancel(uri)
                onBackPressed()
                return@setOnKeyListener true
            }
            false
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? AppCompatActivity)?.let {
            it.setSupportActionBar(cropRotateToolbar)
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.crop_rotate_menu, menu)
        val aspectRatioItem = menu.findItem(R.id.aspect_ratio_action)
        val aspectRatioActionView = aspectRatioItem.actionView

        aspectRatioActionView.setOnClickListener {
            if (uploadWidgetImageView.isAspectRatioLocked) {
                uploadWidgetImageView.isAspectRatioLocked = false
                aspectRatioTextView.text = getString(R.string.menu_item_aspect_ratio_unlocked)
                aspectRatioImageView.setImageResource(R.drawable.unlock)
            } else {
                uploadWidgetImageView.isAspectRatioLocked = true
                aspectRatioTextView.text = getString(R.string.menu_item_aspect_ratio_locked)
                aspectRatioImageView.setImageResource(R.drawable.lock)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            callback?.onCropRotateCancel(uri)
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Set a crop and rotate callback.
     *
     * @param callback Crop and rotate callback to be called for the result.
     */
    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    private val result: CropRotateResult
        get() = CropRotateResult(
            uploadWidgetImageView.rotationAngle,
            uploadWidgetImageView.cropPoints
        )

    private fun onBackPressed() {
        activity?.onBackPressed()
    }

    /**
     * Callback for the result of the crop and rotate.
     */
    interface Callback {
        /**
         * Called when finished to crop and rotate.
         *
         * @param uri The source uri.
         * @param result Crop and rotate result.
         * @param resultBitmap Crop and rotate result bitmap.
         */
        fun onCropRotateFinish(uri: Uri, result: CropRotateResult, resultBitmap: Bitmap)

        /**
         * Called when canceled to crop and rotate.
         *
         * @param uri The source uri.
         */
        fun onCropRotateCancel(uri: Uri)
    }
}