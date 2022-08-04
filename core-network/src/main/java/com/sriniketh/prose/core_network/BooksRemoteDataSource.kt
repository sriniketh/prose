package com.sriniketh.prose.core_network

import com.sriniketh.prose.core_network.model.Volume
import com.sriniketh.prose.core_network.model.Volumes

interface BooksRemoteDataSource {
    suspend fun getVolumes(searchQuery: String): Volumes
    suspend fun getVolume(volumeId: String): Volume
}
