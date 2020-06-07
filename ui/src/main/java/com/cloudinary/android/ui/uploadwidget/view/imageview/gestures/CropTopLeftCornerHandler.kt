package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.Rect
import android.view.MotionEvent

internal class CropTopLeftCornerHandler(
    overlay: Rect,
    private val listener: CropOverlayGestureCallback?
) : CropOverlayGestureHandler(overlay) {

    override fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        bounds.set(
            overlay.left - gestureRegionWidth,
            overlay.top - gestureRegionHeight,
            overlay.left + gestureRegionWidth,
            overlay.top + gestureRegionHeight
        )
        super.handleTouchEvent(event, isAspectRatioLocked)
    }

    public override fun handleGesture(event: MotionEvent, isAspectRatioLocked: Boolean) {
        val left = overlay.left + (event.x - prevTouchEventPoint.x).toInt()
        val top = overlay.top + (event.y - prevTouchEventPoint.y).toInt()
        var right = overlay.right
        var bottom = overlay.bottom

        if (isAspectRatioLocked) {
            bottom -= left - overlay.left
            right -= top - overlay.top
        }

        listener?.onOverlaySizeChanged(left, top, right, bottom)
    }

}