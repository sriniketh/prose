package com.sriniketh.prose.files

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileSourceImplTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val fileSource = FileSourceImpl(context)

    @Test
    fun whenCreateNewFileIsCalledThenItReturnsAResolvableContentUri() {
        val fileName = "created-file-${System.nanoTime()}.txt"

        val uri = fileSource.createNewFile(fileName)

        assertEquals("content", uri.scheme)
        assertEquals("${context.packageName}.fileProvider", uri.authority)
    }

    @Test
    fun whenAConsumerWritesThroughTheCreateNewFileUriThenTheFileExistsInCacheDir() {
        val fileName = "created-file-${System.nanoTime()}.txt"
        val uri = fileSource.createNewFile(fileName)

        context.contentResolver.openOutputStream(uri)?.use { it.write("data".toByteArray()) }

        val expectedFile = File(context.cacheDir, fileName)
        assertTrue(expectedFile.exists())
    }

    @Test
    fun whenWriteToFileIsCalledThenTheReturnedUriContentIsReadableBack() {
        val fileName = "written-file-${System.nanoTime()}.txt"
        val content = "hello prose"

        val uri = fileSource.writeToFile(fileName, content)

        val readBack = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        assertEquals(content, readBack)
    }

    @Test
    fun whenDeleteFileIsCalledWithARealCreatedUriThenItReturnsTrue() {
        val fileName = "deletable-file-${System.nanoTime()}.txt"
        val uri = fileSource.writeToFile(fileName, "content to delete")

        val result = fileSource.deleteFile(uri)

        assertTrue(result)
    }

    @Test
    fun whenDeleteFileIsCalledWithAUriThatHasNoRealFileThenItReturnsFalse() {
        val fileName = "never-written-${System.nanoTime()}.txt"
        val uri = fileSource.createNewFile(fileName)

        val result = fileSource.deleteFile(uri)

        assertFalse(result)
        assertFalse(File(context.cacheDir, fileName).exists())
    }
}
