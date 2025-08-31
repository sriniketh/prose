package com.sriniketh.feature_addhighlight

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditAndSaveHighlightScreenTest {

	@get:Rule
	val composeTestRule = createComposeRule()

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
		composeTestRule.onNodeWithText("Save").assertIsDisplayed().assertIsNotEnabled()
		composeTestRule.onNodeWithText("Cancel").assertIsDisplayed().assertIsNotEnabled()
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
		composeTestRule.onNodeWithTag("AddHighlightLoadingIndicator").assertIsNotDisplayed()
		composeTestRule.onNodeWithText("Save").assertIsDisplayed().assertIsEnabled()
		composeTestRule.onNodeWithText("Cancel").assertIsDisplayed().assertIsEnabled()
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
		composeTestRule.onNodeWithText("Save").performClick()
		assertTrue(saveHighlightCalled)
	}

	@Test
	fun whenCancelButtonIsClickedThenGoBackIsCalled() {
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
		composeTestRule.onNodeWithText("Cancel").performClick()
		assertTrue(goBackCalled)
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
}
