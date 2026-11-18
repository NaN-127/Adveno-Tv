package com.adveno.tv.data.pairing

import com.adveno.tv.domain.model.PairingState
import com.adveno.tv.domain.model.PairingType
import com.adveno.tv.domain.repository.DeviceRepository
import com.adveno.tv.domain.repository.PairingRepository
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
import java.util.UUID
import kotlin.random.Random

/**
 * Mock implementation of PairingRepository.
 * Provides deterministic simulation of the pairing lifecycle:
 * 1. Shows QR with countdown.
 * 2. Emits Connecting when customer begins connection (showing connecting animation).
 * 3. On Paired status:
 *    - New Screen: creates screen, obtains credentials, stores in Keystore/DeviceRepo.
 *    - Reconnect Existing: requests new credentials, replaces in Keystore/DeviceRepo.
 *    - Emits PairedSuccess and navigates to main screen.
 */
class MockPairingRepositoryImpl(
    private val deviceRepository: DeviceRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : PairingRepository {

    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    override val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()

    private var countdownJob: Job? = null
    private var currentScreenId: String = ""
    private var currentSessionId: String = ""
    private var currentPairingToken: String = ""
    private var currentPairingType: PairingType = PairingType.NEW_SCREEN

    override suspend fun startPairingSession(screenId: String, type: PairingType) {
        currentScreenId = screenId
        currentPairingType = type
        _pairingState.value = PairingState.Generating

        countdownJob?.cancel()

        currentSessionId = "sess_" + UUID.randomUUID().toString().take(12)
        val codeSuffix = Random.nextInt(1000, 9999).toString()
        val pairingCode = "ADV-$codeSuffix"
        val qrPayload = "adveno://pair?screenId=$screenId&code=$pairingCode&v=1"
        currentPairingToken = qrPayload

        val totalDurationSeconds = 600
        var remaining = totalDurationSeconds

        _pairingState.value = PairingState.AwaitingPairing(
            screenId = screenId,
            pairingCode = pairingCode,
            qrPayload = qrPayload,
            pairingToken = currentPairingToken,
            sessionId = currentSessionId,
            pairingType = currentPairingType,
            secondsRemaining = remaining,
            totalDurationSeconds = totalDurationSeconds
        )

        countdownJob = scope.launch {
            while (isActive && remaining > 0) {
                delay(1000)
                remaining--
                val currentState = _pairingState.value
                if (currentState is PairingState.AwaitingPairing) {
                    _pairingState.value = currentState.copy(secondsRemaining = remaining)
                }
            }
            if (remaining <= 0 && _pairingState.value is PairingState.AwaitingPairing) {
                _pairingState.value = PairingState.Expired(screenId)
            }
        }
    }

    override suspend fun refreshPairingSession(screenId: String) {
        startPairingSession(screenId, currentPairingType)
    }

    override suspend fun cancelPairingSession() {
        countdownJob?.cancel()
        _pairingState.value = PairingState.Idle
    }

    fun simulateConnectingState() {
        _pairingState.value = PairingState.Connecting(
            sessionId = currentSessionId,
            pairingToken = currentPairingToken,
            pairingType = currentPairingType
        )
    }

    override suspend fun simulateRemoteCustomerPairing(
        companyId: String,
        companyName: String,
        screenName: String,
        type: PairingType
    ) {
        countdownJob?.cancel()

        val screenId = if (currentScreenId.isNotEmpty()) currentScreenId else deviceRepository.getScreenId()
        val credential = "cred_" + UUID.randomUUID().toString().replace("-", "")

        // Revised flow:
        // New Screen + Status Paired: create screen and get credentials
        // Reconnecting + Status Paired: request for credential renewal
        deviceRepository.setPaired(
            isPaired = true,
            screenName = screenName,
            companyName = companyName,
            authToken = credential
        )

        _pairingState.value = PairingState.PairedSuccess(
            companyId = companyId,
            companyName = companyName,
            screenName = screenName,
            screenId = screenId
        )
    }
}
