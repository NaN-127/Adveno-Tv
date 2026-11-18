package com.adveno.tv.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "adveno_device_prefs")

class DevicePreferences(private val context: Context) {

    private object Keys {
        val SCREEN_ID = stringPreferencesKey("screen_id")
        val REMOTE_SCREEN_ID = stringPreferencesKey("remote_screen_id")
        val IS_PAIRED = booleanPreferencesKey("is_paired")
        val SCREEN_NAME = stringPreferencesKey("screen_name")
        val COMPANY_NAME = stringPreferencesKey("company_name")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    val isPairedFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.IS_PAIRED] ?: false
        }

    suspend fun getOrCreateScreenId(): String {
        val currentPrefs = context.dataStore.data.first()
        val existingId = currentPrefs[Keys.SCREEN_ID]
        if (!existingId.isNullOrBlank()) {
            return existingId
        }

        // Generate persistent 8-character uppercase identifier: ADV-XXXX-XXXX
        val rawUuid = UUID.randomUUID().toString().replace("-", "").uppercase()
        val generatedId = "ADV-${rawUuid.substring(0, 4)}-${rawUuid.substring(4, 8)}"

        context.dataStore.edit { preferences ->
            preferences[Keys.SCREEN_ID] = generatedId
        }
        return generatedId
    }

    suspend fun getScreenName(): String? = context.dataStore.data.first()[Keys.SCREEN_NAME]
    suspend fun getCompanyName(): String? = context.dataStore.data.first()[Keys.COMPANY_NAME]
    suspend fun getAuthToken(): String? = context.dataStore.data.first()[Keys.AUTH_TOKEN]?.let { stored ->
        CredentialCipher.decrypt(stored)
    }
    suspend fun getRemoteScreenId(): String? = context.dataStore.data.first()[Keys.REMOTE_SCREEN_ID]

    suspend fun savePairingData(
        isPaired: Boolean,
        screenName: String?,
        companyName: String?,
        token: String?,
        remoteScreenId: String? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PAIRED] = isPaired
            if (screenName != null) prefs[Keys.SCREEN_NAME] = screenName else prefs.remove(Keys.SCREEN_NAME)
            if (companyName != null) prefs[Keys.COMPANY_NAME] = companyName else prefs.remove(Keys.COMPANY_NAME)
            if (token != null) prefs[Keys.AUTH_TOKEN] = CredentialCipher.encrypt(token) else prefs.remove(Keys.AUTH_TOKEN)
            if (remoteScreenId != null) prefs[Keys.REMOTE_SCREEN_ID] = remoteScreenId else prefs.remove(Keys.REMOTE_SCREEN_ID)
        }
    }

    suspend fun clearRegistration() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PAIRED] = false
            prefs.remove(Keys.SCREEN_NAME)
            prefs.remove(Keys.COMPANY_NAME)
            prefs.remove(Keys.AUTH_TOKEN)
            prefs.remove(Keys.REMOTE_SCREEN_ID)
        }
    }
}

/** Stores issued device credentials encrypted with an app-scoped Android Keystore key. */
private object CredentialCipher {
    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "adveno_device_credential_v1"
    private const val ENVELOPE_PREFIX = "v1:"

    fun encrypt(plainText: String): String = runCatching {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        ENVELOPE_PREFIX + Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }.getOrElse { throw IllegalStateException("Unable to securely store device credential", it) }

    fun decrypt(stored: String): String? {
        // Existing installations may contain a legacy value. It is returned only so a
        // later successful pairing can replace it with a Keystore-encrypted credential.
        if (!stored.startsWith(ENVELOPE_PREFIX)) return stored
        return runCatching {
            val parts = stored.split(':', limit = 3)
            require(parts.size == 3) { "Malformed credential envelope" }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                key(),
                GCMParameterSpec(128, Base64.decode(parts[1], Base64.NO_WRAP))
            )
            String(cipher.doFinal(Base64.decode(parts[2], Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrNull()
    }

    private fun key() = (KeyStore.getInstance(KEYSTORE).apply { load(null) }.getKey(KEY_ALIAS, null)
        ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }.generateKey())
}

