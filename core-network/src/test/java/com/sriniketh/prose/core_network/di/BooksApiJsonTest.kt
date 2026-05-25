package com.sriniketh.prose.core_network.di

import com.sriniketh.prose.core_network.model.OpenLibrarySearchResponse
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
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
    fun `decodes volumes when items omit title and authors`() {
        val payload = """
            {
              "kind": "books#volumes",
              "totalItems": 3,
              "items": [
                {
                  "id": "complete",
                  "volumeInfo": {
                    "title": "Complete Volume",
                    "authors": ["Author One"]
                  }
                },
                {
                  "id": "missingAuthors",
                  "volumeInfo": {
                    "title": "Has Title Only"
                  }
                },
                {
                  "id": "missingTitleAndAuthors",
                  "volumeInfo": {
                    "publisher": "Some Publisher"
                  }
                }
              ]
            }
        """.trimIndent()

        val volumes = booksApiJson.decodeFromString<Volumes>(payload)

        assertEquals(3, volumes.items.size)
        assertEquals(listOf("Author One"), volumes.items[0].volumeInfo.authors)

        assertEquals("Has Title Only", volumes.items[1].volumeInfo.title)
        assertEquals(emptyList<String>(), volumes.items[1].volumeInfo.authors)

        assertEquals("", volumes.items[2].volumeInfo.title)
        assertEquals(emptyList<String>(), volumes.items[2].volumeInfo.authors)
    }

    @Test
    fun `decodes imageLinks when thumbnail is omitted`() {
        val payload = """
            {
              "kind": "books#volumes",
              "totalItems": 1,
              "items": [
                {
                  "id": "noThumbnail",
                  "volumeInfo": {
                    "title": "Some Title",
                    "authors": ["Author One"],
                    "imageLinks": {
                      "smallThumbnail": "http://books.google.com/small"
                    }
                  }
                }
              ]
            }
        """.trimIndent()

        val volumes = booksApiJson.decodeFromString<Volumes>(payload)

        assertEquals(null, volumes.items[0].volumeInfo.imageLinks?.thumbnail)
    }
}
