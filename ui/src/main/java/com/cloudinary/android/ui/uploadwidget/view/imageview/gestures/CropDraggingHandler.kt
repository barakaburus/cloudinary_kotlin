package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.Rect
import android.view.MotionEvent

internal class CropDraggingHandler(
    overlay: Rect,
    private val listener: CropOverlayGestureCallback?
) : CropOverlayGestureHandler(overlay) {

    override fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        bounds.set(overlay)
        bounds.inset(gestureRegionWidth, gestureRegionHeight)
        super.handleTouchEvent(event, isAspectRatioLocked)
    }

    public override fun handleGesture(event: MotionEvent, isAspectRatioLocked: Boolean) {
        val distanceX = event.x - prevTouchEventPoint.x
        val distanceY = event.y - prevTouchEventPoint.y

        listener?.onOverlayDragged(distanceX.toInt(), distanceY.toInt())
    }

}