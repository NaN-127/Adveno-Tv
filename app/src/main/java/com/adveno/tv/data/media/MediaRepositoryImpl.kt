package com.adveno.tv.data.media

import com.adveno.tv.domain.model.MediaType
import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.model.PlaylistItem
import com.adveno.tv.domain.repository.MediaRepository
import com.adveno.tv.playback.cache.MediaCacheManager
import com.adveno.tv.playback.cache.MediaDownloader
import java.io.File

class MediaRepositoryImpl(
    private val cacheManager: MediaCacheManager,
    private val downloader: MediaDownloader
) : MediaRepository {

    override suspend fun cacheMediaItem(item: PlaylistItem): Result<File> {
        val extension = when (item.type) {
            MediaType.VIDEO -> "mp4"
            MediaType.IMAGE -> "jpg"
        }
        return downloader.downloadMediaItem(
            mediaId = item.id,
            remoteUrl = item.remoteUrl,
            extension = extension,
            expectedSha256 = item.checksumSha256
        )
    }

    override suspend fun cachePlaylistMedia(playlist: Playlist): Playlist {
        val updatedItems = mutableListOf<PlaylistItem>()

        for (item in playlist.items) {
            val extension = if (item.type == MediaType.VIDEO) "mp4" else "jpg"
            val cachedFile = cacheManager.getCachedFile(item.id, extension)

            if (cachedFile != null) {
                updatedItems.add(item.copy(localFilePath = cachedFile.absolutePath))
            } else {
                val result = cacheMediaItem(item)
                if (result.isSuccess) {
                    val file = result.getOrThrow()
                    updatedItems.add(item.copy(localFilePath = file.absolutePath))
                } else {
                    // Retain original item with remote fallback if download fails
                    updatedItems.add(item)
                }
            }
        }

        return playlist.copy(items = updatedItems)
    }

    override suspend fun cleanupOrphanedMedia(activePlaylist: Playlist) {
        val activeIds = activePlaylist.items.map { it.id }.toSet()
        cacheManager.pruneOrphanedMedia(activeIds)
    }

    override suspend fun getCacheStorageUsedBytes(): Long {
        return cacheManager.getCacheSizeBytes()
    }

    override suspend fun getAvailableDiskSpaceBytes(): Long {
        return cacheManager.getAvailableDiskSpaceBytes()
    }
}

