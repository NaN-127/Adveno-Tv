package com.adveno.tv.core.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.adveno.tv.domain.model.MediaType
import com.adveno.tv.domain.model.Playlist
import com.adveno.tv.domain.model.PlaylistItem

class AdvenoDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "adveno_signage.db"
        const val DATABASE_VERSION = 1

        // Tables
        const val TABLE_PLAYLISTS = "playlists"
        const val TABLE_PLAYLIST_ITEMS = "playlist_items"
        const val TABLE_CACHE_RECORDS = "cache_records"

        // Playlists columns
        const val COL_PL_ID = "id"
        const val COL_PL_NAME = "name"
        const val COL_PL_VERSION = "version"
        const val COL_PL_LOOP_COUNT = "loop_count"
        const val COL_PL_IS_ACTIVE = "is_active"

        // Playlist items columns
        const val COL_ITEM_ID = "id"
        const val COL_ITEM_PLAYLIST_ID = "playlist_id"
        const val COL_ITEM_NAME = "name"
        const val COL_ITEM_MEDIA_TYPE = "media_type"
        const val COL_ITEM_REMOTE_URL = "remote_url"
        const val COL_ITEM_LOCAL_PATH = "local_file_path"
        const val COL_ITEM_DURATION = "duration_seconds"
        const val COL_ITEM_ORDER = "order_index"
        const val COL_ITEM_CHECKSUM = "checksum_sha256"

        // Cache records columns
        const val COL_CACHE_MEDIA_ID = "media_id"
        const val COL_CACHE_LOCAL_PATH = "local_path"
        const val COL_CACHE_FILE_SIZE = "file_size_bytes"
        const val COL_CACHE_CHECKSUM = "checksum_sha256"
        const val COL_CACHE_LAST_ACCESSED = "last_accessed_time"
        const val COL_CACHE_IS_VERIFIED = "is_verified"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_PLAYLISTS (
                $COL_PL_ID TEXT PRIMARY KEY,
                $COL_PL_NAME TEXT NOT NULL,
                $COL_PL_VERSION INTEGER NOT NULL,
                $COL_PL_LOOP_COUNT INTEGER NOT NULL DEFAULT 0,
                $COL_PL_IS_ACTIVE INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_PLAYLIST_ITEMS (
                $COL_ITEM_ID TEXT PRIMARY KEY,
                $COL_ITEM_PLAYLIST_ID TEXT NOT NULL,
                $COL_ITEM_NAME TEXT NOT NULL,
                $COL_ITEM_MEDIA_TYPE TEXT NOT NULL,
                $COL_ITEM_REMOTE_URL TEXT NOT NULL,
                $COL_ITEM_LOCAL_PATH TEXT,
                $COL_ITEM_DURATION INTEGER NOT NULL,
                $COL_ITEM_ORDER INTEGER NOT NULL,
                $COL_ITEM_CHECKSUM TEXT,
                FOREIGN KEY ($COL_ITEM_PLAYLIST_ID) REFERENCES $TABLE_PLAYLISTS($COL_PL_ID) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_CACHE_RECORDS (
                $COL_CACHE_MEDIA_ID TEXT PRIMARY KEY,
                $COL_CACHE_LOCAL_PATH TEXT NOT NULL,
                $COL_CACHE_FILE_SIZE INTEGER NOT NULL,
                $COL_CACHE_CHECKSUM TEXT,
                $COL_CACHE_LAST_ACCESSED INTEGER NOT NULL,
                $COL_CACHE_IS_VERIFIED INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYLIST_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYLISTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CACHE_RECORDS")
        onCreate(db)
    }

    fun saveActivePlaylist(playlist: Playlist) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Mark all existing playlists inactive
            val inactiveValues = ContentValues().apply { put(COL_PL_IS_ACTIVE, 0) }
            db.update(TABLE_PLAYLISTS, inactiveValues, null, null)

            // Insert or replace active playlist
            val playlistValues = ContentValues().apply {
                put(COL_PL_ID, playlist.id)
                put(COL_PL_NAME, playlist.name)
                put(COL_PL_VERSION, playlist.version)
                put(COL_PL_LOOP_COUNT, playlist.loopCount)
                put(COL_PL_IS_ACTIVE, 1)
            }
            db.insertWithOnConflict(TABLE_PLAYLISTS, null, playlistValues, SQLiteDatabase.CONFLICT_REPLACE)

            // Remove existing items for this playlist
            db.delete(TABLE_PLAYLIST_ITEMS, "$COL_ITEM_PLAYLIST_ID = ?", arrayOf(playlist.id))

            // Insert updated items
            for (item in playlist.items) {
                val itemValues = ContentValues().apply {
                    put(COL_ITEM_ID, item.id)
                    put(COL_ITEM_PLAYLIST_ID, playlist.id)
                    put(COL_ITEM_NAME, item.name)
                    put(COL_ITEM_MEDIA_TYPE, item.type.name)
                    put(COL_ITEM_REMOTE_URL, item.remoteUrl)
                    put(COL_ITEM_LOCAL_PATH, item.localFilePath)
                    put(COL_ITEM_DURATION, item.displayDurationSeconds)
                    put(COL_ITEM_ORDER, item.orderIndex)
                    put(COL_ITEM_CHECKSUM, item.checksumSha256)
                }
                db.insertWithOnConflict(TABLE_PLAYLIST_ITEMS, null, itemValues, SQLiteDatabase.CONFLICT_REPLACE)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getActivePlaylist(): Playlist? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_PLAYLISTS WHERE $COL_PL_IS_ACTIVE = 1 LIMIT 1",
            null
        )

        return cursor.use { c ->
            if (!c.moveToFirst()) return null

            val id = c.getString(c.getColumnIndexOrThrow(COL_PL_ID))
            val name = c.getString(c.getColumnIndexOrThrow(COL_PL_NAME))
            val version = c.getLong(c.getColumnIndexOrThrow(COL_PL_VERSION))
            val loopCount = c.getLong(c.getColumnIndexOrThrow(COL_PL_LOOP_COUNT))

            val items = getPlaylistItems(db, id)
            Playlist(
                id = id,
                name = name,
                version = version,
                items = items,
                loopCount = loopCount
            )
        }
    }

    private fun getPlaylistItems(db: SQLiteDatabase, playlistId: String): List<PlaylistItem> {
        val itemsCursor = db.rawQuery(
            "SELECT * FROM $TABLE_PLAYLIST_ITEMS WHERE $COL_ITEM_PLAYLIST_ID = ? ORDER BY $COL_ITEM_ORDER ASC",
            arrayOf(playlistId)
        )

        val items = mutableListOf<PlaylistItem>()
        itemsCursor.use { c ->
            while (c.moveToNext()) {
                val itemId = c.getString(c.getColumnIndexOrThrow(COL_ITEM_ID))
                val itemName = c.getString(c.getColumnIndexOrThrow(COL_ITEM_NAME))
                val typeStr = c.getString(c.getColumnIndexOrThrow(COL_ITEM_MEDIA_TYPE))
                val remoteUrl = c.getString(c.getColumnIndexOrThrow(COL_ITEM_REMOTE_URL))
                val localPath = c.getString(c.getColumnIndexOrThrow(COL_ITEM_LOCAL_PATH))
                val duration = c.getInt(c.getColumnIndexOrThrow(COL_ITEM_DURATION))
                val order = c.getInt(c.getColumnIndexOrThrow(COL_ITEM_ORDER))
                val checksum = c.getString(c.getColumnIndexOrThrow(COL_ITEM_CHECKSUM))

                val type = if (typeStr == MediaType.VIDEO.name) MediaType.VIDEO else MediaType.IMAGE
                items.add(
                    PlaylistItem(
                        id = itemId,
                        name = itemName,
                        type = type,
                        remoteUrl = remoteUrl,
                        localFilePath = localPath,
                        displayDurationSeconds = duration,
                        orderIndex = order,
                        checksumSha256 = checksum
                    )
                )
            }
        }
        return items
    }

    fun clearActivePlaylist() {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_PLAYLISTS SET $COL_PL_IS_ACTIVE = 0")
    }
}

