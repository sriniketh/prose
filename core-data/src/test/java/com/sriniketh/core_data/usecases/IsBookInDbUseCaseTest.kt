package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeBooksRepository
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsBookInDbUseCaseTest {

    private lateinit var booksRepository: FakeBooksRepository
    private lateinit var isBookInDbUseCase: IsBookInDbUseCase

    @Before
    fun setup() {
        booksRepository = FakeBooksRepository()
        isBookInDbUseCase = IsBookInDbUseCase(booksRepository)
    }

    @Test
    fun `when book exists in database then returns true`() = runTest {
        val book = createTestBook()
        booksRepository.doesBookExistResult = true
        booksRepository.shouldDoesBookExistInDbThrowException = false
        val result = isBookInDbUseCase(book)
        assertTrue(result)
    }

    @Test
    fun `when book exists in database then book id is passed to repository`() = runTest {
        val book = createTestBook()
        booksRepository.doesBookExistResult = true
        booksRepository.shouldDoesBookExistInDbThrowException = false
        isBookInDbUseCase(book)
        assertEquals("test-id", booksRepository.bookIdPassed)
    }

    @Test
    fun `when book does not exist in database then returns false`() = runTest {
        val book = createTestBook()
        booksRepository.doesBookExistResult = false
        booksRepository.shouldDoesBookExistInDbThrowException = false
        val result = isBookInDbUseCase(book)
        assertFalse(result)
    }

    @Test
    fun `when repository throws exception then returns false`() = runTest {
        val book = createTestBook()
        booksRepository.shouldDoesBookExistInDbThrowException = true
        val result = isBookInDbUseCase(book)
        assertFalse(result)
    }

    private fun createTestBook() = Book(
        id = "test-id",
        info = BookInfo(
            title = "Test Title",
            subtitle = null,
            authors = listOf("Test Author"),
            thumbnailLink = null,
            publisher = null,
            publishedDate = null,
            description = null,
            pageCount = null,
            averageRating = null,
            ratingsCount = null
        )
    )
}
