package com.cloudinary.android.ui.uploadwidget.view

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.cloudinary.android.ui.R
import com.cloudinary.android.ui.uploadwidget.UploadWidget
import com.cloudinary.android.ui.uploadwidget.model.BitmapManager
import com.cloudinary.android.ui.uploadwidget.model.CropRotateResult
import com.cloudinary.android.ui.uploadwidget.utils.MediaType
import com.cloudinary.android.ui.uploadwidget.utils.getMediaType
import kotlinx.android.synthetic.main.fragment_upload_widget.*
import kotlinx.android.synthetic.main.fragment_upload_widget.view.*

private const val IMAGES_URIS_LIST_ARG = "images_uris_list_arg"

/**
 * Previews media files, and optionally edits them, before uploading.
 */
internal class UploadWidgetFragment : Fragment(), CropRotateFragment.Callback {
    private lateinit var mediaViewPager: ViewPager
    private lateinit var mediaPagerAdapter: MediaPagerAdapter
    private lateinit var thumbnailsRecyclerView: RecyclerView
    private lateinit var thumbnailsAdapter: ThumbnailsAdapter
    private lateinit var uris: ArrayList<Uri>
    private lateinit var uriResults: MutableMap<Uri?, UploadWidget.Result?>

    companion object {
        fun newInstance(imagesUris: ArrayList<Uri>) = UploadWidgetFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(IMAGES_URIS_LIST_ARG, imagesUris)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        uris = arguments?.getParcelableArrayList(IMAGES_URIS_LIST_ARG)!!
        uriResults = HashMap(uris.size)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload_widget, container, false)
        mediaViewPager = view.imagesViewPager
        mediaPagerAdapter = MediaPagerAdapter(uris, mediaViewPager)
        mediaViewPager.adapter = mediaPagerAdapter
        mediaViewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                thumbnailsAdapter.setSelectedThumbnail(position)
                thumbnailsRecyclerView.scrollToPosition(position)
                super.onPageSelected(position)
            }
        })
        view.uploadFab.setOnClickListener {
            (activity as? UploadWidgetListener)?.onConfirm(results)
        }
        thumbnailsRecyclerView = view.thumbnailsRecyclerView
        if (uris.size > 1) {
            thumbnailsAdapter = ThumbnailsAdapter(uris, object : ThumbnailsAdapter.Callback {
                    override fun onThumbnailClicked(uri: Uri) {
                        mediaViewPager.setCurrentItem(mediaPagerAdapter.getMediaPosition(uri), true)
                    }
                })
            thumbnailsRecyclerView.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            thumbnailsRecyclerView.adapter = thumbnailsAdapter
        } else {
            thumbnailsRecyclerView.visibility = View.INVISIBLE
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? AppCompatActivity)?.let {
            it.setSupportActionBar(toolbar)
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.upload_widget_menu, menu)
        val cropItem = menu.findItem(R.id.crop_action)
        val cropActionView = cropItem.actionView
        cropActionView.setOnClickListener {
            val uri = uris[mediaViewPager.currentItem]
            val cropRotateFragment = CropRotateFragment.newInstance(uri, this)
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(android.R.id.content, cropRotateFragment, null)?.addToBackStack(null)?.commit()
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCropRotateFinish(uri: Uri, result: CropRotateResult, resultBitmap: Bitmap) {
        val uwResult = uriResults[uri] ?: UploadWidget.Result(uri)
        uwResult.rotationAngle = result.rotationAngle
        uwResult.cropPoints = result.cropPoints
        uriResults[uri] = uwResult

        val mediaType = getMediaType(context!!, uri) ?: MediaType.IMAGE
        if (mediaType === MediaType.IMAGE) {
            BitmapManager.save(context!!, resultBitmap, object : BitmapManager.SaveCallback {
                override fun onSuccess(resultUri: Uri) {
                    mediaPagerAdapter.updateMediaResult(uri, resultUri)
                }

                override fun onFailure() {}
            })
        }
    }

    override fun onCropRotateCancel(uri: Uri) {
        val result = uriResults[uri]
        result?.rotationAngle = 0
        result?.cropPoints = null
        mediaPagerAdapter.resetMediaResult(uri)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private val results: ArrayList<UploadWidget.Result?>
        get() {
            val results = ArrayList<UploadWidget.Result?>(uriResults.size)
            for (uri in uris) {
                if (!uriResults.containsKey(uri)) {
                    uriResults[uri] = UploadWidget.Result(uri)
                }
            }
            results.addAll(uriResults.values)

            return results
        }

    /**
     * Listener for the Upload Widget.
     */
    interface UploadWidgetListener {
        /**
         * Called when the upload widget results are confirmed.
         *
         * @param results Upload widget's results.
         */
        fun onConfirm(results: ArrayList<UploadWidget.Result?>?)
    }
}