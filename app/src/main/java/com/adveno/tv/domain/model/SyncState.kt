package com.adveno.tv.domain.model

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object CheckingForUpdates : SyncStatus

    data class DownloadingMedia(
        val playlistName: String,
        val currentItemIndex: Int,
        val totalItemsToDownload: Int,
        val progress: Float
    ) : SyncStatus

    data class PromotingPlaylist(
        val playlistName: String,
        val version: Long
    ) : SyncStatus

    data class SyncSuccess(
        val timestamp: Long,
        val version: Long,
        val message: String
    ) : SyncStatus

    data class SyncFailed(
        val reason: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : SyncStatus
}

