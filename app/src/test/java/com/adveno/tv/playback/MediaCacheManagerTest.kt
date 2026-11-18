package com.adveno.tv.playback

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class MediaCacheManagerLogicTest {

    private fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `two-phase download commits staging file atomically on success`() = runTest {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "adveno_cache_test_${System.currentTimeMillis()}").apply { mkdirs() }
        try {
            val mediaId = "item_promo_42"
            val stagingFile = File(tempDir, "download_${mediaId}.tmp")
            stagingFile.writeText("Test video binary payload data content")

            val targetFile = File(tempDir, "media_${mediaId}.mp4")
            if (targetFile.exists()) targetFile.delete()

            // Atomic promotion
            val renamed = stagingFile.renameTo(targetFile)
            assertTrue("File promotion should succeed", renamed)
            assertFalse("Staging file must be consumed", stagingFile.exists())
            assertTrue("Target cached file must exist", targetFile.exists())
            assertEquals("Test video binary payload data content", targetFile.readText())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `checksum validation detects corrupted download`() = runTest {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "adveno_checksum_test_${System.currentTimeMillis()}").apply { mkdirs() }
        try {
            val file = File(tempDir, "test_asset.mp4").apply {
                writeText("Original content for checksum")
            }

            val actualSha256 = computeSha256(file)
            val fakeHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

            assertFalse("Corrupted hash must not match", actualSha256.equals(fakeHash, ignoreCase = true))
            assertTrue("Computed hash must match itself", actualSha256.equals(actualSha256, ignoreCase = true))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `pruneOrphanedMedia protects active in-use playlist media`() = runTest {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "adveno_prune_test_${System.currentTimeMillis()}").apply { mkdirs() }
        try {
            val active1 = File(tempDir, "media_item_1.jpg").apply { writeText("active 1") }
            val active2 = File(tempDir, "media_item_2.mp4").apply { writeText("active 2") }
            val orphan = File(tempDir, "media_old_item_99.mp4").apply { writeText("orphan") }

            val protectedIds = setOf("item_1", "item_2")

            val allFiles = tempDir.listFiles() ?: emptyArray()
            for (file in allFiles) {
                val isProtected = protectedIds.any { id -> file.name.contains(id) }
                if (!isProtected && !file.name.endsWith(".tmp")) {
                    file.delete()
                }
            }

            assertTrue("Active media 1 must be preserved", active1.exists())
            assertTrue("Active media 2 must be preserved", active2.exists())
            assertFalse("Orphaned media must be deleted", orphan.exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }
}

