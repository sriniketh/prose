package com.sriniketh.prose.core_network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryDoc> = emptyList()
)

@Serializable
data class OpenLibraryDoc(
    val key: String,
    val title: String,
    val subtitle: String? = null,
    @SerialName("author_name") val authorName: List<String>? = null,
    @SerialName("cover_i") val coverId: Int? = null,
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
    @SerialName("number_of_pages_median") val numberOfPagesMedian: Int? = null,
    val publisher: List<String>? = null,
    @SerialName("ratings_average") val ratingsAverage: Double? = null,
    @SerialName("ratings_count") val ratingsCount: Int? = null
)

@Serializable
data class OpenLibraryWork(
    @Serializable(with = WorkDescriptionSerializer::class)
    val description: WorkDescription? = null
)

data class WorkDescription(
    val value: String?
)
