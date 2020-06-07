package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.view.MotionEvent

internal interface CropGestureHandler {
    fun setNext(nextHandler: CropGestureHandler?)

    fun handleTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean)
}