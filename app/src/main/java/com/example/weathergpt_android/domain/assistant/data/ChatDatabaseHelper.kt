package com.example.weathergpt_android.domain.assistant.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.weathergpt_android.domain.assistant.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-performance, zero-overhead SQLite Database for WeatherGPT Chat History & Token Usage.
 */
class ChatDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "weathergpt_chats.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_MESSAGES = "messages"
        private const val COL_ID = "id"
        private const val COL_TEXT = "text"
        private const val COL_IS_USER = "is_user"
        private const val COL_TIMESTAMP = "timestamp"
        private const val COL_TOKENS = "tokens"

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
                $COL_TOKENS INTEGER NOT NULL DEFAULT 0
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

        // Initialize default token counters
        val cv = ContentValues().apply {
            put(COL_STAT_KEY, "total_tokens")
            put(COL_STAT_VAL, 0)
        }
        db.insert(TABLE_STATS, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STATS")
        onCreate(db)
    }

    suspend fun saveMessage(message: ChatMessage, estimatedTokens: Int = 0) = withContext(Dispatchers.IO) {
        writableDatabase.use { db ->
            val cv = ContentValues().apply {
                put(COL_ID, message.id)
                put(COL_TEXT, message.text)
                put(COL_IS_USER, if (message.isUser) 1 else 0)
                put(COL_TIMESTAMP, message.timestamp)
                put(COL_TOKENS, estimatedTokens)
            }
            db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)

            // Increment total token count
            if (estimatedTokens > 0) {
                db.execSQL("UPDATE $TABLE_STATS SET $COL_STAT_VAL = $COL_STAT_VAL + $estimatedTokens WHERE $COL_STAT_KEY = 'total_tokens'")
            }
        }
    }

    suspend fun getAllMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ChatMessage>()
        readableDatabase.use { db ->
            val cursor = db.rawQuery("SELECT $COL_ID, $COL_TEXT, $COL_IS_USER, $COL_TIMESTAMP FROM $TABLE_MESSAGES ORDER BY rowid ASC", null)
            cursor.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    val text = it.getString(1)
                    val isUser = it.getInt(2) == 1
                    val timestamp = it.getString(3)
                    list.add(ChatMessage(id = id, text = text, isUser = isUser, timestamp = timestamp))
                }
            }
        }
        list
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

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        writableDatabase.use { db ->
            db.delete(TABLE_MESSAGES, null, null)
        }
    }
}
