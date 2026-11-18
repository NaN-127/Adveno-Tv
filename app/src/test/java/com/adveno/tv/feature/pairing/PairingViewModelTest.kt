package com.adveno.tv.feature.pairing

import app.cash.turbine.test
import com.adveno.tv.data.pairing.MockPairingRepositoryImpl
import com.adveno.tv.feature.initialization.FakeDeviceRepository
import com.adveno.tv.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PairingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun `viewModel starts pairing and emits Ready state with countdown and code`() = runTest {
        val fakeDeviceRepo = FakeDeviceRepository(screenId = "ADV-DEMO-9999")
        val pairingRepo = MockPairingRepositoryImpl(fakeDeviceRepo, testScope)
        val viewModel = PairingViewModel(pairingRepo, fakeDeviceRepo)

        viewModel.uiState.test {
            val item = awaitItem()
            // May start with Loading or directly Ready with UnconfinedTestDispatcher
            val ready = if (item is PairingUiState.Loading) awaitItem() else item
            assertTrue("Expected Ready state, got $ready", ready is PairingUiState.Ready)
            val readyState = ready as PairingUiState.Ready
            assertEquals("ADV-DEMO-9999", readyState.screenId)
            assertTrue(readyState.pairingCode.startsWith("ADV-"))
            assertEquals(600, readyState.secondsRemaining)
            assertEquals("10:00", readyState.formattedTimeRemaining)
        }
    }

    @Test
    fun `simulateCustomerPairing transitions state to Success`() = runTest {
        val fakeDeviceRepo = FakeDeviceRepository(screenId = "ADV-DEMO-9999")
        val pairingRepo = MockPairingRepositoryImpl(fakeDeviceRepo, testScope)
        val viewModel = PairingViewModel(pairingRepo, fakeDeviceRepo)

        viewModel.uiState.test {
            val initial = awaitItem()
            if (initial is PairingUiState.Loading) awaitItem()

            viewModel.simulateCustomerPairing(
                companyId = "comp_test",
                companyName = "Kannur Bakery",
                screenName = "Checkout Screen"
            )

            val success = awaitItem()
            assertTrue("Expected Success, got $success", success is PairingUiState.Success)
            val successState = success as PairingUiState.Success
            assertEquals("Kannur Bakery", successState.companyName)
            assertEquals("Checkout Screen", successState.screenName)
        }
    }
}

