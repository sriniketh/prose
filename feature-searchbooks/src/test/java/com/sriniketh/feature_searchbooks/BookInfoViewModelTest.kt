package com.sriniketh.feature_searchbooks

import androidx.lifecycle.SavedStateHandle
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

    private val volumeId = "test-volume-id"

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeBooksRepository = FakeBooksRepository()
        fetchBookInfoUseCase = FetchBookInfoUseCase(fakeBooksRepository)
        addBookToShelfUseCase = AddBookToShelfUseCase(fakeBooksRepository)
        isBookInDbUseCase = IsBookInDbUseCase(fakeBooksRepository)
        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(bookId: String = volumeId): BookInfoViewModel = BookInfoViewModel(
        fetchBookInfoUseCase,
        addBookToShelfUseCase,
        isBookInDbUseCase,
        SavedStateHandle(mapOf("bookId" to bookId))
    )

    @Test
    fun `when created then reads bookId from SavedStateHandle and loads book detail automatically`() = runTest {
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(volumeId, fakeBooksRepository.volumeIdPassed)
    }

    @Test
    fun `when get book detail is called again after initial load then it is ignored`() = runTest {
        advanceUntilIdle()
        assertEquals(1, fakeBooksRepository.fetchBookInfoInvocationCount)

        viewModel.getBookDetail("some-other-id")
        advanceUntilIdle()

        assertEquals(1, fakeBooksRepository.fetchBookInfoInvocationCount)
        assertEquals(volumeId, fakeBooksRepository.volumeIdPassed)
    }

    @Test
    fun `when get book detail succeeds then book info and can add to shelf are set`() = runTest {
        fakeBooksRepository.doesBookExistResult = false
        advanceUntilIdle()

        val successState = viewModel.uiState.value
        assertFalse(successState.isLoading)
        assertNotNull(successState.book)
        assertEquals("Test Title", successState.book?.title)
        assertTrue(successState.canAddToShelf)
    }

    @Test
    fun `when get book detail succeeds and book exists then can add to shelf is set to false`() =
        runTest {
            fakeBooksRepository.doesBookExistResult = true
            val viewModelWithExistingBook = buildViewModel()
            advanceUntilIdle()

            val successState = viewModelWithExistingBook.uiState.value
            assertFalse(successState.isLoading)
            assertNotNull(successState.book)
            assertFalse(successState.canAddToShelf)
        }

    @Test
    fun `when get book detail fails then loading is set to false and error is shown`() = runTest {
        fakeBooksRepository.shouldFetchBookInfoThrowException = true
        val failingViewModel = buildViewModel()

        failingViewModel.effects.test {
            assertEquals(
                BookInfoEffect.ShowMessage(R.string.book_info_load_error_message),
                awaitItem()
            )
        }

        assertFalse(failingViewModel.uiState.value.isLoading)
    }

    @Test
    fun `when created then bookId is passed to use case`() = runTest {
        advanceUntilIdle()

        assertEquals(volumeId, fakeBooksRepository.volumeIdPassed)
    }

    @Test
    fun `when add book to shelf is called then repository insert is invoked`() = runTest {
        fakeBooksRepository.doesBookExistResult = false
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
        advanceUntilIdle()

        val beforeAddState = viewModel.uiState.value
        assertTrue(beforeAddState.canAddToShelf)

        beforeAddState.addBookToShelf()
        advanceUntilIdle()

        val afterAddState = viewModel.uiState.value
        assertFalse(afterAddState.canAddToShelf)
    }

    @Test
    fun `when add book to shelf succeeds then navigate to bookshelf effect is emitted`() = runTest {
        fakeBooksRepository.doesBookExistResult = false
        advanceUntilIdle()

        val addToShelf = viewModel.uiState.value.addBookToShelf

        viewModel.effects.test {
            addToShelf()

            assertEquals(
                BookInfoEffect.NavigateToBookshelf,
                awaitItem()
            )
        }
    }
}
