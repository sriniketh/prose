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

    var doesBookExistResult = false
    var insertedBook: Book? = null

    var searchResultBuilder: (String) -> BookSearch = { BookSearch(items = listOf(fakeBook)) }

    val fakeBook = Book(
        id = "test-id",
        info = BookInfo(
            title = "Test Title",
            subtitle = "Test Subtitle",
            authors = listOf("Test Author"),
            thumbnailLink = null,
            publisher = "Test Publisher",
            publishedDate = "2023",
            description = "Test Description",
            pageCount = 200,
            averageRating = 4.5,
            ratingsCount = 100
        )
    )

    override suspend fun searchForBooks(searchQuery: String): Result<BookSearch> {
        return if (shouldSearchForBooksThrowException) {
            Result.failure(RuntimeException("Search failed"))
        } else {
            Result.success(searchResultBuilder(searchQuery))
        }
    }

    override suspend fun fetchBookInfo(volumeId: String): Result<Book> {
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

    override suspend fun doesBookExistInDb(bookId: String): Boolean = doesBookExistResult

    override fun getAllSavedBooksFromDb(): Flow<Result<List<Book>>> = flow {
        emit(Result.success(listOf(fakeBook)))
    }

    override suspend fun getBookByIdFromDb(bookId: String): Result<Book> = Result.success(fakeBook)

    override suspend fun deleteBookFromDb(book: Book): Result<Unit> = Result.success(Unit)
}
