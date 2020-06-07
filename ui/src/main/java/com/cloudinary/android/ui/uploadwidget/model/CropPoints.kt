package com.cloudinary.android.ui.uploadwidget.model

import android.graphics.Point
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Points to be used for cropping preprocessing.
 */
@Parcelize
data class CropPoints(var point1: Point, var point2: Point) : Parcelable