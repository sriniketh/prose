package com.sriniketh.feature_addhighlight

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import com.sriniketh.feature_addhighlight.ocr.OcrPage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PageScannerImplTest {

    private lateinit var recognizer: TextRecognizer
    private lateinit var task: Task<Text>
    private lateinit var photo: ImageBitmap
    private lateinit var pageScanner: PageScannerImpl

    @Before
    fun setup() {
        recognizer = mockk(relaxUnitFun = true)
        task = mockk()
        val bitmap = mockk<Bitmap>()
        photo = mockk {
            every { width } returns 300
            every { height } returns 400
        }

        mockkStatic(InputImage::class.java.name, "androidx.compose.ui.graphics.AndroidImageBitmap_androidKt")
        every { photo.asAndroidBitmap() } returns bitmap
        every { InputImage.fromBitmap(bitmap, 0) } returns mockk()
        every { recognizer.process(any<InputImage>()) } returns task

        pageScanner = PageScannerImpl(mockk<Context>()) { recognizer }
    }

    @After
    fun tearDown() {
        unmockkStatic(InputImage::class.java.name, "androidx.compose.ui.graphics.AndroidImageBitmap_androidKt")
    }

    private fun succeedWith(recognized: Text) {
        every { task.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Text>>().onSuccess(recognized)
            task
        }
        every { task.addOnFailureListener(any()) } returns task
    }

    @Test
    fun `when recognition succeeds then the page has the photo size and the recognizer is closed`() =
        runTest {
            succeedWith(mockk {
                every { text } returns ""
                every { textBlocks } returns emptyList()
            })

            val page = pageScanner.recognizeText(photo)

            assertEquals(OcrPage(width = 300, height = 400, lines = emptyList()), page)
            verify(exactly = 1) { recognizer.close() }
        }

    @Test
    fun `when recognition fails then the failure is thrown and the recognizer is closed`() = runTest {
        val failure = RuntimeException("recognition failed")
        every { task.addOnSuccessListener(any()) } returns task
        every { task.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(failure)
            task
        }

        val thrown = runCatching { pageScanner.recognizeText(photo) }.exceptionOrNull()

        assertEquals(failure.message, thrown?.message)
        verify(exactly = 1) { recognizer.close() }
    }

    @Test
    fun `when recognition is cancelled then the recognizer is closed`() = runTest {
        every { task.addOnSuccessListener(any()) } returns task
        every { task.addOnFailureListener(any()) } returns task

        val job = launch { pageScanner.recognizeText(photo) }
        advanceUntilIdle()
        job.cancelAndJoin()

        verify(exactly = 1) { recognizer.close() }
    }
}
