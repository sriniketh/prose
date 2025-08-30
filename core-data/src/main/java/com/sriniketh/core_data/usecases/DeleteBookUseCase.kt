package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import javax.inject.Inject

class DeleteBookUseCase @Inject constructor(
	private val bookRepo: BooksRepository
) {
	suspend operator fun invoke(book: Book): Result<Unit> = bookRepo.deleteBookFromDb(book)
}
