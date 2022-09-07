package com.sriniketh.feature_addhighlight

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import timber.log.Timber
import javax.inject.Inject

class TextAnalyzerImpl @Inject constructor() : TextAnalyzer {

    private var _lastSeenText: Text? = null
    override val lastSeenText: Text?
        get() = _lastSeenText

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            try {
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(inputImage)
                    .addOnSuccessListener { visionText ->
                        _lastSeenText = visionText
                        imageProxy.close()
                    }
                    .addOnFailureListener { error ->
                        Timber.e(error)
                        imageProxy.close()
                    }
            } catch (exception: Exception) {
                Timber.e(exception)
            }
        }
    }
}

interface TextAnalyzer : ImageAnalysis.Analyzer {
    val lastSeenText: Text?
}
