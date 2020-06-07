package com.cloudinary.android.ui.uploadwidget.view.imageview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.cloudinary.android.ui.uploadwidget.model.BitmapManager
import com.cloudinary.android.ui.uploadwidget.model.BitmapManager.LoadCallback
import com.cloudinary.android.ui.uploadwidget.model.CropPoints
import com.cloudinary.android.ui.uploadwidget.model.Dimensions
import kotlin.math.max

/**
 * Previews the Upload Widget's image with editing capabilities.
 */
internal class UploadWidgetImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var cropOverlayView: CropOverlayView = CropOverlayView(context)
    private var imageView: ImageView = ImageView(context)
    private var imageUri: Uri? = null
    private lateinit var bitmap: Bitmap
    private val bitmapBounds = Rect()
    private var originalWidth = 0

    /**
     * Returns the current image rotation angle.
     */
    var rotationAngle = 0
        private set
    private var sizeChanged = false

    init {
        imageView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        imageView.scaleType = ImageView.ScaleType.CENTER
        addView(imageView)

        cropOverlayView.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(cropOverlayView)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageUri?.let { setBitmap(it, w, h) }
        sizeChanged = true
    }

    private fun setBitmap(imageUri: Uri, w: Int, h: Int) {
        BitmapManager.load(context, imageUri, w, h, object : LoadCallback {
            override fun onSuccess(bitmap: Bitmap, originalDimensions: Dimensions) {
                this@UploadWidgetImageView.bitmap = bitmap
                if (rotationAngle != 0) {
                    rotateBitmapBy(rotationAngle)
                }
                updateImageViewBitmap()
                originalWidth = originalDimensions.width
            }

            override fun onFailure() {}
        })
    }

    private fun updateImageViewBitmap() {
        imageView.setImageBitmap(bitmap)
        setBitmapBounds()
        cropOverlayView.reset()
    }

    /**
     * Whether the aspect ratio should be locked.
     */
    var isAspectRatioLocked: Boolean
        get() = cropOverlayView.isAspectRatioLocked
        set(aspectRatioLocked) {
            cropOverlayView.isAspectRatioLocked = aspectRatioLocked
        }

    /**
     * Set an image from the given Uri.
     *
     * @param imageUri Uri of the image to be displayed.
     */
    fun setImageUri(imageUri: Uri?) {
        this.imageUri = imageUri
        imageUri?.let {
            if (sizeChanged) {
                setBitmap(it, width, height)
            }
        }
    }

    /**
     * Show the crop overlay
     */
    fun showCropOverlay() {
        cropOverlayView.visibility = View.VISIBLE
    }

    /**
     * Hide the crop overlay
     */
    fun hideCropOverlay() {
        cropOverlayView.visibility = View.INVISIBLE
    }

    /**
     * Get the current crop overlay's cropping points.
     *
     * @return Crop points that make the crop overlay diagonal.
     */
    val cropPoints: CropPoints
        get() {
            var ratio = originalWidth.toFloat() / bitmap.width
            if (rotationAngle % 180 != 0) {
                ratio = originalWidth.toFloat() / bitmap.height
            }
            val cropPoints = cropOverlayView.cropPoints
            val p1 = cropPoints.point1
            val p2 = cropPoints.point2
            p1.x = ((p1.x - bitmapBounds.left) * ratio).toInt()
            p1.y = ((p1.y - bitmapBounds.top) * ratio).toInt()
            p2.x = ((p2.x - bitmapBounds.left) * ratio).toInt()
            p2.y = ((p2.y - bitmapBounds.top) * ratio).toInt()

            return cropPoints
        }

    /**
     * Return a result bitmap of the editing changes, or the source if none was made.
     */
    val resultBitmap: Bitmap
        get() {
            val cropPoints: CropPoints = cropOverlayView.cropPoints
            val p1 = cropPoints.point1
            val p2 = cropPoints.point2

            return if (p2.x - p1.x != bitmap.width || p2.y - p1.y != bitmap.height) {
                Bitmap.createBitmap(
                    bitmap,
                    p1.x - bitmapBounds.left,
                    p1.y - bitmapBounds.top,
                    p2.x - p1.x,
                    p2.y - p1.y
                )
            } else {
                bitmap
            }
        }

    /**
     * Rotate the image by 90 degrees.
     */
    fun rotateImage() {
        rotationAngle = (rotationAngle + 90) % 360
        rotateBitmapBy(90)
        updateImageViewBitmap()
    }

    private fun rotateBitmapBy(degrees: Int) {
        val matrix = Matrix()
        matrix.setRotate(
            degrees.toFloat(),
            bitmap.width / 2f,
            bitmap.height / 2f
        )
        val scale: Float = if (degrees % 180 != 0) {
            max(
                bitmap.width / height.toFloat(),
                bitmap.height / width.toFloat()
            )
        } else {
            max(
                bitmap.width / width.toFloat(),
                bitmap.height / height.toFloat()
            )
        }
        val dstWidth = bitmap.width / scale
        val dstHeight = bitmap.height / scale
        if (bitmap.width.toFloat() != dstWidth || bitmap.height.toFloat() != dstHeight) {
            val sx = dstWidth / bitmap.width.toFloat()
            val sy = dstHeight / bitmap.height.toFloat()
            matrix.postScale(sx, sy)
        }
        bitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    private fun setBitmapBounds() {
        val left = (width - bitmap.width) / 2
        val top = (height - bitmap.height) / 2
        bitmapBounds.set(left, top, left + bitmap.width, top + bitmap.height)
        cropOverlayView.set(bitmapBounds)
    }
}