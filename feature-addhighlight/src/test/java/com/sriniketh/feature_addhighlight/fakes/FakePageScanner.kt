package com.sriniketh.feature_addhighlight.fakes

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import com.sriniketh.feature_addhighlight.PageScanner
import com.sriniketh.feature_addhighlight.ocr.OcrPage
import io.mockk.mockk

class FakePageScanner : PageScanner {

    val photo: ImageBitmap = mockk()
    var pageToReturn = OcrPage(width = 0, height = 0, lines = emptyList())
    var shouldFailToLoad = false
    var shouldFailToRecognize = false
    val loadedUris = mutableListOf<Uri>()

    override suspend fun loadPhoto(uri: Uri): ImageBitmap {
        loadedUris.add(uri)
        if (shouldFailToLoad) throw RuntimeException("Photo could not be decoded")
        return photo
    }

    override suspend fun recognizeText(photo: ImageBitmap): OcrPage {
        if (shouldFailToRecognize) throw RuntimeException("Text recognition failed")
        return pageToReturn
    }
}
