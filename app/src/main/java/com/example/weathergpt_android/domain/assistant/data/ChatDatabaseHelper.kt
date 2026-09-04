package com.example.weathergpt_android.domain.assistant.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.weathergpt_android.domain.assistant.model.ChatMessage
import com.example.weathergpt_android.domain.assistant.model.ChatSessionSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-performance SQLite Database for WeatherGPT Chat History, Session Grouping,
 * Cloud Sync State, and Token Usage.
 */
class ChatDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "weathergpt_chats.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_MESSAGES = "messages"
        private const val COL_ID = "id"
        private const val COL_TEXT = "text"
        private const val COL_IS_USER = "is_user"
        private const val COL_TIMESTAMP = "timestamp"
        private const val COL_TOKENS = "tokens"
        private const val COL_USER_ID = "user_id"
        private const val COL_SESSION_ID = "session_id"
        private const val COL_SYNCED = "synced"
        private const val COL_CREATED_AT = "created_at"

        private const val TABLE_STATS = "token_stats"
        private const val COL_STAT_KEY = "stat_key"
        private const val COL_STAT_VAL = "stat_val"

        @Volatile
        private var instance: ChatDatabaseHelper? = null

        fun getInstance(context: Context): ChatDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: ChatDatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_MESSAGES (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TEXT TEXT NOT NULL,
                $COL_IS_USER INTEGER NOT NULL,
                $COL_TIMESTAMP TEXT NOT NULL,
                $COL_TOKENS INTEGER NOT NULL DEFAULT 0,
                $COL_USER_ID TEXT NOT NULL DEFAULT '',
                $COL_SESSION_ID TEXT NOT NULL DEFAULT 'default',
                $COL_SYNCED INTEGER NOT NULL DEFAULT 0,
                $COL_CREATED_AT INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_STATS (
                $COL_STAT_KEY TEXT PRIMARY KEY,
                $COL_STAT_VAL INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        val cv = ContentValues().apply {
            put(COL_STAT_KEY, "total_tokens")
            put(COL_STAT_VAL, 0)
        }
        db.insert(TABLE_STATS, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_USER_ID TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_SESSION_ID TEXT NOT NULL DEFAULT 'default'")
                db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_SYNCED INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_CREATED_AT INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) {
                db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_STATS")
                onCreate(db)
            }
        }
    }

    suspend fun saveMessage(
        message: ChatMessage,
        estimatedTokens: Int = 0,
        userId: String = "",
        sessionId: String = "default",
        isSynced: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val effectiveUserId = if (userId.isNotBlank()) userId else message.userId
        val effectiveSessionId = if (sessionId != "default") sessionId else message.sessionId
        val createdAt = if (message.createdAt > 0) message.createdAt else System.currentTimeMillis()

        writableDatabase.use { db ->
            val cv = ContentValues().apply {
                put(COL_ID, message.id)
                put(COL_TEXT, message.text)
                put(COL_IS_USER, if (message.isUser) 1 else 0)
                put(COL_TIMESTAMP, message.timestamp)
                put(COL_TOKENS, estimatedTokens)
                put(COL_USER_ID, effectiveUserId)
                put(COL_SESSION_ID, effectiveSessionId)
                put(COL_SYNCED, if (isSynced) 1 else 0)
                put(COL_CREATED_AT, createdAt)
            }
            db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)

            if (estimatedTokens > 0) {
                db.execSQL("UPDATE $TABLE_STATS SET $COL_STAT_VAL = $COL_STAT_VAL + $estimatedTokens WHERE $COL_STAT_KEY = 'total_tokens'")
            }
        }
    }

    suspend fun getAllMessages(userId: String? = null, sessionId: String? = null): List<ChatMessage> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ChatMessage>()
        readableDatabase.use { db ->
            val query: String
            val args: Array<String>?
            if (!userId.isNullOrBlank() && !sessionId.isNullOrBlank()) {
                query = "SELECT $COL_ID, $COL_TEXT, $COL_IS_USER, $COL_TIMESTAMP, $COL_USER_ID, $COL_SESSION_ID, $COL_CREATED_AT FROM $TABLE_MESSAGES WHERE ($COL_USER_ID = ? OR $COL_USER_ID = '') AND $COL_SESSION_ID = ? ORDER BY $COL_CREATED_AT ASC, rowid ASC"
                args = arrayOf(userId, sessionId)
            } else if (!userId.isNullOrBlank()) {
                query = "SELECT $COL_ID, $COL_TEXT, $COL_IS_USER, $COL_TIMESTAMP, $COL_USER_ID, $COL_SESSION_ID, $COL_CREATED_AT FROM $TABLE_MESSAGES WHERE $COL_USER_ID = ? OR $COL_USER_ID = '' ORDER BY $COL_CREATED_AT ASC, rowid ASC"
                args = arrayOf(userId)
            } else {
                query = "SELECT $COL_ID, $COL_TEXT, $COL_IS_USER, $COL_TIMESTAMP, $COL_USER_ID, $COL_SESSION_ID, $COL_CREATED_AT FROM $TABLE_MESSAGES ORDER BY $COL_CREATED_AT ASC, rowid ASC"
                args = null
            }

            val cursor = db.rawQuery(query, args)
            cursor.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    val text = it.getString(1)
                    val isUser = it.getInt(2) == 1
                    val timestamp = it.getString(3)
                    val uId = it.getString(4) ?: ""
                    val sId = it.getString(5) ?: "default"
                    val created = it.getLong(6)
                    list.add(
                        ChatMessage(
                            id = id,
                            text = text,
                            isUser = isUser,
                            timestamp = timestamp,
                            userId = uId,
                            sessionId = sId,
                            createdAt = created
                        )
                    )
                }
            }
        }
        list
    }

    suspend fun getSessions(userId: String? = null): List<ChatSessionSummary> = withContext(Dispatchers.IO) {
        val sessions = mutableListOf<ChatSessionSummary>()
        readableDatabase.use { db ->
            val query = if (!userId.isNullOrBlank()) {
                "SELECT $COL_SESSION_ID, COUNT(*), MAX($COL_TIMESTAMP), MAX($COL_CREATED_AT) FROM $TABLE_MESSAGES WHERE $COL_USER_ID = ? OR $COL_USER_ID = '' GROUP BY $COL_SESSION_ID ORDER BY MAX($COL_CREATED_AT) DESC"
            } else {
                "SELECT $COL_SESSION_ID, COUNT(*), MAX($COL_TIMESTAMP), MAX($COL_CREATED_AT) FROM $TABLE_MESSAGES GROUP BY $COL_SESSION_ID ORDER BY MAX($COL_CREATED_AT) DESC"
            }
            val args = if (!userId.isNullOrBlank()) arrayOf(userId) else null

            val cursor = db.rawQuery(query, args)
            cursor.use {
                while (it.moveToNext()) {
                    val sId = it.getString(0) ?: "default"
                    val count = it.getInt(1)
                    val lastTs = it.getString(2) ?: "Recent"

                    var title = "Weather Advisory Session"
                    var snippet = "Conversational chat session"

                    val promptCursor = db.rawQuery(
                        "SELECT $COL_TEXT FROM $TABLE_MESSAGES WHERE $COL_SESSION_ID = ? AND $COL_IS_USER = 1 ORDER BY $COL_CREATED_AT ASC, rowid ASC LIMIT 1",
                        arrayOf(sId)
                    )
                    promptCursor.use { pc ->
                        if (pc.moveToFirst()) {
                            val promptText = pc.getString(0)
                            title = if (promptText.length > 32) promptText.take(30) + "..." else promptText
                            snippet = promptText
                        }
                    }

                    sessions.add(
                        ChatSessionSummary(
                            sessionId = sId,
                            title = title,
                            snippet = snippet,
                            lastTimestamp = lastTs,
                            messageCount = count
                        )
                    )
                }
            }
        }
        sessions
    }

    suspend fun getUnsyncedMessages(userId: String? = null): List<ChatMessage> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ChatMessage>()
        readableDatabase.use { db ->
            val query = if (!userId.isNullOrBlank()) {
                "SELECT $COL_ID, $COL_TEXT, $COL_IS_USER, $COL_TIMESTAMP, $COL_USER_ID, $COL_SESSION_ID, $COL_CREATED_AT FROM $TABLE_MESSAGES WHERE $COL_SYNCED = 0 AND ($COL_USER_ID = ? OR $COL_USER_ID = '')"
            } else {
                "SELECT $COL_ID, $COL_TEXT, $COL_IS_USER, $COL_TIMESTAMP, $COL_USER_ID, $COL_SESSION_ID, $COL_CREATED_AT FROM $TABLE_MESSAGES WHERE $COL_SYNCED = 0"
            }
            val args = if (!userId.isNullOrBlank()) arrayOf(userId) else null
            val cursor = db.rawQuery(query, args)
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        ChatMessage(
                            id = it.getString(0),
                            text = it.getString(1),
                            isUser = it.getInt(2) == 1,
                            timestamp = it.getString(3),
                            userId = it.getString(4) ?: "",
                            sessionId = it.getString(5) ?: "default",
                            createdAt = it.getLong(6)
                        )
                    )
                }
            }
        }
        list
    }

    suspend fun markMessagesSynced(messageIds: List<String>) = withContext(Dispatchers.IO) {
        if (messageIds.isEmpty()) return@withContext
        writableDatabase.use { db ->
            val placeholders = messageIds.joinToString(",") { "?" }
            db.execSQL("UPDATE $TABLE_MESSAGES SET $COL_SYNCED = 1 WHERE $COL_ID IN ($placeholders)", messageIds.toTypedArray())
        }
    }

    suspend fun insertBatchFromCloud(messages: List<ChatMessage>, userId: String) = withContext(Dispatchers.IO) {
        if (messages.isEmpty()) return@withContext
        writableDatabase.use { db ->
            db.beginTransaction()
            try {
                for (m in messages) {
                    val cv = ContentValues().apply {
                        put(COL_ID, m.id)
                        put(COL_TEXT, m.text)
                        put(COL_IS_USER, if (m.isUser) 1 else 0)
                        put(COL_TIMESTAMP, m.timestamp)
                        put(COL_TOKENS, m.text.length / 4)
                        put(COL_USER_ID, userId)
                        put(COL_SESSION_ID, m.sessionId.ifBlank { "default" })
                        put(COL_SYNCED, 1)
                        put(COL_CREATED_AT, if (m.createdAt > 0) m.createdAt else System.currentTimeMillis())
                    }
                    db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    suspend fun getTotalTokens(): Int = withContext(Dispatchers.IO) {
        var total = 0
        readableDatabase.use { db ->
            val cursor = db.rawQuery("SELECT $COL_STAT_VAL FROM $TABLE_STATS WHERE $COL_STAT_KEY = 'total_tokens'", null)
            cursor.use {
                if (it.moveToFirst()) {
                    total = it.getInt(0)
                }
            }
        }
        total
    }

    suspend fun clearHistory(userId: String? = null) = withContext(Dispatchers.IO) {
        writableDatabase.use { db ->
            if (!userId.isNullOrBlank()) {
                db.delete(TABLE_MESSAGES, "$COL_USER_ID = ? OR $COL_USER_ID = ''", arrayOf(userId))
            } else {
                db.delete(TABLE_MESSAGES, null, null)
            }
        }
    }
}
