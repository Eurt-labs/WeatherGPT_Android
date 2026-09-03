package com.example.weathergpt_android.core.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Cloud Backend AI Service powered by FastAPI on Render.
 * Calls /api/ai/chat-stream with Google Gemini 2.5 Flash.
 * Zero user API key required — securely authenticated via HMAC & client secret.
 */
class OpenRouterService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Real-time Server-Sent Events (SSE) streaming flow directly from FastAPI / Render backend.
     */
    fun streamChatCompletion(
        userMessage: String,
        locationContext: String = "Live Location",
        weatherContext: String = "",
        history: List<Pair<String, String>> = emptyList(),
        isVoiceMode: Boolean = false
    ): Flow<String> = flow {
        val endpoint = "/api/ai/chat-stream"
        val backendUrl = "${BackendConfig.BASE_URL}$endpoint"

        val historyArray = JSONArray().apply {
            for ((role, text) in history.takeLast(4)) {
                put(JSONObject().apply {
                    put("role", role)
                    put("content", text)
                })
            }
        }

        val jsonBody = JSONObject().apply {
            put("message", userMessage)
            put("location", locationContext)
            put("weather_context", weatherContext)
            put("is_voice_mode", isVoiceMode)
            put("history", historyArray)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val securityHeaders = HmacSigner.generateSecurityHeaders(endpoint)

        val requestBuilder = Request.Builder()
            .url(backendUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")

        for ((headerName, headerVal) in securityHeaders) {
            requestBuilder.addHeader(headerName, headerVal)
        }

        val request = requestBuilder.build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                emit("Cloud backend error: HTTP ${response.code}. Please check connection.")
                return@flow
            }

            val responseBody = response.body
            if (responseBody == null) {
                emit("Empty response from AI server.")
                return@flow
            }

            val reader = BufferedReader(InputStreamReader(responseBody.byteStream(), Charsets.UTF_8))
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.startsWith("data:")) {
                    val data = currentLine.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    if (data.isNotEmpty()) {
                        emit(data)
                    }
                }
            }
        } catch (e: Exception) {
            emit("Connection failed: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Non-streaming fallback for status tests & one-shot queries.
     */
    suspend fun generateChatCompletion(
        userMessage: String,
        locationContext: String = "Live Location",
        weatherContext: String = "",
        history: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val endpoint = "/api/ai/chat-stream"
        val backendUrl = "${BackendConfig.BASE_URL}$endpoint"

        val jsonBody = JSONObject().apply {
            put("message", userMessage)
            put("location", locationContext)
            put("weather_context", weatherContext)
            put("is_voice_mode", false)
            put("history", JSONArray())
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val securityHeaders = HmacSigner.generateSecurityHeaders(endpoint)

        val requestBuilder = Request.Builder()
            .url(backendUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")

        for ((headerName, headerVal) in securityHeaders) {
            requestBuilder.addHeader(headerName, headerVal)
        }

        val request = requestBuilder.build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val reader = BufferedReader(InputStreamReader(response.body?.byteStream() ?: return@withContext Result.failure(Exception("Empty body"))))
            val fullText = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.startsWith("data:")) {
                    val data = currentLine.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    if (data.isNotEmpty()) {
                        fullText.append(data)
                    }
                }
            }
            Result.success(fullText.toString().trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
