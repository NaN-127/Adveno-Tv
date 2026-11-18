package com.adveno.tv.data.pairing

import android.os.Build
import android.util.Log
import com.adveno.tv.data.pairing.model.RegisterScreenResponse
import com.adveno.tv.data.pairing.model.RenewCredentialResponse
import com.adveno.tv.data.pairing.network.PairingApiClient
import com.adveno.tv.domain.model.DeviceCredential
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

private const val TAG = "AdvenoPairing"

/**
 * Pairing state machine defined by the current backend contract:
 * CONNECTING starts exactly one type-specific credential request; PAIRED confirms
 * that result and is the only state that persists it or advances to playback.
 */
class RealPairingRepositoryImpl(
    private val deviceRepository: DeviceRepository,
    private val apiClient: PairingApiClient = PairingApiClient(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : PairingRepository {

    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    override val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()

    private var pollingJob: Job? = null
    private var currentScreenId = ""
    private var currentPairingType = PairingType.NEW_SCREEN
    private var pendingCredential: DeviceCredential? = null

    override suspend fun startPairingSession(screenId: String, type: PairingType) {
        currentScreenId = screenId
        currentPairingType = type
        pendingCredential = null
        _pairingState.value = PairingState.Generating
        pollingJob?.cancel()

        val sessionResult = apiClient.createSession(type)
        if (sessionResult.isFailure) {
            _pairingState.value = PairingState.Failed(
                sessionResult.exceptionOrNull()?.message ?: "Failed to create pairing session"
            )
            return
        }

        val session = sessionResult.getOrThrow()
        val totalSeconds = session.expires_at.secondsUntilExpiry().coerceAtLeast(1)
        _pairingState.value = PairingState.AwaitingPairing(
            screenId = screenId,
            pairingCode = "ADV-" + session.pairing_token.takeLast(4).uppercase(),
            qrPayload = session.pairing_token,
            pairingToken = session.pairing_token,
            sessionId = session.session_id,
            pairingType = type,
            secondsRemaining = totalSeconds,
            totalDurationSeconds = totalSeconds
        )
        startPollingLoop(session.session_id, session.pairing_token, screenId, type)
    }

    private fun startPollingLoop(
        sessionId: String,
        pairingToken: String,
        localScreenId: String,
        type: PairingType
    ) {
        pollingJob = scope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                val statusResult = apiClient.pollSessionStatus(sessionId, pairingToken)
                if (statusResult.isFailure) continue

                when (statusResult.getOrThrow().status) {
                    "PENDING" -> {
                        // Keep QR code visible and continue polling
                    }
                    "CONNECTING" -> {
                        // Connection started -> show connecting animation screen and CONTINUE polling
                        Log.i(TAG, "Connection started (status=CONNECTING). Showing connecting animation screen and continuing poll.")
                        if (_pairingState.value !is PairingState.Connecting) {
                            _pairingState.value = PairingState.Connecting(
                                sessionId = sessionId,
                                pairingToken = pairingToken,
                                pairingType = type
                            )
                        }
                        // Continue polling; do NOT request credential or screen here.
                    }
                    "PAIRED" -> {
                        // Status is PAIRED -> Stop polling and create screen or request credential
                        Log.i(TAG, "Status is PAIRED! Requesting screen creation / credential renewal now.")
                        val finalResult = requestFinalCredential(sessionId, pairingToken, type)
                        if (finalResult.isFailure) {
                            val errorMsg = finalResult.exceptionOrNull()?.message
                                ?: "Unable to issue device credential"
                            Log.e(TAG, "Failed to get credential on PAIRED: $errorMsg")
                            _pairingState.value = PairingState.Failed(errorMsg)
                            break
                        }

                        val credential = finalResult.getOrThrow()
                        Log.i(TAG, "Successfully obtained credential for screen_id=${credential.screenId}. Persisting credentials and transitioning to main screen.")
                        runCatching {
                            deviceRepository.setPaired(
                                isPaired = true,
                                screenName = credential.screenName ?: credential.screenId,
                                companyName = "Adveno Network",
                                authToken = credential.credential,
                                remoteScreenId = credential.screenId
                            )
                        }.onSuccess {
                            _pairingState.value = PairingState.PairedSuccess(
                                companyId = "comp_adveno",
                                companyName = "Adveno Network",
                                screenName = credential.screenName ?: credential.screenId,
                                screenId = credential.screenId
                            )
                            pendingCredential = null
                        }.onFailure { error ->
                            Log.e(TAG, "Unable to securely store device credential: ${error.localizedMessage}")
                            _pairingState.value = PairingState.Failed(
                                "Unable to securely store device credential: ${error.localizedMessage}"
                            )
                        }
                        break
                    }
                    "EXPIRED" -> {
                        Log.w(TAG, "Pairing session expired.")
                        _pairingState.value = PairingState.Expired(localScreenId)
                        break
                    }
                    else -> {
                        Log.w(TAG, "Unknown pairing status: ${statusResult.getOrThrow().status}")
                    }
                }
            }
        }
    }

    private suspend fun requestFinalCredential(
        sessionId: String,
        pairingToken: String,
        type: PairingType
    ): Result<DeviceCredential> = when (type) {
        PairingType.NEW_SCREEN -> apiClient.createPairedScreen(
            sessionId = sessionId,
            pairingToken = pairingToken,
            name = "Android TV",
            deviceModel = Build.MODEL,
            appVersion = deviceRepository.getDeviceIdentity().appVersion
        ).map { it.toDeviceCredential() }

        PairingType.RECONNECT_EXISTING -> apiClient.renewCredential(
            sessionId = sessionId,
            pairingToken = pairingToken
        ).map { it.toDeviceCredential() }
    }

    override suspend fun refreshPairingSession(screenId: String) {
        startPairingSession(screenId, currentPairingType)
    }

    override suspend fun cancelPairingSession() {
        pollingJob?.cancel()
        pendingCredential = null
        _pairingState.value = PairingState.Idle
    }

    override suspend fun simulateRemoteCustomerPairing(
        companyId: String,
        companyName: String,
        screenName: String,
        type: PairingType
    ) {
        pollingJob?.cancel()
        val screenId = if (currentScreenId.isNotEmpty()) currentScreenId else deviceRepository.getScreenId()
        val credential = "cred_" + UUID.randomUUID().toString().replace("-", "")
        deviceRepository.setPaired(true, screenName, companyName, credential)
        _pairingState.value = PairingState.PairedSuccess(companyId, companyName, screenName, screenId)
    }

    private companion object {
        const val POLL_INTERVAL_MS = 2_000L
    }
}

private fun RegisterScreenResponse.toDeviceCredential() = DeviceCredential(
    screenId = screen_id,
    credential = credential,
    screenCode = screen_code,
    screenName = name
)

private fun RenewCredentialResponse.toDeviceCredential() = DeviceCredential(
    screenId = screen_id,
    credential = credential
)

/** Parses API ISO-8601 timestamps without requiring API-26 java.time. */
private fun String.secondsUntilExpiry(nowMillis: Long = System.currentTimeMillis()): Int {
    val normalized = replace(Regex("\\.\\d+(?=(Z|[+-]\\d{2}:\\d{2})$)"), "")
    val formats = listOf(
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US),
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
    )
    val expiryMillis = formats.firstNotNullOfOrNull { format ->
        runCatching { format.apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.parse(normalized)?.time }.getOrNull()
    } ?: return 600
    return ((expiryMillis - nowMillis) / 1_000L).toInt()
}
