package com.sriniketh.feature_addhighlight.fakes

import android.net.Uri
import com.sriniketh.core_platform.FileSource

class FakeFileSource(private val uriToReturn: Uri = Uri.parse("content://test.jpg")) : FileSource {

    val deletedUris = mutableListOf<Uri>()
    val createdFileNames = mutableListOf<String>()

    override fun createNewFile(fileName: String): Uri {
        createdFileNames.add(fileName)
        return uriToReturn
    }

    override fun writeToFile(fileName: String, content: String): Uri = uriToReturn

    override fun deleteFile(uri: Uri): Boolean {
        deletedUris.add(uri)
        return true
    }
}
