package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.HighlightsRepository
import com.sriniketh.core_models.book.Highlight
import javax.inject.Inject

class DeleteHighlightUseCase @Inject constructor(
    private val highlightsRepository: HighlightsRepository
) {
    suspend operator fun invoke(highlight: Highlight): Result<Unit> = highlightsRepository.deleteHighlightFromDb(highlight)
}
