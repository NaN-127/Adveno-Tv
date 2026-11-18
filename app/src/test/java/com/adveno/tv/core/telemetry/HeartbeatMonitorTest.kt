package com.adveno.tv.core.telemetry

import com.adveno.tv.core.network.ConnectivityMonitor
import com.adveno.tv.data.playlist.MockPlaylistRepositoryImpl
import com.adveno.tv.data.synchronization.FakeMediaRepository
import com.adveno.tv.feature.initialization.FakeDeviceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeConnectivityMonitor(var isConnected: Boolean = true) : ConnectivityMonitor {
    override val isOnline: Flow<Boolean> = flowOf(isConnected)
    override fun checkCurrentConnectivity(): Boolean = isConnected
}

@OptIn(ExperimentalCoroutinesApi::class)
class HeartbeatMonitorTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun `sendHeartbeat generates comprehensive telemetry payload and updates status`() = runTest {
        val deviceRepo = FakeDeviceRepository(screenId = "ADV-TEST-HEARTBEAT")
        val playlistRepo = MockPlaylistRepositoryImpl()
        val mediaRepo = FakeMediaRepository()
        val connectivityMonitor = FakeConnectivityMonitor(isConnected = true)

        val monitor = HeartbeatMonitor(
            deviceRepository = deviceRepo,
            playlistRepository = playlistRepo,
            mediaRepository = mediaRepo,
            networkMonitor = connectivityMonitor,
            scope = testScope
        )

        val result = monitor.sendHeartbeat()
        assertTrue("Heartbeat result should succeed", result.isSuccess)

        val payload = result.getOrThrow()
        assertEquals("ADV-TEST-HEARTBEAT", payload.screenId)
        assertEquals("1.0.0", payload.appVersion)
        assertNotNull(payload.currentPlaylistId)
        assertTrue(payload.storageUsedBytes > 0)
        assertTrue(payload.availableStorageBytes > 0)

        // Verify status was updated
        val status = monitor.status.value
        assertEquals(1L, status.totalHeartbeatsSent)
        assertNotNull(status.lastHeartbeatTimestamp)
        assertEquals(payload, status.lastPayload)
    }
}
