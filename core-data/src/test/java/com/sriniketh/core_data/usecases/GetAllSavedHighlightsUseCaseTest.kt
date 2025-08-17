package com.sriniketh.core_data.usecases

import app.cash.turbine.test
import com.sriniketh.core_data.fakes.FakeHighlightsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAllSavedHighlightsUseCaseTest {

    private lateinit var highlightsRepository: FakeHighlightsRepository
    private lateinit var getAllSavedHighlightsUseCase: GetAllSavedHighlightsUseCase

    @Before
    fun setup() {
        highlightsRepository = FakeHighlightsRepository()
        getAllSavedHighlightsUseCase = GetAllSavedHighlightsUseCase(highlightsRepository)
    }

    @Test
    fun `when repository returns success result then emits success flow`() = runTest {
        val bookId = "test-book-id"
        highlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = false
        getAllSavedHighlightsUseCase(bookId).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            awaitComplete()
        }
    }

    @Test
    fun `when repository returns success result then book id is passed to repository`() = runTest {
        val bookId = "test-book-id"
        highlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = false
        getAllSavedHighlightsUseCase(bookId).test {
            awaitItem()
            assertEquals(bookId, highlightsRepository.bookIdPassed)
            awaitComplete()
        }
    }

    @Test
    fun `when repository returns success result then emits highlights list`() = runTest {
        val bookId = "test-book-id"
        highlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = false
        getAllSavedHighlightsUseCase(bookId).test {
            val result = awaitItem()
            val highlights = result.getOrNull()
            assertEquals(1, highlights?.size)
            assertEquals("test-highlight-id", highlights?.first()?.id)
            awaitComplete()
        }
    }

    @Test
    fun `when repository returns failure result then emits failure flow`() = runTest {
        val bookId = "test-book-id"
        highlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = true
        getAllSavedHighlightsUseCase(bookId).test {
            val result = awaitItem()
            assertTrue(result.isFailure)
            awaitComplete()
        }
    }

    @Test
    fun `when repository returns failure result then emits failure flow with correct exception`() = runTest {
        val bookId = "test-book-id"
        highlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = true
        getAllSavedHighlightsUseCase(bookId).test {
            val result = awaitItem()
            val exception = result.exceptionOrNull()
            assertTrue(exception is RuntimeException)
            assertEquals("Get all highlights failed", exception?.message)
            awaitComplete()
        }
    }
}
