package com.adveno.tv.core.telemetry

import com.adveno.tv.core.network.ConnectivityMonitor
import com.adveno.tv.domain.repository.DeviceRepository
import com.adveno.tv.domain.repository.MediaRepository
import com.adveno.tv.domain.repository.PlaylistRepository
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

data class HeartbeatPayload(
    val screenId: String,
    val deviceModel: String,
    val appVersion: String,
    val isOnline: Boolean,
    val currentPlaylistId: String?,
    val currentPlaylistVersion: Long?,
    val storageUsedBytes: Long,
    val availableStorageBytes: Long,
    val timestamp: Long
)

data class HeartbeatStatus(
    val isRunning: Boolean = false,
    val lastHeartbeatTimestamp: Long? = null,
    val totalHeartbeatsSent: Long = 0L,
    val lastPayload: HeartbeatPayload? = null,
    val lastError: String? = null
)

class HeartbeatMonitor(
    private val deviceRepository: DeviceRepository,
    private val playlistRepository: PlaylistRepository,
    private val mediaRepository: MediaRepository,
    private val networkMonitor: ConnectivityMonitor,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _status = MutableStateFlow(HeartbeatStatus())
    val status: StateFlow<HeartbeatStatus> = _status.asStateFlow()

    private var heartbeatJob: Job? = null
    var intervalMs: Long = 30_000L // Configurable default 30s

    fun start() {
        if (heartbeatJob?.isActive == true) return
        _status.value = _status.value.copy(isRunning = true)

        heartbeatJob = scope.launch {
            var backoffDelay = intervalMs

            while (isActive) {
                val isOnline = networkMonitor.checkCurrentConnectivity()

                if (isOnline) {
                    val result = sendHeartbeat()
                    if (result.isSuccess) {
                        backoffDelay = intervalMs
                    } else {
                        // Exponential backoff up to 2 minutes on error
                        backoffDelay = (backoffDelay * 2).coerceAtMost(120_000L)
                    }
                }

                delay(backoffDelay)
            }
        }
    }

    suspend fun sendHeartbeat(): Result<HeartbeatPayload> {
        return try {
            val identity = deviceRepository.getDeviceIdentity()
            val playlist = playlistRepository.getActivePlaylist()
            val usedStorage = mediaRepository.getCacheStorageUsedBytes()
            val freeStorage = mediaRepository.getAvailableDiskSpaceBytes()
            val isOnline = networkMonitor.checkCurrentConnectivity()

            val payload = HeartbeatPayload(
                screenId = identity.screenId,
                deviceModel = identity.deviceName,
                appVersion = identity.appVersion,
                isOnline = isOnline,
                currentPlaylistId = playlist?.id,
                currentPlaylistVersion = playlist?.version,
                storageUsedBytes = usedStorage,
                availableStorageBytes = freeStorage,
                timestamp = System.currentTimeMillis()
            )

            // Simulating transmitting telemetry to Firebase Firestore document 'screens/{screenId}/heartbeat'
            val count = _status.value.totalHeartbeatsSent + 1
            _status.value = _status.value.copy(
                lastHeartbeatTimestamp = payload.timestamp,
                totalHeartbeatsSent = count,
                lastPayload = payload,
                lastError = null
            )

            Result.success(payload)
        } catch (e: Exception) {
            _status.value = _status.value.copy(lastError = e.localizedMessage)
            Result.failure(e)
        }
    }

    fun stop() {
        heartbeatJob?.cancel()
        _status.value = _status.value.copy(isRunning = false)
    }
}

