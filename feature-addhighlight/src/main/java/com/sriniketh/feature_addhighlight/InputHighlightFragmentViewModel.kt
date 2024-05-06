package com.sriniketh.feature_addhighlight

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class InputHighlightFragmentViewModel @Inject constructor(
    private val dateTimeSource: DateTimeSource,
    private val saveHighlightUseCase: SaveHighlightUseCase,
    private val formatCurrentDateTimeUseCase: FormatCurrentDateTimeUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<InputHighlightUiState> =
        MutableStateFlow(InputHighlightUiState())
    internal val uiState: StateFlow<InputHighlightUiState> = _uiState.asStateFlow()

    fun setHighlightText(highlightText: String) {
        _uiState.update { state ->
            state.copy(highlightText = highlightText)
        }
    }

    fun saveHighlight(bookId: String, highlightText: String) {
        viewModelScope.launch {
            _uiState.update { state ->
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
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        wasHighlightSaved = true
                    )
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

internal data class InputHighlightUiState(
    val isLoading: Boolean = false,
    val highlightText: String = "",
    val wasHighlightSaved: Boolean = false,
    @StringRes val snackBarText: Int? = null
)
