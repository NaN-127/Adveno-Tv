package com.adveno.tv.domain.model

enum class MediaType {
    IMAGE,
    VIDEO
}

enum class DisplayScaleMode {
    FIT,
    FILL,
    CROP
}

data class PlaylistItem(
    val id: String,
    val name: String,
    val type: MediaType,
    val remoteUrl: String,
    val localFilePath: String? = null,
    val displayDurationSeconds: Int = 10,
    val orderIndex: Int = 0,
    val checksumSha256: String? = null
) {
    /**
     * Resolves the playable URI: prefers local cached file path, falls back to remoteUrl.
     */
    val playableUri: String
        get() = localFilePath ?: remoteUrl
}

data class Playlist(
    val id: String,
    val name: String,
    val version: Long = 1L,
    val items: List<PlaylistItem> = emptyList(),
    val loopCount: Long = 0L
)

