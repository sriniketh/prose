package com.sriniketh.core_data.transformers

import com.sriniketh.core_db.entity.HighlightEntity
import com.sriniketh.core_models.book.Highlight
import org.junit.Assert.assertEquals
import org.junit.Test

class HighlightTransformersTest {

    @Test
    fun `when converting Highlight to HighlightEntity then id is mapped correctly`() {
        val highlight = createTestHighlight(id = "test-highlight-id")
        val highlightEntity = highlight.asHighlightEntity()
        assertEquals("test-highlight-id", highlightEntity.id)
    }

    @Test
    fun `when converting Highlight to HighlightEntity then book id is mapped correctly`() {
        val highlight = createTestHighlight(bookId = "test-book-id")
        val highlightEntity = highlight.asHighlightEntity()
        assertEquals("test-book-id", highlightEntity.bookId)
    }

    @Test
    fun `when converting Highlight to HighlightEntity then text is mapped correctly`() {
        val text = "This is a test highlight text"
        val highlight = createTestHighlight(text = text)
        val highlightEntity = highlight.asHighlightEntity()
        assertEquals(text, highlightEntity.text)
    }

    @Test
    fun `when converting Highlight to HighlightEntity then saved timestamp is mapped correctly`() {
        val timestamp = "2023-12-25 10:30 AM"
        val highlight = createTestHighlight(savedOnTimestamp = timestamp)
        val highlightEntity = highlight.asHighlightEntity()
        assertEquals(timestamp, highlightEntity.savedOnTimestamp)
    }

    @Test
    fun `when converting HighlightEntity to Highlight then id is mapped correctly`() {
        val highlightEntity = createTestHighlightEntity(id = "entity-highlight-id")
        val highlight = highlightEntity.asHighlight()
        assertEquals("entity-highlight-id", highlight.id)
    }

    @Test
    fun `when converting HighlightEntity to Highlight then book id is mapped correctly`() {
        val highlightEntity = createTestHighlightEntity(bookId = "entity-book-id")
        val highlight = highlightEntity.asHighlight()
        assertEquals("entity-book-id", highlight.bookId)
    }

    @Test
    fun `when converting HighlightEntity to Highlight then text is mapped correctly`() {
        val text = "Entity highlight text content"
        val highlightEntity = createTestHighlightEntity(text = text)
        val highlight = highlightEntity.asHighlight()
        assertEquals(text, highlight.text)
    }

    @Test
    fun `when converting HighlightEntity to Highlight then saved timestamp is mapped correctly`() {
        val timestamp = "2024-01-15 03:45 PM"
        val highlightEntity = createTestHighlightEntity(savedOnTimestamp = timestamp)
        val highlight = highlightEntity.asHighlight()
        assertEquals(timestamp, highlight.savedOnTimestamp)
    }

    private fun createTestHighlight(
        id: String = "test-id",
        bookId: String = "test-book-id",
        text: String = "test text",
        savedOnTimestamp: String = "test timestamp"
    ) = Highlight(
        id = id,
        bookId = bookId,
        text = text,
        savedOnTimestamp = savedOnTimestamp
    )

    private fun createTestHighlightEntity(
        id: String = "test-id",
        bookId: String = "test-book-id",
        text: String = "test text",
        savedOnTimestamp: String = "test timestamp"
    ) = HighlightEntity(
        id = id,
        bookId = bookId,
        text = text,
        savedOnTimestamp = savedOnTimestamp
    )
}
