package com.example.weathergpt_android.core.network

import android.content.Context
import com.example.weathergpt_android.domain.auth.data.UserPreferences
import com.example.weathergpt_android.domain.auth.model.UserSector
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
 * Calls /api/ai/chat-stream with Google Gemini 2.5 Flash, dynamically persona-tuned (SIH26068).
 * Extracts raw textual tokens from SSE JSON chunks cleanly.
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
     * Parses an SSE data payload and extracts only the assistant content token.
     * Prevents raw JSON strings from leaking into the UI / TTS.
     */
    private fun extractTokenFromSseChunk(data: String): String {
        if (data.isBlank() || data == "[DONE]") return ""
        return try {
            val json = JSONObject(data)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val first = choices.getJSONObject(0)
                val delta = first.optJSONObject("delta")
                delta?.optString("content", "") ?: first.optString("text", "")
            } else {
                json.optString("content", json.optString("text", ""))
            }
        } catch (e: Exception) {
            // If it's not JSON, return as plain text
            data
        }
    }

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
        val profile = UserPreferences.getProfile(context)

        val historyArray = JSONArray().apply {
            for ((role, text) in history.takeLast(4)) {
                put(JSONObject().apply {
                    put("role", role)
                    put("content", text)
                })
            }
        }

        val enrichedContext = if (profile.sector == UserSector.FARMER) {
            "$weatherContext | Farmer Crops: ${profile.crops} | Land: ${profile.landArea} | Language: ${profile.preferredLanguage}"
        } else {
            "$weatherContext | User Role: ${profile.sector.title} | Region: ${profile.monitoredRegion} | Language: ${profile.preferredLanguage}"
        }

        val jsonBody = JSONObject().apply {
            put("message", userMessage)
            put("location", locationContext)
            put("weather_context", enrichedContext)
            put("sector_focus", profile.sector.id)
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
                    val rawData = currentLine.removePrefix("data:").trim()
                    if (rawData == "[DONE]") break
                    val token = extractTokenFromSseChunk(rawData)
                    if (token.isNotEmpty()) {
                        emit(token)
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
        val profile = UserPreferences.getProfile(context)

        val jsonBody = JSONObject().apply {
            put("message", userMessage)
            put("location", locationContext)
            put("weather_context", "$weatherContext | Role: ${profile.sector.title}")
            put("sector_focus", profile.sector.id)
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
                    val rawData = currentLine.removePrefix("data:").trim()
                    if (rawData == "[DONE]") break
                    val token = extractTokenFromSseChunk(rawData)
                    if (token.isNotEmpty()) {
                        fullText.append(token)
                    }
                }
            }
            Result.success(fullText.toString().trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
