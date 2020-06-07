package com.cloudinary.android.ui.uploadwidget.model

import android.net.Uri

internal data class Media(var sourceUri: Uri, var resultUri: Uri? = null)