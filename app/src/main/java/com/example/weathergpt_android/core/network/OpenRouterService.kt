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
 * Cloud Backend AI Service powered exclusively by FastAPI on Render.
 * 
 * Direct endpoint: https://weathergpt-backend-m5kk.onrender.com/api/ai/chat-stream
 * Dynamic HMAC-SHA256 authenticated. All fallbacks removed per specification.
 */
class OpenRouterService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Robust SSE token extractor supporting OpenAI/OpenRouter choices format
     * and Google Gemini candidates format from the Render FastAPI stream.
     */
    private fun extractTokenFromSseChunk(data: String): String {
        if (data.isBlank() || data.trim() == "[DONE]") return ""

        val sb = StringBuilder()
        // Extract content after "data:" prefix preserving any leading/trailing whitespace of the token
        val rawSegments = if (data.contains("data:")) {
            data.split("data:").filter { it.isNotEmpty() }.map {
                // If segment begins with a single space that was part of "data: <token>", strip only the single protocol space
                if (it.startsWith(" ")) it.substring(1) else it
            }
        } else {
            listOf(data)
        }

        for (segment in rawSegments) {
            if (segment.trim() == "[DONE]") continue

            // If segment is clean plain text token directly from Render backend
            if (!isRawJsonPayload(segment)) {
                sb.append(segment)
                continue
            }

            var extractedContent: String? = null

            // 1. Try structured JSONObject parsing
            try {
                val json = JSONObject(segment.trim())

                // OpenRouter / OpenAI format
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val first = choices.getJSONObject(0)
                    val delta = first.optJSONObject("delta")
                    val content = delta?.optString("content", "") ?: first.optString("text", "")
                    if (content.isNotEmpty()) {
                        extractedContent = content
                    }
                } else {
                    // Google Gemini format
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val contentObj = firstCandidate.optJSONObject("content")
                        val parts = contentObj?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text", "")
                            if (text.isNotEmpty()) {
                                extractedContent = text
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // 2. Fallback: regex search for "content": "..." within segment
                val match = Regex("\"content\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(segment)
                    ?: Regex("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(segment)
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

            if (extractedContent != null && !isRawJsonPayload(extractedContent)) {
                sb.append(extractedContent)
            }
        }

        val result = sb.toString()
        return if (isRawJsonPayload(result)) "" else result
    }

    private fun isRawJsonPayload(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.startsWith("{") ||
               trimmed.startsWith("data:") ||
               trimmed.startsWith("}") ||
               trimmed.startsWith("]") ||
               trimmed.endsWith("}") ||
               trimmed.contains("\"id\":") ||
               trimmed.contains("\"choices\":") ||
               trimmed.contains("\"candidates\":") ||
               trimmed.contains("\"delta\":") ||
               trimmed.contains("\"usage\":") ||
               trimmed.contains("\"object\":") ||
               trimmed.contains("\"format\":") ||
               trimmed.contains("google-gemini") ||
               trimmed.contains("reasoning") ||
               trimmed.contains("signature") ||
               trimmed.contains("finish_reason") ||
               trimmed.contains("native_finish_reason") ||
               trimmed.contains("chat.completion")
    }

    private val detailKeywords = listOf(
        "in detail", "detail", "elaborate", "explain more", "more details",
        "tell me more", "deep dive", "full analysis", "detailed",
        "विस्तार से", "विस्तृत", "বিস্তারিত", "முழு விவரம்", "వివరంగా"
    )

    private fun isDetailRequest(message: String): Boolean {
        val lower = message.lowercase()
        return detailKeywords.any { lower.contains(it) }
    }

    /**
     * Real-time Server-Sent Events (SSE) streaming flow directly from FastAPI on Render.
     * All local and secondary fallbacks removed: strictly routes through Render.
     */
    fun streamChatCompletion(
        userMessage: String,
        locationContext: String = "Live Location",
        weatherContext: String = "",
        history: List<Pair<String, String>> = emptyList(),
        isVoiceMode: Boolean = false
    ): Flow<String> = flow {
        val profile = UserPreferences.getProfile(context)
        val enrichedContext = if (profile.sector == UserSector.FARMER) {
            "LIVE OPEN-METEO TELEMETRY:\n$weatherContext\n\nUSER PROFILE (COLLECTED DETAILS):\nName: ${profile.name} | Sector: Farmer | Primary Crops: ${profile.crops} | Farm Area: ${profile.landArea} | Region: ${profile.monitoredRegion} | Language: ${profile.preferredLanguage}"
        } else {
            "LIVE OPEN-METEO TELEMETRY:\n$weatherContext\n\nUSER PROFILE (COLLECTED DETAILS):\nName: ${profile.name} | Sector: ${profile.sector.title} | Monitored Region: ${profile.monitoredRegion} | Language: ${profile.preferredLanguage}"
        }

        val endpoint = "/api/ai/chat-stream"
        val backendUrl = "${BackendConfig.getBaseUrl(context)}$endpoint"
        val isDetailMode = isDetailRequest(userMessage)

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
            put("weather_context", enrichedContext)
            put("sector_focus", profile.sector.id)
            put("language", profile.preferredLanguage)
            put("is_voice_mode", isVoiceMode)
            put("is_detail_mode", isDetailMode)
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

        for (attempt in 0..1) {
            try {
                val response = client.newCall(request).execute()

                if (response.code == 402) {
                    emit("⚠️ Render Backend Notice (HTTP 402): OpenRouter quota depleted on Render server. Please update OPENROUTER_API_KEY in the Render dashboard.")
                    response.close()
                    return@flow
                }

                if (!response.isSuccessful) {
                    if (attempt == 0 && response.code in listOf(502, 503, 504)) {
                        response.close()
                        Thread.sleep(2000)
                        continue
                    }
                    val errBody = response.body?.string() ?: ""
                    emit("⚠️ Render Backend Notice (HTTP ${response.code}): $errBody")
                    response.close()
                    return@flow
                }

                val responseBody = response.body
                if (responseBody == null) {
                    emit("Empty response from Render backend.")
                    return@flow
                }

                val reader = BufferedReader(InputStreamReader(responseBody.byteStream(), Charsets.UTF_8))
                var line: String?
                var emittedAnyToken = false

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.isBlank()) continue

                    if (currentLine.contains("402") || currentLine.contains("insufficient credits", ignoreCase = true)) {
                        emit("⚠️ Render Backend Notice (HTTP 402): OpenRouter quota depleted on Render server. Please update OPENROUTER_API_KEY in the Render dashboard.")
                        return@flow
                    }

                    if (currentLine.contains("data:")) {
                        val token = extractTokenFromSseChunk(currentLine)
                        if (token.isNotEmpty()) {
                            emittedAnyToken = true
                            emit(token)
                        }
                    } else if (!isRawJsonPayload(currentLine)) {
                        emittedAnyToken = true
                        emit(currentLine)
                    }
                }

                if (emittedAnyToken) {
                    return@flow
                }
            } catch (e: Exception) {
                if (attempt == 0) {
                    Thread.sleep(2000)
                    continue
                }
                emit("Connection error to Render backend: ${e.localizedMessage ?: e.message}")
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Non-streaming completion for status tests & diagnostics against Render backend.
     */
    suspend fun generateChatCompletion(
        userMessage: String,
        locationContext: String = "Live Location",
        weatherContext: String = "",
        history: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val endpoint = "/api/ai/chat-stream"
        val backendUrl = "${BackendConfig.getBaseUrl(context)}$endpoint"
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
            if (response.code == 402) {
                return@withContext Result.failure(Exception("Render Backend (HTTP 402): OpenRouter quota depleted on Render server. Please update OPENROUTER_API_KEY in the Render dashboard."))
            }
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                return@withContext Result.failure(Exception("Render Backend HTTP ${response.code}: $errBody"))
            }

            val reader = BufferedReader(InputStreamReader(response.body?.byteStream() ?: return@withContext Result.failure(Exception("Empty body"))))
            val fullText = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.contains("402") || currentLine.contains("insufficient credits", ignoreCase = true)) {
                    return@withContext Result.failure(Exception("Render Backend (HTTP 402): OpenRouter quota depleted on Render server. Please update OPENROUTER_API_KEY in the Render dashboard."))
                }
                if (currentLine.startsWith("data:")) {
                    val rawData = currentLine.removePrefix("data:").trim()
                    if (rawData == "[DONE]") break
                    val token = extractTokenFromSseChunk(rawData)
                    if (token.isNotEmpty()) {
                        fullText.append(token)
                    }
                }
            }

            val resultText = fullText.toString().trim()
            if (resultText.contains("Error HTTP 402", ignoreCase = true) || resultText.contains("402")) {
                return@withContext Result.failure(Exception("Render Backend (HTTP 402): OpenRouter quota depleted on Render server. Please update OPENROUTER_API_KEY in the Render dashboard."))
            }

            Result.success(resultText.ifBlank { "Render FastAPI Backend Online ✓" })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
