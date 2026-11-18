package com.adveno.tv.domain.repository

import com.adveno.tv.domain.model.DeviceIdentity
import com.adveno.tv.domain.model.DeviceRegistrationStatus
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeRegistrationStatus(): Flow<DeviceRegistrationStatus>
    suspend fun getScreenId(): String
    suspend fun getDeviceIdentity(): DeviceIdentity
    suspend fun setPaired(
        isPaired: Boolean,
        screenName: String?,
        companyName: String?,
        authToken: String?,
        remoteScreenId: String? = null
    )
    suspend fun clearDeviceRegistration()
}

