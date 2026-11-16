package com.sriniketh.core_platform

import androidx.core.net.toUri
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

    @Test
    fun encodeUriPercentEncodesTheReceiversStringForm() {
        val uri = "https://example.com/search?q=book title".toUri()

        val result = uri.encodeUri()

        assertEquals("https%3A%2F%2Fexample.com%2Fsearch%3Fq%3Dbook%20title", result)
    }

    @Test
    fun decodeUriPercentDecodesThenParsesTheResultBackIntoAUri() {
        val encoded = "https%3A%2F%2Fexample.com%2Fsearch%3Fq%3Dbook%20title"

        val result = encoded.decodeUri()

        assertEquals("https", result.scheme)
        assertEquals("example.com", result.host)
        assertEquals("/search", result.path)
        assertEquals("q=book title", result.query)
    }

    @Test
    fun encodeUriThenDecodeUriRoundtripsBackToTheOriginalUri() {
        val original = "https://example.com/search?q=book title".toUri()

        val roundTripped = original.encodeUri().decodeUri()

        assertEquals(original.toString(), roundTripped.toString())
    }
}
