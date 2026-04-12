package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeFileSource
import com.sriniketh.core_platform.FileSource
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateTempImageFileUseCaseTest {

	private val fileSource: FileSource = FakeFileSource()
	private val createTempImageFileUseCase = CreateTempImageFileUseCase(fileSource)

	@Test
	fun `when invoked then creates new file using file source`() {
		val file = createTempImageFileUseCase()
		assertTrue(file.toString().contains("file://some_path/"))
	}
}
