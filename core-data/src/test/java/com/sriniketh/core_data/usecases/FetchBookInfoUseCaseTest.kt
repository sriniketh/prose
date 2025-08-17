package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeBooksRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FetchBookInfoUseCaseTest {

    private lateinit var booksRepository: FakeBooksRepository
    private lateinit var fetchBookInfoUseCase: FetchBookInfoUseCase

    @Before
    fun setup() {
        booksRepository = FakeBooksRepository()
        fetchBookInfoUseCase = FetchBookInfoUseCase(booksRepository)
    }

    @Test
    fun `when repository fetch succeeds then returns success result`() = runTest {
        val volumeId = "test-volume-id"
        booksRepository.shouldFetchBookInfoThrowException = false
        val result = fetchBookInfoUseCase(volumeId)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when repository fetch succeeds then volume id is passed to repository`() = runTest {
        val volumeId = "test-volume-id"
        booksRepository.shouldFetchBookInfoThrowException = false
        fetchBookInfoUseCase(volumeId)
        assertEquals(volumeId, booksRepository.volumeIdPassed)
    }

    @Test
    fun `when repository fetch succeeds then returns book info`() = runTest {
        val volumeId = "test-volume-id"
        booksRepository.shouldFetchBookInfoThrowException = false
        val result = fetchBookInfoUseCase(volumeId)
        val book = result.getOrNull()
        assertEquals("test-id", book?.id)
        assertEquals("Test Title", book?.info?.title)
    }

    @Test
    fun `when repository fetch fails then returns failure result`() = runTest {
        val volumeId = "test-volume-id"
        booksRepository.shouldFetchBookInfoThrowException = true
        val result = fetchBookInfoUseCase(volumeId)
        assertTrue(result.isFailure)
    }

    @Test
    fun `when repository fetch fails then returns failure result with correct exception`() = runTest {
        val volumeId = "test-volume-id"
        booksRepository.shouldFetchBookInfoThrowException = true
        val result = fetchBookInfoUseCase(volumeId)
        val exception = result.exceptionOrNull()
        assertTrue(exception is RuntimeException)
        assertEquals("Fetch book info failed", exception?.message)
    }
}
