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
        val epochMillis = 1_703_500_200_000L
        val highlight = createTestHighlight(savedOnEpochMillis = epochMillis)
        val highlightEntity = highlight.asHighlightEntity()
        assertEquals(epochMillis, highlightEntity.savedOnEpochMillis)
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
        val epochMillis = 1_705_337_100_000L
        val highlightEntity = createTestHighlightEntity(savedOnEpochMillis = epochMillis)
        val highlight = highlightEntity.asHighlight()
        assertEquals(epochMillis, highlight.savedOnEpochMillis)
    }

    private fun createTestHighlight(
        id: String = "test-id",
        bookId: String = "test-book-id",
        text: String = "test text",
        savedOnEpochMillis: Long = 1_672_531_200_000L
    ) = Highlight(
        id = id,
        bookId = bookId,
        text = text,
        savedOnEpochMillis = savedOnEpochMillis
    )

    private fun createTestHighlightEntity(
        id: String = "test-id",
        bookId: String = "test-book-id",
        text: String = "test text",
        savedOnEpochMillis: Long = 1_672_531_200_000L
    ) = HighlightEntity(
        id = id,
        bookId = bookId,
        text = text,
        savedOnEpochMillis = savedOnEpochMillis
    )
}
