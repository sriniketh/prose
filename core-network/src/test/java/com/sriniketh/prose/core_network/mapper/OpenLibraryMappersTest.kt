package com.sriniketh.prose.core_network.mapper

import com.sriniketh.prose.core_network.model.OpenLibraryDoc
import com.sriniketh.prose.core_network.model.OpenLibrarySearchResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenLibraryMappersTest {

    private fun fullDoc() = OpenLibraryDoc(
        key = "/works/OL1W",
        title = "Title",
        subtitle = "Sub",
        authorName = listOf("Author A", "Author B"),
        coverId = 123,
        firstPublishYear = 1999,
        numberOfPagesMedian = 321,
        publisher = listOf("Pub One", "Pub Two"),
        ratingsAverage = 4.5,
        ratingsCount = 100
    )

    @Test
    fun `maps a search response to volumes preserving order`() {
        val response = OpenLibrarySearchResponse(
            docs = listOf(
                fullDoc(),
                fullDoc().copy(key = "/works/OL2W", title = "Second")
            )
        )

        val volumes = response.asVolumes()

        assertEquals(2, volumes.items.size)
        assertEquals("OL1W", volumes.items[0].id)
        assertEquals("OL2W", volumes.items[1].id)
        assertEquals("Second", volumes.items[1].volumeInfo.title)
    }

    @Test
    fun `maps every field of a doc into volume info`() {
        val volume = fullDoc().asVolume()
        val info = volume.volumeInfo

        assertEquals("OL1W", volume.id)
        assertEquals("Title", info.title)
        assertEquals("Sub", info.subtitle)
        assertEquals(listOf("Author A", "Author B"), info.authors)
        assertEquals("https://covers.openlibrary.org/b/id/123-M.jpg", info.imageLinks?.thumbnail)
        assertEquals("Pub One", info.publisher)
        assertEquals("1999", info.publishedDate)
        assertEquals(321, info.pageCount)
        assertEquals(4.5, info.averageRating!!, 0.0)
        assertEquals(100, info.ratingsCount)
    }

    @Test
    fun `search mapping leaves description null`() {
        assertNull(fullDoc().asVolume().volumeInfo.description)
    }

    @Test
    fun `asVolume merges an explicit description`() {
        assertEquals("My description", fullDoc().asVolume("My description").volumeInfo.description)
    }

    @Test
    fun `null cover id yields null image links`() {
        assertNull(fullDoc().copy(coverId = null).asVolume().volumeInfo.imageLinks)
    }

    @Test
    fun `null author name maps to empty list`() {
        assertEquals(emptyList<String>(), fullDoc().copy(authorName = null).asVolume().volumeInfo.authors)
    }

    @Test
    fun `null publisher and year map to null`() {
        val info = fullDoc().copy(publisher = null, firstPublishYear = null).asVolume().volumeInfo
        assertNull(info.publisher)
        assertNull(info.publishedDate)
    }
}
