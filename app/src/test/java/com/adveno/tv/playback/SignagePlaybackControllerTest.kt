package com.adveno.tv.playback

import com.adveno.tv.domain.model.MediaType
import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.model.PlaylistItem
import com.adveno.tv.playback.controller.SignagePlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignagePlaybackControllerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val samplePlaylist = Playlist(
        id = "test_pl",
        name = "Test Signage Loop",
        items = listOf(
            PlaylistItem("item_1", "Image 1", MediaType.IMAGE, "http://test.com/1.png", displayDurationSeconds = 5),
            PlaylistItem("item_2", "Video 1", MediaType.VIDEO, "http://test.com/2.mp4", displayDurationSeconds = 15),
            PlaylistItem("item_3", "Image 2", MediaType.IMAGE, "http://test.com/3.png", displayDurationSeconds = 8)
        )
    )

    @Test
    fun `starts playlist and loads first item`() = runTest {
        val controller = SignagePlaybackController(testScope)
        controller.setPlaylist(samplePlaylist)

        val state = controller.state.value
        assertNotNull(state.currentItem)
        assertEquals("item_1", state.currentItem?.id)
        assertEquals(0, state.currentIndex)
        assertEquals(3, state.totalItems)
    }

    @Test
    fun `image duration timer auto-advances to next video item`() = runTest {
        val controller = SignagePlaybackController(testScope)
        controller.setPlaylist(samplePlaylist)

        assertEquals("item_1", controller.state.value.currentItem?.id)

        // Advance 5.5 seconds (past the 5s image duration)
        testScope.advanceTimeBy(5500)

        // Should have transitioned to Video item 2
        assertEquals("item_2", controller.state.value.currentItem?.id)
        assertEquals(1, controller.state.value.currentIndex)
    }

    @Test
    fun `looping invariant wraps from last item back to index 0`() = runTest {
        val controller = SignagePlaybackController(testScope)
        controller.setPlaylist(samplePlaylist)

        // Advance: 0 -> 1 -> 2 -> 0 (loop count increments)
        controller.advanceToNext() // now at 1
        assertEquals(1, controller.state.value.currentIndex)

        controller.advanceToNext() // now at 2
        assertEquals(2, controller.state.value.currentIndex)

        controller.advanceToNext() // wraps to 0
        assertEquals(0, controller.state.value.currentIndex)
        assertEquals(1L, controller.state.value.loopCount)
    }

    @Test
    fun `corrupted media item skips immediately to next item`() = runTest {
        val controller = SignagePlaybackController(testScope)
        controller.setPlaylist(samplePlaylist)

        assertEquals("item_1", controller.state.value.currentItem?.id)

        // Report error for item 1
        controller.reportItemError("item_1", "Decoding failed")

        // Should skip directly to item 2
        assertEquals("item_2", controller.state.value.currentItem?.id)
        assertEquals(1, controller.state.value.currentIndex)
        assertNotNull(controller.state.value.lastError)
    }
}

