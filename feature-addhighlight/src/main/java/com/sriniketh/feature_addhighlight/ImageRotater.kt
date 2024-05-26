package com.sriniketh.feature_addhighlight

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.sriniketh.core_platform.logTag
import timber.log.Timber

internal object ImageRotater {

    internal fun getRotatedBitmap(context: Context, imageUri: Uri): Bitmap? {
        try {
            val openedinputStream = context.contentResolver.openInputStream(imageUri)
            if (openedinputStream == null) {
                Timber.d("${this.logTag()}: Unable to open input stream")
                return null
            }

            openedinputStream.use { inputStream ->
                val exifInterface = ExifInterface(inputStream)
                val rotation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val rotationDegrees = exifToDegrees(rotation)
                Timber.d("${this.logTag()}: Rotating bitmap by $rotationDegrees")

                val matrix = Matrix()
                if (rotationDegrees != 0) matrix.postRotate(rotationDegrees.toFloat())
                val bitmap = context.contentResolver.openInputStream(imageUri)
                    .use { BitmapFactory.decodeStream(it) }

                Timber.d("${this.logTag()}: Bitmap rotated successfully")
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        } catch (exception: Exception) {
            Timber.e(exception, this.logTag())
            return null
        }
    }

    private fun exifToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
