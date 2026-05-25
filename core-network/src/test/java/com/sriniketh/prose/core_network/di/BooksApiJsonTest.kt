package com.sriniketh.prose.core_network.di

import com.sriniketh.prose.core_network.model.OpenLibrarySearchResponse
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BooksApiJsonTest {

    @Test
    fun `decodes open library search payload while ignoring unknown keys`() {
        val payload = """
            {
              "numFound": 1,
              "start": 0,
              "docs": [
                {
                  "key": "/works/OL27482W",
                  "type": "work",
                  "title": "The Hobbit",
                  "subtitle": "or There and Back Again",
                  "author_name": ["J.R.R. Tolkien"],
                  "cover_i": 14627509,
                  "first_publish_year": 1937,
                  "number_of_pages_median": 310,
                  "publisher": ["George Allen & Unwin", "Houghton Mifflin"],
                  "ratings_average": 4.3,
                  "ratings_count": 285,
                  "ia": ["hobbitorthereand0000tolk"],
                  "language": ["eng"]
                }
              ]
            }
        """.trimIndent()

        val response = booksApiJson.decodeFromString<OpenLibrarySearchResponse>(payload)

        assertEquals(1, response.docs.size)
        val doc = response.docs[0]
        assertEquals("/works/OL27482W", doc.key)
        assertEquals("The Hobbit", doc.title)
        assertEquals("or There and Back Again", doc.subtitle)
        assertEquals(listOf("J.R.R. Tolkien"), doc.authorName)
        assertEquals(14627509, doc.coverId)
        assertEquals(1937, doc.firstPublishYear)
        assertEquals(310, doc.numberOfPagesMedian)
        assertEquals(listOf("George Allen & Unwin", "Houghton Mifflin"), doc.publisher)
        assertEquals(4.3, doc.ratingsAverage)
        assertEquals(285, doc.ratingsCount)
    }

    @Test
    fun `decodes docs that omit title, authors, and cover`() {
        val payload = """
            {
              "numFound": 2,
              "docs": [
                {
                  "key": "/works/OL1W",
                  "title": "Complete",
                  "author_name": ["Author One"],
                  "cover_i": 42
                },
                {
                  "key": "/works/OL2W"
                }
              ]
            }
        """.trimIndent()

        val response = booksApiJson.decodeFromString<OpenLibrarySearchResponse>(payload)

        assertEquals(2, response.docs.size)
        assertEquals("Complete", response.docs[0].title)
        assertEquals(listOf("Author One"), response.docs[0].authorName)
        assertEquals(42, response.docs[0].coverId)

        assertEquals("/works/OL2W", response.docs[1].key)
        assertEquals("", response.docs[1].title)
        assertNull(response.docs[1].authorName)
        assertNull(response.docs[1].coverId)
    }
}
