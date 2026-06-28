package com.sriniketh.feature_searchbooks

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchBookScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenSearchBarIsDisplayedThenPlaceholderTextIsShown() {
        val uiState = BookSearchUiState()

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = {},
                    navigateToBookInfo = {},
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Search for a book").assertIsDisplayed()
    }

    @Test
    fun whenSearchBarIsDisplayedThenSearchIconIsShown() {
        val uiState = BookSearchUiState()

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = {},
                    navigateToBookInfo = {},
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Search icon").assertIsDisplayed()
    }

    @Test
    fun whenLoadingStateThenProgressIndicatorIsDisplayed() {
        val uiState = BookSearchUiState(isLoading = true)

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = {},
                    navigateToBookInfo = {},
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("SearchBookTextField").performClick()
        composeTestRule.onNodeWithTag("SearchBookLoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun whenBooksArePresentThenBookTitleIsDisplayed() {
        val books = persistentListOf(createTestBookUiState(title = "Test Book Title"))
        val uiState = BookSearchUiState(bookUiStates = books)

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = {},
                    navigateToBookInfo = {},
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("SearchBookTextField").performClick()
        composeTestRule.onNodeWithText("Test Book Title").assertIsDisplayed()
    }

    @Test
    fun whenBooksArePresentThenBookAuthorsAreDisplayed() {
        val books = persistentListOf(createTestBookUiState(authors = persistentListOf("Author One", "Author Two")))
        val uiState = BookSearchUiState(bookUiStates = books)

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = {},
                    navigateToBookInfo = {},
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("SearchBookTextField").performClick()
        composeTestRule.onNodeWithText("Author One, Author Two").assertIsDisplayed()
    }

    @Test
    fun whenBooksArePresentThenBookSubtitleIsDisplayed() {
        val books = persistentListOf(createTestBookUiState(subtitle = "Test Subtitle"))
        val uiState = BookSearchUiState(bookUiStates = books)

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = {},
                    navigateToBookInfo = {},
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("SearchBookTextField").performClick()
        composeTestRule.onNodeWithText("Test Subtitle").assertIsDisplayed()
    }

    @Test
    fun whenBookItemIsClickedThenNavigateToBookInfoIsCalled() {
        val bookId = "test-book-id"
        val books = persistentListOf(createTestBookUiState(id = bookId, title = "Test Book"))
        val uiState = BookSearchUiState(bookUiStates = books)
        var navigatedBookId: String? = null

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = {},
                    navigateToBookInfo = { navigatedBookId = it },
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("SearchBookTextField").performClick()
        composeTestRule.onNodeWithTag("SearchResultItem_test-book-id").performClick()
        assertEquals(bookId, navigatedBookId)
    }

    @Test
    fun whenSearchBarIsExpandedThenCloseIconIsDisplayed() {
        val uiState = BookSearchUiState()

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = {},
                    navigateToBookInfo = {},
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("SearchBookTextField").performClick()
        composeTestRule.onNodeWithContentDescription("Close icon").assertIsDisplayed()
    }

    @Test
    fun whenSearchTextIsEnteredThenSearchForBooksIsCalled() {
        val uiState = BookSearchUiState()
        var searchQuery: String? = null

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = { searchQuery = it },
                    navigateToBookInfo = {},
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("SearchBookTextField").performClick()
        composeTestRule.onNodeWithTag("SearchBookTextField").performTextInput("test query")

        assertEquals("test query", searchQuery)
    }

    @Test
    fun whenCloseIconIsClickedWithTextThenResetSearchIsCalled() {
        val uiState = BookSearchUiState()
        var resetSearchCalled = false

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = {},
                    navigateToBookInfo = {},
                    resetSearch = { resetSearchCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("SearchBookTextField").performClick()
        composeTestRule.onNodeWithTag("SearchBookTextField").performTextInput("test")
        composeTestRule.onNodeWithContentDescription("Close icon").performClick()

        assertTrue(resetSearchCalled)
    }

    @Test
    fun whenNoBooksArePresentThenNoBookItemsAreDisplayed() {
        val uiState = BookSearchUiState(bookUiStates = persistentListOf())

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState,
                    searchForBooks = {},
                    navigateToBookInfo = {},
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Book Title").assertIsNotDisplayed()
    }

    @Test
    fun whenNewResultsArriveThenTheListIsShownFromTheTop() {
        val sharedBooks = (0 until 20).map { index ->
            createTestBookUiState(id = "shared-$index", title = "Shared Book $index")
        }.toPersistentList()
        val newTopBooks = (0 until 5).map { index ->
            createTestBookUiState(id = "new-$index", title = "New Top Book $index")
        }.toPersistentList()
        val uiState = mutableStateOf(BookSearchUiState(bookUiStates = sharedBooks))

        composeTestRule.setContent {
            AppTheme {
                SearchBook(
                    uiState = uiState.value,
                    searchForBooks = {},
                    navigateToBookInfo = {},
                    resetSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("SearchBookTextField").performClick()
        composeTestRule.onNodeWithTag("SearchResultsList").performScrollToIndex(19)
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            uiState.value = BookSearchUiState(bookUiStates = (newTopBooks + sharedBooks).toPersistentList())
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("New Top Book 0").assertIsDisplayed()
    }

    private fun createTestBookUiState(
        id: String = "test-id",
        title: String = "Test Title",
        subtitle: String? = "Test Subtitle",
        authors: ImmutableList<String> = persistentListOf("Test Author"),
        thumbnailLink: String? = null
    ) = BookUiState(
        id = id,
        title = title,
        subtitle = subtitle,
        authors = authors,
        thumbnailLink = thumbnailLink
    )
}
