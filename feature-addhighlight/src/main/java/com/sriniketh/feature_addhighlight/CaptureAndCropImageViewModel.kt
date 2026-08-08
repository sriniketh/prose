package com.sriniketh.feature_addhighlight

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.CreateTempImageFileUseCase
import com.sriniketh.core_data.usecases.DeleteFileUseCase
import com.sriniketh.core_platform.dagger.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaptureAndCropImageViewModel @Inject constructor(
    private val createTempImageFileUseCase: CreateTempImageFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val getRotatedBitmapUseCase: GetRotatedBitmapUseCase,
    private val saveCroppedImageUseCase: SaveCroppedImageUseCase,
    private val savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val imageUri: Uri by lazy {
        savedStateHandle.get<Uri>("imageUri") ?: createTempImageFileUseCase().also {
            savedStateHandle["imageUri"] = it
        }
    }

    private val _screenState: MutableStateFlow<CaptureAndCropImageScreenState> =
        MutableStateFlow(CaptureAndCropImageScreenState.CaptureImage(imageUri))
    internal val screenState: StateFlow<CaptureAndCropImageScreenState> = _screenState.asStateFlow()

    private val _rotatedBitmap = MutableStateFlow<Bitmap?>(null)
    internal val rotatedBitmap: StateFlow<Bitmap?> = _rotatedBitmap.asStateFlow()

    private val _effects = Channel<CaptureAndCropImageEffect>(Channel.BUFFERED)
    internal val effects: Flow<CaptureAndCropImageEffect> = _effects.receiveAsFlow()

    internal fun onImageCaptured() {
        _screenState.update { CaptureAndCropImageScreenState.CropImage(imageUri) }
        loadRotatedBitmap()
    }

    private fun loadRotatedBitmap() {
        viewModelScope.launch {
            val bitmap = getRotatedBitmapUseCase(imageUri)
            _rotatedBitmap.update { bitmap }
        }
    }

    internal fun onImageCropped(croppedBitmap: Bitmap) {
        viewModelScope.launch {
            val saved = saveCroppedImageUseCase(imageUri, croppedBitmap)
            if (saved) {
                _screenState.update { CaptureAndCropImageScreenState.ImageCapturedAndCropped(imageUri) }
            } else {
                _effects.trySend(CaptureAndCropImageEffect.ShowMessage(R.string.crop_image_error_message))
            }
        }
    }

    internal fun onImageLoadFailed() {
        _effects.trySend(CaptureAndCropImageEffect.ShowMessage(R.string.crop_image_error_message))
    }

    override fun onCleared() {
        if (screenState.value !is CaptureAndCropImageScreenState.ImageCapturedAndCropped) {
            CoroutineScope(SupervisorJob() + ioDispatcher).launch {
                deleteFileUseCase(imageUri)
            }
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

internal sealed interface CaptureAndCropImageEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : CaptureAndCropImageEffect
}
