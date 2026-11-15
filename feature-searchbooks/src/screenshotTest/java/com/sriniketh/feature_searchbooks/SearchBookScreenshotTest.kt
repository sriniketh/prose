package com.sriniketh.feature_searchbooks

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.sriniketh.core_design.ui.preview.ScreenSizePreviews
import com.sriniketh.core_design.ui.preview.ScreenStatePreviews
import com.sriniketh.core_design.ui.preview.ScreenVariantPreviews
import com.sriniketh.core_design.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf

private val results = persistentListOf(
    BookUiState(
        id = "1",
        title = "The Left Hand of Darkness",
        subtitle = "50th Anniversary Edition",
        authors = persistentListOf("Ursula K. Le Guin"),
        thumbnailLink = null
    ),
    BookUiState(
        id = "2",
        title = "The Dispossessed",
        subtitle = "An Ambiguous Utopia",
        authors = persistentListOf("Ursula K. Le Guin"),
        thumbnailLink = null
    ),
    BookUiState(
        id = "3",
        title = "A Wizard of Earthsea",
        subtitle = null,
        authors = persistentListOf("Ursula K. Le Guin", "Charles Vess"),
        thumbnailLink = null
    )
)

@PreviewTest
@ScreenSizePreviews
@ScreenVariantPreviews
@Composable
fun SearchBookWithResults() {
    AppTheme {
        SearchBook(
            uiState = BookSearchUiState(bookUiStates = results),
            searchForBooks = {},
            navigateToBookInfo = {},
            resetSearch = {},
            initialQuery = "le guin",
            initiallyExpanded = true
        )
    }
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun SearchBookLoading() {
    AppTheme {
        SearchBook(
            uiState = BookSearchUiState(isLoading = true),
            searchForBooks = {},
            navigateToBookInfo = {},
            resetSearch = {},
            initialQuery = "le guin",
            initiallyExpanded = true
        )
    }
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun SearchBookIdle() {
    AppTheme {
        SearchBook(
            uiState = BookSearchUiState(),
            searchForBooks = {},
            navigateToBookInfo = {},
            resetSearch = {}
        )
    }
}
