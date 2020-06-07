package com.cloudinary.android.ui.uploadwidget.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.ui.R
import com.cloudinary.android.ui.uploadwidget.UploadWidget
import com.cloudinary.android.ui.uploadwidget.view.UploadWidgetFragment.UploadWidgetListener
import java.util.*

private const val UPLOAD_WIDGET_FRAGMENT_TAG = "upload_widget_fragment_tag"

/**
 * Provides a solution out of the box for developers who want to use the Upload Widget.
 */
internal class UploadWidgetActivity : AppCompatActivity(), UploadWidgetListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_widget)
        supportActionBar?.hide()

        val uris = intent.getParcelableArrayListExtra<Uri>(UploadWidget.URIS_EXTRA)!!
        val fragment = supportFragmentManager.findFragmentByTag(UPLOAD_WIDGET_FRAGMENT_TAG)
            ?: UploadWidgetFragment.newInstance(uris)

        supportFragmentManager.beginTransaction().replace(
            R.id.container,
            fragment,
            UPLOAD_WIDGET_FRAGMENT_TAG
        ).commit()
    }

    override fun onConfirm(results: ArrayList<UploadWidget.Result?>?) {
        val data = Intent()
        data.putParcelableArrayListExtra(UploadWidget.RESULT_EXTRA, results)
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}