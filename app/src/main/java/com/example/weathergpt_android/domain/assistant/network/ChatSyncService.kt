package com.example.weathergpt_android.domain.assistant.network

import android.content.Context
import com.example.weathergpt_android.core.network.BackendConfig
import com.example.weathergpt_android.core.network.HmacSigner
import com.example.weathergpt_android.domain.assistant.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Communicates with FastAPI backend for Cloud Chat History synchronization.
 * Supports automatic cloud restoration across re-installations and device changes.
 */
class ChatSyncService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun syncMessagesToCloud(userId: String, messages: List<ChatMessage>): Result<Int> = withContext(Dispatchers.IO) {
        if (userId.isBlank() || messages.isEmpty()) {
            return@withContext Result.success(0)
        }

        val endpoint = "/api/chat/sync"
        val url = "${BackendConfig.BASE_URL}$endpoint"

        val jsonArray = JSONArray()
        for (m in messages) {
            val item = JSONObject().apply {
                put("id", m.id)
                put("user_id", userId)
                put("session_id", m.sessionId.ifBlank { "default" })
                put("text", m.text)
                put("is_user", m.isUser)
                put("timestamp", m.timestamp)
                put("tokens", m.text.length / 4)
                put("created_at", if (m.createdAt > 0) m.createdAt else System.currentTimeMillis())
            }
            jsonArray.put(item)
        }

        val bodyJson = JSONObject().apply {
            put("user_id", userId)
            put("messages", jsonArray)
        }

        val requestBody = bodyJson.toString().toRequestBody("application/json".toMediaType())
        val headers = HmacSigner.generateSecurityHeaders(endpoint)

        val reqBuilder = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")

        for ((k, v) in headers) {
            reqBuilder.addHeader(k, v)
        }

        try {
            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Cloud sync error ${response.code}: $body"))
            }
            val resJson = JSONObject(body)
            Result.success(resJson.optInt("synced_count", messages.size))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCloudHistory(userId: String): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        val endpoint = "/api/chat/history"
        val url = "${BackendConfig.BASE_URL}$endpoint?user_id=$userId"

        val headers = HmacSigner.generateSecurityHeaders(endpoint)

        val reqBuilder = Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "application/json")

        for ((k, v) in headers) {
            reqBuilder.addHeader(k, v)
        }

        try {
            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Cloud fetch error ${response.code}: $body"))
            }

            val resJson = JSONObject(body)
            val msgsArray = resJson.optJSONArray("messages") ?: JSONArray()
            val list = mutableListOf<ChatMessage>()

            for (i in 0 until msgsArray.length()) {
                val item = msgsArray.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = item.optString("id", java.util.UUID.randomUUID().toString()),
                        text = item.optString("text", ""),
                        isUser = item.optBoolean("is_user", false),
                        timestamp = item.optString("timestamp", "Cloud"),
                        userId = item.optString("user_id", userId),
                        sessionId = item.optString("session_id", "default"),
                        createdAt = item.optLong("created_at", System.currentTimeMillis())
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCloudHistory(userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext Result.success(true)

        val endpoint = "/api/chat/history"
        val url = "${BackendConfig.BASE_URL}$endpoint?user_id=$userId"

        val headers = HmacSigner.generateSecurityHeaders(endpoint)

        val reqBuilder = Request.Builder()
            .url(url)
            .delete()

        for ((k, v) in headers) {
            reqBuilder.addHeader(k, v)
        }

        try {
            val response = client.newCall(reqBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Cloud clear error ${response.code}"))
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
