package com.sriniketh.feature_viewhighlights

import app.cash.turbine.test
import com.sriniketh.core_data.usecases.DeleteHighlightUseCase
import com.sriniketh.core_data.usecases.ExportHighlightsUseCase
import com.sriniketh.core_data.usecases.GetAllSavedHighlightsUseCase
import com.sriniketh.feature_viewhighlights.fakes.FakeBooksRepository
import com.sriniketh.feature_viewhighlights.fakes.FakeFileSource
import com.sriniketh.feature_viewhighlights.fakes.FakeHighlightsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class ViewHighlightsViewModelTest {

    private lateinit var fakeHighlightsRepository: FakeHighlightsRepository
    private lateinit var fakeBooksRepository: FakeBooksRepository
    private lateinit var fakeFileSource: FakeFileSource
    private lateinit var getAllSavedHighlightsUseCase: GetAllSavedHighlightsUseCase
    private lateinit var deleteHighlightUseCase: DeleteHighlightUseCase
    private lateinit var exportHighlightsUseCase: ExportHighlightsUseCase
    private lateinit var viewModel: ViewHighlightsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeHighlightsRepository = FakeHighlightsRepository()
        fakeBooksRepository = FakeBooksRepository()
        fakeFileSource = FakeFileSource()
        getAllSavedHighlightsUseCase = GetAllSavedHighlightsUseCase(fakeHighlightsRepository)
        deleteHighlightUseCase = DeleteHighlightUseCase(fakeHighlightsRepository)
        exportHighlightsUseCase = ExportHighlightsUseCase(fakeBooksRepository, fakeHighlightsRepository, fakeFileSource)
        viewModel = ViewHighlightsViewModel(
            getAllSavedHighlightsUseCase,
            deleteHighlightUseCase,
            exportHighlightsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when getHighlights is called then sets loading state to true`() = runTest {
        viewModel.highlightsUIStateFlow.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.getHighlights("test-book-id")

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            skipItems(1)
        }
    }

    @Test
    fun `when getHighlights succeeds then returns highlights`() = runTest {
        val bookId = "test-book-id"
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = false

        viewModel.highlightsUIStateFlow.test {
            awaitItem()

            viewModel.getHighlights(bookId)

            awaitItem()
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertEquals(1, finalState.highlights.size)
            assertEquals("test-highlight-id", finalState.highlights[0].id)
        }
    }

    @Test
    fun `when getHighlights succeeds then passes book id to repository`() = runTest {
        val bookId = "test-book-id"

        viewModel.getHighlights(bookId)
        this.testScheduler.advanceUntilIdle()

        assertEquals(bookId, fakeHighlightsRepository.bookIdPassed)
    }

    @Test
    fun `when getHighlights fails then shows error message`() = runTest {
        val bookId = "test-book-id"
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = true

        viewModel.effects.test {
            viewModel.getHighlights(bookId)

            assertEquals(
                ViewHighlightsEffect.ShowMessage(R.string.gethighlights_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.highlightsUIStateFlow.value.isLoading)
    }

    @Test
    fun `when getHighlights fails then clears highlights list`() = runTest {
        val bookId = "test-book-id"
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = true

        viewModel.highlightsUIStateFlow.test {
            awaitItem()

            viewModel.getHighlights(bookId)

            awaitItem()
            val errorState = awaitItem()
            assertTrue(errorState.highlights.isEmpty())
        }
    }

    @Test
    fun `when processing OnCameraPermissionDenied action then shows permission error`() = runTest {
        viewModel.effects.test {
            viewModel.processAction(ViewHighlightsAction.OnCameraPermissionDenied)

            assertEquals(
                ViewHighlightsEffect.ShowMessage(R.string.permission_denied_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.highlightsUIStateFlow.value.isLoading)
    }

    @Test
    fun `when processing other actions then does nothing`() = runTest {
        viewModel.processAction(ViewHighlightsAction.OnBackPressed)

        viewModel.highlightsUIStateFlow.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.highlights.isEmpty())
        }
    }

    @Test
    fun `when delete action is processed then sets loading state`() = runTest {
        val bookId = "test-book-id"
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = false

        viewModel.highlightsUIStateFlow.test {
            awaitItem()

            viewModel.getHighlights(bookId)
            awaitItem()
            val stateWithHighlights = awaitItem()

            viewModel.processAction(
                ViewHighlightsAction.OnDeleteHighlight(stateWithHighlights.highlights.first().id)
            )
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
        }
    }

    @Test
    fun `when delete action succeeds then passes highlight to repository`() = runTest {
        val bookId = "test-book-id"
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = false

        viewModel.getHighlights(bookId)
        this.testScheduler.advanceUntilIdle()

        viewModel.highlightsUIStateFlow.test {
            val state = awaitItem()
            viewModel.processAction(
                ViewHighlightsAction.OnDeleteHighlight(state.highlights.first().id)
            )

            awaitItem()
        }

        this.testScheduler.advanceUntilIdle()
        assertEquals("test-highlight-id", fakeHighlightsRepository.deletedHighlight?.id)
    }

    @Test
    fun `when delete action fails then shows error message`() = runTest {
        val bookId = "test-book-id"
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = false
        fakeHighlightsRepository.shouldDeleteHighlightFromDbThrowException = true

        viewModel.getHighlights(bookId)
        this.testScheduler.advanceUntilIdle()

        viewModel.effects.test {
            viewModel.processAction(
                ViewHighlightsAction.OnDeleteHighlight(
                    viewModel.highlightsUIStateFlow.value.highlights.first().id
                )
            )

            assertEquals(
                ViewHighlightsEffect.ShowMessage(R.string.delete_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.highlightsUIStateFlow.value.isLoading)
    }

    @Test
    fun `when highlight is mapped to UI state then all fields are correct`() = runTest {
        val bookId = "test-book-id"
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = false

        viewModel.highlightsUIStateFlow.test {
            awaitItem()

            viewModel.getHighlights(bookId)
            awaitItem()
            val state = awaitItem()

            val highlightUIState = state.highlights.first()
            assertEquals("test-highlight-id", highlightUIState.id)
            assertEquals("Test highlight text", highlightUIState.text)
            assertEquals("2023-01-01 12:00 PM", highlightUIState.savedOn)
        }
    }

    @Test
    fun `when OnExportHighlights action is processed then sets loading state to true`() = runTest {
        viewModel.highlightsUIStateFlow.test {
            awaitItem()

            viewModel.processAction(ViewHighlightsAction.OnExportHighlights("test-book-id"))
            testScheduler.advanceUntilIdle()

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when OnExportHighlights action succeeds then emits share effect and clears loading`() = runTest {
        viewModel.effects.test {
            viewModel.processAction(ViewHighlightsAction.OnExportHighlights("test-book-id"))
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ViewHighlightsEffect.ShareHighlights)
            assertNotNull((effect as ViewHighlightsEffect.ShareHighlights).uri)
        }

        assertFalse(viewModel.highlightsUIStateFlow.value.isLoading)
    }

    @Test
    fun `when OnExportHighlights action fails then shows error and clears loading`() = runTest {
        fakeBooksRepository.shouldGetBookByIdFromDbThrowException = true

        viewModel.effects.test {
            viewModel.processAction(ViewHighlightsAction.OnExportHighlights("test-book-id"))
            testScheduler.advanceUntilIdle()

            assertEquals(
                ViewHighlightsEffect.ShowMessage(R.string.export_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.highlightsUIStateFlow.value.isLoading)
    }
}
