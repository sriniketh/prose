package com.sriniketh.core_data.usecases

import app.cash.turbine.test
import com.sriniketh.core_data.fakes.FakeBooksRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAllSavedBooksUseCaseTest {

    private lateinit var booksRepository: FakeBooksRepository
    private lateinit var getAllSavedBooksUseCase: GetAllSavedBooksUseCase

    @Before
    fun setup() {
        booksRepository = FakeBooksRepository()
        getAllSavedBooksUseCase = GetAllSavedBooksUseCase(booksRepository)
    }

    @Test
    fun `when repository returns success result then emits success flow`() = runTest {
        booksRepository.shouldGetAllSavedBooksFromDbThrowException = false
        getAllSavedBooksUseCase().test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            awaitComplete()
        }
    }

    @Test
    fun `when repository returns success result then emits books list`() = runTest {
        booksRepository.shouldGetAllSavedBooksFromDbThrowException = false
        getAllSavedBooksUseCase().test {
            val result = awaitItem()
            val books = result.getOrNull()
            assertEquals(1, books?.size)
            assertEquals("test-id", books?.first()?.id)
            awaitComplete()
        }
    }

    @Test
    fun `when repository returns failure result then emits failure flow`() = runTest {
        booksRepository.shouldGetAllSavedBooksFromDbThrowException = true
        getAllSavedBooksUseCase().test {
            val result = awaitItem()
            assertTrue(result.isFailure)
            awaitComplete()
        }
    }

    @Test
    fun `when repository returns failure result then emits failure flow with correct exception`() = runTest {
        booksRepository.shouldGetAllSavedBooksFromDbThrowException = true
        getAllSavedBooksUseCase().test {
            val result = awaitItem()
            val exception = result.exceptionOrNull()
            assertTrue(exception is RuntimeException)
            assertEquals("Get all books failed", exception?.message)
            awaitComplete()
        }
    }
}
