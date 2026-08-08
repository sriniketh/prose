package com.sriniketh.feature_addhighlight

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.sriniketh.core_data.usecases.CreateTempImageFileUseCase
import com.sriniketh.core_data.usecases.DeleteFileUseCase
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
    private lateinit var createTempImageFileUseCase: CreateTempImageFileUseCase
    private lateinit var deleteFileUseCase: DeleteFileUseCase
    private lateinit var getRotatedBitmapUseCase: GetRotatedBitmapUseCase
    private lateinit var saveCroppedImageUseCase: SaveCroppedImageUseCase
    private lateinit var contentResolver: ContentResolver
    private lateinit var context: Context
    private lateinit var viewModel: CaptureAndCropImageViewModel
    private lateinit var imageUri: Uri

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeFileSource = FakeFileSource()
        createTempImageFileUseCase = CreateTempImageFileUseCase(fakeFileSource)
        deleteFileUseCase = DeleteFileUseCase(fakeFileSource)
        contentResolver = mockk()
        context = mockk()
        every { context.contentResolver } returns contentResolver
        getRotatedBitmapUseCase = GetRotatedBitmapUseCase(context, testDispatcher)
        saveCroppedImageUseCase = SaveCroppedImageUseCase(context, testDispatcher)

        viewModel = CaptureAndCropImageViewModel(
            createTempImageFileUseCase = createTempImageFileUseCase,
            deleteFileUseCase = deleteFileUseCase,
            getRotatedBitmapUseCase = getRotatedBitmapUseCase,
            saveCroppedImageUseCase = saveCroppedImageUseCase,
            savedStateHandle = SavedStateHandle(),
            ioDispatcher = testDispatcher
        )
        imageUri = (viewModel.screenState.value as CaptureAndCropImageScreenState.CaptureImage).imageUri
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
    fun `when cleared before crop completes then temp file is deleted using injected dispatcher`() = runTest {
        val store = ViewModelStore()
        store.put("captureAndCropImageViewModel", viewModel)

        store.clear()
        advanceUntilIdle()

        assertTrue(fakeFileSource.deletedUris.contains(imageUri))
    }

    @Test
    fun `when cleared after crop completes then temp file is not deleted`() = runTest {
        val outputStream = mockk<OutputStream>(relaxed = true)
        every { contentResolver.openOutputStream(imageUri) } returns outputStream
        val croppedBitmap = mockk<Bitmap>()
        every { croppedBitmap.compress(any(), any(), any()) } returns true
        viewModel.onImageCropped(croppedBitmap)
        advanceUntilIdle()

        val store = ViewModelStore()
        store.put("captureAndCropImageViewModel", viewModel)
        store.clear()
        advanceUntilIdle()

        assertFalse(fakeFileSource.deletedUris.contains(imageUri))
    }
}
