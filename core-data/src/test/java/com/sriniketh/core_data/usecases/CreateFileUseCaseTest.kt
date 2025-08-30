package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeFileSource
import com.sriniketh.core_platform.FileSource
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateFileUseCaseTest {

	private val fileSource: FileSource = FakeFileSource()
	private val createFileUseCase = CreateFileUseCase(fileSource)

	@Test
	fun `when invoked then creates new file using file source`() {
		val file = createFileUseCase()
		assertEquals("file://some_path", file.toString())
	}
}
