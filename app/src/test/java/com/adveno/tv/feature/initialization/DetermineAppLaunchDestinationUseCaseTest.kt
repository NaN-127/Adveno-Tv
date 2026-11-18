package com.adveno.tv.feature.initialization

import com.adveno.tv.domain.model.AppLaunchDestination
import com.adveno.tv.domain.model.DeviceIdentity
import com.adveno.tv.domain.model.DeviceRegistrationStatus
import com.adveno.tv.domain.repository.DeviceRepository
import com.adveno.tv.domain.usecase.DetermineAppLaunchDestinationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeDeviceRepository(
    var status: DeviceRegistrationStatus = DeviceRegistrationStatus.UNREGISTERED,
    var screenId: String = "ADV-TEST-1234",
    var shouldThrow: Boolean = false
) : DeviceRepository {

    override fun observeRegistrationStatus(): Flow<DeviceRegistrationStatus> {
        if (shouldThrow) throw IllegalStateException("Database error")
        return flowOf(status)
    }

    override suspend fun getScreenId(): String {
        if (shouldThrow) throw IllegalStateException("Keystore error")
        return screenId
    }

    override suspend fun getDeviceIdentity(): DeviceIdentity {
        return DeviceIdentity(
            screenId = screenId,
            deviceName = "Test TV",
            manufacturer = "Adveno",
            model = "Model X",
            androidVersion = "Android 14",
            apiLevel = 34,
            appVersion = "1.0.0",
            isPaired = status == DeviceRegistrationStatus.PAIRED
        )
    }

    override suspend fun setPaired(
        isPaired: Boolean,
        screenName: String?,
        companyName: String?,
        authToken: String?,
        remoteScreenId: String?
    ) {
        status = if (isPaired) DeviceRegistrationStatus.PAIRED else DeviceRegistrationStatus.UNREGISTERED
    }

    override suspend fun clearDeviceRegistration() {
        status = DeviceRegistrationStatus.UNREGISTERED
    }
}

class DetermineAppLaunchDestinationUseCaseTest {

    @Test
    fun `unregistered device routes to Pairing destination`() = runTest {
        val repo = FakeDeviceRepository(status = DeviceRegistrationStatus.UNREGISTERED)
        val useCase = DetermineAppLaunchDestinationUseCase(repo)

        val destination = useCase()

        assertEquals(AppLaunchDestination.Pairing, destination)
    }

    @Test
    fun `paired device routes to Playback destination`() = runTest {
        val repo = FakeDeviceRepository(status = DeviceRegistrationStatus.PAIRED)
        val useCase = DetermineAppLaunchDestinationUseCase(repo)

        val destination = useCase()

        assertEquals(AppLaunchDestination.Playback, destination)
    }

    @Test
    fun `exception during initialization routes to Error destination`() = runTest {
        val repo = FakeDeviceRepository(shouldThrow = true)
        val useCase = DetermineAppLaunchDestinationUseCase(repo)

        val destination = useCase()

        assertTrue(destination is AppLaunchDestination.Error)
        assertEquals("Keystore error", (destination as AppLaunchDestination.Error).message)
    }
}

