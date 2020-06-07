package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.Rect
import android.view.MotionEvent

internal class CropBottomLeftCornerHandler(
    overlay: Rect,
    private val listener: CropOverlayGestureCallback?
) : CropOverlayGestureHandler(overlay) {

    override fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        bounds.set(
            overlay.left - gestureRegionWidth,
            overlay.bottom - gestureRegionHeight,
            overlay.left + gestureRegionWidth,
            overlay.bottom + gestureRegionHeight
        )
        super.handleTouchEvent(event, isAspectRatioLocked)
    }

    public override fun handleGesture(event: MotionEvent, isAspectRatioLocked: Boolean) {
        val left = overlay.left + (event.x - prevTouchEventPoint.x).toInt()
        var top = overlay.top
        var right = overlay.right
        val bottom = overlay.bottom + (event.y - prevTouchEventPoint.y).toInt()

        if (isAspectRatioLocked) {
            top += left - overlay.left
            right += bottom - overlay.bottom
        }

        listener?.onOverlaySizeChanged(left, top, right, bottom)
    }

}