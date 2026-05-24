package com.sriniketh.prose.core_network.retrofit

import com.sriniketh.prose.core_network.model.OpenLibraryDoc
import com.sriniketh.prose.core_network.model.OpenLibrarySearchResponse
import com.sriniketh.prose.core_network.model.OpenLibraryWork
import com.sriniketh.prose.core_network.model.WorkDescription

class FakeBooksApi : BooksApi {

    var searchQueryPassed: String? = null
    var fieldsPassed: String? = null
    var limitPassed: Int? = null
    override suspend fun search(
        query: String,
        fields: String,
        limit: Int
    ): OpenLibrarySearchResponse {
        searchQueryPassed = query
        fieldsPassed = fields
        limitPassed = limit
        return OpenLibrarySearchResponse(docs = listOf(doc1(), doc2()))
    }

    var workIdPassed: String? = null
    override suspend fun work(workId: String): OpenLibraryWork {
        workIdPassed = workId
        return OpenLibraryWork(description = WorkDescription("work description"))
    }

    private fun doc1() = OpenLibraryDoc(
        key = "/works/OL1W",
        title = "title 1",
        subtitle = "subtitle 1",
        authorName = listOf("author 1", "author 2"),
        coverId = 111,
        firstPublishYear = 2001,
        numberOfPagesMedian = 100,
        publisher = listOf("publisher 1"),
        ratingsAverage = 4.5,
        ratingsCount = 4500
    )

    private fun doc2() = OpenLibraryDoc(
        key = "/works/OL2W",
        title = "title 2",
        subtitle = "subtitle 2",
        authorName = listOf("author 1", "author 2"),
        coverId = 222,
        firstPublishYear = 2002,
        numberOfPagesMedian = 250,
        publisher = listOf("publisher 2"),
        ratingsAverage = 4.3,
        ratingsCount = 500
    )
}
