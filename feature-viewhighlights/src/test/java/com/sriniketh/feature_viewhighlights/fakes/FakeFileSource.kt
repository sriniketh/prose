package com.sriniketh.feature_viewhighlights.fakes

import android.net.Uri
import com.sriniketh.core_platform.FileSource
import io.mockk.every
import io.mockk.mockk

class FakeFileSource : FileSource {

    override fun createNewFile(fileName: String): Uri {
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns "content://com.test.fileProvider/cache/$fileName"
        return mockUri
    }

    override suspend fun writeToFile(fileName: String, content: String): Uri {
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns "content://com.test.fileProvider/cache/$fileName"
        return mockUri
    }

    override suspend fun deleteFile(uri: Uri): Boolean = true
}
