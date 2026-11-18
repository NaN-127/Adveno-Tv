package com.adveno.tv.domain.repository

import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.model.SyncStatus
import kotlinx.coroutines.flow.StateFlow

interface SynchronizationRepository {
    val syncStatus: StateFlow<SyncStatus>
    val lastSyncTimestamp: StateFlow<Long?>

    suspend fun checkForPlaylistUpdates(): Result<Boolean>
    suspend fun synchronizePlaylist(remotePlaylist: Playlist): Result<Unit>
    suspend fun simulateRemoteUpdateNotification(newPlaylist: Playlist)
}

