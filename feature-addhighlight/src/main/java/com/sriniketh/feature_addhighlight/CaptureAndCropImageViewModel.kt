package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sriniketh.core_data.usecases.CreateFileUseCase
import com.sriniketh.core_data.usecases.DeleteFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CaptureAndCropImageViewModel @Inject constructor(
    private val createFileUseCase: CreateFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val imageUri: Uri by lazy {
        savedStateHandle.get<Uri>("imageUri") ?: createFileUseCase().also {
            savedStateHandle["imageUri"] = it
        }
    }

    private val _screenState: MutableStateFlow<CaptureAndCropImageScreenState> =
        MutableStateFlow(CaptureAndCropImageScreenState.CaptureImage(imageUri))
    internal val screenState: StateFlow<CaptureAndCropImageScreenState> = _screenState.asStateFlow()

    internal fun onImageCaptured() {
        _screenState.update { CaptureAndCropImageScreenState.CropImage(imageUri) }
    }

    internal fun onImageCropped() {
        _screenState.update { CaptureAndCropImageScreenState.ImageCapturedAndCropped(imageUri) }
    }

    override fun onCleared() {
        if (screenState.value !is CaptureAndCropImageScreenState.ImageCapturedAndCropped) {
            deleteFileUseCase(imageUri)
            savedStateHandle.remove<Uri>("imageUri")
        }
        super.onCleared()
    }
}

internal sealed interface CaptureAndCropImageScreenState {
    data class CaptureImage(val imageUri: Uri) : CaptureAndCropImageScreenState
    data class CropImage(val imageUri: Uri) : CaptureAndCropImageScreenState
    data class ImageCapturedAndCropped(val imageUri: Uri) : CaptureAndCropImageScreenState
}
