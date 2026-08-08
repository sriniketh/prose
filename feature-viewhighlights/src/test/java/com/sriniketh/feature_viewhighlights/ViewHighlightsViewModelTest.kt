package com.sriniketh.feature_viewhighlights

import androidx.lifecycle.SavedStateHandle
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
class ViewHighlightsViewModelTest {

    private lateinit var fakeHighlightsRepository: FakeHighlightsRepository
    private lateinit var fakeBooksRepository: FakeBooksRepository
    private lateinit var fakeFileSource: FakeFileSource
    private lateinit var getAllSavedHighlightsUseCase: GetAllSavedHighlightsUseCase
    private lateinit var deleteHighlightUseCase: DeleteHighlightUseCase
    private lateinit var exportHighlightsUseCase: ExportHighlightsUseCase

    private val bookId = "test-book-id"

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeHighlightsRepository = FakeHighlightsRepository()
        fakeBooksRepository = FakeBooksRepository()
        fakeFileSource = FakeFileSource()
        getAllSavedHighlightsUseCase = GetAllSavedHighlightsUseCase(fakeHighlightsRepository)
        deleteHighlightUseCase = DeleteHighlightUseCase(fakeHighlightsRepository)
        exportHighlightsUseCase = ExportHighlightsUseCase(fakeBooksRepository, fakeHighlightsRepository, fakeFileSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(bookId: String = this.bookId): ViewHighlightsViewModel =
        ViewHighlightsViewModel(
            getAllSavedHighlightsUseCase,
            deleteHighlightUseCase,
            exportHighlightsUseCase,
            SavedStateHandle(mapOf("bookId" to bookId))
        )

    @Test
    fun `when created then reads bookId from SavedStateHandle and loads highlights automatically`() = runTest {
        val viewModel = buildViewModel()

        viewModel.highlightsUIStateFlow.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            assertEquals(1, loadedState.highlights.size)
            assertEquals("test-highlight-id", loadedState.highlights[0].id)
        }

        assertEquals(bookId, fakeHighlightsRepository.bookIdPassed)
    }

    @Test
    fun `when ViewModel is only created once then repository is only collected once regardless of how many observers subscribe`() =
        runTest {
            val viewModel = buildViewModel()
            advanceUntilIdle()

            assertEquals(1, fakeHighlightsRepository.getAllHighlightsForBookFromDbInvocationCount)

            viewModel.highlightsUIStateFlow.test { awaitItem() }
            viewModel.highlightsUIStateFlow.test { awaitItem() }
            viewModel.highlightsUIStateFlow.test { awaitItem() }

            assertEquals(1, fakeHighlightsRepository.getAllHighlightsForBookFromDbInvocationCount)
        }

    @Test
    fun `when getAllSavedHighlights fails then shows error message exactly once`() = runTest {
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = true
        val viewModel = buildViewModel()

        viewModel.effects.test {
            assertEquals(
                ViewHighlightsEffect.ShowMessage(R.string.gethighlights_error_message),
                awaitItem()
            )
            expectNoEvents()
        }
    }

    @Test
    fun `when getAllSavedHighlights fails then clears highlights list`() = runTest {
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = true
        val viewModel = buildViewModel()

        viewModel.highlightsUIStateFlow.test {
            awaitItem()
            val errorState = awaitItem()
            assertTrue(errorState.highlights.isEmpty())
            assertFalse(errorState.isLoading)
        }
    }

    @Test
    fun `when highlight is mapped to UI state then all fields are correct`() = runTest {
        val viewModel = buildViewModel()

        viewModel.highlightsUIStateFlow.test {
            awaitItem()
            val state = awaitItem()

            val highlightUIState = state.highlights.first()
            assertEquals("test-highlight-id", highlightUIState.id)
            assertEquals("Test highlight text", highlightUIState.text)
            assertEquals("2023-01-01 12:00 PM", highlightUIState.savedOn)
        }
    }

    @Test
    fun `when processing OnCameraPermissionDenied action then shows permission error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effects.test {
            viewModel.processAction(ViewHighlightsAction.OnCameraPermissionDenied)

            assertEquals(
                ViewHighlightsEffect.ShowMessage(R.string.permission_denied_error_message),
                awaitItem()
            )
        }
    }

    @Test
    fun `when processing other actions then does nothing`() = runTest {
        val viewModel = buildViewModel()
        viewModel.processAction(ViewHighlightsAction.OnBackPressed)

        viewModel.highlightsUIStateFlow.test {
            awaitItem()
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(1, state.highlights.size)
        }
    }

    @Test
    fun `when delete action is processed then sets loading state`() = runTest {
        val viewModel = buildViewModel()

        viewModel.highlightsUIStateFlow.test {
            awaitItem()
            val stateWithHighlights = awaitItem()

            viewModel.processAction(
                ViewHighlightsAction.OnDeleteHighlight(stateWithHighlights.highlights.first().id)
            )
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when delete action succeeds then passes highlight to repository`() = runTest {
        val viewModel = buildViewModel()

        viewModel.highlightsUIStateFlow.test {
            awaitItem()
            val state = awaitItem()
            viewModel.processAction(
                ViewHighlightsAction.OnDeleteHighlight(state.highlights.first().id)
            )

            cancelAndIgnoreRemainingEvents()
        }

        advanceUntilIdle()
        assertEquals("test-highlight-id", fakeHighlightsRepository.deletedHighlightId)
    }

    @Test
    fun `when delete action fails then shows error message`() = runTest {
        fakeHighlightsRepository.shouldDeleteHighlightFromDbThrowException = true
        val viewModel = buildViewModel()

        viewModel.highlightsUIStateFlow.test {
            awaitItem()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.effects.test {
            viewModel.processAction(
                ViewHighlightsAction.OnDeleteHighlight("test-highlight-id")
            )

            assertEquals(
                ViewHighlightsEffect.ShowMessage(R.string.delete_error_message),
                awaitItem()
            )
        }
    }

    @Test
    fun `when OnExportHighlights action is processed then sets loading state to true`() = runTest {
        val viewModel = buildViewModel()

        viewModel.highlightsUIStateFlow.test {
            awaitItem()
            awaitItem()

            viewModel.processAction(ViewHighlightsAction.OnExportHighlights(bookId))

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when OnExportHighlights action succeeds then emits share effect and clears loading`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effects.test {
            viewModel.processAction(ViewHighlightsAction.OnExportHighlights(bookId))
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ViewHighlightsEffect.ShareHighlights)
            assertNotNull((effect as ViewHighlightsEffect.ShareHighlights).uri)
        }
    }

    @Test
    fun `when OnExportHighlights action fails then shows error and clears loading`() = runTest {
        fakeBooksRepository.shouldGetBookByIdFromDbThrowException = true
        val viewModel = buildViewModel()

        viewModel.effects.test {
            viewModel.processAction(ViewHighlightsAction.OnExportHighlights(bookId))
            testScheduler.advanceUntilIdle()

            assertEquals(
                ViewHighlightsEffect.ShowMessage(R.string.export_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.highlightsUIStateFlow.value.isLoading)
    }
}
