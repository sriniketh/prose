package com.sriniketh.core_data.transformers

import com.sriniketh.prose.core_network.model.ImageLinks
import com.sriniketh.prose.core_network.model.Volume
import com.sriniketh.prose.core_network.model.VolumeInfo
import com.sriniketh.prose.core_network.model.Volumes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VolumesTransformersTest {

    @Test
    fun `when converting Volumes to BookSearch then items are mapped correctly`() {
        val volumes = createTestVolumes(itemCount = 2)
        val bookSearch = volumes.asBookSearchResult()
        assertEquals(2, bookSearch.items.size)
    }

    @Test
    fun `when converting Volumes to BookSearch then first item id is mapped correctly`() {
        val volumes = createTestVolumes(itemCount = 1)
        val bookSearch = volumes.asBookSearchResult()
        assertEquals("volume-0", bookSearch.items.first().id)
    }

    @Test
    fun `when converting Volume to Book then id is mapped correctly`() {
        val volume = createTestVolume(id = "specific-volume-id")
        val book = volume.asBook()
        assertEquals("specific-volume-id", book.id)
    }

    @Test
    fun `when converting Volume to Book then volume info is mapped to book info`() {
        val volume = createTestVolume(title = "Volume Title")
        val book = volume.asBook()
        assertEquals("Volume Title", book.info.title)
    }

    @Test
    fun `when converting VolumeInfo to BookInfo then title is mapped correctly`() {
        val volumeInfo = createTestVolumeInfo(title = "Test Volume Title")
        val bookInfo = volumeInfo.asBookInfo()
        assertEquals("Test Volume Title", bookInfo.title)
    }

    @Test
    fun `when converting VolumeInfo to BookInfo then subtitle is mapped correctly`() {
        val volumeInfo = createTestVolumeInfo(subtitle = "Test Subtitle")
        val bookInfo = volumeInfo.asBookInfo()
        assertEquals("Test Subtitle", bookInfo.subtitle)
    }

    @Test
    fun `when converting VolumeInfo to BookInfo then authors are mapped correctly`() {
        val authors = listOf("Author One", "Author Two")
        val volumeInfo = createTestVolumeInfo(authors = authors)
        val bookInfo = volumeInfo.asBookInfo()
        assertEquals(authors, bookInfo.authors)
    }

    @Test
    fun `when converting VolumeInfo to BookInfo then thumbnail from image links is mapped correctly`() {
        val imageLinks = ImageLinks(thumbnail = "https://test.com/thumbnail.jpg")
        val volumeInfo = createTestVolumeInfo(imageLinks = imageLinks)
        val bookInfo = volumeInfo.asBookInfo()
        assertEquals("https://test.com/thumbnail.jpg", bookInfo.thumbnailLink)
    }

    @Test
    fun `when converting VolumeInfo to BookInfo then null image links results in null thumbnail`() {
        val volumeInfo = createTestVolumeInfo(imageLinks = null)
        val bookInfo = volumeInfo.asBookInfo()
        assertNull(bookInfo.thumbnailLink)
    }

    @Test
    fun `when converting VolumeInfo to BookInfo then publisher is mapped correctly`() {
        val publisher = "Test Publisher Inc"
        val volumeInfo = createTestVolumeInfo(publisher = publisher)
        val bookInfo = volumeInfo.asBookInfo()
        assertEquals(publisher, bookInfo.publisher)
    }

    @Test
    fun `when converting VolumeInfo to BookInfo then page count is mapped correctly`() {
        val pageCount = 350
        val volumeInfo = createTestVolumeInfo(pageCount = pageCount)
        val bookInfo = volumeInfo.asBookInfo()
        assertEquals(pageCount, bookInfo.pageCount)
    }

    @Test
    fun `when converting VolumeInfo to BookInfo then average rating is mapped correctly`() {
        val averageRating = 4.2
        val volumeInfo = createTestVolumeInfo(averageRating = averageRating)
        val bookInfo = volumeInfo.asBookInfo()
        assertEquals(averageRating, bookInfo.averageRating)
    }

    @Test
    fun `when converting VolumeInfo to BookInfo then ratings count is mapped correctly`() {
        val ratingsCount = 127
        val volumeInfo = createTestVolumeInfo(ratingsCount = ratingsCount)
        val bookInfo = volumeInfo.asBookInfo()
        assertEquals(ratingsCount, bookInfo.ratingsCount)
    }

    private fun createTestVolumes(itemCount: Int) = Volumes(
        items = (0 until itemCount).map { index ->
            createTestVolume(id = "volume-$index")
        }
    )

    private fun createTestVolume(
        id: String = "test-volume-id",
        title: String = "Test Title"
    ) = Volume(
        id = id,
        volumeInfo = createTestVolumeInfo(title = title)
    )

    private fun createTestVolumeInfo(
        title: String = "Test Title",
        subtitle: String? = null,
        description: String? = null,
        authors: List<String> = listOf("Test Author"),
        imageLinks: ImageLinks? = null,
        publisher: String? = null,
        publishedDate: String? = null,
        pageCount: Int? = null,
        averageRating: Double? = null,
        ratingsCount: Int? = null
    ) = VolumeInfo(
        title = title,
        subtitle = subtitle,
        description = description,
        authors = authors,
        imageLinks = imageLinks,
        publisher = publisher,
        publishedDate = publishedDate,
        pageCount = pageCount,
        averageRating = averageRating,
        ratingsCount = ratingsCount
    )
}
