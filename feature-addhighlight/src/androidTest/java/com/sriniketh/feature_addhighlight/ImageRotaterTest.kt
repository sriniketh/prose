package com.sriniketh.feature_addhighlight

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ImageRotaterTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val sourceWidth = 4
    private val sourceHeight = 2

    private fun writeTestImage(fileName: String, orientation: Int?): Uri {
        val bitmap = Bitmap.createBitmap(sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.BLACK)
        bitmap.setPixel(0, 0, Color.RED)

        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        if (orientation != null) {
            val exifInterface = ExifInterface(file.absolutePath)
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            exifInterface.saveAttributes()
        }

        return Uri.fromFile(file)
    }

    @Test
    fun whenExifOrientationIsNormalThenBitmapIsUnrotated() {
        val uri = writeTestImage("normal.png", ExifInterface.ORIENTATION_NORMAL)

        val rotated = ImageRotater.getRotatedBitmap(context, uri)

        assertNotNull(rotated)
        assertEquals(sourceWidth, rotated!!.width)
        assertEquals(sourceHeight, rotated.height)
        assertEquals(Color.RED, rotated.getPixel(0, 0))
    }

    @Test
    fun whenExifOrientationIsMissingThenBitmapIsUnrotated() {
        val uri = writeTestImage("missing_orientation.png", orientation = null)

        val rotated = ImageRotater.getRotatedBitmap(context, uri)

        assertNotNull(rotated)
        assertEquals(sourceWidth, rotated!!.width)
        assertEquals(sourceHeight, rotated.height)
        assertEquals(Color.RED, rotated.getPixel(0, 0))
    }

    @Test
    fun whenExifOrientationIsRotate90ThenBitmapDimensionsAreSwappedAndContentRotatedClockwise() {
        val uri = writeTestImage("rotate90.png", ExifInterface.ORIENTATION_ROTATE_90)

        val rotated = ImageRotater.getRotatedBitmap(context, uri)

        assertNotNull(rotated)
        assertEquals(sourceHeight, rotated!!.width)
        assertEquals(sourceWidth, rotated.height)
        assertEquals(Color.RED, rotated.getPixel(rotated.width - 1, 0))
    }

    @Test
    fun whenExifOrientationIsRotate180ThenBitmapDimensionsAreUnchangedAndContentRotatedHalfway() {
        val uri = writeTestImage("rotate180.png", ExifInterface.ORIENTATION_ROTATE_180)

        val rotated = ImageRotater.getRotatedBitmap(context, uri)

        assertNotNull(rotated)
        assertEquals(sourceWidth, rotated!!.width)
        assertEquals(sourceHeight, rotated.height)
        assertEquals(Color.RED, rotated.getPixel(rotated.width - 1, rotated.height - 1))
    }

    @Test
    fun whenExifOrientationIsRotate270ThenBitmapDimensionsAreSwappedAndContentRotatedCounterClockwise() {
        val uri = writeTestImage("rotate270.png", ExifInterface.ORIENTATION_ROTATE_270)

        val rotated = ImageRotater.getRotatedBitmap(context, uri)

        assertNotNull(rotated)
        assertEquals(sourceHeight, rotated!!.width)
        assertEquals(sourceWidth, rotated.height)
        assertEquals(Color.RED, rotated.getPixel(0, rotated.height - 1))
    }

    @Test
    fun whenRotate90AndRotate270ThenContentEndsUpInDifferentCorners() {
        val rotated90 = ImageRotater.getRotatedBitmap(
            context,
            writeTestImage("rotate90_vs_270_a.png", ExifInterface.ORIENTATION_ROTATE_90)
        )
        val rotated270 = ImageRotater.getRotatedBitmap(
            context,
            writeTestImage("rotate90_vs_270_b.png", ExifInterface.ORIENTATION_ROTATE_270)
        )

        assertNotNull(rotated90)
        assertNotNull(rotated270)
        assertEquals(Color.RED, rotated90!!.getPixel(rotated90.width - 1, 0))
        assertEquals(Color.RED, rotated270!!.getPixel(0, rotated270.height - 1))
        assertEquals(Color.BLACK, rotated90.getPixel(0, rotated90.height - 1))
        assertEquals(Color.BLACK, rotated270.getPixel(rotated270.width - 1, 0))
    }

    @Test
    fun whenImageUriPointsToNonExistentFileThenNullIsReturned() {
        val missingUri = Uri.fromFile(File(context.cacheDir, "does-not-exist.png"))

        val rotated = ImageRotater.getRotatedBitmap(context, missingUri)

        assertNull(rotated)
    }

    @Test
    fun whenImageUriIsMalformedThenNullIsReturned() {
        val invalidUri = Uri.parse("content://com.sriniketh.feature_addhighlight.unknown.provider/missing")

        val rotated = ImageRotater.getRotatedBitmap(context, invalidUri)

        assertNull(rotated)
    }
}
