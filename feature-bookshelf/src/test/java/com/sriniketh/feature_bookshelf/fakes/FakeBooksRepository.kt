package com.sriniketh.feature_bookshelf.fakes

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import com.sriniketh.core_models.search.BookSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeBooksRepository : BooksRepository {

    var shouldGetAllSavedBooksFromDbThrowException = false

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
        return Result.success(BookSearch(items = listOf(fakeBook)))
    }

    override suspend fun fetchBookInfo(volumeId: String): Result<Book> {
        return Result.success(fakeBook)
    }

    override suspend fun insertBookIntoDb(book: Book): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun doesBookExistInDb(bookId: String): Boolean {
        return false
    }

    override fun getAllSavedBooksFromDb(): Flow<Result<List<Book>>> {
        return if (shouldGetAllSavedBooksFromDbThrowException) {
            flowOf(Result.failure(RuntimeException("Get all books failed")))
        } else {
            flowOf(Result.success(listOf(fakeBook)))
        }
    }
}
