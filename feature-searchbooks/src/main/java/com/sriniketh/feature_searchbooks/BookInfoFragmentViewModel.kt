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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookInfoFragmentViewModel @Inject constructor(
    private val fetchBookInfoUseCase: FetchBookInfoUseCase,
    private val addBookToShelfUseCase: AddBookToShelfUseCase
) : ViewModel() {

    private var _uiState: MutableStateFlow<BookInfoUiState> =
        MutableStateFlow(BookInfoUiState())
    internal val uiState: StateFlow<BookInfoUiState> = _uiState

    fun getBookDetail(volumeId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true)
            }
            val result = fetchBookInfoUseCase(volumeId)
            if (result.isSuccess) {
                _uiState.update { state ->
                    val book = result.getOrThrow()
                    state.copy(
                        isLoading = false,
                        book = book,
                        canAddToShelf = true, //TODO: determine if book was already added
                        addBookToShelf = { addBookToShelf(book) }
                    )
                }
            } else if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        snackBarText = R.string.book_info_load_error_message
                    )
                }
            }
        }
    }

    private fun addBookToShelf(book: Book) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true)
            }
            val result = addBookToShelfUseCase(book)
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        snackBarText = R.string.add_to_bookshelf_success_message,
                        canAddToShelf = false
                    )
                }
            } else if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        snackBarText = R.string.add_to_bookshelf_error_message
                    )
                }
            }
        }
    }
}

data class BookInfoUiState(
    val isLoading: Boolean = false,
    @StringRes val snackBarText: Int? = null,
    val book: Book? = null,
    val canAddToShelf: Boolean = false,
    val addBookToShelf: () -> Unit = {}
)
