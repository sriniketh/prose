package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeHighlightsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteHighlightUseCaseTest {

    private lateinit var highlightsRepository: FakeHighlightsRepository
    private lateinit var deleteHighlightUseCase: DeleteHighlightUseCase

    @Before
    fun setup() {
        highlightsRepository = FakeHighlightsRepository()
        deleteHighlightUseCase = DeleteHighlightUseCase(highlightsRepository)
    }

    @Test
    fun `when repository deletion succeeds then returns success result`() = runTest {
        highlightsRepository.shouldDeleteHighlightFromDbThrowException = false
        val result = deleteHighlightUseCase("test-highlight-id")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when repository deletion succeeds then highlight id is passed to repository`() = runTest {
        highlightsRepository.shouldDeleteHighlightFromDbThrowException = false
        deleteHighlightUseCase("test-highlight-id")
        assertEquals("test-highlight-id", highlightsRepository.deletedHighlightId)
    }

    @Test
    fun `when repository deletion fails then returns failure result`() = runTest {
        highlightsRepository.shouldDeleteHighlightFromDbThrowException = true
        val result = deleteHighlightUseCase("test-highlight-id")
        assertTrue(result.isFailure)
    }

    @Test
    fun `when repository deletion fails then returns failure result with correct exception`() = runTest {
        highlightsRepository.shouldDeleteHighlightFromDbThrowException = true
        val result = deleteHighlightUseCase("test-highlight-id")
        val exception = result.exceptionOrNull()
        assertTrue(exception is RuntimeException)
        assertEquals("Delete highlight failed", exception?.message)
    }
}
