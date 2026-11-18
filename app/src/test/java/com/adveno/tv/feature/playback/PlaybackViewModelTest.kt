package com.adveno.tv.feature.playback

import app.cash.turbine.test
import com.adveno.tv.data.playlist.MockPlaylistRepositoryImpl
import com.adveno.tv.domain.model.MediaType
import com.adveno.tv.feature.initialization.FakeDeviceRepository
import com.adveno.tv.playback.controller.SignagePlaybackController
import com.adveno.tv.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun `viewModel loads active playlist and starts continuous playback`() = runTest {
        val playlistRepo = MockPlaylistRepositoryImpl()
        val deviceRepo = FakeDeviceRepository()
        val controller = SignagePlaybackController(testScope)
        val viewModel = PlaybackViewModel(playlistRepo, deviceRepo, controller)

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.currentItem)
            assertEquals("Restaurant Prime Promo Loop", state.playlistName)
            assertEquals(4, state.totalItems)
            assertEquals(MediaType.IMAGE, state.currentItem?.type)
            assertTrue(state.isPlaying)
            assertFalse(state.hasNoContent)
        }
    }

    @Test
    fun `manual nextItem advances to video item`() = runTest {
        val playlistRepo = MockPlaylistRepositoryImpl()
        val deviceRepo = FakeDeviceRepository()
        val controller = SignagePlaybackController(testScope)
        val viewModel = PlaybackViewModel(playlistRepo, deviceRepo, controller)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals("item_1", initial.currentItem?.id)

            viewModel.nextItem()

            var updated = awaitItem()
            if (!updated.isOverlayVisible) {
                updated = awaitItem()
            }
            assertEquals("item_2", updated.currentItem?.id)
            assertEquals(MediaType.VIDEO, updated.currentItem?.type)
            assertTrue(updated.isOverlayVisible)
        }
    }
}
