package com.sriniketh.feature_addhighlight

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.ui.theme.AppTheme
import com.sriniketh.feature_addhighlight.ocr.LineSelection
import com.sriniketh.feature_addhighlight.ocr.OcrLine
import com.sriniketh.feature_addhighlight.ocr.OcrPage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SelectTextScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val page = OcrPage(
        width = 1000,
        height = 1000,
        lines = listOf(
            "First paragraph opens here,",
            "and it ends here.",
            "Second paragraph stands alone."
        ).mapIndexed { index, text ->
            val top = 200f + index * 200f
            OcrLine(
                text = text,
                blockIndex = if (index < 2) 0 else 1,
                corners = listOf(
                    Offset(100f, top),
                    Offset(900f, top),
                    Offset(900f, top + 100f),
                    Offset(100f, top + 100f)
                )
            )
        }
    )

    private var continuedWith: String? = null

    private fun setContent() {
        composeTestRule.setContent {
            var uiState by mutableStateOf(
                CaptureAndSelectTextUiState.Selecting(photo = ImageBitmap(1000, 1000), page = page)
            )
            fun select(transform: (LineSelection) -> LineSelection) {
                uiState = uiState.copy(selection = transform(uiState.selection))
            }
            AppTheme {
                SelectText(
                    uiState = uiState,
                    onToggleBlock = { block -> select { it.toggle(block) } },
                    onStartDrag = { line -> select { it.startDrag(line) } },
                    onDragTo = { line -> select { it.dragTo(line) } },
                    onEndDrag = { select { it.endDrag() } },
                    onRetake = {},
                    onContinue = { continuedWith = it },
                    goBack = {}
                )
            }
        }
    }

    private fun blockCenter(text: String): Offset =
        composeTestRule.onNodeWithContentDescription(text).fetchSemanticsNode().boundsInRoot.center

    @Test
    fun whenNothingIsSelectedThenContinueIsDisabledAndTheHintIsShown() {
        setContent()

        composeTestRule.onNodeWithTag("ContinueWithSelectionButton").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Tap a paragraph or swipe across lines to select text.")
            .assertExists()
    }

    @Test
    fun whenAParagraphIsTappedThenItIsSelectedAndContinueSendsItsText() {
        setContent()
        val paragraph = "First paragraph opens here, and it ends here."

        composeTestRule.onRoot().performTouchInput { click(blockCenter(paragraph)) }

        composeTestRule.onNodeWithContentDescription(paragraph).assertIsSelected()
        composeTestRule.onNodeWithTag("ContinueWithSelectionButton").assertIsEnabled().performClick()
        assertEquals(paragraph, continuedWith)
    }

    @Test
    fun whenAParagraphIsClickedByAccessibilityThenItIsSelected() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Second paragraph stands alone.")
            .performSemanticsAction(SemanticsActions.OnClick)

        composeTestRule.onNodeWithContentDescription("Second paragraph stands alone.").assertIsSelected()
    }

    @Test
    fun whenSwipingAcrossLinesThenEveryLineBetweenIsSelectedInReadingOrder() {
        setContent()
        val firstParagraph = blockCenter("First paragraph opens here, and it ends here.")
        val secondParagraph = blockCenter("Second paragraph stands alone.")

        composeTestRule.onRoot().performTouchInput {
            swipe(start = secondParagraph, end = firstParagraph - Offset(0f, 60f), durationMillis = 500)
        }
        composeTestRule.onNodeWithTag("ContinueWithSelectionButton").performClick()

        assertEquals(
            "First paragraph opens here, and it ends here.\nSecond paragraph stands alone.",
            continuedWith
        )
    }
}
