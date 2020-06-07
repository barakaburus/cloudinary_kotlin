package com.cloudinary.android.ui.uploadwidget.view.imageview.gestures

/**
 * Callback for overlay gestures.
 */
internal interface CropOverlayGestureCallback {
    /**
     * Called when the overlay is dragged.
     * @param distanceX Distance the overlay was dragged on the x-axis.
     * @param distanceY Distance the overlay was dragged on the y-axis.
     */
    fun onOverlayDragged(distanceX: Int, distanceY: Int)

    /**
     * Called when the overlay is resized.
     * @param left Left value of the resized overlay.
     * @param top Top value of the resized overlay.
     * @param right Right value of the resized overlay.
     * @param bottom Bottom value of the resized overlay.
     */
    fun onOverlaySizeChanged(left: Int, top: Int, right: Int, bottom: Int)
}