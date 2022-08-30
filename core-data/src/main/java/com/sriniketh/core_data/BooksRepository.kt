package com.sriniketh.core_data

import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.search.BookSearch
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun searchBooks(searchQuery: String): Flow<Result<BookSearch>>
    fun getBook(volumeId: String): Flow<Result<Book>>
    fun insertBook(book: Book): Flow<Result<Unit>>
    fun getAllBooks(): Flow<Result<List<Book>>>
}
