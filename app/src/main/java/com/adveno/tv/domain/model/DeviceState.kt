package com.adveno.tv.domain.model

enum class DeviceRegistrationStatus {
    UNREGISTERED,
    PAIRING,
    PAIRED,
    DE_REGISTERED
}

sealed interface AppLaunchDestination {
    data object Loading : AppLaunchDestination
    data object Pairing : AppLaunchDestination
    data object Playback : AppLaunchDestination
    data class Error(val message: String) : AppLaunchDestination
}

