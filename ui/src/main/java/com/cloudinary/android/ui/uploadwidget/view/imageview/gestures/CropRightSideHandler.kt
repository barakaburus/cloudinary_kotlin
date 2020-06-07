package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.Rect
import android.view.MotionEvent

internal class CropRightSideHandler(
    overlay: Rect,
    private val listener: CropOverlayGestureCallback?
) : CropOverlayGestureHandler(overlay) {

    override fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        bounds.set(
            overlay.right - gestureRegionWidth,
            overlay.top + gestureRegionHeight,
            overlay.right + gestureRegionWidth,
            overlay.bottom - gestureRegionHeight
        )
        super.handleTouchEvent(event, isAspectRatioLocked)
    }

    public override fun handleGesture(event: MotionEvent, isAspectRatioLocked: Boolean) {
        val left = overlay.left
        var top = overlay.top
        val right = overlay.right + (event.x - prevTouchEventPoint.x).toInt()
        var bottom = overlay.bottom

        if (isAspectRatioLocked) {
            top -= ((right.toFloat() - overlay.right) / 2).toInt()
            bottom += ((right.toFloat() - overlay.right) / 2).toInt()
        }

        listener?.onOverlaySizeChanged(left, top, right, bottom)
    }
}