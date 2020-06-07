package com.cloudinary.android.ui

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.cloudinary.android.ui.uploadwidget.UploadWidget
import com.cloudinary.android.ui.uploadwidget.view.UploadWidgetActivity
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException

private const val TEST_IMAGE = "image.png"
private var assetFile: File? = null

class UploadWidgetTest {

    companion object {

        @BeforeClass @JvmStatic
        @Throws(IOException::class)
        fun setUpClass() {
            assetFile = assetToFile(TEST_IMAGE)
        }
    }

    @Rule
    @JvmField
    internal val activityTestRule = ActivityTestRule(UploadWidgetActivity::class.java, true, false)

    // TODO: Fix UI tests for travis
    @Ignore
    @Test
    fun testUploadWidget() {
        val intent = Intent().apply {
            putParcelableArrayListExtra(UploadWidget.URIS_EXTRA, arrayListOf<Uri>(Uri.fromFile(assetFile)))
        }
        activityTestRule.launchActivity(intent)

        onView(withId(R.id.crop_action)).perform(click())
        onView(withId(R.id.doneButton)).perform(click())
        onView(withId(R.id.uploadFab)).perform(click())
    }
}
