package com.cloudinary.android.ui.uploadwidget.view

import android.net.Uri
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.cloudinary.android.ui.R
import com.cloudinary.android.ui.uploadwidget.model.Media
import com.cloudinary.android.ui.uploadwidget.view.UploadWidgetVideoView.VideoListener
import com.cloudinary.android.ui.uploadwidget.view.imageview.UploadWidgetImageView
import com.cloudinary.android.ui.uploadwidget.utils.MediaType
import com.cloudinary.android.ui.uploadwidget.utils.getMediaType
import java.util.*

/**
 * Displays media files or their results.
 */
internal class MediaPagerAdapter(
    uris: ArrayList<Uri>,
    mediaViewPager: ViewPager
) : PagerAdapter() {
    private val views: SparseArray<View> = SparseArray(uris.size)
    private val mediaList: ArrayList<Media> = ArrayList<Media>(uris.size)
    private var currentPagePosition = 0

    init {
        for (sourceUri in uris) {
            mediaList.add(Media(sourceUri))
        }
        mediaViewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                (views[currentPagePosition] as? UploadWidgetVideoView)?.pause()
                currentPagePosition = position
            }
        })
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val context = container.context
        val media: Media = mediaList[position]
        val uri: Uri = media.resultUri ?: media.sourceUri
        val mediaType = getMediaType(context, uri) ?: MediaType.IMAGE

        val view = if (mediaType === MediaType.IMAGE) {
            val uploadWidgetImageView = UploadWidgetImageView(context).also {
                it.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                it.setImageUri(uri)
            }
            views.put(position, uploadWidgetImageView)
            uploadWidgetImageView
        } else {
            val frameLayout = FrameLayout(context)
            val playOverlay = ImageView(context).apply {
                val playButtonOverlaySize =
                    context.resources.getDimension(R.dimen.video_play_button_overlay_size).toInt()
                layoutParams = FrameLayout.LayoutParams(
                    playButtonOverlaySize,
                    playButtonOverlaySize,
                    Gravity.CENTER
                )
                setImageResource(R.drawable.play_overlay)
            }
            val videoView = UploadWidgetVideoView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
                setVideoURI(uri)
                setListener(object : VideoListener {
                    override fun onPlay() {
                        playOverlay.visibility = View.GONE
                    }

                    override fun onPause() {
                        playOverlay.visibility = View.VISIBLE
                    }
                })
                setOnPreparedListener { seekTo(1) }
            }
            frameLayout.addView(videoView)
            frameLayout.addView(playOverlay)
            views.put(position, videoView)
            frameLayout
        }
        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
        views.remove(position)
    }

    override fun isViewFromObject(view: View, o: Any): Boolean = view === o

    override fun getItemPosition(obj: Any): Int = POSITION_NONE

    override fun getCount(): Int = mediaList.size

    /**
     * Update the result media
     *
     * @param sourceUri The Uri of the source media that's being updated.
     * @param resultUri The Uri of the result media.
     */
    fun updateMediaResult(sourceUri: Uri?, resultUri: Uri?) {
        for (media in mediaList) {
            if (media.sourceUri.toString() == sourceUri.toString()) {
                media.resultUri = resultUri
                notifyDataSetChanged()
                break
            }
        }
    }

    /**
     * Reset the result media
     *
     * @param sourceUri The Uri of the source media to reset.
     */
    fun resetMediaResult(sourceUri: Uri?) = updateMediaResult(sourceUri, null)

    /**
     * Get the media uri's position within the adapter.
     *
     * @param uri Uri of the media.
     * @return Position of the media within the adapter, or -1 of it doesn't exist.
     */
    fun getMediaPosition(uri: Uri): Int {
        for (i in mediaList.indices) {
            val media: Media = mediaList[i]
            if (media.sourceUri.toString() == uri.toString()) {
                return i
            }
        }
        return -1
    }
}