package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.HighlightsRepository
import com.sriniketh.core_models.book.Highlight
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSavedHighlightsUseCase @Inject constructor(
    private val highlightsRepository: HighlightsRepository
) {
    operator fun invoke(bookId: String): Flow<Result<List<Highlight>>> = highlightsRepository.getAllHighlightsForBookFromDb(bookId)
}