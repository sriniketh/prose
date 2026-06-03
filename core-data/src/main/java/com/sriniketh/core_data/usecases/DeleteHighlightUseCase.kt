package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.HighlightsRepository
import javax.inject.Inject

class DeleteHighlightUseCase @Inject constructor(
    private val highlightsRepository: HighlightsRepository
) {
    suspend operator fun invoke(highlightId: String): Result<Unit> = highlightsRepository.deleteHighlightFromDb(highlightId)
}
