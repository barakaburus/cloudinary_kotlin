package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.Rect
import android.view.MotionEvent

internal class CropBottomRightCornerHandler(
    overlay: Rect,
    private val listener: CropOverlayGestureCallback?
) : CropOverlayGestureHandler(overlay) {

    override fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        bounds.set(
            overlay.right - gestureRegionWidth,
            overlay.bottom - gestureRegionHeight,
            overlay.right + gestureRegionWidth,
            overlay.bottom + gestureRegionHeight
        )
        super.handleTouchEvent(event, isAspectRatioLocked)
    }

    public override fun handleGesture(event: MotionEvent, isAspectRatioLocked: Boolean) {
        var left = overlay.left
        var top = overlay.top
        val right = overlay.right + (event.x - prevTouchEventPoint.x).toInt()
        val bottom = overlay.bottom + (event.y - prevTouchEventPoint.y).toInt()

        if (isAspectRatioLocked) {
            left -= bottom - overlay.bottom
            top -= right - overlay.right
        }

        listener?.onOverlaySizeChanged(left, top, right, bottom)
    }
}