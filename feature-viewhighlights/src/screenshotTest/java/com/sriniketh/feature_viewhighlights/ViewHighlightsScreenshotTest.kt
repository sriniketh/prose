package com.sriniketh.feature_viewhighlights

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.sriniketh.core_design.ui.preview.ScreenSizePreviews
import com.sriniketh.core_design.ui.preview.ScreenStatePreviews
import com.sriniketh.core_design.ui.preview.ScreenVariantPreviews
import com.sriniketh.core_design.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf

private val highlights = persistentListOf(
    HighlightUIState(
        id = "1",
        text = "Light is the left hand of darkness, and darkness the right hand of light.",
        savedOn = "Mar 1, 2026"
    ),
    HighlightUIState(
        id = "2",
        text = "It is good to have an end to journey toward; but it is the journey that matters, in the end. " +
            "The unknown, the unforetold, the unproven, that is what life is based on. Ignorance is the ground " +
            "of thought. Unproof is the ground of action. If it were proven that there is no God there would be no religion.",
        savedOn = "Mar 4, 2026"
    ),
    HighlightUIState(
        id = "3",
        text = "To learn which questions are unanswerable, and not to answer them.",
        savedOn = "Mar 9, 2026"
    )
)

@PreviewTest
@ScreenSizePreviews
@ScreenVariantPreviews
@Composable
fun ViewHighlightsWithHighlights() {
    AppTheme {
        ViewHighlights(
            uiState = ViewHighlightsUIState(highlights = highlights),
            bookId = "book",
            onAction = {}
        )
    }
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun ViewHighlightsLoading() {
    AppTheme {
        ViewHighlights(
            uiState = ViewHighlightsUIState(isLoading = true),
            bookId = "book",
            onAction = {}
        )
    }
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun ViewHighlightsEmpty() {
    AppTheme {
        ViewHighlights(
            uiState = ViewHighlightsUIState(),
            bookId = "book",
            onAction = {}
        )
    }
}
