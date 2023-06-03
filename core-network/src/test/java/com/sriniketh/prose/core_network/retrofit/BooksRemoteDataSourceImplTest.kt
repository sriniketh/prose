package com.sriniketh.prose.core_network.retrofit

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BooksRemoteDataSourceImplTest {

    private lateinit var booksApi: FakeBooksApi
    private lateinit var dataSourceImpl: BooksRemoteDataSourceImpl

    @Before
    fun setup() {
        booksApi = FakeBooksApi()
        dataSourceImpl = BooksRemoteDataSourceImpl(booksApi)
    }

    @Test
    fun `getVolumes returns volumes for given searchQuery`() = runTest {
        assertNull(booksApi.searchQueryPassed)
        assertNull(booksApi.apiKeyPassed)
        assertNull(booksApi.projectionPassed)
        val volumes = dataSourceImpl.getVolumes("some search query")
        assertEquals("some search query", booksApi.searchQueryPassed)
        assertEquals("AIzaSyCgOoQQfwhKe_5VKAEJZzw-OcDD_YJFRiw", booksApi.apiKeyPassed)
        assertEquals("lite", booksApi.projectionPassed)
        assertEquals(2, volumes.items.size)

        val volume1 = volumes.items[0]
        val volume1VolumeInfo = volume1.volumeInfo
        assertEquals("someId1", volume1.id)
        assertEquals("title 1", volume1VolumeInfo.title)
        assertEquals("subtitle 1", volume1VolumeInfo.subtitle)
        assertEquals("description 1", volume1VolumeInfo.description)
        assertEquals("author 1", volume1VolumeInfo.authors[0])
        assertEquals("author 2", volume1VolumeInfo.authors[1])
        assertEquals("thumbnail 1", volume1VolumeInfo.imageLinks?.thumbnail)
        assertEquals("publisher 1", volume1VolumeInfo.publisher)
        assertEquals("published date 1", volume1VolumeInfo.publishedDate)
        assertEquals(100, volume1VolumeInfo.pageCount)
        assertEquals(4.5, volume1VolumeInfo.averageRating)
        assertEquals(4500, volume1VolumeInfo.ratingsCount)

        val volume2 = volumes.items[1]
        val volume2VolumeInfo = volume2.volumeInfo
        assertEquals("someId2", volume2.id)
        assertEquals("title 2", volume2VolumeInfo.title)
        assertEquals("subtitle 2", volume2VolumeInfo.subtitle)
        assertEquals("description 2", volume2VolumeInfo.description)
        assertEquals("author 1", volume2VolumeInfo.authors[0])
        assertEquals("author 2", volume2VolumeInfo.authors[1])
        assertEquals("thumbnail 2", volume2VolumeInfo.imageLinks?.thumbnail)
        assertEquals("publisher 2", volume2VolumeInfo.publisher)
        assertEquals("published date 2", volume2VolumeInfo.publishedDate)
        assertEquals(250, volume2VolumeInfo.pageCount)
        assertEquals(4.3, volume2VolumeInfo.averageRating)
        assertEquals(500, volume2VolumeInfo.ratingsCount)
    }

    @Test
    fun `getVolume returns volume for given volumeId`() = runTest {
        assertNull(booksApi.apiKeyPassed)
        assertNull(booksApi.volumeIdPassed)
        val volume = dataSourceImpl.getVolume("some volume id")
        assertEquals("AIzaSyCgOoQQfwhKe_5VKAEJZzw-OcDD_YJFRiw", booksApi.apiKeyPassed)
        assertEquals("some volume id", booksApi.volumeIdPassed)

        assertEquals("someId1", volume.id)
        val volume1VolumeInfo = volume.volumeInfo
        assertEquals("title 1", volume1VolumeInfo.title)
        assertEquals("subtitle 1", volume1VolumeInfo.subtitle)
        assertEquals("description 1", volume1VolumeInfo.description)
        assertEquals("author 1", volume1VolumeInfo.authors[0])
        assertEquals("author 2", volume1VolumeInfo.authors[1])
        assertEquals("thumbnail 1", volume1VolumeInfo.imageLinks?.thumbnail)
        assertEquals("publisher 1", volume1VolumeInfo.publisher)
        assertEquals("published date 1", volume1VolumeInfo.publishedDate)
        assertEquals(100, volume1VolumeInfo.pageCount)
        assertEquals(4.5, volume1VolumeInfo.averageRating)
        assertEquals(4500, volume1VolumeInfo.ratingsCount)
    }
}