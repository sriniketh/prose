package com.sriniketh.core_data.impl.fakes

import com.sriniketh.core_db.dao.BookDao
import com.sriniketh.core_db.entity.BookEntity
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeBookDao : BookDao {

	val booksInDb = mutableListOf<BookEntity>()

	var insertedBookEntity: BookEntity? = null
	var shouldInsertBookThrowException = false
	var shouldInsertBookSuspendForever = false
	override suspend fun insertBook(book: BookEntity) {
		if (shouldInsertBookSuspendForever) awaitCancellation()
		if (shouldInsertBookThrowException) {
			throw RuntimeException("some error inserting book")
		}
		insertedBookEntity = book
	}

	var shouldGetAllBooksThrowException = false
	override fun getAllBooks(): Flow<List<BookEntity>> {
		if (shouldGetAllBooksThrowException) {
			return flow {
				throw RuntimeException("some error fetching all books")
			}
		}
		return flowOf(booksInDb)
	}

	var shouldDoesBookExistThrowException = false
	var shouldDoesBookExistSuspendForever = false
	override suspend fun doesBookExist(bookId: String): Boolean {
		if (shouldDoesBookExistSuspendForever) awaitCancellation()
		if (shouldDoesBookExistThrowException) {
			throw RuntimeException("some error checking if book exists")
		}
		return booksInDb.any { it.id == bookId }
	}

	var shouldGetBookByIdThrowException = false
	var shouldGetBookByIdSuspendForever = false
	override suspend fun getBookById(bookId: String): BookEntity? {
		if (shouldGetBookByIdSuspendForever) awaitCancellation()
		if (shouldGetBookByIdThrowException) {
			throw RuntimeException("some error fetching book by id")
		}
		return booksInDb.find { it.id == bookId }
	}

	var shouldDeleteBookThrowException = false
	var deletedBookEntity: BookEntity? = null
	var shouldDeleteBookSuspendForever = false
	override suspend fun deleteBook(book: BookEntity) {
		if (shouldDeleteBookSuspendForever) awaitCancellation()
		if (shouldDeleteBookThrowException) {
			throw RuntimeException("some error deleting book")
		}
		deletedBookEntity = book
	}
}
