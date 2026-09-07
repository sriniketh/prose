package com.sriniketh.prose.files

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider.getUriForFile
import com.sriniketh.core_platform.FileSource
import com.sriniketh.core_platform.dagger.IoDispatcher
import com.sriniketh.core_platform.logTag
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FileSourceImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FileSource {

    override fun createNewFile(fileName: String): Uri {
        val newFile = File(appContext.cacheDir, fileName)
        val contentUri = getFileProviderUri(newFile)
        Timber.d("${this.logTag()}: Created file $contentUri")
        return contentUri
    }

    override suspend fun writeToFile(fileName: String, content: String): Uri = withContext(ioDispatcher) {
        val file = File(appContext.cacheDir, fileName)
        file.writeText(content)
        val contentUri = getFileProviderUri(file)
        Timber.d("${this.logTag()}: Wrote file $contentUri")
        contentUri
    }

    override suspend fun deleteFile(uri: Uri): Boolean = withContext(ioDispatcher) {
        val result = appContext.contentResolver.delete(uri, null, null)
        Timber.d("${this.logTag()}: Deleted rows $result")
        result > 0
    }

    private fun getFileProviderUri(file: File): Uri {
        val authority = "${appContext.packageName}.fileProvider"
        return getUriForFile(appContext, authority, file)
    }
}
