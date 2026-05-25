package com.sriniketh.prose.core_network.di

import com.sriniketh.prose.core_network.model.Volumes
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Test

class BooksApiJsonTest {

    @Test
    fun `decodes google books payload while ignoring unknown keys`() {
        val payload = """
            {
              "kind": "books#volumes",
              "totalItems": 1,
              "items": [
                {
                  "kind": "books#volume",
                  "id": "someId",
                  "etag": "abc123",
                  "selfLink": "https://www.googleapis.com/books/v1/volumes/someId",
                  "volumeInfo": {
                    "title": "Some Title",
                    "subtitle": "Some Subtitle",
                    "authors": ["Author One"],
                    "publisher": "Some Publisher",
                    "publishedDate": "2020",
                    "description": "A description",
                    "industryIdentifiers": [
                      {"type": "ISBN_13", "identifier": "9781234567897"}
                    ],
                    "pageCount": 321,
                    "categories": ["Fiction"],
                    "averageRating": 4.5,
                    "ratingsCount": 12,
                    "imageLinks": {
                      "smallThumbnail": "http://books.google.com/small",
                      "thumbnail": "http://books.google.com/thumb"
                    },
                    "language": "en",
                    "previewLink": "http://books.google.com/preview"
                  },
                  "saleInfo": {"country": "US", "saleability": "NOT_FOR_SALE"},
                  "accessInfo": {"country": "US", "viewability": "PARTIAL"}
                }
              ]
            }
        """.trimIndent()

        val volumes = booksApiJson.decodeFromString<Volumes>(payload)

        assertEquals(1, volumes.items.size)
        val volumeInfo = volumes.items[0].volumeInfo
        assertEquals("someId", volumes.items[0].id)
        assertEquals("Some Title", volumeInfo.title)
        assertEquals("Some Subtitle", volumeInfo.subtitle)
        assertEquals("Author One", volumeInfo.authors[0])
        assertEquals(321, volumeInfo.pageCount)
        assertEquals(4.5, volumeInfo.averageRating)
        assertEquals(12, volumeInfo.ratingsCount)
        assertEquals("http://books.google.com/thumb", volumeInfo.imageLinks?.thumbnail)
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
