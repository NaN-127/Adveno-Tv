package com.adveno.tv.playback.controller

import com.adveno.tv.domain.model.MediaType
import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.model.PlaylistItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaybackControllerState(
    val playlist: Playlist? = null,
    val currentItem: PlaylistItem? = null,
    val currentIndex: Int = 0,
    val totalItems: Int = 0,
    val progress: Float = 0f,
    val isPlaying: Boolean = true,
    val loopCount: Long = 0L,
    val lastError: String? = null
)

class SignagePlaybackController(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _state = MutableStateFlow(PlaybackControllerState())
    val state: StateFlow<PlaybackControllerState> = _state.asStateFlow()

    private var durationTimerJob: Job? = null
    private var activePlaylist: Playlist? = null
    private var currentIndex: Int = 0
    private var currentLoop: Long = 0L

    fun setPlaylist(playlist: Playlist?) {
        activePlaylist = playlist
        currentIndex = 0
        currentLoop = 0L
        durationTimerJob?.cancel()

        if (playlist == null || playlist.items.isEmpty()) {
            _state.value = PlaybackControllerState(
                playlist = null,
                currentItem = null,
                currentIndex = 0,
                totalItems = 0,
                isPlaying = false
            )
            return
        }

        playCurrentItem()
    }

    private fun playCurrentItem(preserveError: Boolean = false) {
        durationTimerJob?.cancel()
        val playlist = activePlaylist ?: return
        if (playlist.items.isEmpty()) return

        if (currentIndex !in playlist.items.indices) {
            currentIndex = 0
        }

        val item = playlist.items[currentIndex]
        _state.value = _state.value.copy(
            playlist = playlist,
            currentItem = item,
            currentIndex = currentIndex,
            totalItems = playlist.items.size,
            progress = 0f,
            isPlaying = true,
            loopCount = currentLoop,
            lastError = if (preserveError) _state.value.lastError else null
        )

        // If it's an image, manage duration with smooth countdown progress
        if (item.type == MediaType.IMAGE) {
            startImageDurationTimer(item.displayDurationSeconds)
        }
    }

    private fun startImageDurationTimer(durationSeconds: Int) {
        durationTimerJob?.cancel()
        durationTimerJob = scope.launch {
            val totalDurationMs = (durationSeconds.coerceAtLeast(1) * 1000).toLong()
            val intervalMs = 100L
            var elapsedMs = 0L

            while (isActive && elapsedMs < totalDurationMs) {
                delay(intervalMs)
                elapsedMs += intervalMs
                val progress = (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                _state.value = _state.value.copy(progress = progress)
            }

            if (isActive) {
                advanceToNext()
            }
        }
    }

    fun updateVideoProgress(currentPositionMs: Long, totalDurationMs: Long) {
        if (_state.value.currentItem?.type == MediaType.VIDEO && totalDurationMs > 0) {
            val progress = (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
            _state.value = _state.value.copy(progress = progress)
        }
    }

    fun onVideoCompleted() {
        if (_state.value.currentItem?.type == MediaType.VIDEO) {
            advanceToNext()
        }
    }

    fun advanceToNext(preserveError: Boolean = false) {
        durationTimerJob?.cancel()
        val playlist = activePlaylist ?: return
        if (playlist.items.isEmpty()) return

        if (currentIndex >= playlist.items.size - 1) {
            // Infinite signage loop wrap-around
            currentIndex = 0
            currentLoop++
        } else {
            currentIndex++
        }

        playCurrentItem(preserveError = preserveError)
    }

    fun goToPrevious() {
        durationTimerJob?.cancel()
        val playlist = activePlaylist ?: return
        if (playlist.items.isEmpty()) return

        currentIndex = if (currentIndex <= 0) {
            playlist.items.size - 1
        } else {
            currentIndex - 1
        }

        playCurrentItem()
    }

    fun reportItemError(itemId: String, error: String) {
        _state.value = _state.value.copy(lastError = "Error in $itemId: $error")
        // Digital Signage Invariant: Never stall on error; advance to next media item
        advanceToNext(preserveError = true)
    }

    fun togglePlayPause() {
        val newPlaying = !_state.value.isPlaying
        _state.value = _state.value.copy(isPlaying = newPlaying)
        if (!newPlaying) {
            durationTimerJob?.cancel()
        } else {
            _state.value.currentItem?.let { item ->
                if (item.type == MediaType.IMAGE) {
                    val remainingDuration = ((1f - _state.value.progress) * item.displayDurationSeconds).toInt()
                    startImageDurationTimer(remainingDuration.coerceAtLeast(1))
                }
            }
        }
    }

    fun stop() {
        durationTimerJob?.cancel()
        _state.value = PlaybackControllerState()
    }
}

