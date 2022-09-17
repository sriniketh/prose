package com.sriniketh.feature_searchbooks

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookInfoFragmentViewModel @Inject constructor(
    private val bookRepo: BooksRepository
) : ViewModel() {

    private var _uiState: MutableStateFlow<BookInfoUiState> =
        MutableStateFlow(BookInfoUiState.Initial)
    internal val uiState: StateFlow<BookInfoUiState> = _uiState

    fun getBookDetail(volumeId: String) {
        viewModelScope.launch {
            _uiState.emit(BookInfoUiState.Loading)
            bookRepo.getBook(volumeId).collect { result ->
                if (result.isSuccess) {
                    _uiState.emit(BookInfoUiState.BookInfoLoadSuccess(result.getOrThrow()) { book ->
                        addBookToShelf(book)
                    })
                } else if (result.isFailure) {
                    _uiState.emit(BookInfoUiState.Failure(R.string.book_info_load_error_message))
                }
            }
        }
    }

    private fun addBookToShelf(book: Book) {
        viewModelScope.launch {
            _uiState.emit(BookInfoUiState.Loading)
            bookRepo.insertBook(book).collect { result ->
                if (result.isSuccess) {
                    _uiState.emit(BookInfoUiState.AddToBookshelfSuccess)
                } else if (result.isFailure) {
                    _uiState.emit(BookInfoUiState.Failure(R.string.add_to_bookshelf_error_message))
                }
            }
        }
    }
}

internal sealed interface BookInfoUiState {
    object Initial : BookInfoUiState
    object Loading : BookInfoUiState
    data class BookInfoLoadSuccess(
        val book: Book,
        val addBookToShelf: (Book) -> Unit
    ) : BookInfoUiState

    object AddToBookshelfSuccess : BookInfoUiState
    data class Failure(@StringRes val errorMessage: Int) : BookInfoUiState
}
