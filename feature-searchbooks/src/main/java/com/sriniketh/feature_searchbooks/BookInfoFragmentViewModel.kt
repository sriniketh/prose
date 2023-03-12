package com.sriniketh.feature_searchbooks

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.FetchBookInfoUseCase
import com.sriniketh.core_data.usecases.AddBookToShelfUseCase
import com.sriniketh.core_models.book.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookInfoFragmentViewModel @Inject constructor(
    private val fetchBookInfoUseCase: FetchBookInfoUseCase,
    private val addBookToShelfUseCase: AddBookToShelfUseCase
) : ViewModel() {

    private var _uiState: MutableStateFlow<BookInfoUiState> =
        MutableStateFlow(BookInfoUiState.Initial)
    internal val uiState: StateFlow<BookInfoUiState> = _uiState

    fun getBookDetail(volumeId: String) {
        viewModelScope.launch {
            _uiState.emit(BookInfoUiState.Loading)
            val result = fetchBookInfoUseCase(volumeId)
            if (result.isSuccess) {
                _uiState.emit(
                    BookInfoUiState.BookInfoLoadSuccess(result.getOrThrow(), R.string.add_to_shelf_button_text) { book ->
                        addBookToShelf(book)
                    })
            } else if (result.isFailure) {
                _uiState.emit(BookInfoUiState.Failure(R.string.book_info_load_error_message))
            }
        }
    }

    private fun addBookToShelf(book: Book) {
        viewModelScope.launch {
            _uiState.emit(BookInfoUiState.Loading)
            val result = addBookToShelfUseCase(book)
            if (result.isSuccess) {
                _uiState.emit(BookInfoUiState.AddToBookshelfSuccess(R.string.added_to_shelf_button_text))
            } else if (result.isFailure) {
                _uiState.emit(BookInfoUiState.Failure(R.string.add_to_bookshelf_error_message))
            }
        }
    }
}

internal sealed interface BookInfoUiState {
    object Initial : BookInfoUiState
    object Loading : BookInfoUiState
    data class BookInfoLoadSuccess(
        val book: Book,
        @StringRes val addBookToShelfButtonText: Int,
        val addBookToShelf: (Book) -> Unit
    ) : BookInfoUiState

    data class AddToBookshelfSuccess(@StringRes val addBookToShelfButtonText: Int) : BookInfoUiState
    data class Failure(@StringRes val errorMessage: Int) : BookInfoUiState
}
