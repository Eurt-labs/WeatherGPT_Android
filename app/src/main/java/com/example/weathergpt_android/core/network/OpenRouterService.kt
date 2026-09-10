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
 * Cloud Backend AI Service & Multi-Provider Engine.
 * 
 * Supports:
 * 1. Cloud Backend (FastAPI on Render) with automatic HMAC security signatures.
 * 2. Direct Google Gemini API (Free tier from Google AI Studio).
 * 3. Direct OpenRouter API (custom user keys & models).
 * 4. Intelligent Local Meteorological Advisory Fallback (activates on HTTP 402 / credit depletion).
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
     * Robust SSE token extractor supporting OpenAI/OpenRouter choices format
     * AND Google Gemini candidates format, while strictly rejecting raw JSON
     * and HTTP 402 error artifacts.
     */
    private fun extractTokenFromSseChunk(data: String): String {
        if (data.isBlank() || data == "[DONE]") return ""

        val sb = StringBuilder()
        val segments = data.split("data:").map { it.trim() }.filter { it.isNotEmpty() }

        for (segment in segments) {
            if (segment == "[DONE]") continue

            // Discard raw error strings from stream
            if (segment.contains("Error HTTP 402") || segment.contains("HTTP 402") || segment.contains("insufficient credits", ignoreCase = true)) {
                continue
            }

            var extractedContent: String? = null

            // 1. Try structured JSONObject parsing
            try {
                val json = JSONObject(segment)

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
                // If this segment is raw JSON metadata, DISCARD IT
                if (!isRawJsonPayload(segment)) {
                    sb.append(segment)
                }
            }
        }

        val result = sb.toString()
        return if (isRawJsonPayload(result)) "" else result
    }

    private fun isRawJsonPayload(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.startsWith("{") ||
               trimmed.startsWith("data:") ||
               trimmed.contains("\"id\":") ||
               trimmed.contains("\"choices\":") ||
               trimmed.contains("\"candidates\":") ||
               trimmed.contains("\"delta\":") ||
               trimmed.contains("\"usage\":") ||
               trimmed.contains("\"object\":") ||
               trimmed.contains("chat.completion")
    }

    /**
     * Synthesizes an intelligent, context-aware rule-based meteorological advisory
     * directly from live atmospheric measurements in memory.
     * Invoked when cloud AI credits are exhausted (HTTP 402) or offline.
     */
    fun generateLocalMeteorologicalAdvisory(
        locationContext: String,
        weatherContext: String,
        userMessage: String,
        sector: UserSector,
        language: String,
        isVoiceMode: Boolean
    ): String {
        val tempMatch = Regex("Live Atmosphere:\\s*([^|(]+)").find(weatherContext)
        val conditionMatch = Regex("\\(([^,]+),").find(weatherContext)
        val feelsLikeMatch = Regex("Feels\\s*([^)]+)").find(weatherContext)
        val humidityMatch = Regex("Humidity:\\s*([^|]+)").find(weatherContext)
        val windMatch = Regex("Wind:\\s*([^|(]+)").find(weatherContext)
        val aqiMatch = Regex("Air Quality:\\s*([^|.]+)").find(weatherContext)
        val rainMatch = Regex("Next 24h Rain:\\s*([^|]+)").find(weatherContext)
        val irrigationMatch = Regex("Irrigation Advisory:\\s*([^\\n]+)").find(weatherContext)
        val soilMoistureMatch = Regex("Surface Moisture:\\s*([^|]+)").find(weatherContext)

        val temp = tempMatch?.groupValues?.get(1)?.trim() ?: "Current Temp"
        val condition = conditionMatch?.groupValues?.get(1)?.trim() ?: "Clear/Cloudy"
        val feelsLike = feelsLikeMatch?.groupValues?.get(1)?.trim() ?: temp
        val humidity = humidityMatch?.groupValues?.get(1)?.trim() ?: "Normal"
        val wind = windMatch?.groupValues?.get(1)?.trim() ?: "Gentle Breeze"
        val aqi = aqiMatch?.groupValues?.get(1)?.trim() ?: "Moderate"
        val rain24h = rainMatch?.groupValues?.get(1)?.trim() ?: "0.0 mm"
        val irrigation = irrigationMatch?.groupValues?.get(1)?.trim() ?: "Adequate soil moisture"
        val soilMoisture = soilMoistureMatch?.groupValues?.get(1)?.trim() ?: "Normal"

        val isRainLikely = rain24h.contains(Regex("[1-9]")) || weatherContext.contains("rain", ignoreCase = true)
        val isHindi = language.equals("hi", ignoreCase = true)

        if (isVoiceMode) {
            return if (isHindi) {
                val rainNotice = if (isRainLikely) "अगले चौबीस घंटों में बारिश की संभावना है।" else "अगले चौबीस घंटों में भारी बारिश की संभावना नहीं है।"
                "क्लाउड एआई कोटा समाप्त होने के कारण यह लाइव मौसम रिपोर्ट है। $locationContext में तापमान $temp और मौसम $condition है। हवा $wind और नमी $humidity है। $rainNotice"
            } else {
                val rainNotice = if (isRainLikely) "Rain is expected in the next 24 hours." else "No significant rain is expected in the next 24 hours."
                "Cloud AI quota is currently depleted. Live weather report for $locationContext: It is $temp and $condition, with humidity at $humidity and wind at $wind. $rainNotice"
            }
        }

        // Chat Markdown Response
        return if (isHindi) {
            buildString {
                append("⚠️ **क्लाउड AI कोटा समाप्त (HTTP 402)**\n")
                append("वर्तमान में **$locationContext** के लिए लाइव मौसम परामर्श:\n\n")
                append("🌤️ **मौसम स्थिति**: $temp ($condition), महसूस $feelsLike\n")
                append("💧 **नमी**: $humidity | 💨 **हवा**: $wind | 🍃 **वायु गुणवत्ता (AQI)**: $aqi\n")
                append("🌧️ **वर्षा पूर्वानुमान**: अगले 24 घंटे में $rain24h\n\n")
                if (sector == UserSector.FARMER) {
                    append("🌾 **कृषि व सिंचाई मार्गदर्शन**:\n")
                    append("• मिट्टी की नमी: $soilMoisture — $irrigation।\n")
                    append("• छिड़काव परामर्श: हवा की गति ($wind) के अनुसार शांत घंटों में छिड़काव करें।\n\n")
                } else {
                    append("📋 **दैनिक परामर्श**:\n")
                    if (isRainLikely) {
                        append("• बारिश की संभावना को देखते हुए छाता साथ रखें और सुरक्षित यात्रा करें।\n")
                    } else {
                        append("• दिनभर मौसम सामान्य रहने की संभावना है। पर्याप्त पानी पिएं।\n")
                    }
                    append("• वायु गुणवत्ता ($aqi) के अनुसार बाहरी गतिविधियों का ध्यान रखें।\n\n")
                }
                append("💡 *सुझाव: सेटिंग्स (⚙️) में जाकर अपनी मुफ़्त Google Gemini API कुंजी जोड़ें ताकि पूर्ण AI चैट तुरंत सक्रिय हो सके।*")
            }
        } else {
            buildString {
                append("⚠️ **Cloud AI Credits Depleted (HTTP 402)**\n")
                append("Displaying **Live Meteorological Advisory** for $locationContext:\n\n")
                append("🌤️ **Atmospheric Conditions**: $temp ($condition), Feels like $feelsLike\n")
                append("💧 **Humidity**: $humidity | 💨 **Wind**: $wind | 🍃 **Air Quality**: $aqi\n")
                append("🌧️ **Precipitation Outlook**: Next 24h Rain: $rain24h\n\n")
                if (sector == UserSector.FARMER) {
                    append("🌾 **Agricultural & Field Advisory**:\n")
                    append("• **Soil Moisture**: $soilMoisture — $irrigation.\n")
                    append("• **Spraying Advisory**: Wind speed at $wind — proceed with foliar spraying during calm morning hours.\n\n")
                } else {
                    append("📋 **Daily Operations & Commute Advisory**:\n")
                    if (isRainLikely) {
                        append("• Precipitation indicated in the 24-hour window. Carry rain protection and allow extra commute time.\n")
                    } else {
                        append("• Stable conditions expected throughout the day. Ensure adequate hydration.\n")
                    }
                    append("• Air Quality index is $aqi. Plan outdoor exertion accordingly.\n\n")
                }
                append("💡 *Tip: Add your free Google Gemini API key in Settings (⚙️) to re-enable continuous AI conversations.*")
            }
        }
    }

    /**
     * Detail mode keywords.
     */
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
     * Streams SSE from direct Google Gemini API (free tier).
     */
    private fun streamGeminiDirect(
        apiKey: String,
        userMessage: String,
        locationContext: String,
        enrichedContext: String,
        history: List<Pair<String, String>>,
        model: String = AiPreferences.DEFAULT_GEMINI_MODEL
    ): Flow<String> = flow {
        val cleanModel = if (model.isBlank()) AiPreferences.DEFAULT_GEMINI_MODEL else model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:streamGenerateContent?alt=sse&key=$apiKey"

        val promptBuilder = StringBuilder()
        promptBuilder.append("System Instructions: You are WeatherGPT, an expert meteorologist and domain assistant for $locationContext. Weather context:\n$enrichedContext\n\n")
        for ((role, text) in history.takeLast(4)) {
            promptBuilder.append("${role.uppercase()}: $text\n")
        }
        promptBuilder.append("USER: $userMessage\nASSISTANT:")

        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptBuilder.toString())
                        })
                    })
                })
            }
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("maxOutputTokens", 800)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                emit("Google Gemini API error: HTTP ${response.code}. Please verify your API key in Settings.")
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit("Empty response from Google Gemini.")
                return@flow
            }

            val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.isBlank()) continue
                if (currentLine.contains("data:")) {
                    val token = extractTokenFromSseChunk(currentLine)
                    if (token.isNotEmpty()) {
                        emit(token)
                    }
                }
            }
        } catch (e: Exception) {
            emit("Gemini direct connection error: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Streams SSE from direct OpenRouter API.
     */
    private fun streamOpenRouterDirect(
        apiKey: String,
        userMessage: String,
        locationContext: String,
        enrichedContext: String,
        history: List<Pair<String, String>>,
        model: String = AiPreferences.DEFAULT_OPENROUTER_MODEL
    ): Flow<String> = flow {
        val cleanApiKey = apiKey.trim().removePrefix("Bearer ").trim()
        val cleanModel = if (model.isBlank()) AiPreferences.DEFAULT_OPENROUTER_MODEL else model
        val url = "https://openrouter.ai/api/v1/chat/completions"

        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", "You are WeatherGPT, an expert meteorologist powered by Google Gemini 3.6 Flash for $locationContext. Weather context:\n$enrichedContext")
            })
            for ((role, text) in history.takeLast(4)) {
                put(JSONObject().apply {
                    put("role", role)
                    put("content", text)
                })
            }
            put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", cleanModel)
            put("messages", messagesArray)
            put("stream", true)
            put("temperature", 0.4)
            put("max_tokens", 800)
            put("provider", JSONObject().apply {
                put("order", JSONArray().apply {
                    put("google-ai-studio")
                })
                put("ignore", JSONArray().apply {
                    put("google-vertex")
                })
                put("allow_fallbacks", false)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $cleanApiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .addHeader("HTTP-Referer", "https://weathergpt.ai")
            .addHeader("X-Title", "WeatherGPT Android")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                var errMsg = "HTTP ${response.code}"
                try {
                    val errJson = JSONObject(errBody)
                    val errorObj = errJson.optJSONObject("error")
                    if (errorObj != null) {
                        errMsg = errorObj.optString("message", errMsg)
                    }
                } catch (_: Exception) {
                    if (errBody.isNotBlank()) errMsg = errBody
                }

                if (response.code == 402) {
                    emit("OpenRouter Quota Exhausted (HTTP 402): $errMsg. Please check your credit balance at openrouter.ai/credits.")
                } else if (response.code == 401) {
                    emit("OpenRouter Authentication Error (HTTP 401): $errMsg. Please check your OpenRouter API key in local.properties or Settings.")
                } else {
                    emit("OpenRouter error (HTTP ${response.code}): $errMsg")
                }
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit("Empty response from OpenRouter.")
                return@flow
            }

            val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.isBlank()) continue

                if (currentLine.contains("\"error\":")) {
                    try {
                        val rawData = currentLine.removePrefix("data:").trim()
                        val errJson = JSONObject(rawData)
                        val errObj = errJson.optJSONObject("error")
                        val msg = errObj?.optString("message") ?: "OpenRouter stream error"
                        emit("OpenRouter error: $msg")
                        return@flow
                    } catch (_: Exception) {}
                }

                if (currentLine.contains("data:")) {
                    val token = extractTokenFromSseChunk(currentLine)
                    if (token.isNotEmpty()) {
                        emit(token)
                    }
                }
            }
        } catch (e: Exception) {
            emit("OpenRouter connection error: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Real-time Server-Sent Events (SSE) streaming flow.
     * Routes queries through the selected provider or defaults to Cloud Backend with auto-fallback.
     */
    fun streamChatCompletion(
        userMessage: String,
        locationContext: String = "Live Location",
        weatherContext: String = "",
        history: List<Pair<String, String>> = emptyList(),
        isVoiceMode: Boolean = false
    ): Flow<String> = flow {
        val providerMode = AiPreferences.getProviderMode(context)
        val geminiKey = AiPreferences.getGeminiApiKey(context)
        val openRouterKey = AiPreferences.getOpenRouterApiKey(context)
        val profile = UserPreferences.getProfile(context)

        val enrichedContext = if (profile.sector == UserSector.FARMER) {
            "$weatherContext\nUser Profile: Sector: Farmer | Primary Crops: ${profile.crops} | Land Area: ${profile.landArea} | Language: ${profile.preferredLanguage}"
        } else {
            "$weatherContext\nUser Profile: Sector: ${profile.sector.title} | Region: ${profile.monitoredRegion} | Language: ${profile.preferredLanguage}"
        }

        // 1. Direct Gemini Provider selected
        if (providerMode == AiProviderMode.GEMINI_DIRECT && geminiKey.isNotBlank()) {
            streamGeminiDirect(
                apiKey = geminiKey,
                userMessage = userMessage,
                locationContext = locationContext,
                enrichedContext = enrichedContext,
                history = history,
                model = AiPreferences.getGeminiModel(context)
            ).collect { emit(it) }
            return@flow
        }

        // 2. Direct OpenRouter Provider selected
        if (providerMode == AiProviderMode.OPENROUTER_DIRECT && openRouterKey.isNotBlank()) {
            streamOpenRouterDirect(
                apiKey = openRouterKey,
                userMessage = userMessage,
                locationContext = locationContext,
                enrichedContext = enrichedContext,
                history = history,
                model = AiPreferences.getOpenRouterModel(context)
            ).collect { emit(it) }
            return@flow
        }

        // 3. Default Cloud Backend (Render) with Auto-Fallback
        val endpoint = "/api/ai/chat-stream"
        val backendUrl = "${BackendConfig.BASE_URL}$endpoint"
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

        var quotaExhausted = false
        var emittedAnyToken = false

        for (attempt in 0..1) {
            try {
                val response = client.newCall(request).execute()

                // HTTP 402 Payment Required intercepted
                if (response.code == 402) {
                    quotaExhausted = true
                    response.close()
                    break
                }

                if (!response.isSuccessful) {
                    if (attempt == 0 && response.code in listOf(502, 503, 504)) {
                        response.close()
                        Thread.sleep(2000)
                        continue
                    }
                    response.close()
                    break
                }

                val responseBody = response.body
                if (responseBody == null) {
                    break
                }

                val reader = BufferedReader(InputStreamReader(responseBody.byteStream(), Charsets.UTF_8))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.isBlank()) continue

                    // Check for 402 inside SSE stream (Render backend forwards "Error HTTP 402")
                    if (currentLine.contains("402") || currentLine.contains("insufficient credits", ignoreCase = true) || currentLine.contains("Payment Required", ignoreCase = true)) {
                        quotaExhausted = true
                        break
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

                if (quotaExhausted) {
                    break
                }

                if (emittedAnyToken) {
                    return@flow
                }
            } catch (e: Exception) {
                if (attempt == 0) {
                    Thread.sleep(2000)
                    continue
                }
            }
        }

        // Auto-fallback when quota is exhausted or cloud is unavailable
        if (quotaExhausted || !emittedAnyToken) {
            // Auto-fallback to custom Gemini key if saved
            if (geminiKey.isNotBlank()) {
                streamGeminiDirect(
                    apiKey = geminiKey,
                    userMessage = userMessage,
                    locationContext = locationContext,
                    enrichedContext = enrichedContext,
                    history = history,
                    model = AiPreferences.getGeminiModel(context)
                ).collect { emit(it) }
                return@flow
            }

            // Auto-fallback to custom OpenRouter key if saved
            if (openRouterKey.isNotBlank()) {
                streamOpenRouterDirect(
                    apiKey = openRouterKey,
                    userMessage = userMessage,
                    locationContext = locationContext,
                    enrichedContext = enrichedContext,
                    history = history,
                    model = AiPreferences.getOpenRouterModel(context)
                ).collect { emit(it) }
                return@flow
            }

            // Synthesize and emit intelligent meteorological advisory
            val advisory = generateLocalMeteorologicalAdvisory(
                locationContext = locationContext,
                weatherContext = weatherContext,
                userMessage = userMessage,
                sector = profile.sector,
                language = profile.preferredLanguage,
                isVoiceMode = isVoiceMode
            )
            emit(advisory)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Non-streaming completion for status tests & diagnostics.
     */
    suspend fun generateChatCompletion(
        userMessage: String,
        locationContext: String = "Live Location",
        weatherContext: String = "",
        history: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val providerMode = AiPreferences.getProviderMode(context)
        val geminiKey = AiPreferences.getGeminiApiKey(context)
        val openRouterKey = AiPreferences.getOpenRouterApiKey(context)

        if (providerMode == AiProviderMode.GEMINI_DIRECT && geminiKey.isNotBlank()) {
            return@withContext testGeminiDirect(geminiKey)
        }

        if (providerMode == AiProviderMode.OPENROUTER_DIRECT && openRouterKey.isNotBlank()) {
            return@withContext testOpenRouterDirect(openRouterKey)
        }

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
            if (response.code == 402) {
                return@withContext Result.failure(Exception("AI Quota Depleted (HTTP 402): Upstream OpenRouter balance is $0."))
            }
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val reader = BufferedReader(InputStreamReader(response.body?.byteStream() ?: return@withContext Result.failure(Exception("Empty body"))))
            val fullText = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.contains("402") || currentLine.contains("insufficient credits", ignoreCase = true)) {
                    return@withContext Result.failure(Exception("AI Quota Depleted (HTTP 402): Upstream OpenRouter balance is $0."))
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
                return@withContext Result.failure(Exception("AI Quota Depleted (HTTP 402): Upstream OpenRouter balance is $0."))
            }

            Result.success(resultText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifies a Google Gemini API Key directly against Google's endpoint.
     */
    suspend fun testGeminiDirect(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val model = AiPreferences.getGeminiModel(context)
        val cleanModel = if (model.isBlank()) AiPreferences.DEFAULT_GEMINI_MODEL else model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Ping test: respond with OK")
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini HTTP ${response.code}: ${response.message}"))
            }
            Result.success("Google Gemini Online (Direct API)")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testOpenRouterDirect(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanApiKey = apiKey.trim().removePrefix("Bearer ").trim()
        val model = AiPreferences.getOpenRouterModel(context)
        val cleanModel = if (model.isBlank()) AiPreferences.DEFAULT_OPENROUTER_MODEL else model
        val url = "https://openrouter.ai/api/v1/chat/completions"

        val jsonBody = JSONObject().apply {
            put("model", cleanModel)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Ping test: respond with 'Gemini 3.6 Flash Online'")
                })
            })
            put("max_tokens", 10)
            put("provider", JSONObject().apply {
                put("order", JSONArray().apply {
                    put("google-ai-studio")
                })
                put("ignore", JSONArray().apply {
                    put("google-vertex")
                })
                put("allow_fallbacks", false)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $cleanApiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://weathergpt.ai")
            .addHeader("X-Title", "WeatherGPT Android")
            .build()

        try {
            val response = client.newCall(request).execute()
            val resBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                var errMsg = "HTTP ${response.code}"
                try {
                    val errJson = JSONObject(resBody)
                    val errorObj = errJson.optJSONObject("error")
                    if (errorObj != null) {
                        errMsg = errorObj.optString("message", errMsg)
                    }
                } catch (_: Exception) {}

                if (response.code == 402) {
                    return@withContext Result.failure(Exception("OpenRouter 402 (Insufficient credits for Gemini 3.6 Flash): $errMsg"))
                }
                if (response.code == 401) {
                    return@withContext Result.failure(Exception("OpenRouter 401 (Invalid API key): $errMsg"))
                }
                return@withContext Result.failure(Exception("OpenRouter: $errMsg"))
            }
            Result.success("OpenRouter (Gemini 3.6 Flash · AI Studio) Online ✓")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
