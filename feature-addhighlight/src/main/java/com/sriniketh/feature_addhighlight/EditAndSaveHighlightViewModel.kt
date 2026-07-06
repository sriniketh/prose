package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.DeleteFileUseCase
import com.sriniketh.core_data.usecases.FormatCurrentDateTimeUseCase
import com.sriniketh.core_data.usecases.LoadHighlightUseCase
import com.sriniketh.core_data.usecases.SaveHighlightUseCase
import com.sriniketh.core_models.book.Highlight
import com.sriniketh.core_platform.DateTimeSource
import com.sriniketh.core_platform.decodeUri
import com.sriniketh.core_platform.logTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val deleteFileUseCase: DeleteFileUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState: MutableStateFlow<EditAndSaveHighlightUiState> =
        MutableStateFlow(
            EditAndSaveHighlightUiState(
                highlightText = savedStateHandle.get<String>(DRAFT_HIGHLIGHT_TEXT_ARG).orEmpty()
            )
        )
    internal val uiState: StateFlow<EditAndSaveHighlightUiState> =
        _uiState.asStateFlow()

    private val _effects = Channel<EditAndSaveHighlightEffect>(Channel.BUFFERED)
    internal val effects: Flow<EditAndSaveHighlightEffect> = _effects.receiveAsFlow()

    private var savedOnTimestamp: String?
        get() = savedStateHandle.get<String>(SAVED_ON_TIMESTAMP_ARG)
        set(value) {
            savedStateHandle[SAVED_ON_TIMESTAMP_ARG] = value
        }
    private var hasStartedProcessingImage = false
    private var hasStartedLoadingHighlight = false

    init {
        val encodedUri = savedStateHandle.get<String>(URI_ARG)
        val highlightId = savedStateHandle.get<String>(HIGHLIGHT_ID_ARG)
        val hasDraft = savedStateHandle.get<String>(DRAFT_HIGHLIGHT_TEXT_ARG) != null

        if (hasDraft) {
            if (highlightId != null) {
                _uiState.update { state -> state.copy(screenTitle = R.string.edit_highlight_title_text) }
            }
            hasStartedProcessingImage = true
            hasStartedLoadingHighlight = true
        } else if (encodedUri != null) {
            processImageForHighlightText(encodedUri.decodeUri())
        } else if (highlightId != null) {
            loadHighlightText(highlightId)
        }
    }

    internal fun processImageForHighlightText(uri: Uri) {
        if (hasStartedProcessingImage) return
        hasStartedProcessingImage = true

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
                savedStateHandle[DRAFT_HIGHLIGHT_TEXT_ARG] = highlightText
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (_: Exception) {
                _uiState.update { state ->
                    state.copy(isLoading = false)
                }
                _effects.trySend(EditAndSaveHighlightEffect.ShowMessage(R.string.image_processing_failure_error_message))
            } finally {
                deleteFileUseCase(uri)
            }
        }
    }

    internal fun loadHighlightText(highlightId: String) {
        if (hasStartedLoadingHighlight) return
        hasStartedLoadingHighlight = true

        _uiState.update { state ->
            state.copy(isLoading = true, screenTitle = R.string.edit_highlight_title_text)
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
                savedStateHandle[DRAFT_HIGHLIGHT_TEXT_ARG] = highlight?.text.orEmpty()
                savedOnTimestamp = highlight?.savedOnTimestamp
            } else {
                _uiState.update { state ->
                    state.copy(isLoading = false)
                }
                _effects.trySend(EditAndSaveHighlightEffect.ShowMessage(R.string.image_processing_failure_error_message))
            }
        }
    }

    internal fun updateHighlightText(highlightText: String) {
        _uiState.update { state ->
            state.copy(highlightText = highlightText)
        }
        savedStateHandle[DRAFT_HIGHLIGHT_TEXT_ARG] = highlightText
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
                    state.copy(isLoading = false)
                }
                _effects.trySend(EditAndSaveHighlightEffect.HighlightSaved)
            } else if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(isLoading = false)
                }
                _effects.trySend(EditAndSaveHighlightEffect.ShowMessage(R.string.save_highlight_error_message))
            }
        }
    }

    private companion object {
        private const val URI_ARG = "uri"
        private const val HIGHLIGHT_ID_ARG = "highlightId"
        private const val DRAFT_HIGHLIGHT_TEXT_ARG = "draftHighlightText"
        private const val SAVED_ON_TIMESTAMP_ARG = "savedOnTimestamp"
    }
}

internal data class EditAndSaveHighlightUiState(
    val isLoading: Boolean = false,
    @StringRes val screenTitle: Int = R.string.save_highlight_title_text,
    val highlightText: String = ""
)

internal sealed interface EditAndSaveHighlightEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : EditAndSaveHighlightEffect
    data object HighlightSaved : EditAndSaveHighlightEffect
}
