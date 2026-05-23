package com.sriniketh.feature_bookshelf

import app.cash.turbine.test
import com.sriniketh.core_data.usecases.GetAllSavedBooksUseCase
import com.sriniketh.feature_bookshelf.fakes.FakeBooksRepository
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
class BookshelfViewModelTest {

    private lateinit var fakeBooksRepository: FakeBooksRepository
    private lateinit var getAllSavedBooksUseCase: GetAllSavedBooksUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeBooksRepository = FakeBooksRepository()
        getAllSavedBooksUseCase = GetAllSavedBooksUseCase(fakeBooksRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initialized then state has correct defaults`() = runTest {
        val viewModel = BookshelfViewModel(getAllSavedBooksUseCase)

        viewModel.bookshelfUIState.test {
            val initialState = awaitItem()

            assertFalse(initialState.isLoading)
            assertTrue(initialState.books.isEmpty())
        }
    }

    @Test
    fun `when initialized books are loaded and ui state transitions from loading to loaded`() =
        runTest {
            val viewModel = BookshelfViewModel(getAllSavedBooksUseCase)

            viewModel.bookshelfUIState.test {
                val initialState = awaitItem()
                assertFalse(initialState.isLoading)
                assertTrue(initialState.books.isEmpty())

                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)

                val loadedState = awaitItem()
                assertFalse(loadedState.isLoading)
                assertEquals(1, loadedState.books.size)
            }
        }

    @Test
    fun `when initialized ui state contains book details`() = runTest {
        val viewModel = BookshelfViewModel(getAllSavedBooksUseCase)

        viewModel.bookshelfUIState.test {
            skipItems(2)

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            assertEquals(1, loadedState.books.size)

            val bookUiState = loadedState.books[0]
            assertEquals("test-id", bookUiState.id)
            assertEquals("Test Title", bookUiState.title)
            assertEquals(listOf("Test Author"), bookUiState.authors)
            assertEquals("test-thumbnail", bookUiState.thumbnailLink)
        }
    }

    @Test
    fun `when initialized and loading fails then show error`() = runTest {
        fakeBooksRepository.shouldGetAllSavedBooksFromDbThrowException = true
        val failingViewModel = BookshelfViewModel(GetAllSavedBooksUseCase(fakeBooksRepository))

        failingViewModel.effects.test {
            assertEquals(
                BookshelfEffect.ShowMessage(R.string.getallbooks_error_message),
                awaitItem()
            )
        }

        val finalState = failingViewModel.bookshelfUIState.value
        assertFalse(finalState.isLoading)
        assertTrue(finalState.books.isEmpty())
    }

    @Test
    fun `when error occurs then effect is delivered once and not re-delivered to a new collector`() =
        runTest {
            fakeBooksRepository.shouldGetAllSavedBooksFromDbThrowException = true
            val failingViewModel = BookshelfViewModel(GetAllSavedBooksUseCase(fakeBooksRepository))

            failingViewModel.effects.test {
                assertEquals(
                    BookshelfEffect.ShowMessage(R.string.getallbooks_error_message),
                    awaitItem()
                )
                expectNoEvents()
            }

            failingViewModel.effects.test {
                expectNoEvents()
            }
        }

    @Test
    fun `when view highlights for book is set then book view action uses it`() = runTest {
        val viewModel = BookshelfViewModel(getAllSavedBooksUseCase)
        var calledBookId: String? = null
        viewModel.viewHighlightsForBook = { bookId -> calledBookId = bookId }

        viewModel.bookshelfUIState.test {
            skipItems(2)

            val loadedState = awaitItem()
            val bookUiState = loadedState.books[0]

            bookUiState.viewBook(bookUiState.id)

            assertEquals("test-id", calledBookId)
        }
    }
}
