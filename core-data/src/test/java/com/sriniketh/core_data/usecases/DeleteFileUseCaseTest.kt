package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeFileSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteFileUseCaseTest {

	private val fileSource = FakeFileSource()
	private val deleteFileUseCase = DeleteFileUseCase(fileSource)

	@Test
	fun `when invoked then deletes file using file source`() {
		val file = fileSource.createNewFile()
		val result = deleteFileUseCase(file)
		assertTrue(result)
		assertTrue(fileSource.deletedUris.contains(file))
	}

	@Test
	fun `when file source fails to delete file then returns false`() {
		fileSource.shouldDeleteFail = true
		val file = fileSource.createNewFile()
		val result = deleteFileUseCase(file)
		assertFalse(result)
	}
}
