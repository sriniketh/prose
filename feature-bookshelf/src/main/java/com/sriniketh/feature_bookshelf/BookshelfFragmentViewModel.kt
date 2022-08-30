package com.sriniketh.feature_bookshelf

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookshelfFragmentViewModel @Inject constructor(
    private val bookRepo: BooksRepository
) : ViewModel() {

    private val _bookshelfUIState: MutableStateFlow<BookshelfUIState> =
        MutableStateFlow(BookshelfUIState.Initial)
    internal val bookshelfUIState: StateFlow<BookshelfUIState> = _bookshelfUIState.asStateFlow()

    fun getSavedBooks() {
        viewModelScope.launch {
            _bookshelfUIState.emit(BookshelfUIState.Loading)
            bookRepo.getAllBooks().collect { result ->
                if (result.isSuccess) {
                    val books = result.getOrThrow()
                    if (books.isEmpty()) {
                        _bookshelfUIState.emit(BookshelfUIState.SuccessNoBooks)
                    } else {
                        _bookshelfUIState.emit(BookshelfUIState.Success(books.map { it.asBookshelfUIState() }))
                    }
                } else if (result.isFailure) {
                    _bookshelfUIState.emit(BookshelfUIState.Failure(R.string.getallbooks_error_message))
                }
            }
        }
    }

    private fun Book.asBookshelfUIState(): BookUIState = BookUIState(
        id = id,
        info = BookInfoUiState(
            title = info.title,
            authors = info.authors,
            thumbnailLink = info.thumbnailLink
        ),
        viewBook = {}
    )
}

internal sealed interface BookshelfUIState {
    object Initial : BookshelfUIState
    object Loading : BookshelfUIState
    data class Success(val bookUIStates: List<BookUIState>) : BookshelfUIState
    object SuccessNoBooks : BookshelfUIState
    data class Failure(@StringRes val errorMessage: Int) : BookshelfUIState
}

data class BookUIState(
    val id: String,
    val info: BookInfoUiState,
    var viewBook: (String) -> Unit
)

data class BookInfoUiState(
    val title: String,
    val authors: List<String>,
    val thumbnailLink: String?
)
