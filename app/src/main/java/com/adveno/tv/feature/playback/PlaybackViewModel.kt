package com.adveno.tv.feature.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adveno.tv.domain.model.DeviceIdentity
import com.adveno.tv.domain.model.PlaylistItem
import com.adveno.tv.domain.repository.DeviceRepository
import com.adveno.tv.domain.repository.PlaylistRepository
import com.adveno.tv.playback.controller.SignagePlaybackController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val currentItem: PlaylistItem? = null,
    val currentIndex: Int = 0,
    val totalItems: Int = 0,
    val progress: Float = 0f,
    val isPlaying: Boolean = true,
    val loopCount: Long = 0L,
    val playlistName: String = "",
    val deviceIdentity: DeviceIdentity? = null,
    val isOverlayVisible: Boolean = false,
    val hasNoContent: Boolean = false
)

class PlaybackViewModel(
    private val playlistRepository: PlaylistRepository,
    private val deviceRepository: DeviceRepository,
    val controller: SignagePlaybackController = SignagePlaybackController()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private var overlayDismissJob: Job? = null

    init {
        loadDeviceIdentity()
        observePlaylist()
        observeController()
    }

    private fun loadDeviceIdentity() {
        viewModelScope.launch {
            val identity = deviceRepository.getDeviceIdentity()
            _uiState.value = _uiState.value.copy(deviceIdentity = identity)
        }
    }

    private fun observePlaylist() {
        viewModelScope.launch {
            playlistRepository.observeActivePlaylist().collect { playlist ->
                if (playlist == null || playlist.items.isEmpty()) {
                    controller.setPlaylist(null)
                    _uiState.value = _uiState.value.copy(hasNoContent = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        playlistName = playlist.name,
                        hasNoContent = false
                    )
                    controller.setPlaylist(playlist)
                }
            }
        }
    }

    private fun observeController() {
        viewModelScope.launch {
            controller.state.collect { state ->
                _uiState.value = _uiState.value.copy(
                    currentItem = state.currentItem,
                    currentIndex = state.currentIndex,
                    totalItems = state.totalItems,
                    progress = state.progress,
                    isPlaying = state.isPlaying,
                    loopCount = state.loopCount,
                    hasNoContent = state.totalItems == 0
                )
            }
        }
    }

    fun nextItem() {
        controller.advanceToNext()
        showOverlayTemporarily()
    }

    fun previousItem() {
        controller.goToPrevious()
        showOverlayTemporarily()
    }

    fun togglePlayPause() {
        controller.togglePlayPause()
        showOverlayTemporarily()
    }

    fun onVideoCompleted() {
        controller.onVideoCompleted()
    }

    fun onMediaError(itemId: String, error: String) {
        controller.reportItemError(itemId, error)
    }

    fun updateVideoProgress(positionMs: Long, durationMs: Long) {
        controller.updateVideoProgress(positionMs, durationMs)
    }

    fun showOverlayTemporarily() {
        overlayDismissJob?.cancel()
        _uiState.value = _uiState.value.copy(isOverlayVisible = true)
        overlayDismissJob = viewModelScope.launch {
            delay(5000)
            _uiState.value = _uiState.value.copy(isOverlayVisible = false)
        }
    }

    fun hideOverlay() {
        overlayDismissJob?.cancel()
        _uiState.value = _uiState.value.copy(isOverlayVisible = false)
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            controller.stop()
            deviceRepository.clearDeviceRegistration()
            onLoggedOut()
        }
    }

    override fun onCleared() {
        super.onCleared()
        controller.stop()
    }
}

