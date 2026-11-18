package com.adveno.tv.data.synchronization

import com.adveno.tv.data.playlist.MockPlaylistRepositoryImpl
import com.adveno.tv.domain.model.MediaType
import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.model.PlaylistItem
import com.adveno.tv.domain.model.SyncStatus
import com.adveno.tv.domain.repository.MediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FakeMediaRepository : MediaRepository {
    var cachedPlaylists = mutableListOf<Playlist>()
    var cleanupCalledWith: Playlist? = null

    override suspend fun cacheMediaItem(item: PlaylistItem): Result<File> {
        return Result.success(File("/mock/path/${item.id}.mp4"))
    }

    override suspend fun cachePlaylistMedia(playlist: Playlist): Playlist {
        val updated = playlist.copy(
            items = playlist.items.map { it.copy(localFilePath = "/mock/cached/${it.id}") }
        )
        cachedPlaylists.add(updated)
        return updated
    }

    override suspend fun cleanupOrphanedMedia(activePlaylist: Playlist) {
        cleanupCalledWith = activePlaylist
    }

    override suspend fun getCacheStorageUsedBytes(): Long = 1024 * 1024 * 150 // 150MB
    override suspend fun getAvailableDiskSpaceBytes(): Long = 1024L * 1024 * 1024 * 8 // 8GB
}

@OptIn(ExperimentalCoroutinesApi::class)
class SynchronizationRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun `checkForPlaylistUpdates reports unavailable until the API exposes a device manifest`() = runTest {
        val playlistRepo = MockPlaylistRepositoryImpl()
        val mediaRepo = FakeMediaRepository()
        val syncRepo = SynchronizationRepositoryImpl(playlistRepo, mediaRepo, testScope)

        val result = syncRepo.checkForPlaylistUpdates()
        assertTrue(result.isFailure)

        val status = syncRepo.syncStatus.value
        assertTrue("Expected SyncFailed, got $status", status is SyncStatus.SyncFailed)
        assertNotNull(syncRepo.lastSyncTimestamp.value)
    }

    @Test
    fun `synchronizePlaylist safely caches media and promotes new playlist`() = runTest {
        val playlistRepo = MockPlaylistRepositoryImpl()
        val mediaRepo = FakeMediaRepository()
        val syncRepo = SynchronizationRepositoryImpl(playlistRepo, mediaRepo, testScope)

        val incomingPlaylist = Playlist(
            id = "pl_autumn_festival",
            name = "Autumn Mega Sale 2026",
            version = 2L,
            items = listOf(
                PlaylistItem("item_f1", "Poster 1", MediaType.IMAGE, "http://cdn.com/1.jpg"),
                PlaylistItem("item_f2", "Video 2", MediaType.VIDEO, "http://cdn.com/2.mp4")
            )
        )

        val syncResult = syncRepo.synchronizePlaylist(incomingPlaylist)
        assertTrue(syncResult.isSuccess)

        // Verify active playlist in playlistRepo was atomically swapped
        val active = playlistRepo.getActivePlaylist()
        assertNotNull(active)
        assertEquals("Autumn Mega Sale 2026", active?.name)
        assertEquals(2L, active?.version)
        assertEquals(2, active?.items?.size)

        // Verify local paths were populated
        assertTrue(active?.items?.first()?.localFilePath?.contains("mock/cached") == true)

        // Verify orphan cleanup was executed for the promoted playlist
        assertEquals("Autumn Mega Sale 2026", mediaRepo.cleanupCalledWith?.name)
    }
}

