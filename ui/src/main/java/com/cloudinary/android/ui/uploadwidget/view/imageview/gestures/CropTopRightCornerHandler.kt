package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.Rect
import android.view.MotionEvent

internal class CropTopRightCornerHandler(
    overlay: Rect,
    private val listener: CropOverlayGestureCallback?
) : CropOverlayGestureHandler(overlay) {

    override fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        bounds[overlay.right - gestureRegionWidth, overlay.top - gestureRegionHeight, overlay.right + gestureRegionWidth] =
            overlay.top + gestureRegionHeight
        super.handleTouchEvent(event, isAspectRatioLocked)
    }

    public override fun handleGesture(event: MotionEvent, isAspectRatioLocked: Boolean) {
        var left = overlay.left
        val top = overlay.top + (event.y - prevTouchEventPoint.y).toInt()
        val right = overlay.right + (event.x - prevTouchEventPoint.x).toInt()
        var bottom = overlay.bottom

        if (isAspectRatioLocked) {
            left += top - overlay.top
            bottom += right - overlay.right
        }

        listener?.onOverlaySizeChanged(left, top, right, bottom)
    }

}