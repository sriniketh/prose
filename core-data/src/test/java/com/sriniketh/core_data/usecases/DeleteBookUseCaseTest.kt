package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeBooksRepository
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteBookUseCaseTest {

	private val bookRepo = FakeBooksRepository()
	private val deleteBookUseCase = DeleteBookUseCase(bookRepo)

	@Test
	fun `invoke calls deleteBookFromDb in BooksRepository`() = runTest {
		val book = fakeBook()
		deleteBookUseCase(book)
		assertEquals(book, bookRepo.deletedBook)
	}

	@Test
	fun `invoke returns success when deleteBookFromDb returns success`() = runTest {
		val book = fakeBook()
		val result = deleteBookUseCase(book)
		assertTrue(result.isSuccess)
	}

	@Test
	fun `invoke returns failure when deleteBookFromDb returns failure`() = runTest {
		val book = fakeBook()
		bookRepo.shouldDeleteBookFromDbThrowException = true
		val result = deleteBookUseCase(book)
		assertTrue(result.isFailure)
	}

	private fun fakeBook() = Book(
		id = "some-id",
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
