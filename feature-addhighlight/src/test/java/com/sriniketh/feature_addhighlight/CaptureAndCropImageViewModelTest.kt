package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.sriniketh.core_data.usecases.CreateTempImageFileUseCase
import com.sriniketh.core_data.usecases.DeleteFileUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CaptureAndCropImageViewModelTest {

    private lateinit var createTempImageFileUseCase: CreateTempImageFileUseCase
    private lateinit var deleteFileUseCase: DeleteFileUseCase
    private lateinit var fakeUri: Uri

    @Before
    fun setup() {
        fakeUri = mockk()
        createTempImageFileUseCase = mockk()
        every { createTempImageFileUseCase() } returns fakeUri
        deleteFileUseCase = mockk()
        every { deleteFileUseCase(any()) } returns true
    }

    private fun buildViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): CaptureAndCropImageViewModel = CaptureAndCropImageViewModel(
        createTempImageFileUseCase,
        deleteFileUseCase,
        savedStateHandle
    )

    @Test
    fun `when created then screen state starts at CaptureImage`() {
        val viewModel = buildViewModel()

        assertTrue(viewModel.screenState.value is CaptureAndCropImageScreenState.CaptureImage)
    }

    @Test
    fun `when onImageCaptured is called then screen state moves to CropImage`() {
        val viewModel = buildViewModel()

        viewModel.onImageCaptured()

        assertTrue(viewModel.screenState.value is CaptureAndCropImageScreenState.CropImage)
    }

    @Test
    fun `when onImageCropped is called then screen state moves to ImageCapturedAndCropped`() {
        val viewModel = buildViewModel()

        viewModel.onImageCaptured()
        viewModel.onImageCropped()

        assertTrue(viewModel.screenState.value is CaptureAndCropImageScreenState.ImageCapturedAndCropped)
    }

    @Test
    fun `when recreated with same SavedStateHandle after image captured then resumes at CropImage instead of relaunching the camera`() {
        val savedStateHandle = SavedStateHandle()
        val originalViewModel = buildViewModel(savedStateHandle)
        originalViewModel.onImageCaptured()

        val recreatedViewModel = buildViewModel(savedStateHandle)

        assertTrue(recreatedViewModel.screenState.value is CaptureAndCropImageScreenState.CropImage)
    }

    @Test
    fun `when recreated with same SavedStateHandle after image cropped then resumes at ImageCapturedAndCropped`() {
        val savedStateHandle = SavedStateHandle()
        val originalViewModel = buildViewModel(savedStateHandle)
        originalViewModel.onImageCaptured()
        originalViewModel.onImageCropped()

        val recreatedViewModel = buildViewModel(savedStateHandle)

        assertTrue(recreatedViewModel.screenState.value is CaptureAndCropImageScreenState.ImageCapturedAndCropped)
    }

    @Test
    fun `when recreated with same SavedStateHandle then reuses the same imageUri instead of creating a new temp file`() {
        val savedStateHandle = SavedStateHandle()
        val originalViewModel = buildViewModel(savedStateHandle)
        originalViewModel.onImageCaptured()
        val originalUri =
            (originalViewModel.screenState.value as CaptureAndCropImageScreenState.CropImage).imageUri

        val recreatedViewModel = buildViewModel(savedStateHandle)
        val recreatedUri =
            (recreatedViewModel.screenState.value as CaptureAndCropImageScreenState.CropImage).imageUri

        assertEquals(originalUri, recreatedUri)
        verify(exactly = 1) { createTempImageFileUseCase() }
    }

    @Test
    fun `when cleared before crop completes then temp file is deleted`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = buildViewModel(savedStateHandle)
        viewModel.onImageCaptured()
        val store = ViewModelStore()
        store.put("key", viewModel)

        store.clear()

        verify(exactly = 1) { deleteFileUseCase(fakeUri) }
    }

    @Test
    fun `when cleared after crop completes then temp file is not deleted`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = buildViewModel(savedStateHandle)
        viewModel.onImageCaptured()
        viewModel.onImageCropped()
        val store = ViewModelStore()
        store.put("key", viewModel)

        store.clear()

        verify(exactly = 0) { deleteFileUseCase(any()) }
    }
}
