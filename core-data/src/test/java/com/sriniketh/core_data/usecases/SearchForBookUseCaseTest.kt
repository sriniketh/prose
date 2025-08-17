package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeBooksRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchForBookUseCaseTest {

    private lateinit var booksRepository: FakeBooksRepository
    private lateinit var searchForBookUseCase: SearchForBookUseCase

    @Before
    fun setup() {
        booksRepository = FakeBooksRepository()
        searchForBookUseCase = SearchForBookUseCase(booksRepository)
    }

    @Test
    fun `when repository search succeeds then returns success result`() = runTest {
        val query = "test query"
        booksRepository.shouldSearchForBooksThrowException = false
        val result = searchForBookUseCase(query)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when repository search succeeds then query is passed to repository`() = runTest {
        val query = "test query"
        booksRepository.shouldSearchForBooksThrowException = false
        searchForBookUseCase(query)
        assertEquals(query, booksRepository.searchQueryPassed)
    }

    @Test
    fun `when repository search succeeds then returns book search results`() = runTest {
        val query = "test query"
        booksRepository.shouldSearchForBooksThrowException = false
        val result = searchForBookUseCase(query)
        val bookSearch = result.getOrNull()
        assertEquals(1, bookSearch?.items?.size)
        assertEquals("test-id", bookSearch?.items?.first()?.id)
    }

    @Test
    fun `when repository search fails then returns failure result`() = runTest {
        val query = "test query"
        booksRepository.shouldSearchForBooksThrowException = true
        val result = searchForBookUseCase(query)
        assertTrue(result.isFailure)
    }

    @Test
    fun `when repository search fails then returns failure result with correct exception`() = runTest {
        val query = "test query"
        booksRepository.shouldSearchForBooksThrowException = true
        val result = searchForBookUseCase(query)
        val exception = result.exceptionOrNull()
        assertTrue(exception is RuntimeException)
        assertEquals("Search failed", exception?.message)
    }
}
