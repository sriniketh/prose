package com.sriniketh.core_data

import com.sriniketh.core_models.search.BookSearch
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun searchBooks(searchQuery: String): Flow<Result<BookSearch>>
}