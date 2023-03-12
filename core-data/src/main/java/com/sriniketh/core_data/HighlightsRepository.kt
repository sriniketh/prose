package com.sriniketh.core_data

import com.sriniketh.core_models.book.Highlight
import kotlinx.coroutines.flow.Flow

interface HighlightsRepository {
    suspend fun insertHighlightIntoDb(highlight: Highlight): Result<Unit>
    fun getAllHighlightsForBookFromDb(bookId: String): Flow<Result<List<Highlight>>>
    suspend fun deleteHighlightFromDb(highlight: Highlight): Result<Unit>
}