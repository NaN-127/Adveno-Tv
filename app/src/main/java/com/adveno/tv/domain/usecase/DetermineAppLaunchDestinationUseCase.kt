package com.adveno.tv.domain.usecase

import com.adveno.tv.domain.model.AppLaunchDestination
import com.adveno.tv.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.first

class DetermineAppLaunchDestinationUseCase(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(): AppLaunchDestination {
        return try {
            // Ensure Screen ID is initialized and persistent
            deviceRepository.getScreenId()

            val registrationStatus = deviceRepository.observeRegistrationStatus().first()
            if (registrationStatus == com.adveno.tv.domain.model.DeviceRegistrationStatus.PAIRED) {
                AppLaunchDestination.Playback
            } else {
                AppLaunchDestination.Pairing
            }
        } catch (e: Exception) {
            AppLaunchDestination.Error(e.localizedMessage ?: "Failed to initialize device")
        }
    }
}

