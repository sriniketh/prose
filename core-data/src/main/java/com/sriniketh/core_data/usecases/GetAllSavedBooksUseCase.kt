package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSavedBooksUseCase @Inject constructor(
    private val bookRepo: BooksRepository
) {
    operator fun invoke(): Flow<Result<List<Book>>> = bookRepo.getAllSavedBooksFromDb()
}