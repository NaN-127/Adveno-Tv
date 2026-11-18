package com.adveno.tv.domain.model

enum class PairingType {
    NEW_SCREEN,
    RECONNECT_EXISTING
}

enum class PairingSessionStatus {
    PENDING,
    CONNECTING,
    PAIRED,
    EXPIRED,
    ERROR
}

data class PairingSession(
    val sessionId: String,
    val pairingToken: String,
    val pairingType: PairingType,
    val expiresAt: String
)

data class DeviceCredential(
    val screenId: String,
    val credential: String,
    val screenCode: String? = null,
    val screenName: String? = null
)

sealed interface PairingState {
    data object Idle : PairingState
    data object Generating : PairingState

    data class AwaitingPairing(
        val screenId: String = "ADV-TV",
        val pairingCode: String = "ADV-1234",
        val qrPayload: String = "",
        val pairingToken: String = qrPayload,
        val sessionId: String = "",
        val pairingType: PairingType = PairingType.NEW_SCREEN,
        val secondsRemaining: Int = 600,
        val totalDurationSeconds: Int = 600
    ) : PairingState

    data class Connecting(
        val sessionId: String = "",
        val pairingToken: String = "",
        val pairingType: PairingType = PairingType.NEW_SCREEN
    ) : PairingState

    data class PairedSuccess(
        val companyId: String = "comp_adveno",
        val companyName: String = "Adveno Network",
        val screenName: String = "Living Room TV",
        val screenId: String = "ADV-TV"
    ) : PairingState

    data class Expired(val screenId: String) : PairingState
    data class Failed(val reason: String) : PairingState
}
