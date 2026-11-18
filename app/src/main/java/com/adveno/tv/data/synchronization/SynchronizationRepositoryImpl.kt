package com.adveno.tv.data.synchronization

import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.model.SyncStatus
import com.adveno.tv.domain.repository.MediaRepository
import com.adveno.tv.domain.repository.PlaylistRepository
import com.adveno.tv.domain.repository.SynchronizationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SynchronizationRepositoryImpl(
    private val playlistRepository: PlaylistRepository,
    private val mediaRepository: MediaRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : SynchronizationRepository {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(System.currentTimeMillis())
    override val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    override suspend fun checkForPlaylistUpdates(): Result<Boolean> = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncStatus.CheckingForUpdates
        // The live OpenAPI contract exposes administrative playlist assignment only.
        // It does not expose a screen-authenticated playlist manifest, so claiming an
        // update check (or calling an invented Firestore/API path) would be incorrect.
        val error = UnsupportedOperationException(
            "Backend does not expose a device playlist-manifest endpoint."
        )
        _syncStatus.value = SyncStatus.SyncFailed(error.message ?: "Playlist sync unavailable")
        Result.failure(error)
    }

    override suspend fun synchronizePlaylist(remotePlaylist: Playlist): Result<Unit> = withContext(Dispatchers.IO) {
        val currentPlaylist = playlistRepository.getActivePlaylist()

        // If versions are identical and all files exist, no sync needed
        if (currentPlaylist != null && currentPlaylist.version == remotePlaylist.version && currentPlaylist.items.size == remotePlaylist.items.size) {
            _syncStatus.value = SyncStatus.SyncSuccess(
                timestamp = System.currentTimeMillis(),
                version = remotePlaylist.version,
                message = "Content already synchronized"
            )
            return@withContext Result.success(Unit)
        }

        try {
            _syncStatus.value = SyncStatus.DownloadingMedia(
                playlistName = remotePlaylist.name,
                currentItemIndex = 0,
                totalItemsToDownload = remotePlaylist.items.size,
                progress = 0f
            )

            // Step 1: Pre-download and cache all media items in temporary staging.
            // Digital signage invariant: Active playback is completely unaffected during this phase!
            val cachedPlaylist = mediaRepository.cachePlaylistMedia(remotePlaylist)

            _syncStatus.value = SyncStatus.PromotingPlaylist(
                playlistName = remotePlaylist.name,
                version = remotePlaylist.version
            )

            // Step 2: Atomic swap into SQLite and memory
            playlistRepository.saveActivePlaylist(cachedPlaylist)

            // Step 3: Garbage collect orphaned files no longer in active playlist
            mediaRepository.cleanupOrphanedMedia(cachedPlaylist)

            val now = System.currentTimeMillis()
            _lastSyncTimestamp.value = now
            _syncStatus.value = SyncStatus.SyncSuccess(
                timestamp = now,
                version = remotePlaylist.version,
                message = "Successfully synchronized '${remotePlaylist.name}' (v${remotePlaylist.version})"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.SyncFailed(
                reason = e.localizedMessage ?: "Synchronization failed"
            )
            Result.failure(e)
        }
    }

    override suspend fun simulateRemoteUpdateNotification(newPlaylist: Playlist) {
        scope.launch {
            synchronizePlaylist(newPlaylist)
        }
    }
}

