package com.sriniketh.feature_searchbooks

import app.cash.turbine.test
import com.sriniketh.core_data.usecases.AddBookToShelfUseCase
import com.sriniketh.core_data.usecases.FetchBookInfoUseCase
import com.sriniketh.core_data.usecases.IsBookInDbUseCase
import com.sriniketh.feature_searchbooks.fakes.FakeBooksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookInfoViewModelTest {

    private lateinit var fakeBooksRepository: FakeBooksRepository
    private lateinit var fetchBookInfoUseCase: FetchBookInfoUseCase
    private lateinit var addBookToShelfUseCase: AddBookToShelfUseCase
    private lateinit var isBookInDbUseCase: IsBookInDbUseCase
    private lateinit var viewModel: BookInfoViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeBooksRepository = FakeBooksRepository()
        fetchBookInfoUseCase = FetchBookInfoUseCase(fakeBooksRepository)
        addBookToShelfUseCase = AddBookToShelfUseCase(fakeBooksRepository)
        isBookInDbUseCase = IsBookInDbUseCase(fakeBooksRepository)
        viewModel = BookInfoViewModel(
            fetchBookInfoUseCase,
            addBookToShelfUseCase,
            isBookInDbUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when get book detail is called then loading state is set to true`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.getBookDetail("test-volume-id")

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            skipItems(1)
        }
    }

    @Test
    fun `when get book detail succeeds then book info and can add to shelf are set`() = runTest {
        fakeBooksRepository.doesBookExistResult = false

        viewModel.uiState.test {
            awaitItem()

            viewModel.getBookDetail("test-volume-id")

            skipItems(1)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertNotNull(successState.book)
            assertEquals("test-id", successState.book?.id)
            assertTrue(successState.canAddToShelf)
        }
    }

    @Test
    fun `when get book detail succeeds and book exists then can add to shelf is set to false`() =
        runTest {
            fakeBooksRepository.doesBookExistResult = true

            viewModel.uiState.test {
                awaitItem()

                viewModel.getBookDetail("test-volume-id")

                skipItems(1)

                val successState = awaitItem()
                assertFalse(successState.isLoading)
                assertNotNull(successState.book)
                assertFalse(successState.canAddToShelf)
            }
        }

    @Test
    fun `when get book detail fails then loading is set to false and error is shown`() = runTest {
        fakeBooksRepository.shouldFetchBookInfoThrowException = true

        viewModel.uiState.test {
            awaitItem()

            viewModel.getBookDetail("test-volume-id")

            skipItems(1)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(R.string.book_info_load_error_message, errorState.snackBarText)
        }
    }

    @Test
    fun `when get book detail is called then volume id is passed to use case`() = runTest {
        val volumeId = "test-volume-id"

        viewModel.getBookDetail(volumeId)
        advanceUntilIdle()

        assertEquals(volumeId, fakeBooksRepository.volumeIdPassed)
    }

    @Test
    fun `when add book to shelf is called then repository insert is invoked`() = runTest {
        fakeBooksRepository.doesBookExistResult = false

        viewModel.getBookDetail("test-volume-id")
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertNotNull(currentState.book)
        assertTrue(currentState.canAddToShelf)

        currentState.addBookToShelf()
        advanceUntilIdle()

        assertNotNull(fakeBooksRepository.insertedBook)
        assertEquals("test-id", fakeBooksRepository.insertedBook?.id)
    }

    @Test
    fun `when add book to shelf succeeds then can add to shelf becomes false`() = runTest {
        fakeBooksRepository.doesBookExistResult = false

        viewModel.getBookDetail("test-volume-id")
        advanceUntilIdle()

        val beforeAddState = viewModel.uiState.value
        assertTrue(beforeAddState.canAddToShelf)

        beforeAddState.addBookToShelf()
        advanceUntilIdle()

        val afterAddState = viewModel.uiState.value
        assertFalse(afterAddState.canAddToShelf)
    }

    @Test
    fun `when initialized then state has correct defaults`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()

            assertFalse(initialState.isLoading)
            assertEquals(null, initialState.snackBarText)
            assertEquals(null, initialState.book)
            assertFalse(initialState.canAddToShelf)
        }
    }
}
