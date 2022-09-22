package com.sriniketh.core_data

import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.Highlight
import com.sriniketh.core_models.search.BookSearch
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun searchForBooks(searchQuery: String): Flow<Result<BookSearch>>
    fun fetchBookInfo(volumeId: String): Flow<Result<Book>>
    fun insertBookIntoDb(book: Book): Flow<Result<Unit>>
    fun getAllSavedBooksFromDb(): Flow<Result<List<Book>>>
    fun insertHighlightIntoDb(highlight: Highlight): Flow<Result<Unit>>
    fun getAllHighlightsForBookFromDb(bookId: String): Flow<Result<List<Highlight>>>
}
