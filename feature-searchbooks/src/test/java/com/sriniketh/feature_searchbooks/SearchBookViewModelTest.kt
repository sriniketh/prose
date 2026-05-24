package com.sriniketh.feature_searchbooks

import app.cash.turbine.test
import com.sriniketh.core_data.usecases.SearchForBookUseCase
import com.sriniketh.core_models.search.BookSearch
import com.sriniketh.feature_searchbooks.fakes.FakeBooksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val DEBOUNCE_MILLIS = 300L

@OptIn(ExperimentalCoroutinesApi::class)
class SearchBookViewModelTest {

    private lateinit var fakeBooksRepository: FakeBooksRepository
    private lateinit var searchForBookUseCase: SearchForBookUseCase
    private lateinit var viewModel: SearchBookViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeBooksRepository = FakeBooksRepository()
        searchForBookUseCase = SearchForBookUseCase(fakeBooksRepository)
        viewModel = SearchBookViewModel(searchForBookUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when search for book is called then loading state is set to true`() = runTest {
        viewModel.searchUiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.searchForBook("test query")
            advanceTimeBy(DEBOUNCE_MILLIS + 1)

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            skipItems(1)
        }
    }

    @Test
    fun `when search for book succeeds then loading is set to false and books are updated`() = runTest {
        viewModel.searchUiState.test {
            awaitItem()

            viewModel.searchForBook("test query")
            advanceTimeBy(DEBOUNCE_MILLIS + 1)

            skipItems(1)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals(1, successState.bookUiStates.size)
            assertEquals("test-id", successState.bookUiStates[0].id)
            assertEquals("Test Title", successState.bookUiStates[0].title)
        }
    }

    @Test
    fun `when search for book fails then loading is set to false and error is shown`() = runTest {
        fakeBooksRepository.shouldSearchForBooksThrowException = true

        viewModel.effects.test {
            viewModel.searchForBook("test query")
            advanceTimeBy(DEBOUNCE_MILLIS + 1)

            assertEquals(
                SearchBookEffect.ShowMessage(R.string.search_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.searchUiState.value.isLoading)
    }

    @Test
    fun `when reset search is called then books are cleared from state`() = runTest {
        viewModel.searchUiState.test {
            awaitItem()

            viewModel.searchForBook("test query")
            advanceTimeBy(DEBOUNCE_MILLIS + 1)
            skipItems(2)

            viewModel.resetSearch()

            val resetState = awaitItem()
            assertTrue(resetState.bookUiStates.isEmpty())
        }
    }

    @Test
    fun `when book maps to ui state then all fields are mapped correctly`() = runTest {
        viewModel.searchUiState.test {
            awaitItem()

            viewModel.searchForBook("test query")
            advanceTimeBy(DEBOUNCE_MILLIS + 1)
            skipItems(1)

            val state = awaitItem()
            val bookUiState = state.bookUiStates[0]
            assertEquals("test-id", bookUiState.id)
            assertEquals("Test Title", bookUiState.title)
            assertEquals("Test Subtitle", bookUiState.subtitle)
            assertEquals(listOf("Test Author"), bookUiState.authors)
            assertEquals("test-thumbnail", bookUiState.thumbnailLink)
        }
    }

    @Test
    fun `when initialized then state has correct defaults`() = runTest {
        viewModel.searchUiState.test {
            val initialState = awaitItem()

            assertFalse(initialState.isLoading)
            assertTrue(initialState.bookUiStates.isEmpty())
        }
    }

    @Test
    fun `when queries arrive faster than the debounce window then only the last triggers a search`() = runTest {
        viewModel.searchForBook("the h")
        advanceTimeBy(DEBOUNCE_MILLIS / 2)
        viewModel.searchForBook("the ho")
        advanceTimeBy(DEBOUNCE_MILLIS / 2)
        viewModel.searchForBook("the hobbit")
        advanceUntilIdle()

        assertEquals(listOf("the hobbit"), fakeBooksRepository.queriesSearched)
    }

    @Test
    fun `when a slow earlier query resolves after a newer one then it does not overwrite results`() = runTest {
        fakeBooksRepository.searchResultBuilder = { query ->
            BookSearch(items = listOf(fakeBooksRepository.fakeBook.copy(id = query)))
        }
        fakeBooksRepository.searchDelayMillis = 1_000L

        viewModel.searchForBook("the ho")
        advanceTimeBy(DEBOUNCE_MILLIS + 1)

        fakeBooksRepository.searchDelayMillis = 0L
        viewModel.searchForBook("the hobbit")
        advanceUntilIdle()

        val finalState = viewModel.searchUiState.value
        assertEquals(1, finalState.bookUiStates.size)
        assertEquals("the hobbit", finalState.bookUiStates[0].id)
    }

    @Test
    fun `when the identical query is re-submitted with no change then only one search runs`() = runTest {
        viewModel.searchForBook("the hobbit")
        advanceTimeBy(DEBOUNCE_MILLIS + 1)
        viewModel.searchForBook("the hobbit")
        advanceUntilIdle()

        assertEquals(listOf("the hobbit"), fakeBooksRepository.queriesSearched)
    }

    @Test
    fun `when a settled query is re-entered after a brief change within the window then it does not search again`() = runTest {
        viewModel.searchForBook("the hobbit")
        advanceTimeBy(DEBOUNCE_MILLIS + 1)

        viewModel.searchForBook("the hobbi")
        advanceTimeBy(DEBOUNCE_MILLIS / 2)
        viewModel.searchForBook("the hobbit")
        advanceTimeBy(DEBOUNCE_MILLIS + 1)
        advanceUntilIdle()

        assertEquals(listOf("the hobbit"), fakeBooksRepository.queriesSearched)
    }

    @Test
    fun `when query is too short then no search runs`() = runTest {
        viewModel.searchForBook("the")
        advanceUntilIdle()

        assertTrue(fakeBooksRepository.queriesSearched.isEmpty())
    }

    @Test
    fun `when reset is called during an in-flight search then results are not repopulated`() = runTest {
        fakeBooksRepository.searchResultBuilder = { query ->
            BookSearch(items = listOf(fakeBooksRepository.fakeBook.copy(id = query)))
        }
        fakeBooksRepository.searchDelayMillis = 1_000L

        viewModel.searchForBook("the hobbit")
        advanceTimeBy(DEBOUNCE_MILLIS + 1)

        viewModel.resetSearch()
        advanceUntilIdle()

        val finalState = viewModel.searchUiState.value
        assertTrue(finalState.bookUiStates.isEmpty())
        assertFalse(finalState.isLoading)
    }
}
