package com.sriniketh.feature_bookshelf

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import com.sriniketh.feature_bookshelf.fakes.FakeBooksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.collections.immutable.persistentListOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookshelfViewModelTest {

    private lateinit var fakeBooksRepository: FakeBooksRepository

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeBooksRepository = FakeBooksRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initialized then state has correct defaults`() = runTest {
        val viewModel = BookshelfViewModel(fakeBooksRepository, SavedStateHandle())

        viewModel.bookshelfUIState.test {
            val initialState = awaitItem()

            assertFalse(initialState.isLoading)
            assertTrue(initialState.books.isEmpty())
        }
    }

    @Test
    fun `when initialized books are loaded and ui state transitions from loading to loaded`() =
        runTest {
            val viewModel = BookshelfViewModel(fakeBooksRepository, SavedStateHandle())

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
        val viewModel = BookshelfViewModel(fakeBooksRepository, SavedStateHandle())

        viewModel.bookshelfUIState.test {
            skipItems(2)

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            assertEquals(1, loadedState.books.size)

            val bookUiState = loadedState.books[0]
            assertEquals("test-id", bookUiState.id)
            assertEquals("Test Title", bookUiState.title)
            assertEquals(persistentListOf("Test Author"), bookUiState.authors)
            assertEquals("test-thumbnail", bookUiState.thumbnailLink)
        }
    }

    @Test
    fun `when initialized and loading fails then show error`() = runTest {
        fakeBooksRepository.shouldGetAllSavedBooksFromDbThrowException = true
        val failingViewModel = BookshelfViewModel(fakeBooksRepository, SavedStateHandle())

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
            val failingViewModel = BookshelfViewModel(fakeBooksRepository, SavedStateHandle())

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
    fun `when book added flag is set then added message is emitted and cleared`() = runTest {
        val savedStateHandle = SavedStateHandle(
            mapOf(BOOKSHELF_SHOW_ADDED_MESSAGE to true)
        )
        val viewModel = BookshelfViewModel(fakeBooksRepository, savedStateHandle)

        viewModel.effects.test {
            assertEquals(
                BookshelfEffect.ShowMessage(R.string.book_added_to_shelf_message),
                awaitItem()
            )
        }
        advanceUntilIdle()

        assertEquals(false, savedStateHandle.get<Boolean>(BOOKSHELF_SHOW_ADDED_MESSAGE))
    }

    @Test
    fun `when book added flag is false then no added message effect is emitted`() = runTest {
        val savedStateHandle = SavedStateHandle(
            mapOf(BOOKSHELF_SHOW_ADDED_MESSAGE to false)
        )
        val viewModel = BookshelfViewModel(fakeBooksRepository, savedStateHandle)

        viewModel.effects.test {
            expectNoEvents()
        }
    }

    @Test
    fun `when book added flag is set again after being cleared then message is emitted again`() =
        runTest {
            val savedStateHandle = SavedStateHandle(
                mapOf(BOOKSHELF_SHOW_ADDED_MESSAGE to true)
            )
            val viewModel = BookshelfViewModel(fakeBooksRepository, savedStateHandle)

            viewModel.effects.test {
                assertEquals(
                    BookshelfEffect.ShowMessage(R.string.book_added_to_shelf_message),
                    awaitItem()
                )

                savedStateHandle[BOOKSHELF_SHOW_ADDED_MESSAGE] = true

                assertEquals(
                    BookshelfEffect.ShowMessage(R.string.book_added_to_shelf_message),
                    awaitItem()
                )
            }
        }

    @Test
    fun `when repository emits an updated book list then ui state reflects the new list`() =
        runTest {
            val viewModel = BookshelfViewModel(fakeBooksRepository, SavedStateHandle())
            val secondBook = Book(
                id = "second-id",
                info = BookInfo(
                    title = "Second Title",
                    subtitle = null,
                    authors = listOf("Author A", "Author B"),
                    thumbnailLink = null,
                    publisher = null,
                    publishedDate = null,
                    description = null,
                    pageCount = null,
                    averageRating = null,
                    ratingsCount = null
                )
            )

            viewModel.bookshelfUIState.test {
                skipItems(2)

                val loadedState = awaitItem()
                val firstBookId = loadedState.books[0].id
                assertEquals(1, loadedState.books.size)

                fakeBooksRepository.savedBooksFlow.value = Result.success(
                    fakeBooksRepository.savedBooksFlow.value.getOrThrow() + secondBook
                )

                val updatedState = awaitItem()
                assertEquals(2, updatedState.books.size)
                assertEquals(firstBookId, updatedState.books[0].id)
                val secondBookUiState = updatedState.books[1]
                assertEquals("second-id", secondBookUiState.id)
                assertEquals("Second Title", secondBookUiState.title)
                assertEquals(persistentListOf("Author A", "Author B"), secondBookUiState.authors)
                assertEquals(null, secondBookUiState.thumbnailLink)
            }
        }
}
