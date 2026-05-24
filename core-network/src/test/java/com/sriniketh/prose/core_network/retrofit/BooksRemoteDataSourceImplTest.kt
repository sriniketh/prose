package com.sriniketh.prose.core_network.retrofit

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BooksRemoteDataSourceImplTest {

    private lateinit var booksApi: FakeBooksApi
    private lateinit var dataSource: BooksRemoteDataSourceImpl

    @Before
    fun setup() {
        booksApi = FakeBooksApi()
        dataSource = BooksRemoteDataSourceImpl(booksApi)
    }

    @Test
    fun `getVolumes searches and maps results without description`() = runTest {
        assertNull(booksApi.searchQueryPassed)

        val volumes = dataSource.getVolumes("some search query")

        assertEquals("some search query", booksApi.searchQueryPassed)
        assertEquals(2, volumes.items.size)

        val first = volumes.items[0]
        assertEquals("OL1W", first.id)
        assertEquals("title 1", first.volumeInfo.title)
        assertEquals("subtitle 1", first.volumeInfo.subtitle)
        assertEquals(listOf("author 1", "author 2"), first.volumeInfo.authors)
        assertEquals("https://covers.openlibrary.org/b/id/111-M.jpg", first.volumeInfo.imageLinks?.thumbnail)
        assertEquals("publisher 1", first.volumeInfo.publisher)
        assertEquals("2001", first.volumeInfo.publishedDate)
        assertEquals(100, first.volumeInfo.pageCount)
        assertEquals(4.5, first.volumeInfo.averageRating!!, 0.0)
        assertEquals(4500, first.volumeInfo.ratingsCount)
        assertNull(first.volumeInfo.description)

        assertEquals("OL2W", volumes.items[1].id)
    }

    @Test
    fun `getVolume searches by key and merges the work description`() = runTest {
        assertNull(booksApi.searchQueryPassed)
        assertNull(booksApi.workIdPassed)

        val volume = dataSource.getVolume("OL1W")

        assertEquals("key:/works/OL1W", booksApi.searchQueryPassed)
        assertEquals("OL1W", booksApi.workIdPassed)

        assertEquals("OL1W", volume.id)
        assertEquals("title 1", volume.volumeInfo.title)
        assertEquals("work description", volume.volumeInfo.description)
    }
}
