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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchBookFragmentViewModel @Inject constructor(
    private val searchForBookUseCase: SearchForBookUseCase
) : ViewModel() {

    private val _searchUiState: MutableStateFlow<BookSearchUiState> =
        MutableStateFlow(BookSearchUiState.Initial)
    internal val searchUiState: StateFlow<BookSearchUiState> = _searchUiState.asStateFlow()

    var goToBookInfo: (String) -> Unit = {}

    fun searchForBook(query: String) {
        viewModelScope.launch {
            _searchUiState.emit(BookSearchUiState.Loading)
            searchForBookUseCase(query).collect { result ->
                if (result.isSuccess) {
                    _searchUiState.emit(BookSearchUiState.Success(result.getOrThrow().items.map { it.asBookUiState() }))
                } else if (result.isFailure) {
                    _searchUiState.emit(BookSearchUiState.Failure(R.string.search_error_message))
                }
            }
        }
    }

    private fun Book.asBookUiState(): BookUiState = BookUiState(
        id = id,
        title = info.title,
        subtitle = info.subtitle,
        authors = info.authors,
        thumbnailLink = info.thumbnailLink,
        viewDetail = { goToBookInfo(it) }
    )
}

internal sealed interface BookSearchUiState {
    object Initial : BookSearchUiState
    object Loading : BookSearchUiState
    data class Success(val bookUiStates: List<BookUiState>) : BookSearchUiState
    data class Failure(@StringRes val errorMessage: Int) : BookSearchUiState
}

data class BookUiState(
    val id: String,
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val thumbnailLink: String?,
    var viewDetail: (String) -> Unit
)
