package com.sriniketh.prose.core_network.di

import kotlinx.serialization.json.Json

internal val booksApiJson: Json = Json {
    ignoreUnknownKeys = true
}
