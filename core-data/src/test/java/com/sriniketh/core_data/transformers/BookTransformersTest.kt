package com.sriniketh.core_data.transformers

import com.sriniketh.core_db.entity.BookEntity
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class BookTransformersTest {

    @Test
    fun `when converting Book to BookEntity then id is mapped correctly`() {
        val book = createTestBook(id = "test-id")
        val bookEntity = book.asBookEntity()
        assertEquals("test-id", bookEntity.id)
    }

    @Test
    fun `when converting Book to BookEntity then title is mapped correctly`() {
        val book = createTestBook(title = "Specific Title")
        val bookEntity = book.asBookEntity()
        assertEquals("Specific Title", bookEntity.title)
    }

    @Test
    fun `when converting Book to BookEntity then subtitle is mapped correctly`() {
        val book = createTestBook(subtitle = "Specific Subtitle")
        val bookEntity = book.asBookEntity()
        assertEquals("Specific Subtitle", bookEntity.subtitle)
    }

    @Test
    fun `when converting Book to BookEntity then authors are mapped correctly`() {
        val authors = listOf("Jane Doe", "John Smith")
        val book = createTestBook(authors = authors)
        val bookEntity = book.asBookEntity()
        assertEquals(authors, bookEntity.authors)
    }

    @Test
    fun `when converting Book to BookEntity then thumbnail link is mapped correctly`() {
        val thumbnailLink = "https://example.com/cover.jpg"
        val book = createTestBook(thumbnailLink = thumbnailLink)
        val bookEntity = book.asBookEntity()
        assertEquals(thumbnailLink, bookEntity.thumbnailLink)
    }

    @Test
    fun `when converting Book to BookEntity then page count is mapped correctly`() {
        val pageCount = 450
        val book = createTestBook(pageCount = pageCount)
        val bookEntity = book.asBookEntity()
        assertEquals(pageCount, bookEntity.pageCount)
    }

    @Test
    fun `when converting Book to BookEntity then publisher is mapped correctly`() {
        val publisher = "Test Publisher"
        val book = createTestBook(publisher = publisher)
        val bookEntity = book.asBookEntity()
        assertEquals(publisher, bookEntity.publisher)
    }

    @Test
    fun `when converting Book to BookEntity then published date is mapped correctly`() {
        val publishedDate = "2023-01-01"
        val book = createTestBook(publishedDate = publishedDate)
        val bookEntity = book.asBookEntity()
        assertEquals(publishedDate, bookEntity.publishedDate)
    }

    @Test
    fun `when converting Book to BookEntity then description is mapped correctly`() {
        val description = "Test book description"
        val book = createTestBook(description = description)
        val bookEntity = book.asBookEntity()
        assertEquals(description, bookEntity.description)
    }

    @Test
    fun `when converting Book to BookEntity then average rating is mapped correctly`() {
        val averageRating = 3.7
        val book = createTestBook(averageRating = averageRating)
        val bookEntity = book.asBookEntity()
        assertEquals(averageRating, bookEntity.averageRating)
    }

    @Test
    fun `when converting Book to BookEntity then ratings count is mapped correctly`() {
        val ratingsCount = 250
        val book = createTestBook(ratingsCount = ratingsCount)
        val bookEntity = book.asBookEntity()
        assertEquals(ratingsCount, bookEntity.ratingsCount)
    }

    @Test
    fun `when converting BookEntity to Book then all fields are mapped correctly`() {
        val bookEntity = createTestBookEntity(id = "entity-id")
        val book = bookEntity.asBook()
        assertEquals("entity-id", book.id)
    }

    @Test
    fun `when converting BookEntity to Book then book info title is mapped correctly`() {
        val bookEntity = createTestBookEntity(title = "Entity Title")
        val book = bookEntity.asBook()
        assertEquals("Entity Title", book.info.title)
    }

    @Test
    fun `when converting BookEntity to Book then book info authors are mapped correctly`() {
        val authors = listOf("Entity Author 1", "Entity Author 2")
        val bookEntity = createTestBookEntity(authors = authors)
        val book = bookEntity.asBook()
        assertEquals(authors, book.info.authors)
    }

    @Test
    fun `when converting BookEntity to Book then book info average rating is mapped correctly`() {
        val averageRating = 4.8
        val bookEntity = createTestBookEntity(averageRating = averageRating)
        val book = bookEntity.asBook()
        assertEquals(averageRating, book.info.averageRating)
    }

    @Test
    fun `when converting BookEntity to Book then book info subtitle is mapped correctly`() {
        val subtitle = "Entity Subtitle"
        val bookEntity = createTestBookEntity(subtitle = subtitle)
        val book = bookEntity.asBook()
        assertEquals(subtitle, book.info.subtitle)
    }

    @Test
    fun `when converting BookEntity to Book then book info thumbnail link is mapped correctly`() {
        val thumbnailLink = "https://entity.com/thumbnail.jpg"
        val bookEntity = createTestBookEntity(thumbnailLink = thumbnailLink)
        val book = bookEntity.asBook()
        assertEquals(thumbnailLink, book.info.thumbnailLink)
    }

    @Test
    fun `when converting BookEntity to Book then book info publisher is mapped correctly`() {
        val publisher = "Entity Publisher"
        val bookEntity = createTestBookEntity(publisher = publisher)
        val book = bookEntity.asBook()
        assertEquals(publisher, book.info.publisher)
    }

    @Test
    fun `when converting BookEntity to Book then book info published date is mapped correctly`() {
        val publishedDate = "2024-05-15"
        val bookEntity = createTestBookEntity(publishedDate = publishedDate)
        val book = bookEntity.asBook()
        assertEquals(publishedDate, book.info.publishedDate)
    }

    @Test
    fun `when converting BookEntity to Book then book info description is mapped correctly`() {
        val description = "Entity book description"
        val bookEntity = createTestBookEntity(description = description)
        val book = bookEntity.asBook()
        assertEquals(description, book.info.description)
    }

    @Test
    fun `when converting BookEntity to Book then book info page count is mapped correctly`() {
        val pageCount = 320
        val bookEntity = createTestBookEntity(pageCount = pageCount)
        val book = bookEntity.asBook()
        assertEquals(pageCount, book.info.pageCount)
    }

    @Test
    fun `when converting BookEntity to Book then book info ratings count is mapped correctly`() {
        val ratingsCount = 175
        val bookEntity = createTestBookEntity(ratingsCount = ratingsCount)
        val book = bookEntity.asBook()
        assertEquals(ratingsCount, book.info.ratingsCount)
    }

    private fun createTestBook(
        id: String = "test-id",
        title: String = "Test Title",
        subtitle: String? = null,
        authors: List<String> = listOf("Test Author"),
        thumbnailLink: String? = null,
        publisher: String? = null,
        publishedDate: String? = null,
        description: String? = null,
        pageCount: Int? = null,
        averageRating: Double? = null,
        ratingsCount: Int? = null
    ) = Book(
        id = id,
        info = BookInfo(
            title = title,
            subtitle = subtitle,
            authors = authors,
            thumbnailLink = thumbnailLink,
            publisher = publisher,
            publishedDate = publishedDate,
            description = description,
            pageCount = pageCount,
            averageRating = averageRating,
            ratingsCount = ratingsCount
        )
    )

    private fun createTestBookEntity(
        id: String = "test-id",
        title: String = "Test Title",
        subtitle: String? = null,
        authors: List<String> = listOf("Test Author"),
        thumbnailLink: String? = null,
        publisher: String? = null,
        publishedDate: String? = null,
        description: String? = null,
        pageCount: Int? = null,
        averageRating: Double? = null,
        ratingsCount: Int? = null
    ) = BookEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        authors = authors,
        thumbnailLink = thumbnailLink,
        publisher = publisher,
        publishedDate = publishedDate,
        description = description,
        pageCount = pageCount,
        averageRating = averageRating,
        ratingsCount = ratingsCount
    )
}
