package com.adveno.tv.feature.pairing

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adveno.tv.core.common.QrCodeGenerator
import com.adveno.tv.domain.model.PairingState
import com.adveno.tv.domain.model.PairingType
import com.adveno.tv.domain.repository.DeviceRepository
import com.adveno.tv.domain.repository.PairingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PairingUiState {
    data object Loading : PairingUiState

    data class Ready(
        val screenId: String,
        val pairingCode: String,
        val pairingToken: String,
        val qrBitmap: Bitmap?,
        val secondsRemaining: Int,
        val formattedTimeRemaining: String,
        val progress: Float
    ) : PairingUiState

    data class Connecting(
        val sessionId: String,
        val pairingToken: String
    ) : PairingUiState

    data class Success(
        val companyName: String,
        val screenName: String
    ) : PairingUiState

    data class Expired(val screenId: String) : PairingUiState
    data class Error(val message: String) : PairingUiState
}

class PairingViewModel(
    private val pairingRepository: PairingRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PairingUiState>(PairingUiState.Loading)
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private var cachedQrPayload: String? = null
    private var cachedQrBitmap: Bitmap? = null

    init {
        initiatePairing()
        observePairing()
    }

    fun initiatePairing(type: PairingType = PairingType.NEW_SCREEN) {
        viewModelScope.launch {
            _uiState.value = PairingUiState.Loading
            val screenId = try { deviceRepository.getScreenId() } catch (e: Exception) { "ADV-TV" }
            pairingRepository.startPairingSession(screenId = screenId, type = type)
        }
    }

    private fun observePairing() {
        viewModelScope.launch {
            pairingRepository.pairingState.collect { state ->
                when (state) {
                    is PairingState.Idle, is PairingState.Generating -> {
                        _uiState.value = PairingUiState.Loading
                    }
                    is PairingState.AwaitingPairing -> {
                        if (cachedQrPayload != state.pairingToken || cachedQrBitmap == null) {
                            cachedQrPayload = state.pairingToken
                            cachedQrBitmap = QrCodeGenerator.generateQrBitmap(state.pairingToken)
                        }

                        val minutes = state.secondsRemaining / 60
                        val seconds = state.secondsRemaining % 60
                        val formattedTime = String.format("%02d:%02d", minutes, seconds)
                        val progress = state.secondsRemaining.toFloat() / state.totalDurationSeconds.toFloat()
                        val screenId = try { deviceRepository.getScreenId() } catch (e: Exception) { "ADV-TV" }

                        _uiState.value = PairingUiState.Ready(
                            screenId = state.screenId,
                            pairingCode = state.pairingCode,
                            pairingToken = state.pairingToken,
                            qrBitmap = cachedQrBitmap,
                            secondsRemaining = state.secondsRemaining,
                            formattedTimeRemaining = formattedTime,
                            progress = progress
                        )
                    }
                    is PairingState.Connecting -> {
                        _uiState.value = PairingUiState.Connecting(
                            sessionId = state.sessionId,
                            pairingToken = state.pairingToken
                        )
                    }
                    is PairingState.PairedSuccess -> {
                        _uiState.value = PairingUiState.Success(
                            companyName = state.companyName ?: "Adveno Network",
                            screenName = state.screenName
                        )
                    }
                    is PairingState.Expired -> {
                        _uiState.value = PairingUiState.Expired(state.screenId)
                    }
                    is PairingState.Failed -> {
                        _uiState.value = PairingUiState.Error(state.reason)
                    }
                }
            }
        }
    }

    fun refreshCode() {
        viewModelScope.launch {
            _uiState.value = PairingUiState.Loading
            pairingRepository.refreshPairingSession()
        }
    }

    fun simulateCustomerPairing(
        companyId: String = "comp_kannur_01",
        companyName: String = "Malabar Bistro Kannur",
        screenName: String = "Main Dining Display #1",
        type: PairingType = PairingType.NEW_SCREEN
    ) {
        viewModelScope.launch {
            pairingRepository.simulateRemoteCustomerPairing(
                companyId = companyId,
                companyName = companyName,
                screenName = screenName,
                type = type
            )
        }
    }
}
