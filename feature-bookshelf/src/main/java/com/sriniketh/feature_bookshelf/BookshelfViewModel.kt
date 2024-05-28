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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookshelfViewModel @Inject constructor(
    private val getAllSavedBooksUseCase: GetAllSavedBooksUseCase
) : ViewModel() {

    private val _bookshelfUIState: MutableStateFlow<BookshelfUIState> =
        MutableStateFlow(BookshelfUIState())
    internal val bookshelfUIState: StateFlow<BookshelfUIState> = _bookshelfUIState.asStateFlow()

    var viewHighlightsForBook: (String) -> Unit = {}

    init {
        viewModelScope.launch {
            _bookshelfUIState.update { state ->
                state.copy(isLoading = true)
            }
            getAllSavedBooksUseCase().collect { result ->
                if (result.isSuccess) {
                    val books = result.getOrThrow()
                    _bookshelfUIState.update { state ->
                        state.copy(
                            isLoading = false,
                            books = books.map { it.asBookshelfUIState() }
                        )
                    }
                } else if (result.isFailure) {
                    _bookshelfUIState.update { state ->
                        state.copy(
                            isLoading = false,
                            snackBarText = R.string.getallbooks_error_message
                        )
                    }
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

internal data class BookshelfUIState(
    val isLoading: Boolean = false,
    val books: List<BookUIState> = emptyList(),
    @StringRes val snackBarText: Int? = null
)

internal data class BookUIState(
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnailLink: String?,
    var viewBook: (String) -> Unit
)
