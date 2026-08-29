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

class OpenRouterService(private val context: Context) {
    // Ultra-low latency optimized HTTP/2 client with persistent connection pooling
    private val client = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Real-time Server-Sent Events (SSE) streaming flow with automatic multi-provider fallback
     */
    fun streamChatCompletion(
        userMessage: String,
        locationContext: String = "San Francisco, CA",
        weatherContext: String = "24°C, Clear Sky, Humidity 52%, Wind 14 km/h, AQI 34",
        history: List<Pair<String, String>> = emptyList(),
        isVoiceMode: Boolean = false
    ): Flow<String> = flow {
        val apiKey = OpenRouterPreferences.getApiKey(context)
        val candidateModels = OpenRouterPreferences.FALLBACK_MODELS

        val systemPrompt = if (isVoiceMode) {
            """
                You are WeatherGPT Voice, an ultra-fast AI meteorologist.
                Location: $locationContext
                Live Weather: $weatherContext
                
                CRITICAL INSTRUCTIONS FOR LOW LATENCY:
                - Give a direct, punchy, 1-to-2 sentence answer in the user's language (English, Hindi, Marathi, etc.).
                - Never use markdown bolding, bullet points, or preamble like 'Sure!' or 'Here is the forecast:'.
                - Speak naturally for immediate audio playback.
            """.trimIndent()
        } else {
            """
                You are WeatherGPT, an advanced AI meteorologist.
                Location: $locationContext
                Live Weather: $weatherContext
                
                Guidelines:
                - Provide clear, concise, actionable weather intelligence and advice in the user's language (English, Hindi, Marathi, Bengali, Tamil, Telugu, etc.).
                - Keep responses crisp and immediately useful.
            """.trimIndent()
        }

        val messagesArray = JSONArray()
        messagesArray.put(
            JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            }
        )

        for ((role, text) in history.takeLast(4)) {
            messagesArray.put(
                JSONObject().apply {
                    put("role", if (role == "user") "user" else "assistant")
                    put("content", text)
                }
            )
        }

        messagesArray.put(
            JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            }
        )

        // Try candidate models in order if an HTTP 429 occurs
        var succeeded = false
        var lastError = ""

        for (model in candidateModels) {
            val jsonBody = JSONObject().apply {
                // Multi-model array for OpenRouter server-side fallback
                val modelsArray = JSONArray().apply {
                    put(model)
                    for (m in candidateModels) {
                        if (m != model) put(m)
                    }
                }
                put("models", modelsArray)
                put("route", "fallback")
                put("messages", messagesArray)
                put("stream", true)
                put("temperature", 0.3)
                put("max_tokens", if (isVoiceMode) 120 else 450)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val requestBuilder = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .addHeader("HTTP-Referer", "https://weathergpt.ai")
                .addHeader("X-Title", "WeatherGPT Android")

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }

            val request = requestBuilder.build()

            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorBody = response.body?.string() ?: "HTTP $code"
                    lastError = "HTTP $code: $errorBody"
                    // If 429 rate limit or 5xx, try next candidate model
                    if (code == 429 || code >= 500) {
                        continue
                    } else {
                        emit("Error ($code): $errorBody")
                        return@flow
                    }
                }

                val inputStream = response.body?.byteStream()
                if (inputStream != null) {
                    val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                    var line: String?
                    var receivedAnyToken = false

                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line?.trim() ?: continue
                        if (currentLine.startsWith("data:")) {
                            val data = currentLine.removePrefix("data:").trim()
                            if (data == "[DONE]") break
                            if (data.isNotBlank()) {
                                try {
                                    val json = JSONObject(data)
                                    val choices = json.optJSONArray("choices")
                                    if (choices != null && choices.length() > 0) {
                                        val delta = choices.getJSONObject(0).optJSONObject("delta")
                                        val token = delta?.optString("content") ?: ""
                                        if (token.isNotEmpty()) {
                                            emit(token)
                                            receivedAnyToken = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore parse errors
                                }
                            }
                        }
                    }
                    reader.close()
                    if (receivedAnyToken) {
                        succeeded = true
                        break
                    }
                }
            } catch (e: Exception) {
                lastError = e.message ?: "Connection failed"
            }
        }

        if (!succeeded && lastError.isNotEmpty()) {
            emit("Error: $lastError")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Non-streaming fallback with automatic multi-model retry
     */
    suspend fun generateChatCompletion(
        userMessage: String,
        locationContext: String = "San Francisco, CA",
        weatherContext: String = "24°C, Sunny, Humidity 52%, Wind 14 km/h, AQI 34",
        history: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = OpenRouterPreferences.getApiKey(context)
        val candidateModels = OpenRouterPreferences.FALLBACK_MODELS

        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", "You are WeatherGPT. Location: $locationContext. Weather: $weatherContext. Be concise and fast in the user's language.")
            })
            for ((role, text) in history.takeLast(4)) {
                put(JSONObject().apply {
                    put("role", if (role == "user") "user" else "assistant")
                    put("content", text)
                })
            }
            put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })
        }

        var lastException: Exception? = null

        for (model in candidateModels) {
            val jsonBody = JSONObject().apply {
                val modelsArray = JSONArray().apply {
                    put(model)
                    for (m in candidateModels) {
                        if (m != model) put(m)
                    }
                }
                put("models", modelsArray)
                put("route", "fallback")
                put("messages", messagesArray)
                put("temperature", 0.4)
                put("max_tokens", 350)
            }

            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://weathergpt.ai")
                .addHeader("X-Title", "WeatherGPT Android")
                .apply {
                    if (apiKey.isNotBlank()) addHeader("Authorization", "Bearer $apiKey")
                }
                .build()

            try {
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    if (response.code == 429 || response.code >= 500) {
                        lastException = Exception("HTTP ${response.code}: $body")
                        continue
                    }
                    return@withContext Result.failure(Exception("HTTP ${response.code}: $body"))
                }
                val json = JSONObject(body)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val content = choices.getJSONObject(0).optJSONObject("message")?.optString("content") ?: ""
                    return@withContext Result.success(content)
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: Exception("All fallback models failed"))
    }
}
