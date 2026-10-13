package com.sriniketh.feature_bookshelf

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.sriniketh.core_design.ui.preview.ScreenSizePreviews
import com.sriniketh.core_design.ui.preview.ScreenStatePreviews
import com.sriniketh.core_design.ui.preview.ScreenVariantPreviews
import com.sriniketh.core_design.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf

private val books = persistentListOf(
    BookUIState(
        id = "1",
        title = "The Left Hand of Darkness",
        authors = persistentListOf("Ursula K. Le Guin"),
        thumbnailLink = null
    ),
    BookUIState(
        id = "2",
        title = "Good Omens",
        authors = persistentListOf("Terry Pratchett", "Neil Gaiman"),
        thumbnailLink = null
    ),
    BookUIState(
        id = "3",
        title = "Structure and Interpretation of Computer Programs",
        authors = persistentListOf("Harold Abelson", "Gerald Jay Sussman", "Julie Sussman"),
        thumbnailLink = null
    ),
    BookUIState(
        id = "4",
        title = "Piranesi",
        authors = persistentListOf("Susanna Clarke"),
        thumbnailLink = null
    )
)

@PreviewTest
@ScreenSizePreviews
@ScreenVariantPreviews
@Composable
fun BookshelfWithBooks() {
    AppTheme {
        Bookshelf(
            uiState = BookshelfUIState(books = books),
            goToSearch = {},
            goToHighlight = {}
        )
    }
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun BookshelfLoading() {
    AppTheme {
        Bookshelf(
            uiState = BookshelfUIState(isLoading = true),
            goToSearch = {},
            goToHighlight = {}
        )
    }
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun BookshelfEmpty() {
    AppTheme {
        Bookshelf(
            uiState = BookshelfUIState(),
            goToSearch = {},
            goToHighlight = {}
        )
    }
}
