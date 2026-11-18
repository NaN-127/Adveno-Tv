package com.adveno.tv.data.pairing.model

/**
 * Request payload for POST /api/v1/pairing/sessions
 */
data class CreateSessionRequest(
    val pairing_type: String
)

/**
 * Response payload for POST /api/v1/pairing/sessions (201 Created)
 */
data class CreateSessionResponse(
    val session_id: String,
    val pairing_token: String,
    val expires_at: String
)

/**
 * Response payload for GET /api/v1/pairing/sessions/{session_id} (200 OK)
 */
data class SessionStatusResponse(
    val status: String // PENDING, CONNECTING, PAIRED, EXPIRED
)

/**
 * Request payload for POST /api/v1/pairing/sessions/{session_id}/screen
 */
data class RegisterScreenRequest(
    val name: String,
    val device_model: String,
    val app_version: String
)

/**
 * Response payload for POST /api/v1/pairing/sessions/{session_id}/screen (201 Created)
 */
data class RegisterScreenResponse(
    val screen_id: String,
    val screen_code: String?,
    val name: String,
    val credential: String
)

/**
 * Response payload for POST /api/v1/pairing/sessions/{session_id}/credential (200 OK)
 */
data class RenewCredentialResponse(
    val screen_id: String,
    val credential: String
)
