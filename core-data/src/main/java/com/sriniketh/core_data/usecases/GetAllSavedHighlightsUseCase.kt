package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Highlight
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSavedHighlightsUseCase @Inject constructor(
    private val bookRepo: BooksRepository
) {
    operator fun invoke(bookId: String): Flow<Result<List<Highlight>>> = bookRepo.getAllHighlightsForBookFromDb(bookId)
}