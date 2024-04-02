package com.sriniketh.feature_bookshelf

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.GetAllSavedBooksUseCase
import com.sriniketh.core_models.book.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookshelfFragmentViewModel @Inject constructor(
    private val getAllSavedBooksUseCase: GetAllSavedBooksUseCase
) : ViewModel() {

    private val _bookshelfUIState: MutableStateFlow<BookshelfUIState> =
        MutableStateFlow(BookshelfUIState.Loading)
    internal val bookshelfUIState: StateFlow<BookshelfUIState> = _bookshelfUIState.asStateFlow()

    var viewHighlightsForBook: (String) -> Unit = {}

    fun getSavedBooks() {
        viewModelScope.launch {
            _bookshelfUIState.emit(BookshelfUIState.Loading)
            getAllSavedBooksUseCase().collect { result ->
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
        title = info.title,
        authors = info.authors,
        thumbnailLink = info.thumbnailLink,
        viewBook = {
            viewHighlightsForBook(id)
        }
    )
}

internal sealed interface BookshelfUIState {
    data object Loading : BookshelfUIState
    data class Success(val bookUIStates: List<BookUIState>) : BookshelfUIState
    data object SuccessNoBooks : BookshelfUIState
    data class Failure(@StringRes val errorMessage: Int) : BookshelfUIState
}

data class BookUIState(
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnailLink: String?,
    var viewBook: (String) -> Unit
)
