package com.adveno.tv.feature.pairing

import com.adveno.tv.data.pairing.MockPairingRepositoryImpl
import com.adveno.tv.domain.model.PairingState
import com.adveno.tv.feature.initialization.FakeDeviceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockPairingRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun `startPairingSession initiates session with 600s timer and QR payload`() = runTest {
        val fakeDeviceRepo = FakeDeviceRepository()
        val repo = MockPairingRepositoryImpl(fakeDeviceRepo, testScope)

        repo.startPairingSession("ADV-TEST-0001")

        val state = repo.pairingState.value
        assertTrue("Expected AwaitingPairing, got $state", state is PairingState.AwaitingPairing)
        val awaiting = state as PairingState.AwaitingPairing
        assertEquals("ADV-TEST-0001", awaiting.screenId)
        assertTrue(awaiting.pairingCode.startsWith("ADV-"))
        assertTrue(awaiting.qrPayload.contains("adveno://pair"))
        assertEquals(600, awaiting.secondsRemaining)
    }

    @Test
    fun `simulateRemoteCustomerPairing updates device repo and emits PairedSuccess`() = runTest {
        val fakeDeviceRepo = FakeDeviceRepository()
        val repo = MockPairingRepositoryImpl(fakeDeviceRepo, testScope)

        repo.startPairingSession("ADV-TEST-0001")
        repo.simulateRemoteCustomerPairing(
            companyId = "comp_test_123",
            companyName = "Malabar Bistro",
            screenName = "Entrance Display"
        )

        val state = repo.pairingState.value
        assertTrue("Expected PairedSuccess, got $state", state is PairingState.PairedSuccess)
        val success = state as PairingState.PairedSuccess
        assertEquals("comp_test_123", success.companyId)
        assertEquals("Malabar Bistro", success.companyName)
        assertEquals("Entrance Display", success.screenName)
        assertTrue(fakeDeviceRepo.status == com.adveno.tv.domain.model.DeviceRegistrationStatus.PAIRED)
    }
}

