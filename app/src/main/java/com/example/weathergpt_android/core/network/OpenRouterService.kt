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
 * Calls /api/ai/chat-stream with Google Gemini 2.5 Flash, dynamically persona-tuned.
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
     * Robust SSE token extractor that handles multiple glued data: frames,
     * extracts assistant delta content, and strictly rejects raw JSON metadata.
     */
    private fun extractTokenFromSseChunk(data: String): String {
        if (data.isBlank() || data == "[DONE]") return ""

        val sb = StringBuilder()
        // Split in case multiple SSE frames were buffered together e.g. "...}data: {..."
        val segments = data.split("data:").map { it.trim() }.filter { it.isNotEmpty() }

        for (segment in segments) {
            if (segment == "[DONE]") continue

            var extractedContent: String? = null

            // 1. Try structured JSONObject parsing
            try {
                val json = JSONObject(segment)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val first = choices.getJSONObject(0)
                    val delta = first.optJSONObject("delta")
                    val content = delta?.optString("content", "") ?: first.optString("text", "")
                    if (content.isNotEmpty()) {
                        extractedContent = content
                    }
                }
            } catch (_: Exception) {
                // 2. Fallback: regex search for "content": "..." within segment
                val match = Regex("\"content\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(segment)
                if (match != null) {
                    val unescaped = match.groupValues[1]
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                    if (unescaped.isNotEmpty()) {
                        extractedContent = unescaped
                    }
                }
            }

            if (extractedContent != null) {
                sb.append(extractedContent)
            } else {
                // If this segment is raw JSON metadata (contains id, choices, delta, usage, object),
                // DISCARD IT. Never leak raw JSON metadata to the user or speech engine!
                if (!isRawJsonPayload(segment)) {
                    sb.append(segment)
                }
            }
        }

        val result = sb.toString()
        // Final safety check: if the result itself still looks like raw JSON metadata, discard
        return if (isRawJsonPayload(result)) "" else result
    }

    private fun isRawJsonPayload(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.startsWith("{") ||
               trimmed.startsWith("data:") ||
               trimmed.contains("\"id\":") ||
               trimmed.contains("\"choices\":") ||
               trimmed.contains("\"delta\":") ||
               trimmed.contains("\"usage\":") ||
               trimmed.contains("\"object\":") ||
               trimmed.contains("chat.completion")
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
            "$weatherContext\nUser Profile: Sector: Farmer | Primary Crops: ${profile.crops} | Land Area: ${profile.landArea} | Language: ${profile.preferredLanguage}"
        } else {
            "$weatherContext\nUser Profile: Sector: ${profile.sector.title} | Region: ${profile.monitoredRegion} | Language: ${profile.preferredLanguage}"
        }

        val jsonBody = JSONObject().apply {
            put("message", userMessage)
            put("location", locationContext)
            put("weather_context", enrichedContext)
            put("sector_focus", profile.sector.id)
            put("language", profile.preferredLanguage)
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
                if (currentLine.isBlank()) continue

                // Check for SSE data line or inline data segment
                if (currentLine.contains("data:")) {
                    val token = extractTokenFromSseChunk(currentLine)
                    if (token.isNotEmpty()) {
                        emit(token)
                    }
                } else if (!isRawJsonPayload(currentLine)) {
                    // Plain text stream chunk without data: prefix
                    emit(currentLine)
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
            put("language", profile.preferredLanguage)
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
