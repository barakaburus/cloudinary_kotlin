package com.cloudinary.android.ui.uploadwidget

import android.content.Context
import com.cloudinary.Cloudinary
import com.cloudinary.android.ui.uploadwidget.utils.MediaType
import com.cloudinary.android.ui.uploadwidget.utils.getMediaType
import com.cloudinary.android.uploader.request.LocalUriPayload
import com.cloudinary.android.uploader.request.UploadRequest
import com.cloudinary.transformation.resize.Resize.Companion.crop
import com.cloudinary.uploader

internal object UploadWidgetResultProcessor {
    fun process(context: Context, result: UploadWidget.Result): UploadRequest {
        val uploadRequest = UploadRequest.Builder(
            LocalUriPayload(context, result.uri),
            Cloudinary.get().uploader()
        ).build()

        return process(context, uploadRequest, result)
    }

    fun process(
        context: Context,
        uploadRequest: UploadRequest,
        result: UploadWidget.Result
    ): UploadRequest {
        val mediaType = getMediaType(context, result.uri) ?: MediaType.IMAGE
        var processedUploadRequest = uploadRequest

        if (mediaType === MediaType.IMAGE) {
//            val imagePreprocessChain = ImagePreprocessChain()
            if (result.rotationAngle != 0) {
//                imagePreprocessChain.addStep(Rotate(result.rotationAngle))
            }
            result.cropPoints?.let {
                //                imagePreprocessChain.addStep(
//                    Crop(result.cropPoints.getPoint1(), result.cropPoints.getPoint2())
//                )
            }
//            uploadRequest.preprocess(imagePreprocessChain)
        } else if (mediaType == MediaType.VIDEO) {
            processedUploadRequest = UploadRequest.Builder(
                processedUploadRequest.payload as LocalUriPayload,
                Cloudinary.get().uploader()
            ).apply {
                result.cropPoints?.let {
                    params {
                        transformation {
                            crop {
                                x = it.point1.x
                                y = it.point1.y
                                width = it.point2.x - it.point1.x
                                height = it.point2.y - it.point1.y
                            }
                        }
                    }
                }
                options { resourceType = "video" }
            }.build()
        }

        return processedUploadRequest
    }
}