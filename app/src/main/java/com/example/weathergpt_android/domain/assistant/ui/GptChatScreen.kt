package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weathergpt_android.core.components.VoiceWaveAnimation
import com.example.weathergpt_android.domain.voice.sherpa.engine.SherpaOnnxEngine
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.AmbientGlowBackground
import com.example.weathergpt_android.core.components.FrostedIconButton
import com.example.weathergpt_android.core.network.OpenRouterService
import com.example.weathergpt_android.domain.inference.router.InferenceRouter
import com.example.weathergpt_android.domain.inference.model.InferenceMode
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.core.theme.FrostedGlassTokens
import com.example.weathergpt_android.domain.assistant.data.ChatDatabaseHelper
import com.example.weathergpt_android.domain.assistant.model.ChatMessage
import com.example.weathergpt_android.domain.assistant.network.ChatSyncService
import com.example.weathergpt_android.domain.auth.data.UserPreferences
import com.example.weathergpt_android.domain.auth.model.UserSector
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Obsidian Frosted Glass Chat Window for WeatherGPT.
 * Connected with SQLite persistence (ChatDatabaseHelper), SSE token sanitization,
 * TTS speech output, and sector-personalized prompt hints.
 */
@Composable
fun GptChatScreen(
    currentTheme: AppThemeMode,
    locationData: LocationData = LocationData.DEFAULT,
    liveWeatherData: LiveWeatherData = LiveWeatherData.DEFAULT,
    initialPrompt: String? = null,
    onPromptConsumed: () -> Unit = {},
    activeSessionId: String = "default",
    onBack: () -> Unit,
    onOpenPreviousChats: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onLaunchVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inferenceRouter = remember { InferenceRouter(context) }
    val openRouterService = remember { inferenceRouter.openRouterService }
    val dbHelper = remember { ChatDatabaseHelper.getInstance(context) }
    val syncService = remember { ChatSyncService(context) }
    val userProfile = remember { UserPreferences.getProfile(context) }
    val scope = rememberCoroutineScope()

    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val textColor = if (isDark) Color.White else Color(0xFF111113)
    val subtitleColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
    val accentBeige = if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF)

    var isGenerating by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Sherpa On-Device Voice Engine for 100% Offline Dictation (0 Tokens Used)
    val sherpaEngine = remember { SherpaOnnxEngine(context, scope) }
    val isSherpaSpeaking by sherpaEngine.isSpeaking.collectAsStateWithLifecycle()
    val speakingMessageId by sherpaEngine.currentlySpeakingId.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            sherpaEngine.stopSpeaking()
            sherpaEngine.release()
        }
    }

    val messages = remember { mutableStateListOf<ChatMessage>() }

    fun isRawJsonArtifact(t: String): Boolean {
        val trimmed = t.trim()
        return trimmed.isEmpty() ||
               trimmed == "[DONE]" ||
               trimmed.startsWith("{") ||
               trimmed.startsWith("data:") ||
               trimmed.startsWith("}") ||
               trimmed.startsWith("]") ||
               trimmed.endsWith("}") ||
               trimmed.endsWith("]") ||
               trimmed.contains("\"format\":") ||
               trimmed.contains("google-gemini") ||
               trimmed.contains("finish_reason") ||
               trimmed.contains("native_finish_reason") ||
               trimmed.contains("\"signature\":") ||
               trimmed.contains("\"reasoning\":") ||
               trimmed.contains("\"reasoning_details\":") ||
               trimmed.contains("\"choices\":") ||
               trimmed.contains("\"delta\":") ||
               trimmed.contains("\"candidates\":")
    }

    /**
     * Sanitizes tokens so raw JSON SSE chunks never appear in the chat bubble.
     */
    fun sanitizeChunk(raw: String): String {
        if (raw.contains("Error HTTP 402") || raw.contains("HTTP 402") || raw.contains("insufficient credits", ignoreCase = true)) {
            return ""
        }
        val trimmed = raw.trim()
        if (isRawJsonArtifact(trimmed)) {
            val match = Regex("\"content\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(raw)
                ?: Regex("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(raw)
            if (match != null) {
                val extracted = match.groupValues[1]
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                return if (isRawJsonArtifact(extracted)) "" else extracted
            }
            return ""
        }
        return raw
    }

    fun stripRawJsonArtifacts(text: String): String {
        return text
            .replace(Regex("data:\\s*\\{.*?\\}", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("\\{\"id\":.*?\\}", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("\"format\"\\s*:\\s*\"[^\"]*\".*?\\}\\]?\\}?", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("(?:\",\\s*)?\"format\":.*", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("\\{\"candidates\".*?\\}\\]?\\}?", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("\\{\"choices\".*?\\}\\]?\\}?", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("(?:\",\\s*)?\"(?:finish_reason|native_finish_reason|signature|reasoning).*?\\}\\]?\\}?", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("[\"'\\]\\}]{2,}"), "")
            .replace("Error HTTP 402", "")
            .replace("HTTP 402", "")
            .replace("data:", "")
            .trimEnd()
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || isGenerating) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = userText.trim(),
            isUser = true,
            timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
            userId = userProfile.userId,
            sessionId = activeSessionId,
            createdAt = System.currentTimeMillis()
        )
        messages.add(userMessage)
        scope.launch {
            dbHelper.saveMessage(userMessage, userMessage.text.length / 4, userId = userProfile.userId, sessionId = activeSessionId)
            if (userProfile.userId.isNotBlank()) {
                syncService.syncMessagesToCloud(userProfile.userId, listOf(userMessage))
            }
        }

        val assistantMessageId = UUID.randomUUID().toString()
        val assistantMessage = ChatMessage(
            id = assistantMessageId,
            text = "",
            isUser = false,
            timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
            userId = userProfile.userId,
            sessionId = activeSessionId,
            createdAt = System.currentTimeMillis()
        )
        messages.add(assistantMessage)
        isGenerating = true

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }

        scope.launch {
            val history = messages
                .filter { it.id != assistantMessageId && it.text.isNotBlank() }
                .takeLast(6)
                .map { (if (it.isUser) "user" else "assistant") to it.text }

            val weatherContext = liveWeatherData.toDenseMeteorologicalContext()

            var accumulatedResponse = ""

            inferenceRouter.streamChat(
                userMessage = userText,
                locationContext = locationData.denseLocationContext,
                weatherContext = weatherContext,
                history = history,
                isVoiceMode = false
            ).catch { error ->
                val index = messages.indexOfFirst { it.id == assistantMessageId }
                if (index != -1) {
                    val errorMsg = "Connection error: ${error.localizedMessage ?: "Please check internet connection."}"
                    messages[index] = messages[index].copy(text = errorMsg)
                    dbHelper.saveMessage(messages[index], 0)
                }
                isGenerating = false
            }.collect { token ->
                val cleanToken = sanitizeChunk(token)
                if (cleanToken.isNotEmpty()) {
                    accumulatedResponse += cleanToken
                    val cleanFull = stripRawJsonArtifacts(accumulatedResponse)
                    val index = messages.indexOfFirst { it.id == assistantMessageId }
                    if (index != -1) {
                        messages[index] = messages[index].copy(text = cleanFull)
                        listState.scrollToItem(messages.size - 1)
                    }
                }
            }

            // Save final assistant message to SQLite & Cloud
            val finalizedText = stripRawJsonArtifacts(accumulatedResponse)
            if (finalizedText.isNotBlank()) {
                val index = messages.indexOfFirst { it.id == assistantMessageId }
                if (index != -1) {
                    val finalMsg = messages[index].copy(
                        text = finalizedText,
                        userId = userProfile.userId,
                        sessionId = activeSessionId,
                        createdAt = System.currentTimeMillis()
                    )
                    messages[index] = finalMsg
                    dbHelper.saveMessage(finalMsg, finalizedText.length / 4, userId = userProfile.userId, sessionId = activeSessionId)
                    if (userProfile.userId.isNotBlank()) {
                        syncService.syncMessagesToCloud(userProfile.userId, listOf(finalMsg))
                    }
                }
            }
            isGenerating = false
        }
    }

    // Load persisted chat history from SQLite / Cloud on launch, then consume initial prompt
    LaunchedEffect(activeSessionId, userProfile.userId) {
        val savedHistory = dbHelper.getAllMessages(userProfile.userId, if (activeSessionId != "default") activeSessionId else null)
        messages.clear()
        if (savedHistory.isNotEmpty()) {
            messages.addAll(savedHistory)
        } else if (userProfile.userId.isNotBlank()) {
            // Check cloud if local is empty (e.g. after reinstall)
            val cloudRes = syncService.fetchCloudHistory(userProfile.userId)
            cloudRes.onSuccess { cloudMsgs ->
                if (cloudMsgs.isNotEmpty()) {
                    dbHelper.insertBatchFromCloud(cloudMsgs, userProfile.userId)
                    val restored = dbHelper.getAllMessages(userProfile.userId, if (activeSessionId != "default") activeSessionId else null)
                    messages.addAll(restored)
                }
            }
        }

        if (messages.isEmpty()) {
            val greeting = when (userProfile.sector) {
                UserSector.FARMER -> "नमस्ते ${userProfile.name}! I am WeatherGPT Kisan AI.\n\nCurrently in ${locationData.cityName}, temperature is ${liveWeatherData.temperature} with ${liveWeatherData.condition}. Soil moisture is ${liveWeatherData.soilMoisture}. Ask about irrigation, sowing, or weather advisories for ${userProfile.crops}!"
                UserSector.DISASTER_OFFICER -> "Hello Officer ${userProfile.name}. WeatherGPT Disaster Command active.\n\nCurrent Flood Risk is ${liveWeatherData.floodRiskLevel}. River discharge & storm alert models standing by for ${userProfile.monitoredRegion}."
                UserSector.COMMUTER -> "Hi ${userProfile.name}! Currently in ${locationData.cityName}: ${liveWeatherData.temperature}, ${liveWeatherData.condition}, and AQI is ${liveWeatherData.aqi}. Ask for rain windows and travel advisories!"
                UserSector.AVIATION_LOGISTICS -> "Aviation WeatherGPT online. Surface wind ${liveWeatherData.windSpeed}, visibility good. Ask for cloud ceilings, crosswinds, or route briefings."
            }
            val welcomeMsg = ChatMessage(
                id = "welcome_msg",
                text = greeting,
                isUser = false,
                timestamp = "Just now",
                userId = userProfile.userId,
                sessionId = activeSessionId
            )
            messages.add(welcomeMsg)
            scope.launch {
                dbHelper.saveMessage(welcomeMsg, 0, userId = userProfile.userId, sessionId = activeSessionId)
            }
        }

        // Auto-send initial prompt only AFTER history has finished loading into messages!
        if (!initialPrompt.isNullOrBlank()) {
            val promptToSend = initialPrompt
            onPromptConsumed()
            sendMessage(promptToSend)
        }
    }

    AmbientGlowBackground(
        currentTheme = currentTheme,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrostedIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = onBack,
                    size = 40.dp,
                    isDark = isDark
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (isGenerating) accentBeige else Color(0xFF10B981))
                        )
                        Text(
                            text = userProfile.sector.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    val activeMode = remember(isGenerating) { inferenceRouter.getActiveMode() }
                    val (modeLabel, modeColor) = when (activeMode) {
                        InferenceMode.ON_DEVICE -> "📱 On-Device (Offline)" to Color(0xFFF59E0B)
                        InferenceMode.PC_SERVER -> "💻 PC Local Server" to Color(0xFF3B82F6)
                        InferenceMode.CLOUD -> "☁️ Gemini 3.6 Flash" to Color(0xFF10B981)
                    }
                    Text(
                        text = "${locationData.cityName} • $modeLabel",
                        fontSize = 11.sp,
                        color = modeColor
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Previous Chats / History Button
                    FrostedIconButton(
                        icon = Icons.Rounded.History,
                        onClick = onOpenPreviousChats,
                        size = 40.dp,
                        iconSize = 19.dp,
                        isDark = isDark
                    )

                    // Clear History Button
                    FrostedIconButton(
                        icon = Icons.Rounded.DeleteOutline,
                        onClick = {
                            scope.launch {
                                dbHelper.clearHistory(userProfile.userId)
                                if (userProfile.userId.isNotBlank()) {
                                    syncService.clearCloudHistory(userProfile.userId)
                                }
                            }
                            messages.clear()
                            val reset = ChatMessage(
                                id = "cleared",
                                text = "Conversation history cleared. Ask me anything about the weather!",
                                isUser = false,
                                timestamp = "Just now",
                                userId = userProfile.userId,
                                sessionId = activeSessionId
                            )
                            messages.add(reset)
                        },
                        size = 40.dp,
                        iconSize = 19.dp,
                        isDark = isDark
                    )

                    // Settings Button
                    FrostedIconButton(
                        icon = Icons.Rounded.Settings,
                        onClick = onOpenSettings,
                        size = 40.dp,
                        iconSize = 19.dp,
                        isDark = isDark
                    )
                }
            }

            // Message Stream LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isThisMsgSpeaking = speakingMessageId == msg.id
                    ChatBubbleItem(
                        message = msg,
                        isDark = isDark,
                        isSpeaking = isThisMsgSpeaking,
                        onToggleSpeak = {
                            if (isThisMsgSpeaking) {
                                sherpaEngine.stopSpeaking()
                            } else {
                                sherpaEngine.speakMessage(
                                    utteranceId = msg.id,
                                    text = msg.text,
                                    languageCode = userProfile.preferredLanguage
                                )
                            }
                        }
                    )
                }
            }

            // Quick Prompt Suggestion Pills (if not generating)
            AnimatedVisibility(
                visible = !isGenerating && messages.size <= 4,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val suggestions = when (userProfile.sector) {
                    UserSector.FARMER -> listOf(
                        "🌾 Irrigation advice for ${userProfile.crops.split(",").firstOrNull() ?: "Wheat"}",
                        "💧 Current soil moisture status",
                        "🌧️ Will it rain in next 3 days?"
                    )
                    UserSector.DISASTER_OFFICER -> listOf(
                        "🚨 Flash flood and river discharge check",
                        "⚡ Severe weather alerts",
                        "🛡️ Evacuation protocol briefing"
                    )
                    UserSector.COMMUTER -> listOf(
                        "☔ Do I need an umbrella today?",
                        "🚗 Rain impact on evening commute",
                        "😷 Air Quality Index breakdown"
                    )
                    UserSector.AVIATION_LOGISTICS -> listOf(
                        "✈️ METAR brief: Crosswinds & gusts",
                        "☁️ Cloud ceiling & visibility",
                        "📦 Road freight weather hazards"
                    )
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions) { prompt ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { sendMessage(prompt) },
                            shape = RoundedCornerShape(16.dp),
                            color = FrostedGlassTokens.surfaceSubtle(isDark),
                            border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Frosted Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(FrostedGlassTokens.ElevationRaised, RoundedCornerShape(30.dp), ambientColor = FrostedGlassTokens.ShadowColor, spotColor = FrostedGlassTokens.ShadowColor),
                shape = RoundedCornerShape(30.dp),
                color = FrostedGlassTokens.surfaceRaised(isDark),
                border = BorderStroke(
                    1.dp,
                    if (isDark) accentBeige.copy(alpha = 0.22f) else accentBeige.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Voice AI Mic Button inside Input Bar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FrostedGlassTokens.surfaceSubtle(isDark))
                            .clickable(onClick = onLaunchVoice),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Voice Mode",
                            tint = if (isDark) accentBeige else Color(0xFF18181B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Input Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Ask Weather AI in ${userProfile.preferredLanguage.uppercase()}...",
                                color = subtitleColor,
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = TextStyle(
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(if (isDark) accentBeige else Color(0xFF18181B)),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (inputText.isNotBlank()) {
                                    val t = inputText
                                    inputText = ""
                                    sendMessage(t)
                                }
                            }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Send Button with Monochromatic Champagne / High-contrast Charcoal Gradient
                    val sendGradient = if (isDark) {
                        Brush.radialGradient(
                            listOf(Color(0xFFFFFFFF), Color(0xFFE8E3D5), Color(0xFFD0C9BA))
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(Color(0xFF27272A), Color(0xFF18181B), Color(0xFF09090B))
                        )
                    }
                    val sendTint = if (isDark) Color(0xFF121214) else Color.White

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(sendGradient)
                            .clickable(enabled = !isGenerating && inputText.isNotBlank()) {
                                if (inputText.isNotBlank()) {
                                    val t = inputText
                                    inputText = ""
                                    sendMessage(t)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = sendTint, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send",
                                tint = sendTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    isDark: Boolean,
    isSpeaking: Boolean = false,
    onToggleSpeak: () -> Unit = {}
) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start

    val accentBeige = if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (isUser) {
            // User message bubble: Monochromatic Obsidian Pill with subtle beige border
            val userBubbleGradient = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFF222226), Color(0xFF161618)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF1E1E22), Color(0xFF111113)))
            }
            val userBorderColor = if (isDark) accentBeige.copy(alpha = 0.25f) else accentBeige.copy(alpha = 0.40f)

            Surface(
                modifier = Modifier
                    .widthIn(max = 295.dp)
                    .shadow(FrostedGlassTokens.ElevationDefault, RoundedCornerShape(22.dp, 22.dp, 4.dp, 22.dp), ambientColor = FrostedGlassTokens.ShadowColor, spotColor = FrostedGlassTokens.ShadowColor),
                shape = RoundedCornerShape(22.dp, 22.dp, 4.dp, 22.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, userBorderColor)
            ) {
                Box(
                    modifier = Modifier
                        .background(userBubbleGradient)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Assistant frosted glass message bubble with monochromatic/beige accents
            Surface(
                modifier = Modifier
                    .widthIn(max = 330.dp)
                    .shadow(FrostedGlassTokens.ElevationDefault, RoundedCornerShape(4.dp, 22.dp, 22.dp, 22.dp), ambientColor = FrostedGlassTokens.ShadowColor, spotColor = FrostedGlassTokens.ShadowColor),
                shape = RoundedCornerShape(4.dp, 22.dp, 22.dp, 22.dp),
                color = FrostedGlassTokens.surface(isDark),
                border = BorderStroke(
                    1.dp,
                    if (isSpeaking) {
                        accentBeige
                    } else {
                        FrostedGlassTokens.border(isDark)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0x30E8E3D5) else Color(0x1818181B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isDark) accentBeige else Color(0xFF18181B),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "WeatherGPT AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) accentBeige else Color(0xFF18181B)
                            )
                        }

                        // On-Device Voice Readout & Stop Button (0 Tokens)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onToggleSpeak),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSpeaking) Color(0x30EF4444) else FrostedGlassTokens.surfaceSubtle(isDark),
                            border = BorderStroke(
                                1.dp,
                                if (isSpeaking) Color(0xFFEF4444).copy(alpha = 0.8f) else FrostedGlassTokens.borderSubtle(isDark)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = if (isSpeaking) "Stop Dictation" else "Listen Aloud (0 Tokens)",
                                    tint = if (isSpeaking) Color(0xFFEF4444) else (if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B)),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isSpeaking) "Stop" else "Listen",
                                    fontSize = 10.sp,
                                    fontWeight = if (isSpeaking) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSpeaking) Color(0xFFFF5252) else (if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B))
                                )
                            }
                        }
                    }

                    Text(
                        text = if (message.text.isEmpty()) "..." else message.text,
                        color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF0F172A),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )

                    // Dynamic animated audio wave (tap to stop)
                    if (isSpeaking) {
                        VoiceWaveAnimation(
                            isDark = isDark,
                            onStopClick = onToggleSpeak
                        )
                    }

                    Text(
                        text = message.timestamp,
                        color = if (isDark) Color.White.copy(alpha = 0.40f) else Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
