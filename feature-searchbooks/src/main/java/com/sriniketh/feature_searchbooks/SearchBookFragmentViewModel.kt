package com.sriniketh.feature_searchbooks

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.search.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchBookFragmentViewModel @Inject constructor(
    private val bookRepo: BooksRepository
) : ViewModel() {

    private val _searchUiState: MutableStateFlow<BookSearchUiState> =
        MutableStateFlow(BookSearchUiState.Initial)
    val searchUiState: StateFlow<BookSearchUiState> = _searchUiState

    fun searchForBook(query: String) {
        viewModelScope.launch {
            _searchUiState.emit(BookSearchUiState.Loading)
            bookRepo.searchBooks(query).collect { result ->
                if (result.isSuccess) {
                    _searchUiState.emit(BookSearchUiState.Success(result.getOrThrow().items))
                } else if (result.isFailure) {
                    _searchUiState.emit(BookSearchUiState.Failure(R.string.search_error_message))
                }
            }
        }
    }
}

sealed interface BookSearchUiState {
    object Initial : BookSearchUiState
    object Loading : BookSearchUiState
    data class Success(val books: List<Book>) : BookSearchUiState
    data class Failure(@StringRes val errorMessage: Int) : BookSearchUiState
}
