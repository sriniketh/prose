package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.search.BookSearch
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchForBookUseCase @Inject constructor(
    private val bookRepo: BooksRepository
) {
    operator fun invoke(query: String): Flow<Result<BookSearch>> = bookRepo.searchForBooks(query)
}
