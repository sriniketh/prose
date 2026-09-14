package com.sriniketh.feature_addhighlight

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.mlkit.common.MlKitException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class TextAnalyzerImplTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun renderTextImageUri(text: String): Uri {
        val width = 600
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 90f
            isAntiAlias = true
        }
        canvas.drawText(text, 30f, height / 2f + 30f, paint)

        val file = File(context.cacheDir, "ocr_test_image.png")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        return Uri.fromFile(file)
    }

    @Test
    fun whenImageContainsRenderedTextThenRecognizedTextContainsIt() = runBlocking {
        val textAnalyzer = TextAnalyzerImpl(context)
        val imageUri = renderTextImageUri("PROSE")

        val result = try {
            withTimeout(90_000) { textAnalyzer.analyzeImage(imageUri) }
        } catch (mlKitException: MlKitException) {
            Assume.assumeNoException(
                "On-device text recognition model is unavailable on this device (no signed-in " +
                    "Play Store account to download the optional module), so the real recognizer " +
                    "cannot run here. Skipping rather than faking a recognized result.",
                mlKitException
            )
            return@runBlocking
        }

        assertTrue(result.text.uppercase().contains("PROSE"))
    }
}
