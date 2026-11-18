package com.adveno.tv.data.pairing.network

import android.util.Log
import com.adveno.tv.core.network.ApiConfig
import com.adveno.tv.data.pairing.model.CreateSessionResponse
import com.adveno.tv.data.pairing.model.RegisterScreenResponse
import com.adveno.tv.data.pairing.model.RenewCredentialResponse
import com.adveno.tv.data.pairing.model.SessionStatusResponse
import com.adveno.tv.domain.model.PairingType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "AdvenoNetwork"

class PairingApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun createSession(pairingType: PairingType): Result<CreateSessionResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "${ApiConfig.BASE_URL}/api/v1/pairing/sessions"
            val jsonBody = JSONObject().apply {
                put("pairing_type", pairingType.name)
            }.toString()

            Log.i(TAG, "--> POST $url | payload: $jsonBody")

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json")
                .header("ngrok-skip-browser-warning", "true")
                .header("User-Agent", "AdvenoTv/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                Log.i(TAG, "<-- Create pairing session [${response.code}]")

                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Create pairing session failed: HTTP ${response.code}"))
                }
                val json = JSONObject(bodyString)
                val result = CreateSessionResponse(
                    session_id = json.getString("session_id"),
                    pairing_token = json.getString("pairing_token"),
                    expires_at = json.getString("expires_at")
                )
                Result.success(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in createSession: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun pollSessionStatus(sessionId: String, pairingToken: String): Result<SessionStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "${ApiConfig.BASE_URL}/api/v1/pairing/sessions/$sessionId"
            val request = Request.Builder()
                .url(url)
                .get()
                .header("X-Pairing-Token", pairingToken)
                .header("ngrok-skip-browser-warning", "true")
                .header("User-Agent", "AdvenoTv/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (response.code != 200) {
                    Log.w(TAG, "<-- Poll status [${response.code}] from GET $url")
                } else {
                    Log.v(TAG, "<-- Poll status [200]: $bodyString")
                }

                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Pairing status request failed: HTTP ${response.code}"))
                }
                val json = JSONObject(bodyString)
                val status = json.getString("status")
                Result.success(SessionStatusResponse(status = status))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in pollSessionStatus: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Administrative operation from the backend contract.  It deliberately requires a
     * user bearer token in addition to the pairing token; an unattended TV never has
     * that token and must not try to manufacture one.
     */
    suspend fun createPairedScreen(
        sessionId: String,
        pairingToken: String,
        userAccessToken: String? = null,
        name: String,
        deviceModel: String?,
        appVersion: String?
    ): Result<RegisterScreenResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "${ApiConfig.BASE_URL}/api/v1/pairing/sessions/$sessionId/screen"
            val jsonBody = JSONObject().apply {
                put("name", name)
                put("device_model", deviceModel)
                put("app_version", appVersion)
            }.toString()

            Log.i(TAG, "--> POST $url (Register Screen) | payload: $jsonBody")

            val requestBuilder = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody(jsonMediaType))
                .header("X-Pairing-Token", pairingToken)
                .header("Content-Type", "application/json")
                .header("ngrok-skip-browser-warning", "true")
                .header("User-Agent", "AdvenoTv/1.0")
            userAccessToken?.takeIf { it.isNotBlank() }?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }
            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                Log.i(TAG, "<-- Create paired screen [${response.code}]")

                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Create paired screen failed: HTTP ${response.code}"))
                }
                val json = JSONObject(bodyString)
                val result = RegisterScreenResponse(
                    screen_id = json.getString("screen_id"),
                    screen_code = json.getString("screen_code"),
                    name = json.getString("name"),
                    credential = json.getString("credential")
                )
                Result.success(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create paired screen failed", e)
            Result.failure(e)
        }
    }

    suspend fun renewCredential(
        sessionId: String,
        pairingToken: String
    ): Result<RenewCredentialResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "${ApiConfig.BASE_URL}/api/v1/pairing/sessions/$sessionId/credential"
            Log.i(TAG, "--> POST $url (Renew Credential)")

            val request = Request.Builder()
                .url(url)
                // The documented credential endpoint has no request body.
                .post(ByteArray(0).toRequestBody())
                .header("X-Pairing-Token", pairingToken)
                .header("ngrok-skip-browser-warning", "true")
                .header("User-Agent", "AdvenoTv/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                Log.i(TAG, "<-- Issue device credential [${response.code}]")

                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Issue device credential failed: HTTP ${response.code}"))
                }
                val json = JSONObject(bodyString)
                val result = RenewCredentialResponse(
                    screen_id = json.getString("screen_id"),
                    credential = json.getString("credential")
                )
                Result.success(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in renewCredential: ${e.message}", e)
            Result.failure(e)
        }
    }
}
