package com.adveno.tv.feature.diagnostics

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adveno.tv.core.kiosk.KioskCapability
import com.adveno.tv.core.kiosk.KioskManager
import com.adveno.tv.core.network.ConnectivityMonitor
import com.adveno.tv.core.telemetry.HeartbeatMonitor
import com.adveno.tv.core.telemetry.HeartbeatStatus
import com.adveno.tv.domain.model.DeviceIdentity
import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.repository.DeviceRepository
import com.adveno.tv.domain.repository.MediaRepository
import com.adveno.tv.domain.repository.PlaylistRepository
import com.adveno.tv.domain.repository.SynchronizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiagnosticsUiState(
    val deviceIdentity: DeviceIdentity? = null,
    val activePlaylist: Playlist? = null,
    val isOnline: Boolean = true,
    val heartbeatStatus: HeartbeatStatus = HeartbeatStatus(),
    val lastHeartbeatFormatted: String = "Never",
    val lastSyncFormatted: String = "Never",
    val cacheUsedMb: String = "--",
    val availableDiskGb: String = "--",
    val kioskCapability: KioskCapability = KioskCapability.USER_PINNED_LOCK_TASK,
    val isLockTaskActive: Boolean = false,
    val isPinging: Boolean = false
)

class DiagnosticsViewModel(
    private val deviceRepository: DeviceRepository,
    private val playlistRepository: PlaylistRepository,
    private val mediaRepository: MediaRepository,
    private val syncRepository: SynchronizationRepository,
    private val heartbeatMonitor: HeartbeatMonitor,
    private val networkMonitor: ConnectivityMonitor,
    private val kioskManager: KioskManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeStreams()
    }

    private fun loadData() {
        viewModelScope.launch {
            val identity = deviceRepository.getDeviceIdentity()
            val playlist = playlistRepository.getActivePlaylist()
            val usedBytes = mediaRepository.getCacheStorageUsedBytes()
            val freeBytes = mediaRepository.getAvailableDiskSpaceBytes()

            val usedMb = String.format("%.1f MB", usedBytes / (1024.0 * 1024.0))
            val freeGb = String.format("%.1f GB", freeBytes / (1024.0 * 1024.0 * 1024.0))

            _uiState.value = _uiState.value.copy(
                deviceIdentity = identity,
                activePlaylist = playlist,
                cacheUsedMb = usedMb,
                availableDiskGb = freeGb,
                kioskCapability = kioskManager.getKioskCapability(),
                isLockTaskActive = kioskManager.isLockTaskActive()
            )
        }
    }

    private fun observeStreams() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.value = _uiState.value.copy(isOnline = online)
            }
        }

        viewModelScope.launch {
            heartbeatMonitor.status.collect { status ->
                val formatted = if (status.lastHeartbeatTimestamp != null) {
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(status.lastHeartbeatTimestamp))
                } else "Never"

                _uiState.value = _uiState.value.copy(
                    heartbeatStatus = status,
                    lastHeartbeatFormatted = formatted
                )
            }
        }

        viewModelScope.launch {
            syncRepository.lastSyncTimestamp.collect { ts ->
                val formatted = if (ts != null && ts > 0) {
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ts))
                } else "Never"
                _uiState.value = _uiState.value.copy(lastSyncFormatted = formatted)
            }
        }
    }

    fun forceHeartbeatPing() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPinging = true)
            heartbeatMonitor.sendHeartbeat()
            _uiState.value = _uiState.value.copy(isPinging = false)
        }
    }

    fun toggleKioskLock(activity: Activity) {
        if (kioskManager.isLockTaskActive()) {
            kioskManager.stopLockTask(activity)
        } else {
            kioskManager.startLockTask(activity)
        }
        _uiState.value = _uiState.value.copy(isLockTaskActive = kioskManager.isLockTaskActive())
    }
}

