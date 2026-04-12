package com.sriniketh.core_platform

import android.net.Uri

interface FileSource {
    fun createNewFile(fileName: String): Uri
    fun writeToFile(fileName: String, content: String): Uri
    fun deleteFile(uri: Uri): Boolean
}
