package com.sriniketh.prose.core_network.model

import com.sriniketh.prose.core_network.di.booksApiJson
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkDescriptionSerializerTest {

    @Test
    fun `parses description given as a plain string`() {
        val work = booksApiJson.decodeFromString<OpenLibraryWork>(
            """{"description":"A great book"}"""
        )
        assertEquals("A great book", work.description?.value)
    }

    @Test
    fun `parses description given as a typed object`() {
        val work = booksApiJson.decodeFromString<OpenLibraryWork>(
            """{"description":{"type":"/type/text","value":"A great book"}}"""
        )
        assertEquals("A great book", work.description?.value)
    }

    @Test
    fun `leaves description null when field is absent`() {
        val work = booksApiJson.decodeFromString<OpenLibraryWork>("""{}""")
        assertNull(work.description)
    }

    @Test
    fun `leaves description null when field is json null`() {
        val work = booksApiJson.decodeFromString<OpenLibraryWork>("""{"description":null}""")
        assertNull(work.description)
    }
}
