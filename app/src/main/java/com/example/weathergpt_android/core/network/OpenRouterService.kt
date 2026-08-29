package com.example.weathergpt_android.core.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenRouterService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateChatCompletion(
        userMessage: String,
        locationContext: String = "San Francisco, CA",
        weatherContext: String = "24°C, Sunny, Humidity 52%, Wind 14 km/h, AQI 34",
        history: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = OpenRouterPreferences.getApiKey(context)
        val model = OpenRouterPreferences.getSelectedModel(context)

        val systemPrompt = """
            You are WeatherGPT, an advanced AI meteorologist and conversational weather intelligence assistant.
            Current User Location: $locationContext
            Current Atmospheric Conditions: $weatherContext
            
            Guidelines:
            - Provide clear, concise, actionable weather intelligence, forecasts, and lifestyle suggestions.
            - If relevant, mention temperature trends, rain probability, wind, UV/AQI safety, or clothing advice.
            - Keep responses conversational, natural, and helpful.
        """.trimIndent()

        val messagesArray = JSONArray()

        // System message
        messagesArray.put(
            JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            }
        )

        // Previous conversation turns
        for ((role, text) in history) {
            messagesArray.put(
                JSONObject().apply {
                    put("role", if (role == "user") "user" else "assistant")
                    put("content", text)
                }
            )
        }

        // Current user message
        messagesArray.put(
            JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            }
        )

        val jsonBody = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("temperature", 0.7)
            put("max_tokens", 800)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val requestBuilder = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://weathergpt.ai")
            .addHeader("X-Title", "WeatherGPT Android")

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val request = requestBuilder.build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val json = JSONObject(responseBody)
                    json.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}: $responseBody"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val json = JSONObject(responseBody)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.optJSONObject("message")
                val content = message?.optString("content") ?: ""
                Result.success(content)
            } else {
                Result.failure(Exception("No completion choices returned from OpenRouter"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
