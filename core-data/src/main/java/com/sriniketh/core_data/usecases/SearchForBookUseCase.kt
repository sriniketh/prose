package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.search.BookSearch
import javax.inject.Inject

class SearchForBookUseCase @Inject constructor(
    private val bookRepo: BooksRepository
) {
    suspend operator fun invoke(query: String): Result<BookSearch> = bookRepo.searchForBooks(query)
}
