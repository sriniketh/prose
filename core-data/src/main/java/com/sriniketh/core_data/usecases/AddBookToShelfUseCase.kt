package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddBookToShelfUseCase @Inject constructor(
    private val bookRepo: BooksRepository
) {
    operator fun invoke(book: Book): Flow<Result<Unit>> = bookRepo.insertBookIntoDb(book)
}
