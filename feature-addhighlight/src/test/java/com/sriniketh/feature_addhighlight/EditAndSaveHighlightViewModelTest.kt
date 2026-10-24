package com.sriniketh.feature_addhighlight

import app.cash.turbine.test
import com.sriniketh.core_data.usecases.FormatCurrentDateTimeUseCase
import com.sriniketh.feature_addhighlight.fakes.FakeDateTimeSource
import com.sriniketh.feature_addhighlight.fakes.FakeHighlightsRepository
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
    private lateinit var fakeHighlightsRepository: FakeHighlightsRepository
    private lateinit var formatCurrentDateTimeUseCase: FormatCurrentDateTimeUseCase
    private lateinit var viewModel: EditAndSaveHighlightViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeDateTimeSource = FakeDateTimeSource()
        fakeHighlightsRepository = FakeHighlightsRepository()
        formatCurrentDateTimeUseCase = FormatCurrentDateTimeUseCase()

        viewModel = EditAndSaveHighlightViewModel(
            dateTimeSource = fakeDateTimeSource,
            highlightsRepository = fakeHighlightsRepository,
            formatCurrentDateTimeUseCase = formatCurrentDateTimeUseCase
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
    fun `when started with text then highlight text is that text`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.startWithText("Selected text from the page")

            assertEquals("Selected text from the page", awaitItem().highlightText)
        }
    }

    @Test
    fun `when started again after the text was edited then edits are kept`() = runTest {
        viewModel.startWithText("Selected text from the page")
        viewModel.updateHighlightText("Edited text")

        viewModel.startWithText("Selected text from the page")

        assertEquals("Edited text", viewModel.uiState.value.highlightText)
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
    fun `when save highlight succeeds then highlight is inserted with correct fields`() = runTest {
        viewModel.saveHighlight("book-id", "highlight text")
        advanceUntilIdle()

        val insertedHighlight = fakeHighlightsRepository.insertedHighlight
        assertEquals("book-id", insertedHighlight?.bookId)
        assertEquals("highlight text", insertedHighlight?.text)
        assertTrue(insertedHighlight?.id.orEmpty().isNotBlank())
        assertEquals(
            formatCurrentDateTimeUseCase(fakeDateTimeSource.currentTime),
            insertedHighlight?.savedOnTimestamp
        )
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
    fun `when update highlight succeeds then highlight is inserted with provided highlight id`() =
        runTest {
            viewModel.updateHighlight("book-id", "updated highlight text", "highlight-id")
            advanceUntilIdle()

            val insertedHighlight = fakeHighlightsRepository.insertedHighlight
            assertEquals("highlight-id", insertedHighlight?.id)
            assertEquals("book-id", insertedHighlight?.bookId)
            assertEquals("updated highlight text", insertedHighlight?.text)
        }

    @Test
    fun `when highlight was loaded then updating it preserves the original saved on timestamp`() =
        runTest {
            viewModel.loadHighlightText("highlight-id")
            advanceUntilIdle()

            viewModel.updateHighlight("book-id", "edited highlight text", "highlight-id")
            advanceUntilIdle()

            assertEquals(
                "2023-01-01",
                fakeHighlightsRepository.insertedHighlight?.savedOnTimestamp
            )
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
}
