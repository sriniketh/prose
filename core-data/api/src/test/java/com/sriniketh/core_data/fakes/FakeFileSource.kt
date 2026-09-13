package com.sriniketh.core_data.fakes

import android.net.Uri
import com.sriniketh.core_platform.FileSource
import io.mockk.every
import io.mockk.mockk
import java.io.IOException

class FakeFileSource : FileSource {

	val deletedUris = mutableListOf<Uri>()
	var shouldDeleteFail = false

	var lastWrittenFileName: String? = null
	var lastWrittenContent: String? = null
	var shouldWriteToFileFail = false

	override fun createNewFile(fileName: String): Uri {
		val mockUri = mockk<Uri>()
		every { mockUri.toString() } returns "file://some_path/$fileName"
		return mockUri
	}

	override fun writeToFile(fileName: String, content: String): Uri {
		if (shouldWriteToFileFail) {
			throw IOException("Failed to write file")
		}
		lastWrittenFileName = fileName
		lastWrittenContent = content
		val mockUri = mockk<Uri>()
		every { mockUri.toString() } returns "content://com.test.fileProvider/cache/$fileName"
		return mockUri
	}

	override fun deleteFile(uri: Uri): Boolean {
		deletedUris.add(uri)
		return !shouldDeleteFail
	}
}
