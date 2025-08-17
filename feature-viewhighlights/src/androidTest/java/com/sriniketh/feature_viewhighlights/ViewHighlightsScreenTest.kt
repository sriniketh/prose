package com.sriniketh.feature_viewhighlights

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewHighlightsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenHighlightsListIsEmptyThenEmptyMessageIsDisplayed() {
        val uiState = ViewHighlightsUIState(highlights = emptyList())
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("No saved highlights").assertIsDisplayed()
    }

    @Test
    fun whenHighlightsListIsNotEmptyThenEmptyMessageIsNotDisplayed() {
        val highlights = listOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("No saved highlights").assertIsNotDisplayed()
    }

    @Test
    fun whenHighlightsArePresentThenHighlightTextIsDisplayed() {
        val highlightText = "This is a test highlight"
        val highlights = listOf(createTestHighlightUIState(text = highlightText))
        val uiState = ViewHighlightsUIState(highlights = highlights)
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText(highlightText).assertIsDisplayed()
    }

    @Test
    fun whenHighlightsArePresentThenSavedTimestampIsDisplayed() {
        val savedOn = "2023-12-25"
        val highlights = listOf(createTestHighlightUIState(savedOn = savedOn))
        val uiState = ViewHighlightsUIState(highlights = highlights)
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("Saved on: $savedOn").assertIsDisplayed()
    }

    @Test
    fun whenHighlightsArePresentThenMoreOptionsButtonIsDisplayed() {
        val highlights = listOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Options Menu").assertIsDisplayed()
    }

    @Test
    fun whenPageTitleIsDisplayedThenShowsCorrectText() {
        val uiState = ViewHighlightsUIState()
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("Saved highlights").assertIsDisplayed()
    }

    @Test
    fun whenFloatingActionButtonIsDisplayedThenShowsAddIcon() {
        val uiState = ViewHighlightsUIState()
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Add highlight").assertIsDisplayed()
    }

    @Test
    fun whenBackButtonIsClickedThenOnBackPressedEventIsTriggered() {
        val uiState = ViewHighlightsUIState()
        var eventTriggered: ViewHighlightsEvent? = null
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = { eventTriggered = it }
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Go back").performClick()
        assertEquals(ViewHighlightsEvent.OnBackPressed, eventTriggered)
    }

    @Test
    fun whenMoreOptionsIsClickedThenDropdownMenuIsDisplayed() {
        val highlights = listOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Options Menu").performClick()
        composeTestRule.onNodeWithText("Copy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Edit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
    }

    @Test
    fun whenEditMenuItemIsClickedThenOnEditHighlightEventIsTriggered() {
        val highlightId = "test-highlight-id"
        val highlights = listOf(createTestHighlightUIState(id = highlightId))
        val uiState = ViewHighlightsUIState(highlights = highlights)
        var eventTriggered: ViewHighlightsEvent? = null
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = { eventTriggered = it }
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Options Menu").performClick()
        composeTestRule.onNodeWithText("Edit").performClick()
        assertTrue(eventTriggered is ViewHighlightsEvent.OnEditHighlight)
        assertEquals(highlightId, (eventTriggered as ViewHighlightsEvent.OnEditHighlight).highlightId)
    }

    @Test
    fun whenDeleteMenuItemIsClickedThenDeleteDialogIsDisplayed() {
        val highlights = listOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Options Menu").performClick()
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.onNodeWithText("Delete highlight").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to remove this highlight from your device?").assertIsDisplayed()
    }

    @Test
    fun whenDeleteDialogConfirmIsClickedThenOnDeleteIsCalled() {
        val highlights = listOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)
        var onDeleteCalled = false
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState.copy(
                        highlights = listOf(
                            createTestHighlightUIState(
                                onDelete = { onDeleteCalled = true }
                            )
                        )
                    ),
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Options Menu").performClick()
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.onNodeWithText("Delete").performClick()
        assertTrue(onDeleteCalled)
    }

    @Test
    fun whenDeleteDialogCancelIsClickedThenDialogIsDismissed() {
        val highlights = listOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)
        
        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Options Menu").performClick()
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Delete highlight").assertIsNotDisplayed()
    }

    private fun createTestHighlightUIState(
        id: String = "test-id",
        text: String = "test text",
        savedOn: String = "2023-01-01",
        onDelete: () -> Unit = {}
    ) = HighlightUIState(
        id = id,
        text = text,
        savedOn = savedOn,
        onDelete = onDelete
    )
}
