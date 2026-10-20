package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_platform.FileSource
import com.sriniketh.core_platform.logTag
import com.sriniketh.feature_addhighlight.ocr.LineSelection
import com.sriniketh.feature_addhighlight.ocr.OcrPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class CaptureAndSelectTextViewModel @Inject constructor(
    private val fileSource: FileSource,
    private val pageScanner: PageScanner,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val photoUri: Uri = savedStateHandle.get<Uri>(PHOTO_URI)
        ?: fileSource.createNewFile("${UUID.randomUUID()}.jpg").also {
            savedStateHandle[PHOTO_URI] = it
        }

    private var hasPhoto: Boolean
        get() = savedStateHandle[HAS_PHOTO] ?: false
        set(value) {
            savedStateHandle[HAS_PHOTO] = value
        }

    private var isCameraOpen: Boolean
        get() = savedStateHandle[CAMERA_OPEN] ?: false
        set(value) {
            savedStateHandle[CAMERA_OPEN] = value
        }

    private val _uiState: MutableStateFlow<CaptureAndSelectTextUiState> = MutableStateFlow(
        if (isCameraOpen) CaptureAndSelectTextUiState.CameraOpen
        else CaptureAndSelectTextUiState.OpenCamera(photoUri)
    )
    internal val uiState: StateFlow<CaptureAndSelectTextUiState> = _uiState.asStateFlow()

    private var readJob: Job? = null

    init {
        if (hasPhoto && !isCameraOpen) readPhoto()
    }

    internal fun onCameraOpened() {
        isCameraOpen = true
        _uiState.value = CaptureAndSelectTextUiState.CameraOpen
    }

    internal fun onCameraClosed(photoTaken: Boolean) {
        isCameraOpen = false
        if (photoTaken) hasPhoto = true
        if (hasPhoto) {
            readPhoto()
        } else {
            _uiState.value = CaptureAndSelectTextUiState.Abandoned
        }
    }

    internal fun retake() {
        readJob?.cancel()
        _uiState.value = CaptureAndSelectTextUiState.OpenCamera(photoUri)
    }

    internal fun toggleBlock(lines: IntRange) = updateSelection { it.toggle(lines) }

    internal fun startDrag(line: Int) = updateSelection { it.startDrag(line) }

    internal fun dragTo(line: Int) = updateSelection { it.dragTo(line) }

    internal fun endDrag() = updateSelection { it.endDrag() }

    private fun updateSelection(transform: (LineSelection) -> LineSelection) {
        _uiState.update { state ->
            if (state is CaptureAndSelectTextUiState.Selecting) {
                state.copy(selection = transform(state.selection))
            } else {
                state
            }
        }
    }

    private fun readPhoto() {
        readJob?.cancel()
        _uiState.value = CaptureAndSelectTextUiState.Reading(photo = null)
        readJob = viewModelScope.launch {
            var photo: ImageBitmap? = null
            try {
                photo = pageScanner.loadPhoto(photoUri)
                _uiState.value = CaptureAndSelectTextUiState.Reading(photo)
                val page = pageScanner.recognizeText(photo)
                _uiState.value = if (page.lines.isEmpty()) {
                    CaptureAndSelectTextUiState.Failed(photo, R.string.no_text_found_message)
                } else {
                    CaptureAndSelectTextUiState.Selecting(photo, page)
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Timber.e(exception, this.logTag())
                _uiState.value = CaptureAndSelectTextUiState.Failed(
                    photo,
                    R.string.image_processing_failure_error_message
                )
            }
        }
    }

    override fun onCleared() {
        fileSource.deleteFile(photoUri)
        super.onCleared()
    }

    private companion object {
        const val PHOTO_URI = "photoUri"
        const val HAS_PHOTO = "hasPhoto"
        const val CAMERA_OPEN = "cameraOpen"
    }
}

internal sealed interface CaptureAndSelectTextUiState {
    data class OpenCamera(val photoUri: Uri) : CaptureAndSelectTextUiState
    data object CameraOpen : CaptureAndSelectTextUiState
    data class Reading(val photo: ImageBitmap?) : CaptureAndSelectTextUiState
    data class Selecting(
        val photo: ImageBitmap,
        val page: OcrPage,
        val selection: LineSelection = LineSelection()
    ) : CaptureAndSelectTextUiState {
        val selectedText: String = page.textOf(selection.selected)
    }

    data class Failed(
        val photo: ImageBitmap?,
        @StringRes val message: Int
    ) : CaptureAndSelectTextUiState

    data object Abandoned : CaptureAndSelectTextUiState
}
