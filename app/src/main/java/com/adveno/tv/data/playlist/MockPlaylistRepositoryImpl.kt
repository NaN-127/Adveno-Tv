package com.adveno.tv.data.playlist

import com.adveno.tv.domain.model.MediaType
import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.model.PlaylistItem
import com.adveno.tv.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockPlaylistRepositoryImpl : PlaylistRepository {

    private val defaultPlaylist = Playlist(
        id = "pl_default_kannur",
        name = "Restaurant Prime Promo Loop",
        version = 1L,
        items = listOf(
            PlaylistItem(
                id = "item_1",
                name = "Weekend Special Buffet Poster",
                type = MediaType.IMAGE,
                remoteUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1920&q=80",
                displayDurationSeconds = 10,
                orderIndex = 0
            ),
            PlaylistItem(
                id = "item_2",
                name = "Chef Signature Dish Presentation",
                type = MediaType.VIDEO,
                remoteUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                displayDurationSeconds = 15,
                orderIndex = 1
            ),
            PlaylistItem(
                id = "item_3",
                name = "Festival 20% Discount Offer",
                type = MediaType.IMAGE,
                remoteUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=1920&q=80",
                displayDurationSeconds = 8,
                orderIndex = 2
            ),
            PlaylistItem(
                id = "item_4",
                name = "Evening Live Music & Mocktails Promo",
                type = MediaType.VIDEO,
                remoteUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                displayDurationSeconds = 15,
                orderIndex = 3
            )
        )
    )

    private val _activePlaylist = MutableStateFlow<Playlist?>(defaultPlaylist)

    override fun observeActivePlaylist(): Flow<Playlist?> {
        return _activePlaylist.asStateFlow()
    }

    override suspend fun getActivePlaylist(): Playlist? {
        return _activePlaylist.value
    }

    override suspend fun saveActivePlaylist(playlist: Playlist) {
        _activePlaylist.value = playlist
    }

    override suspend fun clearPlaylist() {
        _activePlaylist.value = null
    }
}

