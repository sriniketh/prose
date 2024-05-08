package com.sriniketh.prose.files

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider.getUriForFile
import com.sriniketh.core_platform.FileSource
import java.io.File
import java.util.UUID

class FileSourceImpl(private val appContext: Context) : FileSource {

    override fun createNewFile(): Uri {
        val newFile = File(appContext.cacheDir, generateFileName())
        val authority = "${appContext.packageName}.fileProvider"
        val contentUri: Uri = getUriForFile(appContext, authority, newFile)
        return contentUri
    }

    private fun generateFileName(): String = "${UUID.randomUUID()}.jpg"

    override fun deleteFile(uri: Uri): Boolean {
        val result = appContext.contentResolver.delete(uri, null, null)
        return result > 0
    }
}
