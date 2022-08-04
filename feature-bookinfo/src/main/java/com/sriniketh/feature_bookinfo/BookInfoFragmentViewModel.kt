package com.sriniketh.feature_bookinfo

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
                    _uiState.emit(BookInfoUiState.Success(result.getOrThrow()) { book ->
                        addBookToShelf(book)
                    })
                } else if (result.isFailure) {
                    _uiState.emit(BookInfoUiState.Failure(R.string.book_info_load_error_message))
                }
            }
        }
    }

    private fun addBookToShelf(book: Book) {
        println("ADDING BOOK TO SHELF ${book.id}")
    }
}

internal sealed interface BookInfoUiState {
    object Initial : BookInfoUiState
    object Loading : BookInfoUiState
    data class Success(
        val book: Book,
        val addBookToShelf: (Book) -> Unit
    ) : BookInfoUiState

    data class Failure(@StringRes val errorMessage: Int) : BookInfoUiState
}
