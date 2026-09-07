package com.sriniketh.feature_addhighlight

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.sriniketh.feature_addhighlight.fakes.FakeFileSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.OutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureAndCropImageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeFileSource: FakeFileSource
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getRotatedBitmapUseCase: GetRotatedBitmapUseCase
    private lateinit var saveCroppedImageUseCase: SaveCroppedImageUseCase
    private lateinit var contentResolver: ContentResolver
    private lateinit var context: Context
    private lateinit var viewModel: CaptureAndCropImageViewModel
    private lateinit var imageUri: Uri

    private fun buildViewModel(
        fileSource: FakeFileSource = fakeFileSource,
        savedStateHandle: SavedStateHandle = this.savedStateHandle
    ): CaptureAndCropImageViewModel = CaptureAndCropImageViewModel(
        fileSource = fileSource,
        getRotatedBitmapUseCase = getRotatedBitmapUseCase,
        saveCroppedImageUseCase = saveCroppedImageUseCase,
        savedStateHandle = savedStateHandle,
        ioDispatcher = testDispatcher
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeFileSource = FakeFileSource()
        savedStateHandle = SavedStateHandle()
        contentResolver = mockk()
        context = mockk()
        every { context.contentResolver } returns contentResolver
        getRotatedBitmapUseCase = GetRotatedBitmapUseCase(context, testDispatcher)
        saveCroppedImageUseCase = SaveCroppedImageUseCase(context, testDispatcher)

        viewModel = buildViewModel()
        imageUri = (viewModel.screenState.value as CaptureAndCropImageScreenState.CaptureImage).imageUri
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initialized then state is capture image with a newly created file`() {
        val state = viewModel.screenState.value

        assertTrue(state is CaptureAndCropImageScreenState.CaptureImage)
        val captureImageState = state as CaptureAndCropImageScreenState.CaptureImage
        assertEquals(fakeFileSource.uriToReturnForNewFile, captureImageState.imageUri)
        assertTrue(fakeFileSource.createdFileNames.single().endsWith(".jpg"))
    }

    @Test
    fun `when initialized then created image uri is saved into saved state handle`() {
        val captureImageState =
            viewModel.screenState.value as CaptureAndCropImageScreenState.CaptureImage

        assertEquals(captureImageState.imageUri, savedStateHandle.get<Uri>("imageUri"))
    }

    @Test
    fun `when saved state handle already has an image uri then it is reused without creating a new file`() {
        val existingUri = mockk<Uri>()
        val restoredFileSource = FakeFileSource()
        val restoredSavedStateHandle = SavedStateHandle(mapOf("imageUri" to existingUri))
        val restoredViewModel = buildViewModel(
            fileSource = restoredFileSource,
            savedStateHandle = restoredSavedStateHandle
        )

        val state = restoredViewModel.screenState.value

        assertEquals(CaptureAndCropImageScreenState.CaptureImage(existingUri), state)
        assertTrue(restoredFileSource.createdFileNames.isEmpty())
    }

    @Test
    fun `when image captured then state transitions to crop image with same uri`() {
        every { contentResolver.openInputStream(imageUri) } returns null
        val initialUri =
            (viewModel.screenState.value as CaptureAndCropImageScreenState.CaptureImage).imageUri

        viewModel.onImageCaptured()

        assertEquals(
            CaptureAndCropImageScreenState.CropImage(initialUri),
            viewModel.screenState.value
        )
    }

    @Test
    fun `when image cropped and write succeeds then state transitions to captured and cropped`() = runTest {
        val outputStream = mockk<OutputStream>(relaxed = true)
        every { contentResolver.openOutputStream(imageUri) } returns outputStream
        val croppedBitmap = mockk<Bitmap>()
        every { croppedBitmap.compress(any(), any(), any()) } returns true

        viewModel.onImageCropped(croppedBitmap)
        advanceUntilIdle()

        assertTrue(viewModel.screenState.value is CaptureAndCropImageScreenState.ImageCapturedAndCropped)
    }

    @Test
    fun `when image cropped and output stream is null then failure effect is emitted instead of silently succeeding`() =
        runTest {
            every { contentResolver.openOutputStream(imageUri) } returns null
            val croppedBitmap = mockk<Bitmap>()

            viewModel.effects.test {
                viewModel.onImageCropped(croppedBitmap)

                assertEquals(
                    CaptureAndCropImageEffect.ShowMessage(R.string.crop_image_error_message),
                    awaitItem()
                )
            }

            assertFalse(viewModel.screenState.value is CaptureAndCropImageScreenState.ImageCapturedAndCropped)
        }

    @Test
    fun `when image fails to load then failure effect is emitted instead of silently succeeding`() = runTest {
        viewModel.effects.test {
            viewModel.onImageLoadFailed()

            assertEquals(
                CaptureAndCropImageEffect.ShowMessage(R.string.crop_image_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.screenState.value is CaptureAndCropImageScreenState.ImageCapturedAndCropped)
    }

    @Test
    fun `when image captured then rotated bitmap is loaded using injected dispatcher`() = runTest {
        every { contentResolver.openInputStream(imageUri) } returns null

        viewModel.onImageCaptured()
        advanceUntilIdle()

        assertTrue(viewModel.screenState.value is CaptureAndCropImageScreenState.CropImage)
        assertNull(viewModel.rotatedBitmap.value)
    }

    @Test
    fun `when cleared before image is captured and cropped then file is deleted and saved state is cleared`() =
        runTest {
            val initialUri =
                (viewModel.screenState.value as CaptureAndCropImageScreenState.CaptureImage).imageUri
            val viewModelStore = ViewModelStore()
            viewModelStore.put("key", viewModel)

            viewModelStore.clear()
            advanceUntilIdle()

            assertTrue(fakeFileSource.deletedUris.contains(initialUri))
            assertNull(savedStateHandle.get<Uri>("imageUri"))
        }

    @Test
    fun `when cleared after image is captured and cropped then file is not deleted and saved state is preserved`() =
        runTest {
            val outputStream = mockk<OutputStream>(relaxed = true)
            every { contentResolver.openOutputStream(imageUri) } returns outputStream
            val croppedBitmap = mockk<Bitmap>()
            every { croppedBitmap.compress(any(), any(), any()) } returns true
            viewModel.onImageCropped(croppedBitmap)
            advanceUntilIdle()
            val croppedUri =
                (viewModel.screenState.value as CaptureAndCropImageScreenState.ImageCapturedAndCropped).imageUri

            val viewModelStore = ViewModelStore()
            viewModelStore.put("key", viewModel)
            viewModelStore.clear()
            advanceUntilIdle()

            assertTrue(fakeFileSource.deletedUris.isEmpty())
            assertEquals(croppedUri, savedStateHandle.get<Uri>("imageUri"))
        }
}
