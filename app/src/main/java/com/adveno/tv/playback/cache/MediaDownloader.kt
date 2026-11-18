package com.adveno.tv.playback.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MediaDownloader(
    private val cacheManager: MediaCacheManager
) {

    suspend fun downloadMediaItem(
        mediaId: String,
        remoteUrl: String,
        extension: String,
        expectedSha256: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        // Check if already cached and verified
        val existing = cacheManager.getCachedFile(mediaId, extension)
        if (existing != null && cacheManager.verifyFileChecksum(existing, expectedSha256)) {
            return@withContext Result.success(existing)
        }

        val tempFile = cacheManager.getTempStagingFile(mediaId)

        try {
            val url = URL(remoteUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
            }

            if (connection.responseCode !in 200..299) {
                cacheManager.discardStagedFile(mediaId)
                return@withContext Result.failure(
                    IllegalStateException("Server returned HTTP ${connection.responseCode} for $remoteUrl")
                )
            }

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output, bufferSize = 16384)
                }
            }

            // Verify integrity
            if (!cacheManager.verifyFileChecksum(tempFile, expectedSha256)) {
                cacheManager.discardStagedFile(mediaId)
                return@withContext Result.failure(
                    IllegalStateException("Checksum verification failed for media $mediaId")
                )
            }

            // Two-phase atomic promotion
            val promotedFile = cacheManager.promoteStagedFile(mediaId, extension)
            Result.success(promotedFile)
        } catch (e: Exception) {
            cacheManager.discardStagedFile(mediaId)
            Result.failure(e)
        }
    }
}

