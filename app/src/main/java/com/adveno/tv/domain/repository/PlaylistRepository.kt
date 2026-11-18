package com.adveno.tv.domain.repository

import com.adveno.tv.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun observeActivePlaylist(): Flow<Playlist?>
    suspend fun getActivePlaylist(): Playlist?
    suspend fun saveActivePlaylist(playlist: Playlist)
    suspend fun clearPlaylist()
}

