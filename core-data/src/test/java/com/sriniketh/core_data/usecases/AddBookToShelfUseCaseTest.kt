package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeBooksRepository
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddBookToShelfUseCaseTest {

    private lateinit var booksRepository: FakeBooksRepository
    private lateinit var addBookToShelfUseCase: AddBookToShelfUseCase

    @Before
    fun setup() {
        booksRepository = FakeBooksRepository()
        addBookToShelfUseCase = AddBookToShelfUseCase(booksRepository)
    }

    @Test
    fun `when repository insertion succeeds then returns success result`() = runTest {
        val book = createTestBook()
        booksRepository.shouldInsertBookIntoDboThrowException = false
        val result = addBookToShelfUseCase(book)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when repository insertion succeeds then book is passed to repository`() = runTest {
        val book = createTestBook()
        booksRepository.shouldInsertBookIntoDboThrowException = false
        addBookToShelfUseCase(book)
        assertEquals(book, booksRepository.insertedBook)
    }

    @Test
    fun `when repository insertion fails then returns failure result`() = runTest {
        val book = createTestBook()
        booksRepository.shouldInsertBookIntoDboThrowException = true
        val result = addBookToShelfUseCase(book)
        assertTrue(result.isFailure)
    }

    @Test
    fun `when repository insertion fails then returns failure result with correct exception`() = runTest {
        val book = createTestBook()
        booksRepository.shouldInsertBookIntoDboThrowException = true
        val result = addBookToShelfUseCase(book)
        val exception = result.exceptionOrNull()
        assertTrue(exception is RuntimeException)
        assertEquals("Insert book failed", exception?.message)
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
