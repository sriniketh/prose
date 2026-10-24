package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.sriniketh.feature_addhighlight.fakes.FakeFileSource
import com.sriniketh.feature_addhighlight.fakes.FakePageScanner
import com.sriniketh.feature_addhighlight.ocr.OcrLine
import com.sriniketh.feature_addhighlight.ocr.OcrPage
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureAndSelectTextViewModelTest {
    private lateinit var dispatcher: TestDispatcher
    private lateinit var fakeFileSource: FakeFileSource
    private lateinit var fakePageScanner: FakePageScanner
    private lateinit var savedStateHandle: SavedStateHandle

    private val corners = listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(10f, 5f), Offset(0f, 5f))
    private val page = OcrPage(
        width = 100,
        height = 100,
        lines = listOf(
            OcrLine(text = "First line", blockIndex = 0, corners = corners),
            OcrLine(text = "of the page.", blockIndex = 0, corners = corners),
            OcrLine(text = "Second paragraph.", blockIndex = 1, corners = corners)
        )
    )

    @Before
    fun setup() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        fakeFileSource = FakeFileSource()
        fakePageScanner = FakePageScanner().apply { pageToReturn = page }
        savedStateHandle = SavedStateHandle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = CaptureAndSelectTextViewModel(
        fileSource = fakeFileSource,
        pageScanner = fakePageScanner,
        savedStateHandle = savedStateHandle
    )

    private fun CaptureAndSelectTextViewModel.takePhoto() {
        onCameraOpened()
        onCameraClosed(photoTaken = true)
        dispatcher.scheduler.advanceUntilIdle()
    }

    private val CaptureAndSelectTextViewModel.selecting
        get() = uiState.value as CaptureAndSelectTextUiState.Selecting

    @Test
    fun `when initialized then the camera opens on a new jpg file`() {
        val viewModel = createViewModel()

        assertEquals(
            CaptureAndSelectTextUiState.OpenCamera(fakeFileSource.uriToReturnForNewFile),
            viewModel.uiState.value
        )
        assertTrue(fakeFileSource.createdFileNames.single().endsWith(".jpg"))
    }

    @Test
    fun `when saved state already has a photo uri then it is reused without creating a file`() {
        val existingUri = mockk<Uri>()
        savedStateHandle = SavedStateHandle(mapOf("photoUri" to existingUri))

        val viewModel = createViewModel()

        assertEquals(CaptureAndSelectTextUiState.OpenCamera(existingUri), viewModel.uiState.value)
        assertTrue(fakeFileSource.createdFileNames.isEmpty())
    }

    @Test
    fun `when the camera is opened then state waits for the camera result`() {
        val viewModel = createViewModel()

        viewModel.onCameraOpened()

        assertEquals(CaptureAndSelectTextUiState.CameraOpen, viewModel.uiState.value)
    }

    @Test
    fun `when recreated while the camera is open then it waits for the camera result`() {
        createViewModel().onCameraOpened()

        val restored = createViewModel()

        assertEquals(CaptureAndSelectTextUiState.CameraOpen, restored.uiState.value)
    }

    @Test
    fun `when a photo is taken then its text is read and nothing is selected`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.onCameraOpened()

        viewModel.onCameraClosed(photoTaken = true)

        assertEquals(CaptureAndSelectTextUiState.Reading(photo = null), viewModel.uiState.value)
        advanceUntilIdle()
        assertEquals(
            CaptureAndSelectTextUiState.Selecting(photo = fakePageScanner.photo, page = page),
            viewModel.uiState.value
        )
        assertEquals("", viewModel.selecting.selectedText)
        assertEquals(listOf(fakeFileSource.uriToReturnForNewFile), fakePageScanner.loadedUris)
    }

    @Test
    fun `when the first photo is cancelled then the flow is abandoned`() {
        val viewModel = createViewModel()
        viewModel.onCameraOpened()

        viewModel.onCameraClosed(photoTaken = false)

        assertEquals(CaptureAndSelectTextUiState.Abandoned, viewModel.uiState.value)
    }

    @Test
    fun `when a retake is cancelled then the earlier photo is read again`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.takePhoto()

        viewModel.retake()
        assertEquals(
            CaptureAndSelectTextUiState.OpenCamera(fakeFileSource.uriToReturnForNewFile),
            viewModel.uiState.value
        )
        viewModel.onCameraOpened()
        viewModel.onCameraClosed(photoTaken = false)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is CaptureAndSelectTextUiState.Selecting)
        assertEquals(2, fakePageScanner.loadedUris.size)
    }

    @Test
    fun `when recreated after a photo was taken then the photo is read again`() = runTest(dispatcher) {
        createViewModel().takePhoto()

        val restored = createViewModel()
        advanceUntilIdle()

        assertTrue(restored.uiState.value is CaptureAndSelectTextUiState.Selecting)
    }

    @Test
    fun `when the photo has no text then reading fails with a no text message`() = runTest(dispatcher) {
        fakePageScanner.pageToReturn = OcrPage(width = 100, height = 100, lines = emptyList())
        val viewModel = createViewModel()

        viewModel.takePhoto()

        assertEquals(
            CaptureAndSelectTextUiState.Failed(fakePageScanner.photo, R.string.no_text_found_message),
            viewModel.uiState.value
        )
    }

    @Test
    fun `when the photo cannot be decoded then reading fails without a photo`() = runTest(dispatcher) {
        fakePageScanner.shouldFailToLoad = true
        val viewModel = createViewModel()

        viewModel.takePhoto()

        assertEquals(
            CaptureAndSelectTextUiState.Failed(null, R.string.image_processing_failure_error_message),
            viewModel.uiState.value
        )
    }

    @Test
    fun `when text recognition fails then reading fails with the photo shown`() = runTest(dispatcher) {
        fakePageScanner.shouldFailToRecognize = true
        val viewModel = createViewModel()

        viewModel.takePhoto()

        assertEquals(
            CaptureAndSelectTextUiState.Failed(
                fakePageScanner.photo,
                R.string.image_processing_failure_error_message
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun `when a block is toggled then its text is selected`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.takePhoto()

        viewModel.toggleBlock(page.blocks[1])

        assertEquals("Second paragraph.", viewModel.selecting.selectedText)
    }

    @Test
    fun `when lines are dragged across then the dragged range is selected`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.takePhoto()

        viewModel.startDrag(1)
        viewModel.dragTo(2)
        viewModel.endDrag()

        assertEquals("of the page.\nSecond paragraph.", viewModel.selecting.selectedText)
    }

    @Test
    fun `when selection changes outside of selecting then state is unchanged`() {
        val viewModel = createViewModel()

        viewModel.toggleBlock(0..1)

        assertEquals(
            CaptureAndSelectTextUiState.OpenCamera(fakeFileSource.uriToReturnForNewFile),
            viewModel.uiState.value
        )
    }

    @Test
    fun `when cleared then the photo file is deleted`() {
        val viewModel = createViewModel()
        val viewModelStore = ViewModelStore()
        viewModelStore.put("key", viewModel)

        viewModelStore.clear()

        assertEquals(listOf(fakeFileSource.uriToReturnForNewFile), fakeFileSource.deletedUris)
    }
}
