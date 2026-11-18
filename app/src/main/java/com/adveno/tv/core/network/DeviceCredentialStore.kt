package com.adveno.tv.core.network

import com.adveno.tv.core.datastore.DevicePreferences
import java.util.concurrent.atomic.AtomicReference

/**
 * In-memory view of the Keystore-encrypted device credential. OkHttp reads this
 * value synchronously, while persistence remains in [DevicePreferences].
 */
class DeviceCredentialStore(
    private val preferences: DevicePreferences
) {
    private val credential = AtomicReference<String?>(null)

    suspend fun warmUp() {
        credential.set(preferences.getAuthToken())
    }

    fun current(): String? = credential.get()

    fun update(value: String?) {
        credential.set(value)
    }
}
