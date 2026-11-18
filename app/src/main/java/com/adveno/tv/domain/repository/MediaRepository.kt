package com.adveno.tv.domain.repository

import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.model.PlaylistItem
import java.io.File

interface MediaRepository {
    suspend fun cacheMediaItem(item: PlaylistItem): Result<File>
    suspend fun cachePlaylistMedia(playlist: Playlist): Playlist
    suspend fun cleanupOrphanedMedia(activePlaylist: Playlist)
    suspend fun getCacheStorageUsedBytes(): Long
    suspend fun getAvailableDiskSpaceBytes(): Long
}

