package com.sriniketh.core_data.usecases

import android.net.Uri
import com.sriniketh.core_platform.FileSource
import javax.inject.Inject

class DeleteFileUseCase @Inject constructor(
    private val fileSource: FileSource
) {
    suspend operator fun invoke(uri: Uri): Boolean = fileSource.deleteFile(uri)
}
