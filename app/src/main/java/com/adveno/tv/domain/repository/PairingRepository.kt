package com.adveno.tv.domain.repository

import com.adveno.tv.domain.model.PairingState
import com.adveno.tv.domain.model.PairingType
import kotlinx.coroutines.flow.StateFlow

interface PairingRepository {
    val pairingState: StateFlow<PairingState>
    suspend fun startPairingSession(screenId: String = "ADV-TV", type: PairingType = PairingType.NEW_SCREEN)
    suspend fun refreshPairingSession(screenId: String = "ADV-TV")
    suspend fun cancelPairingSession()
    suspend fun simulateRemoteCustomerPairing(
        companyId: String = "comp_adveno",
        companyName: String = "Adveno Network",
        screenName: String = "Living Room TV",
        type: PairingType = PairingType.NEW_SCREEN
    )
}
