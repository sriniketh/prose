package com.sriniketh.feature_viewhighlights

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.DeleteHighlightUseCase
import com.sriniketh.core_data.usecases.GetAllSavedHighlightsUseCase
import com.sriniketh.core_models.book.Highlight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewHighlightsFragmentViewModel @Inject constructor(
    private val getAllSavedHighlightsUseCase: GetAllSavedHighlightsUseCase,
    private val deleteHighlightUseCase: DeleteHighlightUseCase
) : ViewModel() {

    private val _highlightsUIStateFlow: MutableStateFlow<ViewHighlightsUIState> =
        MutableStateFlow(ViewHighlightsUIState.Initial)
    internal val highlightsUIStateFlow: StateFlow<ViewHighlightsUIState> =
        _highlightsUIStateFlow.asStateFlow()

    fun getHighlights(bookId: String) {
        viewModelScope.launch {
            _highlightsUIStateFlow.emit(ViewHighlightsUIState.Loading)
            getAllSavedHighlightsUseCase(bookId).collect { result ->
                if (result.isSuccess) {
                    val highlights = result.getOrThrow()
                    if (highlights.isEmpty()) {
                        _highlightsUIStateFlow.emit(ViewHighlightsUIState.SuccessNoHighlights)
                    } else {
                        _highlightsUIStateFlow.emit(ViewHighlightsUIState.Success(highlightsUIState = highlights.map { it.asHighlightUIState() }))
                    }
                } else if (result.isFailure) {
                    _highlightsUIStateFlow.emit(ViewHighlightsUIState.Failure(R.string.gethighlights_error_message))
                }
            }
        }
    }

    private fun Highlight.asHighlightUIState(): HighlightUIState = HighlightUIState(
        text = text,
        savedOn = savedOnTimestamp,
        onDelete = {
            viewModelScope.launch {
                val result = deleteHighlightUseCase.invoke(this@asHighlightUIState)
                if (result.isFailure) {
                    _highlightsUIStateFlow.emit(ViewHighlightsUIState.Failure(R.string.delete_error_message))
                }
            }
        }
    )
}

internal sealed interface ViewHighlightsUIState {
    object Initial : ViewHighlightsUIState
    object Loading : ViewHighlightsUIState
    data class Success(val highlightsUIState: List<HighlightUIState>) : ViewHighlightsUIState
    object SuccessNoHighlights : ViewHighlightsUIState
    data class Failure(@StringRes val errorMessage: Int) : ViewHighlightsUIState
}

data class HighlightUIState(
    val text: String,
    val savedOn: String,
    val onDelete: () -> Unit
)
