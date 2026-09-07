package com.sriniketh.core_platform

import android.net.Uri

interface FileSource {
    fun createNewFile(fileName: String): Uri
    suspend fun writeToFile(fileName: String, content: String): Uri
    suspend fun deleteFile(uri: Uri): Boolean
}
