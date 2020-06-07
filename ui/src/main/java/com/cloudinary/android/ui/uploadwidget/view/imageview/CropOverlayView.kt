package com.cloudinary.android.ui.uploadwidget.view.imageview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import com.cloudinary.android.ui.uploadwidget.model.CropPoints
import com.cloudinary.android.ui.uploadwidget.view.imageview.gestures.CropOverlayGestureCallback
import com.cloudinary.android.ui.uploadwidget.view.imageview.gestures.CropOverlayGestureDetector

private const val NUMBER_OF_GUIDELINES = 4
private const val CORNER_HANDLE_LENGTH = 50
private const val SIDE_HANDLE_LENGTH = 40
private const val HANDLE_OFFSET_FROM_OVERLAY = 5
private const val HANDLE_THICKNESS = 10
private const val MIN_OVERLAY_SIZE = 5

/**
 * Represents the crop overlay which covers the image with the cropping rectangle, while dimming the surrounding area.
 */
internal class CropOverlayView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), CropOverlayGestureCallback {
    private val dottedPath = Path()

    // Side handles
    private val leftHandlePath = Path()
    private val topHandlePath = Path()
    private val rightHandlePath = Path()
    private val bottomHandlePath = Path()

    // Corner handles
    private val topLeftHandlePath = Path()
    private val topRightHandlePath = Path()
    private val bottomRightHandlePath = Path()
    private val bottomLeftHandlePath = Path()
    private val dottedPaint = Paint()
    private val guidelinesPaint = Paint()
    private val handlePaint = Paint()
    private val dimBackgroundPaint = Paint()
    private val overlay = Rect()
    private var gestureDetector: CropOverlayGestureDetector =
        CropOverlayGestureDetector(overlay, this)
    private lateinit var imageBounds: Rect

    /**
     * Whether the aspect ratio should be locked.
     */
    var isAspectRatioLocked = false

    init {
        dottedPaint.color = Color.WHITE
        dottedPaint.style = Paint.Style.STROKE
        dottedPaint.strokeWidth = 5f
        dottedPaint.pathEffect = DashPathEffect(floatArrayOf(5f, 10f), 0f)
        guidelinesPaint.color = Color.WHITE
        guidelinesPaint.style = Paint.Style.STROKE
        handlePaint.color = Color.WHITE
        handlePaint.style = Paint.Style.FILL
        dimBackgroundPaint.color = ColorUtils.setAlphaComponent(Color.BLACK, 125)
        dimBackgroundPaint.style = Paint.Style.FILL
        visibility = INVISIBLE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!overlay.isEmpty) {
            drawDottedPath(canvas)
            drawGuidelines(canvas)
            drawHandles(canvas)
            dimSurroundingBackground(canvas)
        }
    }

    private fun drawDottedPath(canvas: Canvas) {
        dottedPath.reset()
        dottedPath.moveTo(overlay.left.toFloat(), overlay.top.toFloat())
        dottedPath.lineTo(overlay.right.toFloat(), overlay.top.toFloat())
        dottedPath.lineTo(overlay.right.toFloat(), overlay.bottom.toFloat())
        dottedPath.lineTo(overlay.left.toFloat(), overlay.bottom.toFloat())
        dottedPath.lineTo(overlay.left.toFloat(), overlay.top.toFloat())
        canvas.drawPath(dottedPath, dottedPaint)
    }

    private fun drawGuidelines(canvas: Canvas) {
        // Vertical guidelines
        val widthDiff = overlay.width() / NUMBER_OF_GUIDELINES
        var guidelineX: Float = overlay.left.toFloat()
        while (guidelineX <= overlay.right) {
            canvas.drawLine(
                guidelineX,
                overlay.top.toFloat(),
                guidelineX,
                overlay.bottom.toFloat(),
                guidelinesPaint
            )
            guidelineX += widthDiff
        }

        // Horizontal guidelines
        val heightDiff = overlay.height() / NUMBER_OF_GUIDELINES
        var guidelineY: Float = overlay.top.toFloat()
        while (guidelineY <= overlay.bottom) {
            canvas.drawLine(
                overlay.left.toFloat(),
                guidelineY,
                overlay.right.toFloat(),
                guidelineY,
                guidelinesPaint
            )
            guidelineY += heightDiff
        }
    }

    private fun drawHandles(canvas: Canvas) {
        // Middle handles
        leftHandlePath.reset()
        val leftRadiusValues = floatArrayOf(0f, 0f, 10f, 10f, 10f, 10f, 0f, 0f)
        leftHandlePath.addRoundRect(
            RectF(
                (overlay.left - HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.centerY() - SIDE_HANDLE_LENGTH / 2).toFloat(),
                (overlay.left + HANDLE_THICKNESS).toFloat(),
                (overlay.centerY() + SIDE_HANDLE_LENGTH / 2).toFloat()
            ),
            leftRadiusValues, Path.Direction.CCW
        )
        canvas.drawPath(leftHandlePath, handlePaint)

        topHandlePath.reset()
        val topRadiusValues = floatArrayOf(0f, 0f, 0f, 0f, 10f, 10f, 10f, 10f)
        topHandlePath.addRoundRect(
            RectF(
                (overlay.centerX() - SIDE_HANDLE_LENGTH / 2).toFloat(),
                (overlay.top - HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.centerX() + SIDE_HANDLE_LENGTH / 2).toFloat(),
                (overlay.top + HANDLE_THICKNESS).toFloat()
            ),
            topRadiusValues, Path.Direction.CCW
        )
        canvas.drawPath(topHandlePath, handlePaint)

        rightHandlePath.reset()
        val rightRadiusValues = floatArrayOf(10f, 10f, 0f, 0f, 0f, 0f, 10f, 10f)
        rightHandlePath.addRoundRect(
            RectF(
                (overlay.right - HANDLE_THICKNESS).toFloat(),
                (overlay.centerY() - SIDE_HANDLE_LENGTH / 2).toFloat(),
                (overlay.right + HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.centerY() + SIDE_HANDLE_LENGTH / 2).toFloat()
            ),
            rightRadiusValues, Path.Direction.CCW
        )
        canvas.drawPath(rightHandlePath, handlePaint)

        bottomHandlePath.reset()
        val bottomRadiusValues = floatArrayOf(10f, 10f, 10f, 10f, 0f, 0f, 0f, 0f)
        bottomHandlePath.addRoundRect(
            RectF(
                (overlay.centerX() - SIDE_HANDLE_LENGTH / 2).toFloat(),
                (overlay.bottom - HANDLE_THICKNESS).toFloat(),
                (overlay.centerX() + SIDE_HANDLE_LENGTH / 2).toFloat(),
                (overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY).toFloat()
            ),
            bottomRadiusValues, Path.Direction.CCW
        )
        canvas.drawPath(bottomHandlePath, handlePaint)

        // Corner handles
        topLeftHandlePath.reset()
        val topLeftHandleRadiusValues =
            floatArrayOf(0f, 0f, 0f, 0f, 10f, 10f, 0f, 0f)
        topLeftHandlePath.addRoundRect(
            RectF(
                (overlay.left - HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.top - HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.left + HANDLE_THICKNESS).toFloat(),
                (overlay.top + CORNER_HANDLE_LENGTH).toFloat()
            ),
            topLeftHandleRadiusValues, Path.Direction.CCW
        )
        topLeftHandlePath.addRoundRect(
            RectF(
                (overlay.left - HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.top - HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.left + CORNER_HANDLE_LENGTH).toFloat(),
                (overlay.top + HANDLE_THICKNESS).toFloat()
            ),
            topLeftHandleRadiusValues, Path.Direction.CCW
        )
        canvas.drawPath(topLeftHandlePath, handlePaint)

        topRightHandlePath.reset()
        val topRightRadiusValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 10f, 10f)
        topRightHandlePath.addRoundRect(
            RectF(
                (overlay.right - CORNER_HANDLE_LENGTH).toFloat(),
                (overlay.top - HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.right + HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.top + HANDLE_THICKNESS).toFloat()
            ),
            topRightRadiusValues, Path.Direction.CCW
        )
        topRightHandlePath.addRoundRect(
            RectF(
                (overlay.right - HANDLE_THICKNESS).toFloat(),
                (overlay.top - HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.right + HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.top + CORNER_HANDLE_LENGTH).toFloat()
            ),
            topRightRadiusValues, Path.Direction.CCW
        )
        canvas.drawPath(topRightHandlePath, handlePaint)

        bottomRightHandlePath.reset()
        val bottomRightRadiusValues =
            floatArrayOf(10f, 10f, 0f, 0f, 0f, 0f, 0f, 0f)
        bottomRightHandlePath.addRoundRect(
            RectF(
                (overlay.right - HANDLE_THICKNESS).toFloat(),
                (overlay.bottom - CORNER_HANDLE_LENGTH).toFloat(),
                (overlay.right + HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY).toFloat()
            ),
            bottomRightRadiusValues, Path.Direction.CCW
        )
        bottomRightHandlePath.addRoundRect(
            RectF(
                (overlay.right - CORNER_HANDLE_LENGTH).toFloat(),
                (overlay.bottom - HANDLE_THICKNESS).toFloat(),
                (overlay.right + HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY).toFloat()
            ),
            bottomRightRadiusValues, Path.Direction.CCW
        )
        canvas.drawPath(bottomRightHandlePath, handlePaint)

        bottomLeftHandlePath.reset()
        val bottomLeftRadiusValues =
            floatArrayOf(0f, 0f, 10f, 10f, 0f, 0f, 0f, 0f)
        bottomLeftHandlePath.addRoundRect(
            RectF(
                (overlay.left - HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.bottom - CORNER_HANDLE_LENGTH).toFloat(),
                (overlay.left + HANDLE_THICKNESS).toFloat(),
                (overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY).toFloat()
            ),
            bottomLeftRadiusValues, Path.Direction.CCW
        )
        bottomLeftHandlePath.addRoundRect(
            RectF(
                (overlay.left - HANDLE_OFFSET_FROM_OVERLAY).toFloat(),
                (overlay.bottom - HANDLE_THICKNESS).toFloat(),
                (overlay.left + CORNER_HANDLE_LENGTH).toFloat(),
                (overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY).toFloat()
            ),
            bottomLeftRadiusValues, Path.Direction.CCW
        )
        canvas.drawPath(bottomLeftHandlePath, handlePaint)
    }

    private fun dimSurroundingBackground(canvas: Canvas) {
        // left
        canvas.drawRect(
            0f,
            overlay.top - HANDLE_OFFSET_FROM_OVERLAY.toFloat(),
            overlay.left - HANDLE_OFFSET_FROM_OVERLAY.toFloat(),
            overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY.toFloat(),
            dimBackgroundPaint
        )
        // top
        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            overlay.top - HANDLE_OFFSET_FROM_OVERLAY.toFloat(),
            dimBackgroundPaint
        )
        // right
        canvas.drawRect(
            overlay.right + HANDLE_OFFSET_FROM_OVERLAY.toFloat(),
            overlay.top - HANDLE_OFFSET_FROM_OVERLAY.toFloat(),
            width.toFloat(),
            overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY.toFloat(),
            dimBackgroundPaint
        )
        // bottom
        canvas.drawRect(
            0f,
            overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY.toFloat(),
            width.toFloat(),
            height.toFloat(),
            dimBackgroundPaint
        )
    }

    /**
     * Get the current crop overlay's cropping points.
     * @return Crop points that make the crop overlay diagonal.
     */
    val cropPoints: CropPoints
        get() = CropPoints(
            Point(overlay.left, overlay.top),
            Point(overlay.right, overlay.bottom)
        )

    /**
     * Reset the crop overlay to cover its entire bounds.
     */
    fun reset() {
        overlay.set(imageBounds)
        invalidate()
    }

    /**
     * Set the crop overlay bounds, resetting the crop overlay to fit the given bounds.
     * @param imageBounds Bounds for the crop overlay.
     */
    fun set(imageBounds: Rect) {
        this.imageBounds = imageBounds
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event, isAspectRatioLocked)
        return true
    }

    override fun onOverlayDragged(distanceX: Int, distanceY: Int) {
        var distanceX = distanceX
        var distanceY = distanceY
        if (overlay.left + distanceX <= imageBounds.left || overlay.right + distanceX >= imageBounds.right) {
            distanceX = 0
        }
        if (overlay.top + distanceY <= imageBounds.top || overlay.bottom + distanceY >= imageBounds.bottom) {
            distanceY = 0
        }
        overlay.offset(distanceX, distanceY)
        invalidate()
    }

    override fun onOverlaySizeChanged(left: Int, top: Int, right: Int, bottom: Int) {
        if (left >= imageBounds.left &&
            top >= imageBounds.top &&
            right <= imageBounds.right &&
            bottom <= imageBounds.bottom &&
            right - left > MIN_OVERLAY_SIZE && bottom - top > MIN_OVERLAY_SIZE
        ) {
            overlay.set(left, top, right, bottom)
            invalidate()
        }
    }
}