package com.adveno.tv.feature.device

import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adveno.tv.core.network.ConnectivityMonitor
import com.adveno.tv.domain.model.DeviceIdentity
import com.adveno.tv.domain.model.MediaType
import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.model.PlaylistItem
import com.adveno.tv.domain.model.SyncStatus
import com.adveno.tv.domain.repository.DeviceRepository
import com.adveno.tv.domain.repository.SynchronizationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceStatusUiState(
    val isLoading: Boolean = true,
    val deviceIdentity: DeviceIdentity? = null,
    val totalStorageGb: String = "--",
    val freeStorageGb: String = "--",
    val isOnline: Boolean = true,
    val syncStatusText: String = "Idle",
    val lastSyncFormatted: String = "Just now",
    val isSyncing: Boolean = false,
    val isUnpaired: Boolean = false
)

class DeviceStatusViewModel(
    private val deviceRepository: DeviceRepository,
    private val syncRepository: SynchronizationRepository,
    private val networkMonitor: ConnectivityMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceStatusUiState())
    val uiState: StateFlow<DeviceStatusUiState> = _uiState.asStateFlow()

    init {
        loadDeviceStatus()
        observeNetwork()
        observeSyncStatus()
    }

    fun loadDeviceStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val identity = deviceRepository.getDeviceIdentity()

            val (totalGb, freeGb) = withContext(Dispatchers.IO) {
                runCatching {
                    val stat = StatFs(Environment.getDataDirectory().path)
                    val totalBytes = stat.blockCountLong * stat.blockSizeLong
                    val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
                    val total = String.format("%.1f GB", totalBytes / (1024.0 * 1024.0 * 1024.0))
                    val free = String.format("%.1f GB", freeBytes / (1024.0 * 1024.0 * 1024.0))
                    total to free
                }.getOrDefault("--" to "--")
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                deviceIdentity = identity,
                totalStorageGb = totalGb,
                freeStorageGb = freeGb,
                isUnpaired = false
            )
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                val wasOffline = !_uiState.value.isOnline
                _uiState.value = _uiState.value.copy(isOnline = online)

                // Reactive reconciliation: Reconnecting triggers automatic update verification
                if (wasOffline && online) {
                    syncRepository.checkForPlaylistUpdates()
                }
            }
        }
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            syncRepository.syncStatus.collect { status ->
                val (statusText, isSyncing) = when (status) {
                    is SyncStatus.Idle -> "Idle" to false
                    is SyncStatus.CheckingForUpdates -> "Checking for updates..." to true
                    is SyncStatus.DownloadingMedia -> "Downloading: ${status.playlistName} (${status.currentItemIndex + 1}/${status.totalItemsToDownload})" to true
                    is SyncStatus.PromotingPlaylist -> "Activating ${status.playlistName} (v${status.version})..." to true
                    is SyncStatus.SyncSuccess -> status.message to false
                    is SyncStatus.SyncFailed -> "Sync failed: ${status.reason}" to false
                }
                _uiState.value = _uiState.value.copy(
                    syncStatusText = statusText,
                    isSyncing = isSyncing
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

    fun triggerSync() {
        viewModelScope.launch {
            syncRepository.checkForPlaylistUpdates()
        }
    }

    fun simulateFCMUpdate() {
        viewModelScope.launch {
            val updatedPlaylist = Playlist(
                id = "pl_default_kannur",
                name = "Evening Flash Specials (Mobile App Update)",
                version = System.currentTimeMillis() / 1000,
                items = listOf(
                    PlaylistItem(
                        id = "item_new_1",
                        name = "50% Off Beverages Poster",
                        type = MediaType.IMAGE,
                        remoteUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=1920&q=80",
                        displayDurationSeconds = 10,
                        orderIndex = 0
                    ),
                    PlaylistItem(
                        id = "item_new_2",
                        name = "Live Kitchen Cooking Showcase",
                        type = MediaType.VIDEO,
                        remoteUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                        displayDurationSeconds = 15,
                        orderIndex = 1
                    )
                )
            )
            syncRepository.simulateRemoteUpdateNotification(updatedPlaylist)
        }
    }

    fun unpairDevice() {
        viewModelScope.launch {
            deviceRepository.clearDeviceRegistration()
            _uiState.value = _uiState.value.copy(isUnpaired = true)
        }
    }
}
