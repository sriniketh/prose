package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.sriniketh.feature_addhighlight.fakes.FakeFileSource
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureAndCropImageViewModelTest {
    private lateinit var fakeFileSource: FakeFileSource
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: CaptureAndCropImageViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeFileSource = FakeFileSource()
        savedStateHandle = SavedStateHandle()
        viewModel = CaptureAndCropImageViewModel(
            fileSource = fakeFileSource,
            savedStateHandle = savedStateHandle
        )
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
        val restoredViewModel = CaptureAndCropImageViewModel(
            fileSource = restoredFileSource,
            savedStateHandle = restoredSavedStateHandle
        )

        val state = restoredViewModel.screenState.value

        assertEquals(CaptureAndCropImageScreenState.CaptureImage(existingUri), state)
        assertTrue(restoredFileSource.createdFileNames.isEmpty())
    }

    @Test
    fun `when image captured then state transitions to crop image with same uri`() {
        val initialUri =
            (viewModel.screenState.value as CaptureAndCropImageScreenState.CaptureImage).imageUri

        viewModel.onImageCaptured()

        assertEquals(
            CaptureAndCropImageScreenState.CropImage(initialUri),
            viewModel.screenState.value
        )
    }

    @Test
    fun `when image cropped then state transitions to image captured and cropped with same uri`() {
        val initialUri =
            (viewModel.screenState.value as CaptureAndCropImageScreenState.CaptureImage).imageUri
        viewModel.onImageCaptured()

        viewModel.onImageCropped()

        assertEquals(
            CaptureAndCropImageScreenState.ImageCapturedAndCropped(initialUri),
            viewModel.screenState.value
        )
    }

    @Test
    fun `when cleared before image is captured and cropped then file is deleted and saved state is cleared`() {
        val initialUri =
            (viewModel.screenState.value as CaptureAndCropImageScreenState.CaptureImage).imageUri
        val viewModelStore = ViewModelStore()
        viewModelStore.put("key", viewModel)

        viewModelStore.clear()

        assertTrue(fakeFileSource.deletedUris.contains(initialUri))
        assertNull(savedStateHandle.get<Uri>("imageUri"))
    }

    @Test
    fun `when cleared after image is captured and cropped then file is not deleted and saved state is preserved`() {
        viewModel.onImageCaptured()
        viewModel.onImageCropped()
        val croppedUri =
            (viewModel.screenState.value as CaptureAndCropImageScreenState.ImageCapturedAndCropped).imageUri
        val viewModelStore = ViewModelStore()
        viewModelStore.put("key", viewModel)

        viewModelStore.clear()

        assertTrue(fakeFileSource.deletedUris.isEmpty())
        assertEquals(croppedUri, savedStateHandle.get<Uri>("imageUri"))
    }
}
