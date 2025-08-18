package com.sriniketh.feature_bookshelf

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookshelfScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenUIStateIsLoadingThenProgressIndicatorIsDisplayed() {
        val uiState = BookshelfUIState(isLoading = true)

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("BookshelfLoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun whenBookshelfScreenIsDisplayedThenPageTitleShowsBookshelf() {
        val uiState = BookshelfUIState()

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Bookshelf").assertIsDisplayed()
    }

    @Test
    fun whenFloatingActionButtonIsDisplayedThenSearchIconIsShown() {
        val uiState = BookshelfUIState()

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("search for a book button").assertIsDisplayed()
    }

    @Test
    fun whenFloatingActionButtonIsClickedThenGoToSearchIsCalled() {
        val uiState = BookshelfUIState()
        var goToSearchCalled = false

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = { goToSearchCalled = true },
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("search for a book button").performClick()
        assertTrue(goToSearchCalled)
    }

    @Test
    fun whenBooksListIsEmptyAndNotLoadingThenEmptyMessageIsDisplayed() {
        val uiState = BookshelfUIState(isLoading = false, books = emptyList())

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Go ahead, grab a book!").assertIsDisplayed()
    }

    @Test
    fun whenBooksListIsNotEmptyThenEmptyMessageIsNotDisplayed() {
        val books = listOf(createTestBookUIState())
        val uiState = BookshelfUIState(books = books)

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Go ahead, grab a book!").assertIsNotDisplayed()
    }

    @Test
    fun whenBooksArePresentThenBookTitleIsDisplayed() {
        val books = listOf(createTestBookUIState(title = "Test Book Title"))
        val uiState = BookshelfUIState(books = books)

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Book Title").assertIsDisplayed()
    }

    @Test
    fun whenBooksArePresentThenBookAuthorsAreDisplayed() {
        val books = listOf(createTestBookUIState(authors = listOf("Author One", "Author Two")))
        val uiState = BookshelfUIState(books = books)

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Author One, Author Two").assertIsDisplayed()
    }

    @Test
    fun whenBookItemIsClickedThenGoToHighlightIsCalled() {
        val bookId = "test-book-id"
        val books = listOf(createTestBookUIState(id = bookId, title = "Test Book"))
        val uiState = BookshelfUIState(books = books)
        var calledBookId: String? = null

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = {},
                    goToHighlight = { calledBookId = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Test Book").performClick()
        assertTrue(calledBookId == bookId)
    }

    @Test
    fun whenNoBooksArePresentThenNoBookItemsAreDisplayed() {
        val uiState = BookshelfUIState(books = emptyList())

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Book Title").assertIsNotDisplayed()
    }

    private fun createTestBookUIState(
        id: String = "test-id",
        title: String = "Test Title",
        authors: List<String> = listOf("Test Author"),
        thumbnailLink: String? = null
    ) = BookUIState(
        id = id,
        title = title,
        authors = authors,
        thumbnailLink = thumbnailLink,
        viewBook = {}
    )
}
