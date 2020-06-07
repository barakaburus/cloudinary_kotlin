package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.Rect
import android.view.MotionEvent

internal class CropBottomSideHandler(
    overlay: Rect,
    private val listener: CropOverlayGestureCallback?
) : CropOverlayGestureHandler(overlay) {

    override fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        bounds.set(
            overlay.left + gestureRegionWidth,
            overlay.bottom - gestureRegionHeight,
            overlay.right - gestureRegionWidth,
            overlay.bottom + gestureRegionHeight
        )
        super.handleTouchEvent(event, isAspectRatioLocked)
    }

    public override fun handleGesture(event: MotionEvent, isAspectRatioLocked: Boolean) {
        var left = overlay.left
        val top = overlay.top
        var right = overlay.right
        val bottom = overlay.bottom + (event.y - prevTouchEventPoint.y).toInt()

        if (isAspectRatioLocked) {
            left -= ((bottom.toFloat() - overlay.bottom) / 2).toInt()
            right += ((bottom.toFloat() - overlay.bottom) / 2).toInt()
        }

        listener?.onOverlaySizeChanged(left, top, right, bottom)
    }
}