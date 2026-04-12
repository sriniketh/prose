package com.sriniketh.feature_viewhighlights.fakes

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import com.sriniketh.core_models.search.BookSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBooksRepository : BooksRepository {

    var shouldGetBookByIdFromDbThrowException = false

    override suspend fun searchForBooks(searchQuery: String): Result<BookSearch> {
        return Result.failure(NotImplementedError())
    }

    override suspend fun fetchBookInfo(volumeId: String): Result<Book> {
        return Result.failure(NotImplementedError())
    }

    override suspend fun insertBookIntoDb(book: Book): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun doesBookExistInDb(bookId: String): Boolean = true

    override fun getAllSavedBooksFromDb(): Flow<Result<List<Book>>> = flow {
        emit(Result.success(emptyList()))
    }

    override suspend fun deleteBookFromDb(book: Book): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getBookByIdFromDb(bookId: String): Result<Book> {
        return if (shouldGetBookByIdFromDbThrowException) {
            Result.failure(NoSuchElementException("Book not found"))
        } else {
            Result.success(
                Book(
                    id = bookId,
                    info = BookInfo(
                        title = "Test Book Title",
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
            )
        }
    }
}
