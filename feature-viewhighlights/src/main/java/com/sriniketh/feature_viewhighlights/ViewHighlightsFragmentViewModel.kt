package com.sriniketh.feature_viewhighlights

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ViewHighlightsFragmentViewModel @Inject constructor() : ViewModel() {

    private val _highlightsUIStateFlow: MutableStateFlow<ViewHighlightsUIState> =
        MutableStateFlow(ViewHighlightsUIState.Initial)
    internal val highlightsUIStateFlow: StateFlow<ViewHighlightsUIState> =
        _highlightsUIStateFlow.asStateFlow()

    fun getHighlights(bookId: String) {}
}

internal sealed interface ViewHighlightsUIState {
    object Initial : ViewHighlightsUIState
    object Loading : ViewHighlightsUIState
    data class Success(val highlightsUIState: List<HighlightUIState>) : ViewHighlightsUIState
    object SuccessNoHighlights : ViewHighlightsUIState
    data class Failure(@StringRes val errorMessage: Int) : ViewHighlightsUIState
}

class HighlightUIState
