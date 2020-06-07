package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

import android.graphics.Rect
import android.view.MotionEvent

/**
 * Detects various gestures on the crop overlay, firing a [CropOverlayGestureCallback] for the corresponding gestures.
 *
 * @constructor Creates a CropOverlayGestureDetector with the supplied crop overlay rectangle, and a listener
 * to respond for the detected gestures.
 * @param overlay  Crop overlay rectangle.
 * @param listener Notified with the callbacks for the corresponding gestures.
 */
internal class CropOverlayGestureDetector(overlay: Rect, listener: CropOverlayGestureCallback?) {
    private val cropGestureHandler: CropGestureHandler

    /**
     * Handles touch events on the crop overlay.
     * @param event Motion event which triggered the event.
     * @param isAspectRatioLocked Whether the crop overlay's aspect ratio is locked or not.
     */
    fun onTouchEvent(event: MotionEvent, isAspectRatioLocked: Boolean) {
        cropGestureHandler.handleTouchEvent(event, isAspectRatioLocked)
    }

    init {
        val cropLeftSideHandler: CropGestureHandler = CropLeftSideHandler(overlay, listener)
        val cropTopLeftCornerHandler: CropGestureHandler = CropTopLeftCornerHandler(overlay, listener)
        val cropTopSideHandler: CropGestureHandler = CropTopSideHandler(overlay, listener)
        val cropTopRightCornerHandler: CropGestureHandler = CropTopRightCornerHandler(overlay, listener)
        val cropRightSideHandler: CropGestureHandler = CropRightSideHandler(overlay, listener)
        val cropBottomRightCornerHandler: CropGestureHandler = CropBottomRightCornerHandler(overlay, listener)
        val cropBottomSideHandler: CropGestureHandler = CropBottomSideHandler(overlay, listener)
        val cropBottomLeftCornerHandler: CropGestureHandler = CropBottomLeftCornerHandler(overlay, listener)
        val cropDraggingHandler: CropGestureHandler = CropDraggingHandler(overlay, listener)

        cropLeftSideHandler.setNext(cropTopLeftCornerHandler)
        cropTopLeftCornerHandler.setNext(cropTopSideHandler)
        cropTopSideHandler.setNext(cropTopRightCornerHandler)
        cropTopRightCornerHandler.setNext(cropRightSideHandler)
        cropRightSideHandler.setNext(cropBottomRightCornerHandler)
        cropBottomRightCornerHandler.setNext(cropBottomSideHandler)
        cropBottomSideHandler.setNext(cropBottomLeftCornerHandler)
        cropBottomLeftCornerHandler.setNext(cropDraggingHandler)
        cropGestureHandler = cropLeftSideHandler
    }
}