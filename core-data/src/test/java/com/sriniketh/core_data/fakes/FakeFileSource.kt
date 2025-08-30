package com.sriniketh.core_data.fakes

import android.net.Uri
import com.sriniketh.core_platform.FileSource
import io.mockk.every
import io.mockk.mockk

class FakeFileSource : FileSource {

	val deletedUris = mutableListOf<Uri>()
	var shouldDeleteFail = false

	override fun createNewFile(): Uri {
		val mockUri = mockk<Uri>()
		every { mockUri.toString() } returns "file://some_path"
		return mockUri
	}

	override fun deleteFile(uri: Uri): Boolean {
		deletedUris.add(uri)
		return !shouldDeleteFail
	}
}
