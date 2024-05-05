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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewHighlightsFragmentViewModel @Inject constructor(
    private val getAllSavedHighlightsUseCase: GetAllSavedHighlightsUseCase,
    private val deleteHighlightUseCase: DeleteHighlightUseCase
) : ViewModel() {

    private val _highlightsUIStateFlow: MutableStateFlow<ViewHighlightsUIState> =
        MutableStateFlow(ViewHighlightsUIState())
    internal val highlightsUIStateFlow: StateFlow<ViewHighlightsUIState> =
        _highlightsUIStateFlow.asStateFlow()

    fun getHighlights(bookId: String) {
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
                            highlights = highlights.map { it.asHighlightUIState() }
                        )
                    }
                } else if (result.isFailure) {
                    _highlightsUIStateFlow.update { state ->
                        state.copy(
                            isLoading = false,
                            snackBarText = R.string.gethighlights_error_message
                        )
                    }
                }
            }
        }
    }

    private fun Highlight.asHighlightUIState(): HighlightUIState = HighlightUIState(
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
                        state.copy(
                            isLoading = false,
                            snackBarText = R.string.delete_error_message
                        )
                    }
                }
            }
        }
    )
}

internal data class ViewHighlightsUIState(
    val isLoading: Boolean = false,
    val highlights: List<HighlightUIState> = emptyList(),
    @StringRes val snackBarText: Int? = null
)

data class HighlightUIState(
    val text: String,
    val savedOn: String,
    val onDelete: () -> Unit
)
