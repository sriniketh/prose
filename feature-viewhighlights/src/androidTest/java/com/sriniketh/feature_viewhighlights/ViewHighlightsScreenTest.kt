package com.sriniketh.feature_viewhighlights

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.sriniketh.core_design.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewHighlightsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val grantCameraPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Test
    fun whenHighlightsListIsEmptyThenEmptyMessageIsDisplayed() {
        val uiState = ViewHighlightsUIState(highlights = persistentListOf())

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No saved highlights").assertIsDisplayed()
    }

    @Test
    fun whenHighlightsListIsNotEmptyThenEmptyMessageIsNotDisplayed() {
        val highlights = persistentListOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No saved highlights").assertDoesNotExist()
    }

    @Test
    fun whenHighlightsArePresentThenHighlightTextIsDisplayed() {
        val highlightText = "This is a test highlight"
        val highlights = persistentListOf(createTestHighlightUIState(text = highlightText))
        val uiState = ViewHighlightsUIState(highlights = highlights)

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithText(highlightText).assertIsDisplayed()
    }

    @Test
    fun whenHighlightsArePresentThenSavedTimestampIsDisplayed() {
        val savedOn = "2023-12-25"
        val highlights = persistentListOf(createTestHighlightUIState(savedOn = savedOn))
        val uiState = ViewHighlightsUIState(highlights = highlights)

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Saved on: $savedOn").assertIsDisplayed()
    }

    @Test
    fun whenHighlightsArePresentThenMoreOptionsButtonIsDisplayed() {
        val highlights = persistentListOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = {}
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
                    bookId = "test-book-id",
                    onAction = {}
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
                    bookId = "test-book-id",
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add highlight").assertIsDisplayed()
    }

    @Test
    fun whenBackButtonIsClickedThenOnBackPressedEventIsTriggered() {
        val uiState = ViewHighlightsUIState()
        var actionTriggered: ViewHighlightsAction? = null

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = { actionTriggered = it }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Go back").performClick()
        assertEquals(ViewHighlightsAction.OnBackPressed, actionTriggered)
    }

    @Test
    fun whenMoreOptionsIsClickedThenDropdownMenuIsDisplayed() {
        val highlights = persistentListOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = {}
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
        val highlights = persistentListOf(createTestHighlightUIState(id = highlightId))
        val uiState = ViewHighlightsUIState(highlights = highlights)
        var actionTriggered: ViewHighlightsAction? = null

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = { actionTriggered = it }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Options Menu").performClick()
        composeTestRule.onNodeWithTag("HighlightMenuItemEdit").performClick()
        assertTrue(actionTriggered is ViewHighlightsAction.OnEditHighlight)
        assertEquals(highlightId, (actionTriggered as ViewHighlightsAction.OnEditHighlight).highlightId)
    }

    @Test
    fun whenDeleteMenuItemIsClickedThenDeleteDialogIsDisplayed() {
        val highlights = persistentListOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Options Menu").performClick()
        composeTestRule.onNodeWithTag("HighlightMenuItemDelete").performClick()
        composeTestRule.onNodeWithText("Delete highlight").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to remove this highlight from your device?").assertIsDisplayed()
    }

    @Test
    fun whenDeleteDialogConfirmIsClickedThenOnDeleteIsCalled() {
        val highlightId = "test-id"
        val uiState = ViewHighlightsUIState(highlights = persistentListOf(createTestHighlightUIState(id = highlightId)))
        var actionTriggered: ViewHighlightsAction? = null

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = { actionTriggered = it }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Options Menu").performClick()
        composeTestRule.onNodeWithTag("HighlightMenuItemDelete").performClick()
        composeTestRule.onNodeWithTag("DeleteHighlightConfirmButton").performClick()
        assertTrue(actionTriggered is ViewHighlightsAction.OnDeleteHighlight)
        assertEquals(highlightId, (actionTriggered as ViewHighlightsAction.OnDeleteHighlight).highlightId)
    }

    @Test
    fun whenDeleteDialogCancelIsClickedThenDialogIsDismissed() {
        val highlights = persistentListOf(createTestHighlightUIState())
        val uiState = ViewHighlightsUIState(highlights = highlights)

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Options Menu").performClick()
        composeTestRule.onNodeWithTag("HighlightMenuItemDelete").performClick()
        composeTestRule.onNodeWithTag("DeleteHighlightCancelButton").performClick()
        composeTestRule.onNodeWithText("Delete highlight").assertDoesNotExist()
    }

    @Test
    fun whenCameraPermissionNeverRequestedThenFabClickRequestsPermission() {
        val uiState = ViewHighlightsUIState()
        var actionTriggered: ViewHighlightsAction? = null

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    onAction = { actionTriggered = it }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add highlight").performClick()
        composeTestRule.onNodeWithText("Camera access needed").assertDoesNotExist()
        composeTestRule.onNodeWithText("Camera access denied").assertDoesNotExist()
        assertEquals(ViewHighlightsAction.OnCameraPermissionGranted, actionTriggered)
    }

    @Test
    fun whenCameraPermissionDeniedOnceThenFabClickShowsRationaleDialog() {
        val uiState = ViewHighlightsUIState()

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    hasCameraPermissionBeenRequested = true,
                    shouldShowCameraPermissionRationale = { true },
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add highlight").performClick()
        composeTestRule.onNodeWithText("Camera access needed").assertIsDisplayed()
    }

    @Test
    fun whenRationaleDialogConfirmedThenDialogDismissesAndPermissionIsRequestedAgain() {
        val uiState = ViewHighlightsUIState()
        var actionTriggered: ViewHighlightsAction? = null

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    hasCameraPermissionBeenRequested = true,
                    shouldShowCameraPermissionRationale = { true },
                    onAction = { actionTriggered = it }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add highlight").performClick()
        composeTestRule.onNodeWithTag("CameraPermissionRationaleConfirmButton").performClick()
        composeTestRule.onNodeWithText("Camera access needed").assertDoesNotExist()
        assertEquals(ViewHighlightsAction.OnCameraPermissionGranted, actionTriggered)
    }

    @Test
    fun whenRationaleDialogCancelledThenDialogDismissesWithoutRequestingPermission() {
        val uiState = ViewHighlightsUIState()
        var actionTriggered: ViewHighlightsAction? = null

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    hasCameraPermissionBeenRequested = true,
                    shouldShowCameraPermissionRationale = { true },
                    onAction = { actionTriggered = it }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add highlight").performClick()
        composeTestRule.onNodeWithTag("CameraPermissionRationaleCancelButton").performClick()
        composeTestRule.onNodeWithText("Camera access needed").assertDoesNotExist()
        assertEquals(null, actionTriggered)
    }

    @Test
    fun whenCameraPermissionPermanentlyDeniedThenFabClickShowsSettingsDialog() {
        val uiState = ViewHighlightsUIState()

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    hasCameraPermissionBeenRequested = true,
                    shouldShowCameraPermissionRationale = { false },
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add highlight").performClick()
        composeTestRule.onNodeWithText("Camera access denied").assertIsDisplayed()
    }

    @Test
    fun whenSettingsDialogConfirmedThenOnOpenAppSettingsIsCalled() {
        val uiState = ViewHighlightsUIState()
        var settingsOpened = false

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    hasCameraPermissionBeenRequested = true,
                    shouldShowCameraPermissionRationale = { false },
                    onOpenAppSettings = { settingsOpened = true },
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add highlight").performClick()
        composeTestRule.onNodeWithTag("CameraPermissionSettingsConfirmButton").performClick()
        composeTestRule.onNodeWithText("Camera access denied").assertDoesNotExist()
        assertTrue(settingsOpened)
    }

    @Test
    fun whenSettingsDialogCancelledThenDialogDismissesWithoutOpeningSettings() {
        val uiState = ViewHighlightsUIState()
        var settingsOpened = false

        composeTestRule.setContent {
            AppTheme {
                ViewHighlights(
                    uiState = uiState,
                    bookId = "test-book-id",
                    hasCameraPermissionBeenRequested = true,
                    shouldShowCameraPermissionRationale = { false },
                    onOpenAppSettings = { settingsOpened = true },
                    onAction = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add highlight").performClick()
        composeTestRule.onNodeWithTag("CameraPermissionSettingsCancelButton").performClick()
        composeTestRule.onNodeWithText("Camera access denied").assertDoesNotExist()
        assertEquals(false, settingsOpened)
    }

    private fun createTestHighlightUIState(
        id: String = "test-id",
        text: String = "test text",
        savedOn: String = "2023-01-01"
    ) = HighlightUIState(
        id = id,
        text = text,
        savedOn = savedOn
    )
}
