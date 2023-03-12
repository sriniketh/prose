package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import javax.inject.Inject

class FetchBookInfoUseCase @Inject constructor(
    private val bookRepo: BooksRepository
) {
    suspend operator fun invoke(volumeId: String): Result<Book> = bookRepo.fetchBookInfo(volumeId)
}
