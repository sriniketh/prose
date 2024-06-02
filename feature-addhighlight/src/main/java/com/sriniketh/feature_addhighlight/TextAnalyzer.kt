package com.sriniketh.feature_addhighlight

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sriniketh.core_platform.logTag
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject

class TextAnalyzerImpl @Inject constructor(@ApplicationContext private val appContext: Context) :
    TextAnalyzer {

    override suspend fun analyzeImage(uri: Uri): Text =
        suspendCancellableCoroutine { continuation ->
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromFilePath(appContext, uri)
            recognizer.process(image)
                .addOnSuccessListener {
                    Timber.d("${this.logTag()}: Recognized text: ${it.text}")
                    continuation.resumeWith(Result.success(it))
                }
                .addOnFailureListener {
                    Timber.e(it, this.logTag())
                    continuation.resumeWith(Result.failure(it))
                }
            continuation.invokeOnCancellation { recognizer.close() }
        }
}

interface TextAnalyzer {
    suspend fun analyzeImage(uri: Uri): Text
}
