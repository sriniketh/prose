package com.sriniketh.feature_searchbooks

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.SearchForBookUseCase
import com.sriniketh.core_models.book.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchBookViewModel @Inject constructor(
    private val searchForBookUseCase: SearchForBookUseCase
) : ViewModel() {

    private val _searchUiState: MutableStateFlow<BookSearchUiState> =
        MutableStateFlow(BookSearchUiState())
    internal val searchUiState: StateFlow<BookSearchUiState> = _searchUiState.asStateFlow()

    fun searchForBook(query: String) {
        viewModelScope.launch {
            _searchUiState.update { state ->
                state.copy(isLoading = true)
            }
            val result = searchForBookUseCase(query)
            if (result.isSuccess) {
                _searchUiState.update { state ->
                    state.copy(
                        isLoading = false,
                        bookUiStates = result.getOrThrow().items.map { it.asBookUiState() }
                    )
                }
            } else if (result.isFailure) {
                _searchUiState.update { state ->
                    state.copy(isLoading = false, snackBarText = R.string.search_error_message)
                }
            }
        }
    }

    fun resetSearch() {
        _searchUiState.update { state ->
            state.copy(bookUiStates = emptyList())
        }
    }

    private fun Book.asBookUiState(): BookUiState = BookUiState(
        id = id,
        title = info.title,
        subtitle = info.subtitle,
        authors = info.authors,
        thumbnailLink = info.thumbnailLink
    )
}

internal data class BookSearchUiState(
    val isLoading: Boolean = false,
    val bookUiStates: List<BookUiState> = emptyList(),
    @StringRes val snackBarText: Int? = null,
)

internal data class BookUiState(
    val id: String,
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val thumbnailLink: String?
)
