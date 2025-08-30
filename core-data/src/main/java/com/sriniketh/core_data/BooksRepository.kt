package com.sriniketh.core_data

import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.search.BookSearch
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
	suspend fun searchForBooks(searchQuery: String): Result<BookSearch>
	suspend fun fetchBookInfo(volumeId: String): Result<Book>
	suspend fun insertBookIntoDb(book: Book): Result<Unit>
	suspend fun doesBookExistInDb(bookId: String): Boolean
	fun getAllSavedBooksFromDb(): Flow<Result<List<Book>>>
	suspend fun deleteBookFromDb(book: Book): Result<Unit>
}
