package com.cloudinary.android.ui.uploadwidget.view

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.cloudinary.android.ui.R
import com.cloudinary.android.ui.uploadwidget.model.BitmapManager
import com.cloudinary.android.ui.uploadwidget.model.BitmapManager.LoadCallback
import com.cloudinary.android.ui.uploadwidget.model.Dimensions
import com.cloudinary.android.ui.uploadwidget.view.ThumbnailsAdapter.ThumbnailViewHolder
import com.cloudinary.android.ui.uploadwidget.utils.MediaType
import com.cloudinary.android.ui.uploadwidget.utils.getMediaType
import kotlinx.android.synthetic.main.thumbnail_list_item.view.*
import java.util.*

/**
 * Displays the images' thumbnails.
 */
internal class ThumbnailsAdapter(
    private val imagesUris: ArrayList<Uri>,
    private val callback: Callback?
) : RecyclerView.Adapter<ThumbnailViewHolder>() {
    private var selectedThumbnailPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.thumbnail_list_item, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val uri = imagesUris[position]
        if (position == selectedThumbnailPosition) {
            holder.imageView.setBackgroundResource(R.drawable.selected_thumbnail_border)
        } else {
            holder.imageView.setBackgroundResource(0)
        }
        holder.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        holder.imageView.setImageBitmap(null)
        holder.itemView.setOnClickListener {
            notifyItemChanged(selectedThumbnailPosition)
            selectedThumbnailPosition = holder.adapterPosition
            notifyItemChanged(selectedThumbnailPosition)
            callback?.onThumbnailClicked(uri)
        }
        val context: Context = holder.itemView.context
        val thumbnailSize = context.resources.getDimension(R.dimen.thumbnail_size).toInt()
        val mediaType = getMediaType(context, uri)
        if (mediaType === MediaType.IMAGE) {
            holder.mediaTypeIcon.visibility = View.GONE
            BitmapManager.load(context, uri, thumbnailSize, thumbnailSize, object : LoadCallback {
                    override fun onSuccess(bitmap: Bitmap, originalDimensions: Dimensions) {
                        if (holder.adapterPosition == position) {
                            holder.imageView.setImageBitmap(bitmap)
                        }
                    }

                    override fun onFailure() {}
                })
        } else if (mediaType === MediaType.VIDEO) {
            holder.mediaTypeIcon.visibility = View.VISIBLE
            holder.mediaTypeIcon.setImageResource(R.drawable.video)
            BitmapManager.thumbnail(context, uri, thumbnailSize, thumbnailSize, object : LoadCallback {
                    override fun onSuccess(bitmap: Bitmap, originalDimensions: Dimensions) {
                        if (holder.adapterPosition == position) {
                            holder.imageView.setImageBitmap(bitmap)
                        }
                    }

                    override fun onFailure() {}
                })
        }
    }

    override fun getItemCount(): Int = imagesUris.size

    /**
     * Set the selected thumbnail.
     *
     * @param position Position of the new selected thumbnail.
     */
    fun setSelectedThumbnail(position: Int) {
        notifyItemChanged(selectedThumbnailPosition)
        notifyItemChanged(position)
        selectedThumbnailPosition = position
    }

    internal class ThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.imageView
        val mediaTypeIcon: ImageView = itemView.mediaTypeIcon
    }

    /**
     * Callback for interacting with the thumbnail list.
     */
    interface Callback {
        /**
         * Called when a thumbnail is clicked.
         *
         * @param uri Uri of the clicked thumbnail.
         */
        fun onThumbnailClicked(uri: Uri)
    }
}