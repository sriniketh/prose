package com.sriniketh.feature_addhighlight

import android.content.Context
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sriniketh.core_platform.logTag
import com.sriniketh.feature_addhighlight.ocr.OcrLine
import com.sriniketh.feature_addhighlight.ocr.OcrPage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class PageScannerImpl @Inject constructor(@ApplicationContext private val appContext: Context) :
    PageScanner {

    private var recognizerFactory: () -> TextRecognizer =
        { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    internal constructor(
        appContext: Context,
        recognizerFactory: () -> TextRecognizer
    ) : this(appContext) {
        this.recognizerFactory = recognizerFactory
    }

    override suspend fun loadPhoto(uri: Uri): ImageBitmap = withContext(Dispatchers.IO) {
        ImageRotater.getRotatedBitmap(appContext, uri)?.asImageBitmap()
            ?: throw IOException("Unable to decode photo at $uri")
    }

    override suspend fun recognizeText(photo: ImageBitmap): OcrPage =
        suspendCancellableCoroutine { continuation ->
            val recognizer = recognizerFactory()
            val image = InputImage.fromBitmap(photo.asAndroidBitmap(), 0)
            recognizer.process(image)
                .addOnSuccessListener {
                    Timber.d("${this.logTag()}: Recognized text: ${it.text}")
                    continuation.resumeWith(Result.success(it.toOcrPage(photo.width, photo.height)))
                    recognizer.close()
                }
                .addOnFailureListener {
                    Timber.e(it, this.logTag())
                    continuation.resumeWith(Result.failure(it))
                    recognizer.close()
                }
            continuation.invokeOnCancellation { recognizer.close() }
        }
}

interface PageScanner {
    suspend fun loadPhoto(uri: Uri): ImageBitmap
    suspend fun recognizeText(photo: ImageBitmap): OcrPage
}

internal fun Text.toOcrPage(width: Int, height: Int): OcrPage = OcrPage(
    width = width,
    height = height,
    lines = textBlocks.flatMapIndexed { blockIndex, block ->
        block.lines.filter { it.text.isNotBlank() }.mapNotNull { line ->
            line.corners()?.let { corners ->
                OcrLine(text = line.text.trim(), blockIndex = blockIndex, corners = corners)
            }
        }
    }
)

private fun Text.Line.corners(): List<Offset>? =
    cornerPoints?.takeIf { it.size == 4 }?.map { Offset(it.x.toFloat(), it.y.toFloat()) }
        ?: boundingBox?.run {
            listOf(
                Offset(left.toFloat(), top.toFloat()),
                Offset(right.toFloat(), top.toFloat()),
                Offset(right.toFloat(), bottom.toFloat()),
                Offset(left.toFloat(), bottom.toFloat()),
            )
        }
