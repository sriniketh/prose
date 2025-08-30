package com.sriniketh.core_data

import com.sriniketh.core_data.transformers.asBook
import com.sriniketh.core_data.transformers.asBookEntity
import com.sriniketh.core_data.transformers.asBookSearchResult
import com.sriniketh.core_db.dao.BookDao
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.search.BookSearch
import com.sriniketh.core_platform.logTag
import com.sriniketh.prose.core_network.BooksRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class BooksRepositoryImpl @Inject constructor(
	private val remoteBookDataSource: BooksRemoteDataSource,
	private val localBookDataSource: BookDao
) : BooksRepository {

	override suspend fun searchForBooks(searchQuery: String): Result<BookSearch> =
		try {
			val books = remoteBookDataSource.getVolumes(searchQuery).asBookSearchResult()
			Result.success(books)
		} catch (exception: Exception) {
			Timber.e(exception, this.logTag())
			Result.failure(exception)
		}

	override suspend fun fetchBookInfo(volumeId: String): Result<Book> =
		try {
			val book = remoteBookDataSource.getVolume(volumeId).asBook()
			Result.success(book)
		} catch (exception: Exception) {
			Timber.e(exception, this.logTag())
			Result.failure(exception)
		}

	override suspend fun insertBookIntoDb(book: Book): Result<Unit> =
		try {
			localBookDataSource.insertBook(book.asBookEntity())
			Result.success(Unit)
		} catch (exception: Exception) {
			Timber.e(exception, this.logTag())
			Result.failure(exception)
		}

	override suspend fun doesBookExistInDb(bookId: String): Boolean =
		try {
			localBookDataSource.doesBookExist(bookId)
		} catch (exception: Exception) {
			Timber.e(exception, this.logTag())
			false
		}

	override fun getAllSavedBooksFromDb(): Flow<Result<List<Book>>> =
		localBookDataSource.getAllBooks()
			.map { entities ->
				Result.success(entities.map { entity -> entity.asBook() })
			}
			.catch { exception ->
				Timber.e(exception, this.logTag())
				emit(Result.failure(exception))
			}

	override suspend fun deleteBookFromDb(book: Book): Result<Unit> =
		try {
			localBookDataSource.deleteBook(book.asBookEntity())
			Result.success(Unit)
		} catch (exception: Exception) {
			Timber.e(exception, this.logTag())
			Result.failure(exception)
		}
}
