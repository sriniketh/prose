package com.sriniketh.prose.files

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider.getUriForFile
import com.sriniketh.core_platform.FileSource
import com.sriniketh.core_platform.logTag
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject

class FileSourceImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : FileSource {

    override fun createNewFile(): Uri {
        val newFile = File(appContext.cacheDir, generateFileName())
        val authority = "${appContext.packageName}.fileProvider"
        val contentUri: Uri = getUriForFile(appContext, authority, newFile)
        Timber.d("${this.logTag()}: Created file $contentUri")
        return contentUri
    }

    private fun generateFileName(): String = "${UUID.randomUUID()}.jpg"

    override fun deleteFile(uri: Uri): Boolean {
        val result = appContext.contentResolver.delete(uri, null, null)
        Timber.d("${this.logTag()}: Deleted rows $result")
        return result > 0
    }
}
