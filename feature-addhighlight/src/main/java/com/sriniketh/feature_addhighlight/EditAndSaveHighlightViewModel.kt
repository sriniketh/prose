package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.DeleteFileUseCase
import com.sriniketh.core_data.usecases.FormatCurrentDateTimeUseCase
import com.sriniketh.core_data.usecases.LoadHighlightUseCase
import com.sriniketh.core_data.usecases.SaveHighlightUseCase
import com.sriniketh.core_models.book.Highlight
import com.sriniketh.core_platform.DateTimeSource
import com.sriniketh.core_platform.logTag
import dagger.hilt.android.lifecycle.HiltViewModel
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
class EditAndSaveHighlightViewModel @Inject constructor(
    private val dateTimeSource: DateTimeSource,
    private val textAnalyzer: TextAnalyzer,
    private val saveHighlightUseCase: SaveHighlightUseCase,
    private val loadHighlightUseCase: LoadHighlightUseCase,
    private val formatCurrentDateTimeUseCase: FormatCurrentDateTimeUseCase,
    private val deleteFileUseCase: DeleteFileUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<EditAndSaveHighlightUiState> =
        MutableStateFlow(EditAndSaveHighlightUiState())
    internal val uiState: StateFlow<EditAndSaveHighlightUiState> =
        _uiState.asStateFlow()

    private var savedOnTimestamp: String? = null

    internal fun processImageForHighlightText(uri: Uri) {
        _uiState.update { state ->
            state.copy(isLoading = true)
        }
        viewModelScope.launch {
            try {
                val visionText = textAnalyzer.analyzeImage(uri)
                val highlightText = visionText.text.replace("\n", " ")
                Timber.d("${this.logTag()}: Transformed text: $highlightText")
                _uiState.update { state ->
                    state.copy(isLoading = false, highlightText = highlightText)
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                _uiState.update { state ->
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

    internal fun loadHighlightText(highlightId: String) {
        _uiState.update { state ->
            state.copy(isLoading = true)
        }
        viewModelScope.launch {
            val result = loadHighlightUseCase(highlightId)
            if (result.isSuccess) {
                val highlight = result.getOrNull()
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        highlightText = highlight?.text.orEmpty()
                    )
                }
                savedOnTimestamp = highlight?.savedOnTimestamp
            } else {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        snackBarText = R.string.image_processing_failure_error_message
                    )
                }
            }
        }
    }

    internal fun updateHighlightText(highlightText: String) {
        _uiState.update { state ->
            state.copy(highlightText = highlightText)
        }
    }

    internal fun saveHighlight(bookId: String, highlightText: String) {
        saveHighlightToPersistence(bookId, highlightText)
    }

    internal fun updateHighlight(bookId: String, highlightText: String, highlightId: String) {
        saveHighlightToPersistence(
            bookId = bookId,
            highlightText = highlightText,
            highlightId = highlightId
        )
    }

    private fun saveHighlightToPersistence(
        bookId: String,
        highlightText: String,
        highlightId: String = UUID.randomUUID().toString()
    ) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true)
            }
            val result = saveHighlightUseCase(
                highlight = Highlight(
                    id = highlightId,
                    bookId = bookId,
                    text = highlightText,
                    savedOnTimestamp = savedOnTimestamp
                        ?: formatCurrentDateTimeUseCase(dateTimeSource.now())
                )
            )
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(isLoading = false, highlightSaved = true)
                }
            } else if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        snackBarText = R.string.save_highlight_error_message
                    )
                }
            }
        }
    }
}

internal data class EditAndSaveHighlightUiState(
    val isLoading: Boolean = false,
    val highlightText: String = "",
    @StringRes val snackBarText: Int? = null,
    val highlightSaved: Boolean = false
)
