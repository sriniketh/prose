package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Highlight
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveHighlightUseCase @Inject constructor(
    private val bookRepo: BooksRepository
) {
    operator fun invoke(highlight: Highlight): Flow<Result<Unit>> = bookRepo.insertHighlightIntoDb(highlight)
}