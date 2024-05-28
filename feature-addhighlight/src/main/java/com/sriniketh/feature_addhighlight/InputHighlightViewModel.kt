package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.CreateFileUseCase
import com.sriniketh.core_data.usecases.DeleteFileUseCase
import com.sriniketh.core_data.usecases.FormatCurrentDateTimeUseCase
import com.sriniketh.core_data.usecases.SaveHighlightUseCase
import com.sriniketh.core_models.book.Highlight
import com.sriniketh.core_platform.DateTimeSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class InputHighlightViewModel @Inject constructor(
    private val dateTimeSource: DateTimeSource,
    private val textAnalyzer: TextAnalyzer,
    private val saveHighlightUseCase: SaveHighlightUseCase,
    private val formatCurrentDateTimeUseCase: FormatCurrentDateTimeUseCase,
    private val createFileUseCase: CreateFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase
) : ViewModel() {

    private val imageUri: Uri by lazy { createFileUseCase() }

    private val _screenState: MutableStateFlow<InputHighlightScreenState> =
        MutableStateFlow(InputHighlightScreenState.CaptureImage(imageUri))
    internal val screenState: StateFlow<InputHighlightScreenState> = _screenState.asStateFlow()

    private val _editHighlightUiState: MutableStateFlow<EditAndSaveHighlightUiState> =
        MutableStateFlow(EditAndSaveHighlightUiState())
    internal val editHighlightUiState: StateFlow<EditAndSaveHighlightUiState> =
        _editHighlightUiState.asStateFlow()

    internal fun onImageCaptured() {
        _screenState.update { InputHighlightScreenState.CropImage(imageUri) }
    }

    internal fun onImageCropped() {
        _screenState.update { InputHighlightScreenState.EditAndSaveHighlight }
        processImageForHighlightsText(imageUri)
    }

    internal fun onHighlightTextUpdated(highlightText: String) {
        _editHighlightUiState.update { state ->
            state.copy(highlightText = highlightText)
        }
    }

    internal fun onHighlightSaved(bookId: String, highlightText: String) {
        viewModelScope.launch {
            _editHighlightUiState.update { state ->
                state.copy(isLoading = true)
            }
            val result = saveHighlightUseCase(
                highlight = Highlight(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    text = highlightText,
                    savedOnTimestamp = formatCurrentDateTimeUseCase(dateTimeSource.now())
                )
            )
            if (result.isSuccess) {
                _editHighlightUiState.update { state ->
                    state.copy(isLoading = false)
                }
                _screenState.update { InputHighlightScreenState.SaveHighlightSuccessful }
            } else if (result.isFailure) {
                _editHighlightUiState.update { state ->
                    state.copy(
                        isLoading = false,
                        snackBarText = R.string.save_highlight_error_message
                    )
                }
            }
        }
    }

    private fun processImageForHighlightsText(uri: Uri) {
        _editHighlightUiState.update { state ->
            state.copy(isLoading = true)
        }
        viewModelScope.launch {
            try {
                val visionText = textAnalyzer.analyzeImage(uri)
                val highlightText = visionText.textBlocks.joinToString("\n") { it.text }
                _editHighlightUiState.update { state ->
                    state.copy(isLoading = false, highlightText = highlightText)
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                _editHighlightUiState.update { state ->
                    state.copy(
                        isLoading = false,
                        snackBarText = R.string.image_processing_failure_error_message
                    )
                }
            } finally {
                deleteFileUseCase(uri)
            }
        }
    }

    override fun onCleared() {
        deleteFileUseCase(imageUri)
        super.onCleared()
    }
}

internal sealed interface InputHighlightScreenState {
    data class CaptureImage(val imageUri: Uri) : InputHighlightScreenState
    data class CropImage(val imageUri: Uri) : InputHighlightScreenState
    data object EditAndSaveHighlight : InputHighlightScreenState
    data object SaveHighlightSuccessful : InputHighlightScreenState
}

internal data class EditAndSaveHighlightUiState(
    val isLoading: Boolean = false,
    val highlightText: String = "",
    @StringRes val snackBarText: Int? = null
)
