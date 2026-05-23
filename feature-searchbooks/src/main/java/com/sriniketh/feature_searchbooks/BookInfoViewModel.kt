package com.sriniketh.feature_searchbooks

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.FetchBookInfoUseCase
import com.sriniketh.core_data.usecases.AddBookToShelfUseCase
import com.sriniketh.core_data.usecases.IsBookInDbUseCase
import com.sriniketh.core_models.book.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookInfoViewModel @Inject constructor(
    private val fetchBookInfoUseCase: FetchBookInfoUseCase,
    private val addBookToShelfUseCase: AddBookToShelfUseCase,
    private val isBookInDbUseCase: IsBookInDbUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<BookInfoUiState> =
        MutableStateFlow(BookInfoUiState())
    internal val uiState: StateFlow<BookInfoUiState> = _uiState.asStateFlow()

    private val _effects = Channel<BookInfoEffect>(Channel.BUFFERED)
    internal val effects: Flow<BookInfoEffect> = _effects.receiveAsFlow()

    fun getBookDetail(volumeId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true)
            }
            val result = fetchBookInfoUseCase(volumeId)
            if (result.isSuccess) {
                val book = result.getOrThrow()
                val isInDb = isBookInDbUseCase(book)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        book = book,
                        canAddToShelf = !isInDb,
                        addBookToShelf = { addBookToShelf(book) }
                    )
                }
            } else if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(isLoading = false)
                }
                _effects.trySend(BookInfoEffect.ShowMessage(R.string.book_info_load_error_message))
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
                        canAddToShelf = false
                    )
                }
                _effects.trySend(BookInfoEffect.NavigateToBookshelf)
            } else if (result.isFailure) {
                _uiState.update { state ->
                    state.copy(isLoading = false)
                }
                _effects.trySend(BookInfoEffect.ShowMessage(R.string.add_to_bookshelf_error_message))
            }
        }
    }
}

data class BookInfoUiState(
    val isLoading: Boolean = false,
    val book: Book? = null,
    val canAddToShelf: Boolean = false,
    val addBookToShelf: () -> Unit = {}
)

internal sealed interface BookInfoEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : BookInfoEffect
    data object NavigateToBookshelf : BookInfoEffect
}
