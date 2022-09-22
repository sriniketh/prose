package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchBookInfoUseCase @Inject constructor(
    private val bookRepo: BooksRepository
) {
    operator fun invoke(volumeId: String): Flow<Result<Book>> = bookRepo.fetchBookInfo(volumeId)
}
