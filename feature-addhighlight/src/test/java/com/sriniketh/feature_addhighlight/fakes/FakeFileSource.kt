package com.sriniketh.feature_addhighlight.fakes

import android.net.Uri
import com.sriniketh.core_platform.FileSource
import io.mockk.mockk

class FakeFileSource : FileSource {

    var deletedUris = mutableListOf<Uri>()
    var shouldDeleteFail = false

    override fun createNewFile(): Uri {
        return mockk<Uri>()
    }

    override fun deleteFile(uri: Uri): Boolean {
        deletedUris.add(uri)
        return !shouldDeleteFail
    }
}
