package com.adveno.tv.playback.cache

import android.content.Context
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class MediaCacheManager(private val context: Context) {

    val cacheDir: File by lazy {
        val dir = File(context.filesDir, "media_cache")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    suspend fun getTempStagingFile(mediaId: String): File = withContext(Dispatchers.IO) {
        File(cacheDir, "download_${mediaId}.tmp")
    }

    suspend fun promoteStagedFile(mediaId: String, extension: String): File = withContext(Dispatchers.IO) {
        val tempFile = File(cacheDir, "download_${mediaId}.tmp")
        val cleanExt = extension.trimStart('.').ifEmpty { "media" }
        val targetFile = File(cacheDir, "media_${mediaId}.${cleanExt}")

        if (tempFile.exists()) {
            if (targetFile.exists()) {
                targetFile.delete()
            }
            val renamed = tempFile.renameTo(targetFile)
            if (!renamed) {
                // Fallback copy & delete if atomic rename fails across boundaries
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
        }
        targetFile
    }

    suspend fun discardStagedFile(mediaId: String) = withContext(Dispatchers.IO) {
        val tempFile = File(cacheDir, "download_${mediaId}.tmp")
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    suspend fun getCachedFile(mediaId: String, extension: String): File? = withContext(Dispatchers.IO) {
        val cleanExt = extension.trimStart('.').ifEmpty { "media" }
        val targetFile = File(cacheDir, "media_${mediaId}.${cleanExt}")
        if (targetFile.exists() && targetFile.length() > 0) targetFile else null
    }

    suspend fun verifyFileChecksum(file: File, expectedSha256: String?): Boolean = withContext(Dispatchers.IO) {
        if (!file.exists() || file.length() == 0L) return@withContext false
        if (expectedSha256.isNullOrBlank()) return@withContext true // Skip if no hash provided

        try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val computedHash = digest.digest().joinToString("") { "%02x".format(it) }
            computedHash.equals(expectedSha256.trim(), ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun pruneOrphanedMedia(protectedMediaIds: Set<String>) = withContext(Dispatchers.IO) {
        val allFiles = cacheDir.listFiles() ?: return@withContext
        for (file in allFiles) {
            val name = file.name
            // Never delete active downloads or protected in-use playlist assets
            val isProtected = protectedMediaIds.any { id -> name.contains(id) }
            if (!isProtected && !name.endsWith(".tmp")) {
                file.delete()
            }
        }
    }

    suspend fun getCacheSizeBytes(): Long = withContext(Dispatchers.IO) {
        cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    suspend fun getAvailableDiskSpaceBytes(): Long = withContext(Dispatchers.IO) {
        try {
            val stat = StatFs(context.filesDir.path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }
}

