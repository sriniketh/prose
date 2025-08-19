package com.sriniketh.feature_addhighlight

import android.net.Uri
import app.cash.turbine.test
import com.sriniketh.core_data.usecases.DeleteFileUseCase
import com.sriniketh.core_data.usecases.FormatCurrentDateTimeUseCase
import com.sriniketh.core_data.usecases.LoadHighlightUseCase
import com.sriniketh.core_data.usecases.SaveHighlightUseCase
import com.sriniketh.feature_addhighlight.fakes.FakeDateTimeSource
import com.sriniketh.feature_addhighlight.fakes.FakeFileSource
import com.sriniketh.feature_addhighlight.fakes.FakeHighlightsRepository
import com.sriniketh.feature_addhighlight.fakes.FakeTextAnalyzer
import io.mockk.mockk
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditAndSaveHighlightViewModelTest {
    private lateinit var fakeDateTimeSource: FakeDateTimeSource
    private lateinit var fakeTextAnalyzer: FakeTextAnalyzer
    private lateinit var fakeHighlightsRepository: FakeHighlightsRepository
    private lateinit var fakeFileSource: FakeFileSource
    private lateinit var saveHighlightUseCase: SaveHighlightUseCase
    private lateinit var loadHighlightUseCase: LoadHighlightUseCase
    private lateinit var formatCurrentDateTimeUseCase: FormatCurrentDateTimeUseCase
    private lateinit var deleteFileUseCase: DeleteFileUseCase
    private lateinit var viewModel: EditAndSaveHighlightViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeDateTimeSource = FakeDateTimeSource()
        fakeTextAnalyzer = FakeTextAnalyzer()
        fakeHighlightsRepository = FakeHighlightsRepository()
        fakeFileSource = FakeFileSource()
        saveHighlightUseCase = SaveHighlightUseCase(fakeHighlightsRepository)
        loadHighlightUseCase = LoadHighlightUseCase(fakeHighlightsRepository)
        formatCurrentDateTimeUseCase = FormatCurrentDateTimeUseCase()
        deleteFileUseCase = DeleteFileUseCase(fakeFileSource)
        
        viewModel = EditAndSaveHighlightViewModel(
            dateTimeSource = fakeDateTimeSource,
            textAnalyzer = fakeTextAnalyzer,
            saveHighlightUseCase = saveHighlightUseCase,
            loadHighlightUseCase = loadHighlightUseCase,
            formatCurrentDateTimeUseCase = formatCurrentDateTimeUseCase,
            deleteFileUseCase = deleteFileUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initialized then state has correct defaults`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()

            assertFalse(initialState.isLoading)
            assertEquals(R.string.save_highlight_title_text, initialState.screenTitle)
            assertEquals("", initialState.highlightText)
            assertEquals(null, initialState.snackBarText)
            assertFalse(initialState.highlightSaved)
        }
    }

    @Test
    fun `when update highlight text is called then text is updated`() = runTest {
        val newText = "Updated highlight text"

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateHighlightText(newText)

            val updatedState = awaitItem()
            assertEquals(newText, updatedState.highlightText)
        }
    }

    @Test
    fun `when process image for highlight text is called then loading is set to true`() = runTest {
        val fakeUri = mockk<Uri>()

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.processImageForHighlightText(fakeUri)

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            skipItems(1)
        }
    }

    @Test
    fun `when process image for highlight text succeeds then text is updated`() = runTest {
        val fakeUri = mockk<Uri>()
        fakeTextAnalyzer.textToReturn = "Processed text from image"

        viewModel.uiState.test {
            awaitItem()

            viewModel.processImageForHighlightText(fakeUri)
            skipItems(1)

            val resultState = awaitItem()
            assertFalse(resultState.isLoading)
            assertEquals("Processed text from image", resultState.highlightText)
        }
    }

    @Test
    fun `when process image for highlight text fails then error is shown`() = runTest {
        val fakeUri = mockk<Uri>()
        fakeTextAnalyzer.shouldThrowException = true

        viewModel.uiState.test {
            awaitItem()

            viewModel.processImageForHighlightText(fakeUri)
            skipItems(1)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(R.string.image_processing_failure_error_message, errorState.snackBarText)
        }
    }

    @Test
    fun `when process image for highlight text completes then file is deleted`() = runTest {
        val fakeUri = mockk<Uri>()

        viewModel.processImageForHighlightText(fakeUri)
        advanceUntilIdle()

        assertTrue(fakeFileSource.deletedUris.contains(fakeUri))
    }

    @Test
    fun `when save highlight is called then loading is set to true`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.saveHighlight("book-id", "highlight text")

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            skipItems(1)
        }
    }

    @Test
    fun `when save highlight succeeds then highlight saved is set to true`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.saveHighlight("book-id", "highlight text")
            skipItems(1)

            val savedState = awaitItem()
            assertFalse(savedState.isLoading)
            assertTrue(savedState.highlightSaved)
        }
    }

    @Test
    fun `when save highlight fails then error is shown`() = runTest {
        fakeHighlightsRepository.shouldInsertHighlightIntoDbThrowException = true

        viewModel.uiState.test {
            awaitItem()

            viewModel.saveHighlight("book-id", "highlight text")
            skipItems(1)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(R.string.save_highlight_error_message, errorState.snackBarText)
        }
    }

    @Test
    fun `when load highlight text is called then loading is set to true`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.loadHighlightText("highlight-id")

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            skipItems(1)
        }
    }

    @Test
    fun `when load highlight text succeeds then text and title are updated`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.loadHighlightText("highlight-id")
            skipItems(1)

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            assertEquals("Test highlight text", loadedState.highlightText)
            assertEquals(R.string.edit_highlight_title_text, loadedState.screenTitle)
        }
    }

    @Test
    fun `when load highlight text fails then error is shown`() = runTest {
        fakeHighlightsRepository.shouldLoadHighlightFromDbThrowException = true

        viewModel.uiState.test {
            awaitItem()

            viewModel.loadHighlightText("highlight-id")
            skipItems(1)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(R.string.image_processing_failure_error_message, errorState.snackBarText)
        }
    }

    @Test
    fun `when update highlight is called then loading is set to true`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.updateHighlight("book-id", "updated highlight text", "highlight-id")

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            skipItems(1)
        }
    }

    @Test
    fun `when update highlight succeeds then highlight saved is set to true`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.updateHighlight("book-id", "updated highlight text", "highlight-id")
            skipItems(1)

            val savedState = awaitItem()
            assertFalse(savedState.isLoading)
            assertTrue(savedState.highlightSaved)
        }
    }

    @Test
    fun `when update highlight fails then error is shown`() = runTest {
        fakeHighlightsRepository.shouldInsertHighlightIntoDbThrowException = true

        viewModel.uiState.test {
            awaitItem()

            viewModel.updateHighlight("book-id", "updated highlight text", "highlight-id")
            skipItems(1)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(R.string.save_highlight_error_message, errorState.snackBarText)
        }
    }
}
