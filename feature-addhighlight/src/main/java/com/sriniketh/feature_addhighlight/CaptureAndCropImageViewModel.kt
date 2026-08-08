package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sriniketh.core_data.usecases.CreateTempImageFileUseCase
import com.sriniketh.core_data.usecases.DeleteFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CaptureAndCropImageViewModel @Inject constructor(
    private val createTempImageFileUseCase: CreateTempImageFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val imageUri: Uri by lazy {
        savedStateHandle.get<Uri>("imageUri") ?: createTempImageFileUseCase().also {
            savedStateHandle["imageUri"] = it
        }
    }

    private var phase: CaptureAndCropImagePhase
        get() = savedStateHandle.get<String>(PHASE_ARG)
            ?.let { CaptureAndCropImagePhase.valueOf(it) }
            ?: CaptureAndCropImagePhase.CAPTURE
        set(value) {
            savedStateHandle[PHASE_ARG] = value.name
        }

    private val _screenState: MutableStateFlow<CaptureAndCropImageScreenState> =
        MutableStateFlow(phase.asScreenState(imageUri))
    internal val screenState: StateFlow<CaptureAndCropImageScreenState> = _screenState.asStateFlow()

    internal fun onImageCaptured() {
        phase = CaptureAndCropImagePhase.CROP
        _screenState.update { CaptureAndCropImageScreenState.CropImage(imageUri) }
    }

    internal fun onImageCropped() {
        phase = CaptureAndCropImagePhase.DONE
        _screenState.update { CaptureAndCropImageScreenState.ImageCapturedAndCropped(imageUri) }
    }

    override fun onCleared() {
        if (screenState.value !is CaptureAndCropImageScreenState.ImageCapturedAndCropped) {
            deleteFileUseCase(imageUri)
            savedStateHandle.remove<Uri>("imageUri")
            savedStateHandle.remove<String>(PHASE_ARG)
        }
        super.onCleared()
    }

    private companion object {
        private const val PHASE_ARG = "captureAndCropImagePhase"
    }
}

private enum class CaptureAndCropImagePhase {
    CAPTURE, CROP, DONE
}

private fun CaptureAndCropImagePhase.asScreenState(imageUri: Uri): CaptureAndCropImageScreenState =
    when (this) {
        CaptureAndCropImagePhase.CAPTURE -> CaptureAndCropImageScreenState.CaptureImage(imageUri)
        CaptureAndCropImagePhase.CROP -> CaptureAndCropImageScreenState.CropImage(imageUri)
        CaptureAndCropImagePhase.DONE -> CaptureAndCropImageScreenState.ImageCapturedAndCropped(imageUri)
    }

internal sealed interface CaptureAndCropImageScreenState {
    data class CaptureImage(val imageUri: Uri) : CaptureAndCropImageScreenState
    data class CropImage(val imageUri: Uri) : CaptureAndCropImageScreenState
    data class ImageCapturedAndCropped(val imageUri: Uri) : CaptureAndCropImageScreenState
}
