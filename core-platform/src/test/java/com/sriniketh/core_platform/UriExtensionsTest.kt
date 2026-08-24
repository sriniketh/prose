package com.sriniketh.core_platform

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class UriExtensionsTest {

    @Before
    fun setup() {
        mockkStatic(Uri::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `buildHttpsUri returns null when receiver is null`() {
        val nullString: String? = null

        val result = nullString.buildHttpsUri()

        assertNull(result)
    }

    @Test
    fun `buildHttpsUri rewrites an http uri's scheme to https`() {
        val parsedUri = mockk<Uri>()
        val builder = mockk<Uri.Builder>()
        val httpsUri = mockk<Uri>()
        every { Uri.parse("http://example.com/books") } returns parsedUri
        every { parsedUri.buildUpon() } returns builder
        every { builder.scheme("https") } returns builder
        every { builder.build() } returns httpsUri
        every { httpsUri.scheme } returns "https"
        every { httpsUri.host } returns "example.com"
        every { httpsUri.path } returns "/books"

        val result = "http://example.com/books".buildHttpsUri()

        assertSame(httpsUri, result)
        assertEquals("https", result?.scheme)
        assertEquals("example.com", result?.host)
        assertEquals("/books", result?.path)
    }

    @Test
    fun `buildHttpsUri rewrites a schemeless uri's scheme to https`() {
        val parsedUri = mockk<Uri>()
        val builder = mockk<Uri.Builder>()
        val httpsUri = mockk<Uri>()
        every { Uri.parse("//example.com/books") } returns parsedUri
        every { parsedUri.buildUpon() } returns builder
        every { builder.scheme("https") } returns builder
        every { builder.build() } returns httpsUri
        every { httpsUri.scheme } returns "https"
        every { httpsUri.host } returns "example.com"
        every { httpsUri.path } returns "/books"

        val result = "//example.com/books".buildHttpsUri()

        assertSame(httpsUri, result)
        assertEquals("https", result?.scheme)
        assertEquals("example.com", result?.host)
        assertEquals("/books", result?.path)
        verify(exactly = 1) { Uri.parse("//example.com/books") }
        verify(exactly = 1) { builder.scheme("https") }
    }

    @Test
    fun `encodeUri percent-encodes the receiver's string form`() {
        val uri = mockk<Uri>()
        val decoded = "https://example.com/search?q=book title"
        val encoded = "https%3A%2F%2Fexample.com%2Fsearch%3Fq%3Dbook%20title"
        every { uri.toString() } returns decoded
        every { Uri.encode(decoded) } returns encoded

        val result = uri.encodeUri()

        assertEquals(encoded, result)
        verify(exactly = 1) { Uri.encode(decoded) }
    }

    @Test
    fun `decodeUri percent-decodes then parses the result back into a Uri`() {
        val decoded = "https://example.com/search?q=book title"
        val encoded = "https%3A%2F%2Fexample.com%2Fsearch%3Fq%3Dbook%20title"
        val decodedUri = mockk<Uri>()
        every { Uri.decode(encoded) } returns decoded
        every { Uri.parse(decoded) } returns decodedUri

        val result = encoded.decodeUri()

        assertSame(decodedUri, result)
        verify(exactly = 1) { Uri.decode(encoded) }
        verify(exactly = 1) { Uri.parse(decoded) }
    }

    @Test
    fun `encodeUri then decodeUri roundtrips back to the original uri`() {
        val originalUri = mockk<Uri>()
        val decoded = "https://example.com/search?q=book title"
        val encoded = "https%3A%2F%2Fexample.com%2Fsearch%3Fq%3Dbook%20title"
        every { originalUri.toString() } returns decoded
        every { Uri.encode(decoded) } returns encoded
        every { Uri.decode(encoded) } returns decoded
        every { Uri.parse(decoded) } returns originalUri

        val roundTripped = originalUri.encodeUri().decodeUri()

        assertSame(originalUri, roundTripped)
    }
}
