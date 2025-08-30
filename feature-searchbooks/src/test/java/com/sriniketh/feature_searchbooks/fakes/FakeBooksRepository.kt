package com.sriniketh.feature_searchbooks.fakes

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import com.sriniketh.core_models.search.BookSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBooksRepository : BooksRepository {

	var shouldSearchForBooksThrowException = false
	var shouldFetchBookInfoThrowException = false
	var shouldInsertBookIntoDboThrowException = false
	var shouldGetAllSavedBooksFromDbThrowException = false
	var shouldDoesBookExistInDbThrowException = false
	var shouldDeleteBookFromDbThrowException = false

	var searchQueryPassed: String? = null
	var volumeIdPassed: String? = null
	var bookIdPassed: String? = null
	var insertedBook: Book? = null
	var deletedBook: Book? = null
	var doesBookExistResult = false

	private val fakeBook = Book(
		id = "test-id",
		info = BookInfo(
			title = "Test Title",
			subtitle = "Test Subtitle",
			authors = listOf("Test Author"),
			thumbnailLink = "test-thumbnail",
			publisher = "Test Publisher",
			publishedDate = "2023",
			description = "Test Description",
			pageCount = 200,
			averageRating = 4.5,
			ratingsCount = 100
		)
	)

	override suspend fun searchForBooks(searchQuery: String): Result<BookSearch> {
		searchQueryPassed = searchQuery
		return if (shouldSearchForBooksThrowException) {
			Result.failure(RuntimeException("Search failed"))
		} else {
			Result.success(BookSearch(items = listOf(fakeBook)))
		}
	}

	override suspend fun fetchBookInfo(volumeId: String): Result<Book> {
		volumeIdPassed = volumeId
		return if (shouldFetchBookInfoThrowException) {
			Result.failure(RuntimeException("Fetch book info failed"))
		} else {
			Result.success(fakeBook)
		}
	}

	override suspend fun insertBookIntoDb(book: Book): Result<Unit> {
		insertedBook = book
		return if (shouldInsertBookIntoDboThrowException) {
			Result.failure(RuntimeException("Insert book failed"))
		} else {
			Result.success(Unit)
		}
	}

	override suspend fun doesBookExistInDb(bookId: String): Boolean {
		bookIdPassed = bookId
		return if (shouldDoesBookExistInDbThrowException) {
			false
		} else {
			doesBookExistResult
		}
	}

	override fun getAllSavedBooksFromDb(): Flow<Result<List<Book>>> = flow {
		if (shouldGetAllSavedBooksFromDbThrowException) {
			emit(Result.failure(RuntimeException("Get all books failed")))
		} else {
			emit(Result.success(listOf(fakeBook)))
		}
	}

	override suspend fun deleteBookFromDb(book: Book): Result<Unit> {
		deletedBook = book
		return if (shouldDeleteBookFromDbThrowException) {
			Result.failure(RuntimeException("Delete book failed"))
		} else {
			Result.success(Unit)
		}
	}
}
