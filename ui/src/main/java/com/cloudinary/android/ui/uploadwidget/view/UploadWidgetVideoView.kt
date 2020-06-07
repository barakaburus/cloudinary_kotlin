package com.cloudinary.android.ui.uploadwidget.view

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.widget.VideoView
import androidx.core.view.GestureDetectorCompat

internal class UploadWidgetVideoView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : VideoView(context, attrs, defStyle) {
    private var mListener: VideoListener? = null
    private var gestureDetector = GestureDetectorCompat(context, object : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (isPlaying) {
                pause()
            } else {
                start()
            }
            return super.onSingleTapUp(e)
        }
    })

    fun setListener(listener: VideoListener?) {
        mListener = listener
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return true
    }

    override fun pause() {
        super.pause()
        mListener?.onPause()
    }

    override fun start() {
        super.start()
        mListener?.onPlay()
    }

    interface VideoListener {
        fun onPlay()
        fun onPause()
    }
}