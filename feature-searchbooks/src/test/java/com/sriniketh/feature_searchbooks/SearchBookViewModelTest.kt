package com.sriniketh.feature_searchbooks

import app.cash.turbine.test
import com.sriniketh.core_data.usecases.SearchForBookUseCase
import com.sriniketh.feature_searchbooks.fakes.FakeBooksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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

        viewModel.searchUiState.test {
            awaitItem()

            viewModel.searchForBook("test query")

            skipItems(1)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(R.string.search_error_message, errorState.snackBarText)
        }
    }

    @Test
    fun `when reset search is called then books are cleared from state`() = runTest {
        viewModel.searchUiState.test {
            awaitItem()

            viewModel.searchForBook("test query")
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
            assertEquals(null, initialState.snackBarText)
        }
    }
}
