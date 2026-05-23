package com.sriniketh.feature_viewhighlights

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.DeleteHighlightUseCase
import com.sriniketh.core_data.usecases.ExportHighlightsUseCase
import com.sriniketh.core_data.usecases.GetAllSavedHighlightsUseCase
import com.sriniketh.core_models.book.Highlight
import dagger.hilt.android.lifecycle.HiltViewModel
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
class ViewHighlightsViewModel @Inject constructor(
    private val getAllSavedHighlightsUseCase: GetAllSavedHighlightsUseCase,
    private val deleteHighlightUseCase: DeleteHighlightUseCase,
    private val exportHighlightsUseCase: ExportHighlightsUseCase
) : ViewModel() {

    private val _highlightsUIStateFlow: MutableStateFlow<ViewHighlightsUIState> =
        MutableStateFlow(ViewHighlightsUIState())
    internal val highlightsUIStateFlow: StateFlow<ViewHighlightsUIState> =
        _highlightsUIStateFlow.asStateFlow()

    private val _effects = Channel<ViewHighlightsEffect>(Channel.BUFFERED)
    internal val effects: Flow<ViewHighlightsEffect> = _effects.receiveAsFlow()

    internal fun getHighlights(bookId: String) {
        viewModelScope.launch {
            _highlightsUIStateFlow.update { state ->
                state.copy(isLoading = true)
            }
            getAllSavedHighlightsUseCase(bookId).collect { result ->
                if (result.isSuccess) {
                    val highlights = result.getOrThrow()
                    _highlightsUIStateFlow.update { state ->
                        state.copy(
                            isLoading = false,
                            highlights = highlights.sortedBy { it.savedOnTimestamp }
                                .map { it.asHighlightUIState() }
                        )
                    }
                } else if (result.isFailure) {
                    _highlightsUIStateFlow.update { state ->
                        state.copy(isLoading = false)
                    }
                    _effects.trySend(ViewHighlightsEffect.ShowMessage(R.string.gethighlights_error_message))
                }
            }
        }
    }

    internal fun processAction(action: ViewHighlightsAction) {
        when (action) {
            ViewHighlightsAction.OnCameraPermissionDenied -> {
                _highlightsUIStateFlow.update { state ->
                    state.copy(isLoading = false)
                }
                _effects.trySend(ViewHighlightsEffect.ShowMessage(R.string.permission_denied_error_message))
            }

            is ViewHighlightsAction.OnExportHighlights -> {
                exportHighlights(action.bookId)
            }

            else -> Unit
        }
    }

    private fun exportHighlights(bookId: String) {
        viewModelScope.launch {
            _highlightsUIStateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val result = exportHighlightsUseCase(bookId)
            if (result.isSuccess) {
                _highlightsUIStateFlow.update { state ->
                    state.copy(isLoading = false)
                }
                _effects.trySend(ViewHighlightsEffect.ShareHighlights(result.getOrThrow()))
            } else {
                _highlightsUIStateFlow.update { state ->
                    state.copy(isLoading = false)
                }
                _effects.trySend(ViewHighlightsEffect.ShowMessage(R.string.export_error_message))
            }
        }
    }

    private fun Highlight.asHighlightUIState(): HighlightUIState = HighlightUIState(
        id = id,
        text = text,
        savedOn = savedOnTimestamp,
        onDelete = {
            viewModelScope.launch {
                _highlightsUIStateFlow.update { state ->
                    state.copy(isLoading = true)
                }
                val result = deleteHighlightUseCase.invoke(this@asHighlightUIState)
                if (result.isFailure) {
                    _highlightsUIStateFlow.update { state ->
                        state.copy(isLoading = false)
                    }
                    _effects.trySend(ViewHighlightsEffect.ShowMessage(R.string.delete_error_message))
                }
            }
        }
    )
}

internal data class ViewHighlightsUIState(
    val isLoading: Boolean = false,
    val highlights: List<HighlightUIState> = emptyList()
)

internal data class HighlightUIState(
    val id: String,
    val text: String,
    val savedOn: String,
    val onDelete: () -> Unit
)

internal sealed interface ViewHighlightsAction {
    data object OnBackPressed : ViewHighlightsAction
    data object OnCameraPermissionDenied : ViewHighlightsAction
    data object OnCameraPermissionGranted : ViewHighlightsAction
    data class OnEditHighlight(val highlightId: String) : ViewHighlightsAction
    data class OnExportHighlights(val bookId: String) : ViewHighlightsAction
}

internal sealed interface ViewHighlightsEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : ViewHighlightsEffect
    data class ShareHighlights(val uri: Uri) : ViewHighlightsEffect
}
