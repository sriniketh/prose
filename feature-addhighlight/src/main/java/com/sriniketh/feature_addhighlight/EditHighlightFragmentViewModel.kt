package com.sriniketh.feature_addhighlight

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Highlight
import com.sriniketh.core_platform.permissions.DateTimeSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditHighlightFragmentViewModel @Inject constructor(
    private val repo: BooksRepository,
    private val dateTimeSource: DateTimeSource
) : ViewModel() {

    private val _uiState: MutableStateFlow<EditHighlightUIState> = MutableStateFlow(EditHighlightUIState.Initial)
    internal val uiState: StateFlow<EditHighlightUIState> = _uiState.asStateFlow()

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a")

    fun saveHighlight(bookId: String, highlightText: String) {
        viewModelScope.launch {
            _uiState.emit(EditHighlightUIState.Loading)
            repo.insertHighlight(
                highlight = Highlight(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    text = highlightText,
                    savedOnTimestamp = dateTimeSource.now().format(dateTimeFormatter)
                )
            ).collect { result ->
                if (result.isSuccess) {
                    _uiState.emit(EditHighlightUIState.AddHighlightSuccess)
                } else if (result.isFailure) {
                    _uiState.emit(EditHighlightUIState.Failure(R.string.save_highlight_error_message))
                }
            }
        }
    }
}

internal sealed interface EditHighlightUIState {
    object Initial : EditHighlightUIState
    object Loading : EditHighlightUIState
    object AddHighlightSuccess : EditHighlightUIState
    data class Failure(@StringRes val errorMessage: Int) : EditHighlightUIState
}
