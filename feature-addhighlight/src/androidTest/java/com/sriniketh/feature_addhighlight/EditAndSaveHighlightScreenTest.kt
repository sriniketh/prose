package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_data.usecases.FormatCurrentDateTimeUseCase
import com.sriniketh.core_design.ui.theme.AppTheme
import com.sriniketh.feature_addhighlight.fakes.FakeDateTimeSource
import com.sriniketh.feature_addhighlight.fakes.FakeFileSource
import com.sriniketh.feature_addhighlight.fakes.FakeHighlightsRepository
import com.sriniketh.feature_addhighlight.fakes.FakeTextAnalyzer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditAndSaveHighlightScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(
        textAnalyzer: FakeTextAnalyzer = FakeTextAnalyzer(),
        highlightsRepository: FakeHighlightsRepository = FakeHighlightsRepository()
    ): EditAndSaveHighlightViewModel = EditAndSaveHighlightViewModel(
        dateTimeSource = FakeDateTimeSource(),
        textAnalyzer = textAnalyzer,
        highlightsRepository = highlightsRepository,
        formatCurrentDateTimeUseCase = FormatCurrentDateTimeUseCase(),
        fileSource = FakeFileSource()
    )

    @Test
    fun whenUIStateIsLoadingThenProgressIndicatorIsDisplayed() {
        val uiState = EditAndSaveHighlightUiState(isLoading = true)

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlight(
                    uiState = uiState,
                    updateHighlightText = {},
                    saveHighlight = {},
                    goBack = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("AddHighlightLoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun whenUIStateIsLoadingThenSaveButtonClickIsIgnored() {
        val uiState = EditAndSaveHighlightUiState(isLoading = true, highlightText = "Some text")
        var saveHighlightCalled = false

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlight(
                    uiState = uiState,
                    updateHighlightText = {},
                    saveHighlight = { saveHighlightCalled = true },
                    goBack = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("SaveHighlightButton").performClick()
        assertFalse(saveHighlightCalled)
    }

    @Test
    fun whenUIStateIsNotLoadingThenProgressIndicatorIsNotDisplayed() {
        val uiState = EditAndSaveHighlightUiState(isLoading = false)

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlight(
                    uiState = uiState,
                    updateHighlightText = {},
                    saveHighlight = {},
                    goBack = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("AddHighlightLoadingIndicator").assertDoesNotExist()
    }

    @Test
    fun whenScreenIsDisplayedThenPageTitleShowsCorrectText() {
        val uiState = EditAndSaveHighlightUiState()

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlight(
                    uiState = uiState,
                    updateHighlightText = {},
                    saveHighlight = {},
                    goBack = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Save highlight").assertIsDisplayed()
    }

    @Test
    fun whenBackButtonIsClickedThenGoBackIsTriggered() {
        val uiState = EditAndSaveHighlightUiState()
        var goBackCalled = false

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlight(
                    uiState = uiState,
                    updateHighlightText = {},
                    saveHighlight = {},
                    goBack = { goBackCalled = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Go back").performClick()
        assertTrue(goBackCalled)
    }

    @Test
    fun whenHighlightTextIsProvidedThenTextFieldDisplaysText() {
        val testText = "This is a test highlight text"
        val uiState = EditAndSaveHighlightUiState(highlightText = testText)

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlight(
                    uiState = uiState,
                    updateHighlightText = {},
                    saveHighlight = {},
                    goBack = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(testText).assertIsDisplayed()
    }

    @Test
    fun whenTextFieldIsChangedThenOnHighlightTextChangedIsCalled() {
        val uiState = EditAndSaveHighlightUiState()
        var changedText = ""

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlight(
                    uiState = uiState,
                    updateHighlightText = { changedText = it },
                    saveHighlight = {},
                    goBack = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        val newText = "Updated highlight text"
        composeTestRule.onNodeWithTag("AddHighlightTextField").performTextInput(newText)
        assertTrue(changedText.contains(newText))
    }

    @Test
    fun whenSaveButtonIsClickedThenOnSaveHighlightIsCalled() {
        val uiState = EditAndSaveHighlightUiState(highlightText = "Some text")
        var saveHighlightCalled = false

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlight(
                    uiState = uiState,
                    updateHighlightText = {},
                    saveHighlight = { saveHighlightCalled = true },
                    goBack = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("SaveHighlightButton").performClick()
        assertTrue(saveHighlightCalled)
    }

    @Test
    fun whenTextFieldIsDisplayedThenItAcceptsInput() {
        val uiState = EditAndSaveHighlightUiState(highlightText = "")

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlight(
                    uiState = uiState,
                    updateHighlightText = {},
                    saveHighlight = {},
                    goBack = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("AddHighlightTextField").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun whenCaptureFlowScreenIsDisplayedThenProcessedHighlightTextIsShown() {
        val fakeTextAnalyzer = FakeTextAnalyzer().apply { textToReturn = "Recognized highlight text" }
        val viewModel = createViewModel(textAnalyzer = fakeTextAnalyzer)

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlightScreen(
                    viewModel = viewModel,
                    uri = Uri.parse("content://test.jpg"),
                    bookId = "book-id",
                    goBack = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Recognized highlight text").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Recognized highlight text").assertIsDisplayed()
    }

    @Test
    fun whenCaptureFlowImageProcessingFailsThenErrorSnackbarIsShown() {
        val fakeTextAnalyzer = FakeTextAnalyzer().apply { shouldThrowException = true }
        val viewModel = createViewModel(textAnalyzer = fakeTextAnalyzer)

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlightScreen(
                    viewModel = viewModel,
                    uri = Uri.parse("content://test.jpg"),
                    bookId = "book-id",
                    goBack = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Error processing image for highlight text.")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun whenCaptureFlowSaveSucceedsThenGoBackIsInvoked() {
        val fakeTextAnalyzer = FakeTextAnalyzer().apply { textToReturn = "Recognized highlight text" }
        val viewModel = createViewModel(textAnalyzer = fakeTextAnalyzer)
        var goBackCalled = false

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlightScreen(
                    viewModel = viewModel,
                    uri = Uri.parse("content://test.jpg"),
                    bookId = "book-id",
                    goBack = { goBackCalled = true }
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Recognized highlight text").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("SaveHighlightButton").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) { goBackCalled }
        assertTrue(goBackCalled)
    }

    @Test
    fun whenEditFlowScreenIsDisplayedThenLoadedHighlightTextAndTitleAreShown() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlightScreen(
                    viewModel = viewModel,
                    highlightId = "highlight-id",
                    bookId = "book-id",
                    goBack = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Test highlight text").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Edit highlight").assertIsDisplayed()
    }

    @Test
    fun whenEditFlowLoadFailsThenErrorSnackbarIsShown() {
        val fakeHighlightsRepository = FakeHighlightsRepository().apply {
            shouldLoadHighlightFromDbThrowException = true
        }
        val viewModel = createViewModel(highlightsRepository = fakeHighlightsRepository)

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlightScreen(
                    viewModel = viewModel,
                    highlightId = "highlight-id",
                    bookId = "book-id",
                    goBack = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Error processing image for highlight text.")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun whenEditFlowUpdateFailsThenErrorSnackbarIsShown() {
        val fakeHighlightsRepository = FakeHighlightsRepository().apply {
            shouldInsertHighlightIntoDbThrowException = true
        }
        val viewModel = createViewModel(highlightsRepository = fakeHighlightsRepository)

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlightScreen(
                    viewModel = viewModel,
                    highlightId = "highlight-id",
                    bookId = "book-id",
                    goBack = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Test highlight text").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("SaveHighlightButton").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Error saving highlight.").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun whenEditFlowUpdateSucceedsThenGoBackIsInvoked() {
        val viewModel = createViewModel()
        var goBackCalled = false

        composeTestRule.setContent {
            AppTheme {
                EditAndSaveHighlightScreen(
                    viewModel = viewModel,
                    highlightId = "highlight-id",
                    bookId = "book-id",
                    goBack = { goBackCalled = true }
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Test highlight text").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("SaveHighlightButton").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) { goBackCalled }
        assertTrue(goBackCalled)
    }
}
