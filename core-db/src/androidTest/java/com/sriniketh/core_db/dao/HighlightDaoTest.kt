package com.sriniketh.core_db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_db.BookDatabase
import com.sriniketh.core_db.entity.BookEntity
import com.sriniketh.core_db.entity.HighlightEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HighlightDaoTest {

    private lateinit var database: BookDatabase
    private lateinit var bookDao: BookDao
    private lateinit var highlightDao: HighlightDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BookDatabase::class.java
        ).build()
        bookDao = database.bookDao()
        highlightDao = database.highlightDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createBook(id: String = "book-1") = BookEntity(
        id = id,
        title = "Dune",
        subtitle = "subtitle",
        authors = listOf("Frank Herbert"),
        thumbnailLink = "https://example.com/thumbnail.png",
        publisher = "Chilton Books",
        publishedDate = "1965",
        description = "A science fiction novel",
        pageCount = 412,
        averageRating = 4.5,
        ratingsCount = 100
    )

    private fun createHighlight(
        id: String = "highlight-1",
        bookId: String = "book-1",
        text: String = "Fear is the mind-killer."
    ) = HighlightEntity(
        id = id,
        bookId = bookId,
        text = text,
        savedOnTimestamp = "2026-08-23T00:00:00Z"
    )

    private suspend fun insertBook(id: String = "book-1") {
        bookDao.insertBook(createBook(id))
    }

    @Test
    fun insertHighlightThenGetHighlightByIdReturnsSameHighlight() = runBlocking {
        insertBook()
        val highlight = createHighlight()

        highlightDao.insertHighlight(highlight)

        assertEquals(highlight, highlightDao.getHighlightById(highlight.id))
    }

    @Test
    fun insertHighlightWithDuplicateIdReplacesExistingHighlight() = runBlocking {
        insertBook()
        val original = createHighlight(text = "Original text")
        val replacement = createHighlight(text = "Replacement text")

        highlightDao.insertHighlight(original)
        highlightDao.insertHighlight(replacement)

        assertEquals(replacement, highlightDao.getHighlightById(original.id))
    }

    @Test
    fun getAllHighlightsForBookFiltersByBookId() = runBlocking {
        insertBook("book-1")
        insertBook("book-2")
        val highlightForBookOne = createHighlight(id = "highlight-1", bookId = "book-1")
        val otherHighlightForBookOne = createHighlight(id = "highlight-2", bookId = "book-1")
        val highlightForBookTwo = createHighlight(id = "highlight-3", bookId = "book-2")

        highlightDao.insertHighlight(highlightForBookOne)
        highlightDao.insertHighlight(otherHighlightForBookOne)
        highlightDao.insertHighlight(highlightForBookTwo)

        val highlightsForBookOne = highlightDao.getAllHighlightsForBook("book-1").first()
        assertEquals(
            setOf(highlightForBookOne, otherHighlightForBookOne),
            highlightsForBookOne.toSet()
        )
    }

    @Test
    fun getAllHighlightsForBookReturnsEmptyListWhenNoneMatch() = runBlocking {
        insertBook()

        val highlights = highlightDao.getAllHighlightsForBook("book-1").first()

        assertTrue(highlights.isEmpty())
    }

    @Test
    fun deleteHighlightByIdRemovesHighlight() = runBlocking {
        insertBook()
        val highlight = createHighlight()
        highlightDao.insertHighlight(highlight)

        highlightDao.deleteHighlightById(highlight.id)

        assertNull(highlightDao.getHighlightById(highlight.id))
    }
}
