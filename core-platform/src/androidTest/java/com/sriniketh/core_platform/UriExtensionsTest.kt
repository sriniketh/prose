package com.sriniketh.core_platform

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UriExtensionsTest {

    @Test
    fun buildHttpsUriReturnsNullWhenReceiverIsNull() {
        val nullString: String? = null

        val result = nullString.buildHttpsUri()

        assertNull(result)
    }

    @Test
    fun buildHttpsUriRewritesAnHttpUriSchemeToHttps() {
        val result = "http://example.com/books".buildHttpsUri()

        assertEquals("https", result?.scheme)
        assertEquals("example.com", result?.host)
        assertEquals("/books", result?.path)
    }

    @Test
    fun buildHttpsUriRewritesASchemelessUriSchemeToHttps() {
        val result = "//example.com/books".buildHttpsUri()

        assertEquals("https", result?.scheme)
        assertEquals("example.com", result?.host)
        assertEquals("/books", result?.path)
    }
}
