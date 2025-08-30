package com.sriniketh.core_data.fakes

import com.sriniketh.core_db.dao.BookDao
import com.sriniketh.core_db.entity.BookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeBookDao : BookDao {

	val booksInDb = mutableListOf<BookEntity>()

	var insertedBookEntity: BookEntity? = null
	var shouldInsertBookThrowException = false
	override suspend fun insertBook(book: BookEntity) {
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
	override fun doesBookExist(bookId: String): Boolean {
		if (shouldDoesBookExistThrowException) {
			throw RuntimeException("some error checking if book exists")
		}
		return booksInDb.any { it.id == bookId }
	}

	var shouldDeleteBookThrowException = false
	var deletedBookEntity: BookEntity? = null
	override suspend fun deleteBook(book: BookEntity) {
		if (shouldDeleteBookThrowException) {
			throw RuntimeException("some error deleting book")
		}
		deletedBookEntity = book
	}
}
