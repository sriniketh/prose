package com.sriniketh.feature_searchbooks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.ui.theme.AppTheme
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookInfoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenLoadingStateThenProgressIndicatorIsDisplayed() {
        val uiState = BookInfoUiState(isLoading = true)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("BookInfoLoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun whenBookIsLoadedThenBookTitleIsDisplayed() {
        val book = createTestBook(title = "Test Book Title")
        val uiState = BookInfoUiState(book = book)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Book Title").assertIsDisplayed()
    }

    @Test
    fun whenBookIsLoadedThenAuthorsAreDisplayed() {
        val book = createTestBook(authors = listOf("Author One", "Author Two"))
        val uiState = BookInfoUiState(book = book)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Author One, Author Two").assertIsDisplayed()
    }

    @Test
    fun whenBookIsLoadedThenPublisherIsDisplayed() {
        val book = createTestBook(publisher = "Test Publisher")
        val uiState = BookInfoUiState(book = book)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Publisher").assertIsDisplayed()
    }

    @Test
    fun whenBookIsLoadedThenDescriptionIsDisplayed() {
        val book = createTestBook(description = "This is a test book description")
        val uiState = BookInfoUiState(book = book)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("This is a test book description").assertIsDisplayed()
    }

    @Test
    fun whenBookIsLoadedThenPageCountIsDisplayed() {
        val book = createTestBook(pageCount = 250)
        val uiState = BookInfoUiState(book = book)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Number of pages: 250", substring = true).assertIsDisplayed()
    }

    @Test
    fun whenBookIsLoadedThenRatingsAreDisplayed() {
        val book = createTestBook(averageRating = 4.5, ratingsCount = 123)
        val uiState = BookInfoUiState(book = book)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Ratings: 4.50/5.0 out of 123 reviews").assertIsDisplayed()
    }

    @Test
    fun whenCanAddToShelfThenFloatingActionButtonIsDisplayed() {
        val book = createTestBook()
        val uiState = BookInfoUiState(book = book, canAddToShelf = true)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add to shelf").assertIsDisplayed()
    }

    @Test
    fun whenCannotAddToShelfThenFloatingActionButtonIsHidden() {
        val book = createTestBook()
        val uiState = BookInfoUiState(book = book, canAddToShelf = false)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add to shelf").assertIsNotDisplayed()
    }

    @Test
    fun whenFloatingActionButtonIsClickedThenAddBookToShelfIsCalled() {
        val book = createTestBook()
        var addToShelfCalled = false
        val uiState = BookInfoUiState(
            book = book,
            canAddToShelf = true,
            addBookToShelf = { addToShelfCalled = true }
        )

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add to shelf").performClick()
        assertTrue(addToShelfCalled)
    }

    @Test
    fun whenBackButtonIsClickedThenGoBackIsCalled() {
        val book = createTestBook()
        val uiState = BookInfoUiState(book = book)
        var goBackCalled = false

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = { goBackCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Go back").performClick()
        assertTrue(goBackCalled)
    }

    @Test
    fun whenNoBookIsLoadedThenDefaultTitleIsDisplayed() {
        val uiState = BookInfoUiState(book = null)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Book info").assertIsDisplayed()
    }

    @Test
    fun whenBookHasPublishedDateThenPublishedDateIsDisplayed() {
        val book = createTestBook(publishedDate = "2023-01-01")
        val uiState = BookInfoUiState(book = book)

        composeTestRule.setContent {
            AppTheme {
                BookInfo(
                    uiState = uiState,
                    goBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("2023-01-01", substring = true).assertIsDisplayed()
    }

    private fun createTestBook(
        id: String = "test-id",
        title: String = "Test Title",
        authors: List<String> = listOf("Test Author"),
        publisher: String? = "Test Publisher",
        description: String? = "Test Description",
        pageCount: Int? = 200,
        averageRating: Double? = 4.0,
        ratingsCount: Int? = 100,
        publishedDate: String? = "2023"
    ) = Book(
        id = id,
        info = BookInfo(
            title = title,
            subtitle = "Test Subtitle",
            authors = authors,
            thumbnailLink = null,
            publisher = publisher,
            publishedDate = publishedDate,
            description = description,
            pageCount = pageCount,
            averageRating = averageRating,
            ratingsCount = ratingsCount
        )
    )
}
