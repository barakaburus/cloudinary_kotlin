package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.Rect
import android.view.MotionEvent

internal class CropLeftSideHandler(
    overlay: Rect,
    private val listener: CropOverlayGestureCallback?
) : CropOverlayGestureHandler(overlay) {

    override fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        bounds.set(
            overlay.left - gestureRegionWidth,
            overlay.top + gestureRegionHeight,
            overlay.left + gestureRegionWidth,
            overlay.bottom - gestureRegionHeight
        )
        super.handleTouchEvent(event, isAspectRatioLocked)
    }

    public override fun handleGesture(event: MotionEvent, isAspectRatioLocked: Boolean) {
        val left = overlay.left + (event.x - prevTouchEventPoint.x).toInt()
        var top = overlay.top
        val right = overlay.right
        var bottom = overlay.bottom

        if (isAspectRatioLocked) {
            top += ((left.toFloat() - overlay.left) / 2).toInt()
            bottom -= ((left.toFloat() - overlay.left) / 2).toInt()
        }

        listener?.onOverlaySizeChanged(left, top, right, bottom)
    }
}