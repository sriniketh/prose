package com.sriniketh.feature_searchbooks

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.sriniketh.core_design.ui.preview.ScreenSizePreviews
import com.sriniketh.core_design.ui.preview.ScreenStatePreviews
import com.sriniketh.core_design.ui.preview.ScreenVariantPreviews
import com.sriniketh.core_design.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf

private val book = BookInfoUiData(
    title = "The Left Hand of Darkness",
    authors = persistentListOf("Ursula K. Le Guin"),
    thumbnailLink = null,
    publisher = "Ace Books",
    publishedDate = "1969-03-01",
    description = "<p>A lone human ambassador is sent to <b>Winter</b>, an alien world whose inhabitants can choose and change their gender.</p>",
    pageCount = 304,
    averageRating = 4.1,
    ratingsCount = 2873
)

@PreviewTest
@ScreenSizePreviews
@ScreenVariantPreviews
@Composable
fun BookInfoCanAddToShelf() {
    AppTheme {
        BookInfo(
            uiState = BookInfoUiState(book = book, canAddToShelf = true),
            goBack = {}
        )
    }
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun BookInfoAlreadyOnShelf() {
    AppTheme {
        BookInfo(
            uiState = BookInfoUiState(book = book, canAddToShelf = false),
            goBack = {}
        )
    }
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun BookInfoLoading() {
    AppTheme {
        BookInfo(
            uiState = BookInfoUiState(isLoading = true),
            goBack = {}
        )
    }
}
