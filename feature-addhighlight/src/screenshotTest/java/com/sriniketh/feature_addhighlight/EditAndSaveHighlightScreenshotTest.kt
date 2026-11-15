package com.sriniketh.feature_addhighlight

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.sriniketh.core_design.ui.preview.ScreenSizePreviews
import com.sriniketh.core_design.ui.preview.ScreenStatePreviews
import com.sriniketh.core_design.ui.preview.ScreenVariantPreviews
import com.sriniketh.core_design.ui.theme.AppTheme

private const val highlightText =
    "It is good to have an end to journey toward; but it is the journey that matters, in the end."

@PreviewTest
@ScreenSizePreviews
@ScreenVariantPreviews
@Composable
fun SaveHighlightWithRecognizedText() {
    AppTheme {
        EditAndSaveHighlight(
            uiState = EditAndSaveHighlightUiState(highlightText = highlightText),
            updateHighlightText = {},
            saveHighlight = {},
            goBack = {}
        )
    }
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun SaveHighlightRecognizingText() {
    AppTheme {
        EditAndSaveHighlight(
            uiState = EditAndSaveHighlightUiState(isLoading = true),
            updateHighlightText = {},
            saveHighlight = {},
            goBack = {}
        )
    }
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun EditHighlight() {
    AppTheme {
        EditAndSaveHighlight(
            uiState = EditAndSaveHighlightUiState(
                screenTitle = R.string.edit_highlight_title_text,
                highlightText = highlightText
            ),
            updateHighlightText = {},
            saveHighlight = {},
            goBack = {}
        )
    }
}
