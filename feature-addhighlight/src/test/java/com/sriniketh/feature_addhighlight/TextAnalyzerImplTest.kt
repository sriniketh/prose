package com.sriniketh.feature_addhighlight

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TextAnalyzerImplTest {

    private lateinit var appContext: Context
    private lateinit var recognizer: TextRecognizer
    private lateinit var task: Task<Text>
    private lateinit var textAnalyzer: TextAnalyzerImpl

    @Before
    fun setup() {
        appContext = mockk()
        recognizer = mockk(relaxUnitFun = true)
        task = mockk()

        mockkStatic(InputImage::class)
        every { InputImage.fromFilePath(any(), any()) } returns mockk()
        every { recognizer.process(any<InputImage>()) } returns task

        textAnalyzer = TextAnalyzerImpl(appContext) { recognizer }
    }

    @After
    fun tearDown() {
        unmockkStatic(InputImage::class)
    }

    @Test
    fun `when recognition succeeds then recognizer is closed`() = runTest {
        val recognizedText = mockk<Text> {
            every { text } returns "Recognized text"
        }
        every { task.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Text>>().onSuccess(recognizedText)
            task
        }
        every { task.addOnFailureListener(any()) } returns task

        val result = textAnalyzer.analyzeImage(mockk())

        assertEquals(recognizedText, result)
        verify(exactly = 1) { recognizer.close() }
    }

    @Test
    fun `when recognition fails then recognizer is closed`() = runTest {
        val failure = RuntimeException("recognition failed")
        every { task.addOnSuccessListener(any()) } returns task
        every { task.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(failure)
            task
        }

        val thrown = runCatching { textAnalyzer.analyzeImage(mockk()) }.exceptionOrNull()

        assertEquals(failure.message, thrown?.message)
        verify(exactly = 1) { recognizer.close() }
    }

    @Test
    fun `when recognition is cancelled then recognizer is closed`() = runTest {
        every { task.addOnSuccessListener(any()) } returns task
        every { task.addOnFailureListener(any()) } returns task

        val job = launch { textAnalyzer.analyzeImage(mockk()) }
        advanceUntilIdle()
        job.cancelAndJoin()

        verify(exactly = 1) { recognizer.close() }
    }
}
