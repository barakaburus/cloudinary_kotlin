package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.PointF
import android.graphics.Rect
import android.view.MotionEvent
import kotlin.math.max

private const val GESTURE_REGION = 0.25f
private const val MIN_GESTURE_REGION = 30

/**
 * Base class for the crop overlay handlers.
 */
internal abstract class CropOverlayGestureHandler(
    protected val overlay: Rect
) : CropGestureHandler {
    protected val bounds = Rect()
    protected val prevTouchEventPoint = PointF()
    private var nextHandler: CropGestureHandler? = null
    private var isStartedGesture = false

    override fun setNext(nextHandler: CropGestureHandler?) {
        this.nextHandler = nextHandler
    }

    /**
     * Detects whether the handler should handle this event or pass it along the chain.
     * Child handlers should call this method after updating its bounds in order to avoid inconsistencies.
     *
     * @param event Motion event which triggered the event.
     * @param isAspectRatioLocked Whether the crop overlay's aspect ratio is locked or not.
     */
    override fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        when (event.action) {
            MotionEvent.ACTION_DOWN ->
                if (bounds.contains(event.x.toInt(), event.y.toInt())) {
                    isStartedGesture = true
                    prevTouchEventPoint.set(event.x, event.y)
                    handleGesture(event, isAspectRatioLocked)
                } else {
                    isStartedGesture = false
                    nextHandler?.handleTouchEvent(event, isAspectRatioLocked)
                }

            MotionEvent.ACTION_MOVE ->
                if (isStartedGesture) {
                    handleGesture(event, isAspectRatioLocked)
                    prevTouchEventPoint.set(event.x, event.y)
                } else {
                    nextHandler?.handleTouchEvent(event, isAspectRatioLocked)
                }

            MotionEvent.ACTION_UP -> isStartedGesture = false
        }
    }

    /**
     * Override this method to handle the touch gesture that occurred on the crop overlay.
     * This method is invoked only if the [MotionEvent.ACTION_DOWN] event was handled by the handler.
     *
     * @param event Motion event which triggered the gesture's touch event.
     * @param isAspectRatioLocked Whether the crop overlay's aspect ratio is locked or not.
     */
    protected abstract fun handleGesture(event: MotionEvent, isAspectRatioLocked: Boolean)

    protected val gestureRegionWidth: Int
        get() = max((GESTURE_REGION * overlay.width()).toInt(), MIN_GESTURE_REGION)

    protected val gestureRegionHeight: Int
        get() = max((GESTURE_REGION * overlay.height()).toInt(), MIN_GESTURE_REGION)
}