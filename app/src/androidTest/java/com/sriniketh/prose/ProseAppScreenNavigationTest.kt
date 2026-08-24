package com.sriniketh.prose

import android.content.Context
import android.view.KeyEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sriniketh.core_db.BookDatabase
import com.sriniketh.core_db.entity.BookEntity
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

private const val BOOK_DATABASE_NAME = "book-db"

private class SeededBookRule : ExternalResource() {

    val book = BookEntity(
        id = "prose-app-screen-navigation-test-book-${System.nanoTime()}",
        title = "Navigation Test Book",
        subtitle = null,
        authors = listOf("Navigation Test Author"),
        thumbnailLink = null,
        publisher = null,
        publishedDate = null,
        description = null,
        pageCount = null,
        averageRating = null,
        ratingsCount = null
    )

    override fun before() {
        withDatabase { it.bookDao().insertBook(book) }
    }

    override fun after() {
        withDatabase { it.bookDao().deleteBook(book) }
    }

    private fun withDatabase(action: suspend (BookDatabase) -> Unit) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.databaseBuilder(context, BookDatabase::class.java, BOOK_DATABASE_NAME).build()
        try {
            runBlocking { action(database) }
        } finally {
            database.close()
        }
    }
}

@RunWith(AndroidJUnit4::class)
class ProseAppScreenNavigationTest {

    private val seededBookRule = SeededBookRule()
    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(seededBookRule).around(composeTestRule)

    private val seededBookId get() = seededBookRule.book.id

    @Test
    fun whenSearchButtonIsClickedThenSearchScreenIsDisplayed() {
        composeTestRule.onNodeWithContentDescription("search for a book button").performClick()

        composeTestRule.onNodeWithTag("SearchBookTextField").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bookshelf").assertDoesNotExist()
    }

    @Test
    fun whenBackIsPressedFromSearchScreenThenBookshelfScreenIsDisplayedAgain() {
        composeTestRule.onNodeWithContentDescription("search for a book button").performClick()
        composeTestRule.onNodeWithTag("SearchBookTextField").assertIsDisplayed()

        pressSystemBackUntilBookshelfIsDisplayed()

        composeTestRule.onNodeWithContentDescription("search for a book button").assertIsDisplayed()
    }

    @Test
    fun whenASeededBookItemIsClickedThenViewHighlightsScreenIsDisplayedForThatBook() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeTestRule.onNodeWithTag("BookItem_$seededBookId").assertIsDisplayed() }.isSuccess
        }

        composeTestRule.onNodeWithTag("BookItem_$seededBookId").performClick()

        composeTestRule.onNodeWithText("Saved highlights").assertIsDisplayed()
        composeTestRule.onNodeWithText("No saved highlights").assertIsDisplayed()
    }

    @Test
    fun whenBackIsPressedFromViewHighlightsScreenThenBookshelfScreenIsDisplayedAgain() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            runCatching { composeTestRule.onNodeWithTag("BookItem_$seededBookId").assertIsDisplayed() }.isSuccess
        }
        composeTestRule.onNodeWithTag("BookItem_$seededBookId").performClick()
        composeTestRule.onNodeWithText("Saved highlights").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Go back").performClick()

        composeTestRule.onNodeWithText("Bookshelf").assertIsDisplayed()
        composeTestRule.onNodeWithTag("BookItem_$seededBookId").assertIsDisplayed()
    }

    private fun pressSystemBackUntilBookshelfIsDisplayed(maxPresses: Int = 5) {
        var presses = 0
        while (presses < maxPresses &&
            runCatching { composeTestRule.onNodeWithText("Bookshelf").assertIsDisplayed() }.isFailure
        ) {
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            composeTestRule.waitForIdle()
            presses++
        }
        composeTestRule.onNodeWithText("Bookshelf").assertIsDisplayed()
    }
}
