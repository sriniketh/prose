package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.sriniketh.core_data.usecases.DeleteFileUseCase
import com.sriniketh.core_data.usecases.FormatCurrentDateTimeUseCase
import com.sriniketh.core_data.usecases.LoadHighlightUseCase
import com.sriniketh.core_data.usecases.SaveHighlightUseCase
import com.sriniketh.feature_addhighlight.fakes.FakeDateTimeSource
import com.sriniketh.feature_addhighlight.fakes.FakeFileSource
import com.sriniketh.feature_addhighlight.fakes.FakeHighlightsRepository
import com.sriniketh.feature_addhighlight.fakes.FakeTextAnalyzer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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

        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): EditAndSaveHighlightViewModel = EditAndSaveHighlightViewModel(
        dateTimeSource = fakeDateTimeSource,
        textAnalyzer = fakeTextAnalyzer,
        saveHighlightUseCase = saveHighlightUseCase,
        loadHighlightUseCase = loadHighlightUseCase,
        formatCurrentDateTimeUseCase = formatCurrentDateTimeUseCase,
        deleteFileUseCase = deleteFileUseCase,
        savedStateHandle = savedStateHandle
    )

    @Test
    fun `when initialized then state has correct defaults`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()

            assertFalse(initialState.isLoading)
            assertEquals(R.string.save_highlight_title_text, initialState.screenTitle)
            assertEquals("", initialState.highlightText)
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

        viewModel.effects.test {
            viewModel.processImageForHighlightText(fakeUri)

            assertEquals(
                EditAndSaveHighlightEffect.ShowMessage(R.string.image_processing_failure_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.uiState.value.isLoading)
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
    fun `when save highlight succeeds then highlight saved effect is emitted`() = runTest {
        viewModel.effects.test {
            viewModel.saveHighlight("book-id", "highlight text")

            assertEquals(EditAndSaveHighlightEffect.HighlightSaved, awaitItem())
        }

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `when save highlight fails then error is shown`() = runTest {
        fakeHighlightsRepository.shouldInsertHighlightIntoDbThrowException = true

        viewModel.effects.test {
            viewModel.saveHighlight("book-id", "highlight text")

            assertEquals(
                EditAndSaveHighlightEffect.ShowMessage(R.string.save_highlight_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.uiState.value.isLoading)
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

        viewModel.effects.test {
            viewModel.loadHighlightText("highlight-id")

            assertEquals(
                EditAndSaveHighlightEffect.ShowMessage(R.string.image_processing_failure_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.uiState.value.isLoading)
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
    fun `when update highlight succeeds then highlight saved effect is emitted`() = runTest {
        viewModel.effects.test {
            viewModel.updateHighlight("book-id", "updated highlight text", "highlight-id")

            assertEquals(EditAndSaveHighlightEffect.HighlightSaved, awaitItem())
        }

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `when update highlight fails then error is shown`() = runTest {
        fakeHighlightsRepository.shouldInsertHighlightIntoDbThrowException = true

        viewModel.effects.test {
            viewModel.updateHighlight("book-id", "updated highlight text", "highlight-id")

            assertEquals(
                EditAndSaveHighlightEffect.ShowMessage(R.string.save_highlight_error_message),
                awaitItem()
            )
        }

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `when created with a uri in SavedStateHandle then processes the image automatically`() = runTest {
        fakeTextAnalyzer.textToReturn = "Text from auto-processed image"
        mockkStatic(Uri::class)
        every { Uri.decode(any()) } answers { firstArg() }
        every { Uri.parse(any()) } returns mockk(relaxed = true)
        try {
            val savedStateHandle = SavedStateHandle(mapOf("uri" to "content://test/image.jpg"))

            val autoInitViewModel = buildViewModel(savedStateHandle)

            autoInitViewModel.uiState.test {
                awaitItem()
                val loadedState = awaitItem()
                assertEquals("Text from auto-processed image", loadedState.highlightText)
            }
            assertEquals(1, fakeTextAnalyzer.analyzeImageInvocationCount)
        } finally {
            unmockkStatic(Uri::class)
        }
    }

    @Test
    fun `when created with a highlightId in SavedStateHandle then loads the highlight automatically`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("highlightId" to "test-highlight-id"))

        val autoInitViewModel = buildViewModel(savedStateHandle)

        autoInitViewModel.uiState.test {
            awaitItem()
            val loadedState = awaitItem()
            assertEquals("Test highlight text", loadedState.highlightText)
        }
        assertEquals(1, fakeHighlightsRepository.loadHighlightFromDbInvocationCount)
    }

    @Test
    fun `when process image for highlight text is called twice then only processes once`() = runTest {
        val fakeUri = mockk<Uri>()

        viewModel.processImageForHighlightText(fakeUri)
        viewModel.processImageForHighlightText(fakeUri)
        advanceUntilIdle()

        assertEquals(1, fakeTextAnalyzer.analyzeImageInvocationCount)
        assertEquals(1, fakeFileSource.deletedUris.size)
    }

    @Test
    fun `when load highlight text is called twice then only loads once`() = runTest {
        viewModel.loadHighlightText("highlight-id")
        viewModel.loadHighlightText("highlight-id")
        advanceUntilIdle()

        assertEquals(1, fakeHighlightsRepository.loadHighlightFromDbInvocationCount)
    }

    @Test
    fun `when updateHighlightText is called then draft is persisted to SavedStateHandle`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val originalViewModel = buildViewModel(savedStateHandle)

        originalViewModel.updateHighlightText("in progress edit")

        val recreatedViewModel = buildViewModel(savedStateHandle)

        assertEquals("in progress edit", recreatedViewModel.uiState.value.highlightText)
    }

    @Test
    fun `when recreated after process death with a draft then it does not reprocess the deleted image`() = runTest {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "uri" to "content://test/image.jpg",
                "draftHighlightText" to "recovered draft text"
            )
        )

        val recreatedViewModel = buildViewModel(savedStateHandle)
        advanceUntilIdle()

        assertEquals("recovered draft text", recreatedViewModel.uiState.value.highlightText)
        assertEquals(0, fakeTextAnalyzer.analyzeImageInvocationCount)
    }

    @Test
    fun `when recreated after process death with a draft for an existing highlight then title reflects edit mode`() =
        runTest {
            val savedStateHandle = SavedStateHandle(
                mapOf(
                    "highlightId" to "test-highlight-id",
                    "draftHighlightText" to "recovered draft text"
                )
            )

            val recreatedViewModel = buildViewModel(savedStateHandle)
            advanceUntilIdle()

            assertEquals("recovered draft text", recreatedViewModel.uiState.value.highlightText)
            assertEquals(R.string.edit_highlight_title_text, recreatedViewModel.uiState.value.screenTitle)
            assertEquals(0, fakeHighlightsRepository.loadHighlightFromDbInvocationCount)
        }
}
