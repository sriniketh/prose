package com.sriniketh.feature_addhighlight.fakes

import android.net.Uri
import com.google.mlkit.vision.text.Text
import com.sriniketh.feature_addhighlight.TextAnalyzer
import io.mockk.every
import io.mockk.mockk

class FakeTextAnalyzer : TextAnalyzer {

    var shouldThrowException = false
    var analyzedUri: Uri? = null
    var textToReturn = "Fake analyzed text from image"

    override suspend fun analyzeImage(uri: Uri): Text {
        analyzedUri = uri
        if (shouldThrowException) {
            throw RuntimeException("Text analysis failed")
        }

        return mockk<Text> {
            every { text } returns textToReturn
        }
    }
}
