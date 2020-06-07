package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.Rect
import android.view.MotionEvent

internal class CropTopSideHandler(
    overlay: Rect,
    private val listener: CropOverlayGestureCallback?
) : CropOverlayGestureHandler(overlay) {

    override fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        bounds.set(
            overlay.left + gestureRegionWidth,
            overlay.top - gestureRegionHeight,
            overlay.right - gestureRegionWidth,
            overlay.top + gestureRegionHeight
        )
        super.handleTouchEvent(event, isAspectRatioLocked)
    }

    public override fun handleGesture(event: MotionEvent, isAspectRatioLocked: Boolean) {
        var left = overlay.left
        val top = overlay.top + (event.y - prevTouchEventPoint.y).toInt()
        var right = overlay.right
        val bottom = overlay.bottom

        if (isAspectRatioLocked) {
            left += ((top.toFloat() - overlay.top) / 2).toInt()
            right -= ((top.toFloat() - overlay.top) / 2).toInt()
        }

        listener?.onOverlaySizeChanged(left, top, right, bottom)
    }

}