package com.sriniketh.feature_bookshelf

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.ui.theme.AppTheme
import com.sriniketh.feature_bookshelf.fakes.FakeBooksRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        val uiState = BookshelfUIState(isLoading = false, books = persistentListOf())

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
        val books = persistentListOf(createTestBookUIState())
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

        composeTestRule.onNodeWithText("Go ahead, grab a book!").assertDoesNotExist()
    }

    @Test
    fun whenBooksArePresentThenBookTitleIsDisplayed() {
        val books = persistentListOf(createTestBookUIState(title = "Test Book Title"))
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
        val books = persistentListOf(createTestBookUIState(authors = persistentListOf("Author One", "Author Two")))
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
        val books = persistentListOf(createTestBookUIState(id = bookId, title = "Test Book"))
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

        composeTestRule.onNodeWithTag("BookItem_test-book-id").performClick()
        assertTrue(calledBookId == bookId)
    }

    @Test
    fun whenMultipleBooksArePresentThenAllBookTitlesAreDisplayed() {
        val books = persistentListOf(
            createTestBookUIState(id = "book-1", title = "First Book Title"),
            createTestBookUIState(id = "book-2", title = "Second Book Title")
        )
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

        composeTestRule.onNodeWithText("First Book Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second Book Title").assertIsDisplayed()
    }

    @Test
    fun whenMultipleBooksArePresentThenClickingSecondBookItemCallsGoToHighlightWithCorrectId() {
        val books = persistentListOf(
            createTestBookUIState(id = "book-1", title = "First Book Title"),
            createTestBookUIState(id = "book-2", title = "Second Book Title")
        )
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

        composeTestRule.onNodeWithTag("BookItem_book-2").performClick()
        assertEquals("book-2", calledBookId)
    }

    @Test
    fun whenSnackbarHostStateShowsMessageThenMessageIsDisplayed() {
        val uiState = BookshelfUIState()

        composeTestRule.setContent {
            AppTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar("Book added to shelf")
                }
                Bookshelf(
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Book added to shelf").assertIsDisplayed()
    }

    @Test
    fun whenNoBooksArePresentThenNoBookItemsAreDisplayed() {
        val uiState = BookshelfUIState(books = persistentListOf())

        composeTestRule.setContent {
            AppTheme {
                Bookshelf(
                    uiState = uiState,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Book Title").assertDoesNotExist()
    }

    @Test
    fun whenBookshelfScreenIsDisplayedThenBooksFromViewModelAreShown() {
        val fakeBooksRepository = FakeBooksRepository()
        val viewModel = BookshelfViewModel(fakeBooksRepository, SavedStateHandle())

        composeTestRule.setContent {
            AppTheme {
                BookshelfScreen(
                    viewModel = viewModel,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
    }

    @Test
    fun whenBookAddedFlagIsSetThenBookshelfScreenShowsAddedSnackbarMessage() {
        val fakeBooksRepository = FakeBooksRepository()
        val savedStateHandle = SavedStateHandle(mapOf(BOOKSHELF_SHOW_ADDED_MESSAGE to true))
        val viewModel = BookshelfViewModel(fakeBooksRepository, savedStateHandle)

        composeTestRule.setContent {
            AppTheme {
                BookshelfScreen(
                    viewModel = viewModel,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Book has been added to shelf!").assertIsDisplayed()
    }

    @Test
    fun whenLoadingBooksFailsThenBookshelfScreenShowsErrorSnackbarMessage() {
        val fakeBooksRepository = FakeBooksRepository().apply {
            shouldGetAllSavedBooksFromDbThrowException = true
        }
        val viewModel = BookshelfViewModel(fakeBooksRepository, SavedStateHandle())

        composeTestRule.setContent {
            AppTheme {
                BookshelfScreen(
                    viewModel = viewModel,
                    goToSearch = {},
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Error retrieving saved books.").assertIsDisplayed()
    }

    @Test
    fun whenFabIsClickedOnBookshelfScreenThenGoToSearchLambdaIsInvoked() {
        val fakeBooksRepository = FakeBooksRepository()
        val viewModel = BookshelfViewModel(fakeBooksRepository, SavedStateHandle())
        var goToSearchCalled = false

        composeTestRule.setContent {
            AppTheme {
                BookshelfScreen(
                    viewModel = viewModel,
                    goToSearch = { goToSearchCalled = true },
                    goToHighlight = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("search for a book button").performClick()
        assertTrue(goToSearchCalled)
    }

    @Test
    fun whenBookItemIsClickedOnBookshelfScreenThenGoToHighlightLambdaIsInvokedWithBookId() {
        val fakeBooksRepository = FakeBooksRepository()
        val viewModel = BookshelfViewModel(fakeBooksRepository, SavedStateHandle())
        var calledBookId: String? = null

        composeTestRule.setContent {
            AppTheme {
                BookshelfScreen(
                    viewModel = viewModel,
                    goToSearch = {},
                    goToHighlight = { calledBookId = it }
                )
            }
        }

        composeTestRule.waitForIdle()
        assertNull(calledBookId)

        composeTestRule.onNodeWithTag("BookItem_test-id").performClick()
        assertEquals("test-id", calledBookId)
    }

    private fun createTestBookUIState(
        id: String = "test-id",
        title: String = "Test Title",
        authors: ImmutableList<String> = persistentListOf("Test Author"),
        thumbnailLink: String? = null
    ) = BookUIState(
        id = id,
        title = title,
        authors = authors,
        thumbnailLink = thumbnailLink
    )
}
