package com.adveno.tv.feature.initialization

import app.cash.turbine.test
import com.adveno.tv.domain.model.DeviceRegistrationStatus
import com.adveno.tv.domain.usecase.DetermineAppLaunchDestinationUseCase
import com.adveno.tv.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitializationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is Loading and navigates to Pairing when unregistered`() = runTest {
        val repo = FakeDeviceRepository(status = DeviceRegistrationStatus.UNREGISTERED)
        val useCase = DetermineAppLaunchDestinationUseCase(repo)
        val viewModel = InitializationViewModel(useCase)

        viewModel.uiState.test {
            // Initial state is Loading
            val initial = awaitItem()
            assertEquals(InitializationUiState.Loading, initial)

            // Advance the splash delay
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1300)

            // Emits NavigateToPairing
            val result = awaitItem()
            assertEquals(InitializationUiState.NavigateToPairing, result)
        }
    }

    @Test
    fun `initializes and navigates to Playback when device is already paired`() = runTest {
        val repo = FakeDeviceRepository(status = DeviceRegistrationStatus.PAIRED)
        val useCase = DetermineAppLaunchDestinationUseCase(repo)
        val viewModel = InitializationViewModel(useCase)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(InitializationUiState.Loading, initial)

            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1300)

            val result = awaitItem()
            assertEquals(InitializationUiState.NavigateToPlayback, result)
        }
    }
}

