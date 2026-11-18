package com.adveno.tv.data.device

import android.content.Context
import android.os.Build
import com.adveno.tv.core.datastore.DevicePreferences
import com.adveno.tv.core.network.DeviceCredentialStore
import com.adveno.tv.domain.model.DeviceIdentity
import com.adveno.tv.domain.model.DeviceRegistrationStatus
import com.adveno.tv.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceRepositoryImpl(
    private val context: Context,
    private val preferences: DevicePreferences,
    private val credentialStore: DeviceCredentialStore
) : DeviceRepository {

    override fun observeRegistrationStatus(): Flow<DeviceRegistrationStatus> {
        return preferences.isPairedFlow.map { isPaired ->
            if (isPaired) DeviceRegistrationStatus.PAIRED else DeviceRegistrationStatus.UNREGISTERED
        }
    }

    override suspend fun getScreenId(): String {
        return preferences.getOrCreateScreenId()
    }

    override suspend fun getDeviceIdentity(): DeviceIdentity {
        val screenId = preferences.getRemoteScreenId() ?: preferences.getOrCreateScreenId()
        val screenName = preferences.getScreenName()
        val companyName = preferences.getCompanyName()

        val packageInfo = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }.getOrNull()
        val versionName = packageInfo?.versionName ?: "1.0.0"

        return DeviceIdentity(
            screenId = screenId,
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            apiLevel = Build.VERSION.SDK_INT,
            appVersion = versionName,
            isPaired = screenName != null,
            registeredScreenName = screenName,
            companyName = companyName
        )
    }

    override suspend fun setPaired(
        isPaired: Boolean,
        screenName: String?,
        companyName: String?,
        authToken: String?,
        remoteScreenId: String?
    ) {
        preferences.savePairingData(isPaired, screenName, companyName, authToken, remoteScreenId)
        credentialStore.update(authToken)
    }

    override suspend fun clearDeviceRegistration() {
        preferences.clearRegistration()
        credentialStore.update(null)
    }
}

