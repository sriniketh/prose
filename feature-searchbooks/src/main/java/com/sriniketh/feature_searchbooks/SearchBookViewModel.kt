package com.sriniketh.feature_searchbooks

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sriniketh.core_data.usecases.SearchForBookUseCase
import com.sriniketh.core_models.book.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchBookViewModel @Inject constructor(
    private val searchForBookUseCase: SearchForBookUseCase
) : ViewModel() {

    private val _searchUiState: MutableStateFlow<BookSearchUiState> =
        MutableStateFlow(BookSearchUiState())
    internal val searchUiState: StateFlow<BookSearchUiState> = _searchUiState.asStateFlow()

    private val _effects = Channel<SearchBookEffect>(Channel.BUFFERED)
    internal val effects: Flow<SearchBookEffect> = _effects.receiveAsFlow()

    private val queryFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val resetChannel: Channel<Unit> = Channel(Channel.CONFLATED)

    init {
        val searches: Flow<SearchAction> = queryFlow
            .filter { query -> query.length > MIN_QUERY_LENGTH }
            .debounce(DEBOUNCE_MILLIS)
            .distinctUntilChanged()
            .map { query -> SearchAction.Search(query) }
        val resets: Flow<SearchAction> = resetChannel.receiveAsFlow().map { SearchAction.Clear }
        merge(searches, resets)
            .flatMapLatest { action ->
                when (action) {
                    is SearchAction.Search -> searchResultsFlow(action.query)
                    SearchAction.Clear -> flowOf(BookSearchUiState())
                }
            }
            .onEach { state -> _searchUiState.value = state }
            .launchIn(viewModelScope)
    }

    fun searchForBook(query: String) {
        queryFlow.value = query
    }

    fun resetSearch() {
        resetChannel.trySend(Unit)
    }

    private fun searchResultsFlow(query: String): Flow<BookSearchUiState> = flow {
        emit(_searchUiState.value.copy(isLoading = true))
        val result = searchForBookUseCase(query)
        if (result.isSuccess) {
            emit(
                BookSearchUiState(
                    isLoading = false,
                    bookUiStates = result.getOrThrow().items.map { it.asBookUiState() }
                )
            )
        } else {
            emit(_searchUiState.value.copy(isLoading = false))
            _effects.trySend(SearchBookEffect.ShowMessage(R.string.search_error_message))
        }
    }

    private fun Book.asBookUiState(): BookUiState = BookUiState(
        id = id,
        title = info.title,
        subtitle = info.subtitle,
        authors = info.authors,
        thumbnailLink = info.thumbnailLink
    )

    private sealed interface SearchAction {
        data class Search(val query: String) : SearchAction
        data object Clear : SearchAction
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 3
        const val DEBOUNCE_MILLIS = 300L
    }
}

internal data class BookSearchUiState(
    val isLoading: Boolean = false,
    val bookUiStates: List<BookUiState> = emptyList()
)

internal data class BookUiState(
    val id: String,
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val thumbnailLink: String?
)

internal sealed interface SearchBookEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : SearchBookEffect
}
