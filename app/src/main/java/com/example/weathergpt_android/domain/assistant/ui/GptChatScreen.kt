package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.graphics.graphicsLayer
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
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Job
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
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
import com.example.weathergpt_android.domain.weather.repository.UnifiedWeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val weatherRepo = remember { UnifiedWeatherRepository(context) }
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
    val clipboardManager = LocalClipboardManager.current
    val inputFocusRequester = remember { FocusRequester() }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var generationJob by remember { mutableStateOf<Job?>(null) }
    var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }
    var isEditingPrompt by remember { mutableStateOf(false) }

    // Sherpa On-Device Voice Engine for 100% Offline Dictation (0 Tokens Used)
    val sherpaEngine = remember { SherpaOnnxEngine(context, scope) }
    val isSherpaSpeaking by sherpaEngine.isSpeaking.collectAsStateWithLifecycle()
    val speakingMessageId by sherpaEngine.currentlySpeakingId.collectAsStateWithLifecycle()

    val stopActiveGeneration: () -> Unit = {
        if (isGenerating) {
            generationJob?.cancel()
            generationJob = null
            inferenceRouter.stopGeneration()
            isGenerating = false

            val lastIndex = messages.indexOfLast { !it.isUser }
            if (lastIndex != -1) {
                val lastMsg = messages[lastIndex]
                if (lastMsg.text.isBlank()) {
                    val remId = lastMsg.id
                    messages.removeAt(lastIndex)
                    scope.launch {
                        withContext(NonCancellable + Dispatchers.IO) {
                            dbHelper.deleteMessage(remId)
                        }
                    }
                } else {
                    scope.launch {
                        withContext(NonCancellable + Dispatchers.IO) {
                            dbHelper.saveMessage(lastMsg, lastMsg.text.length / 4, userId = userProfile.userId, sessionId = activeSessionId)
                        }
                    }
                }
            }
        }
    }

    val handleBack: () -> Unit = {
        stopActiveGeneration()
        onBack()
    }

    fun copyToClipboard(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        clipboardManager.setText(AnnotatedString(clean))
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun editPrompt(promptText: String) {
        stopActiveGeneration()
        isEditingPrompt = true
        inputText = promptText
        inputFocusRequester.requestFocus()
        scope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            generationJob?.cancel()
            inferenceRouter.stopGeneration()
            sherpaEngine.stopSpeaking()
            sherpaEngine.release()
        }
    }

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
     * Sanitizes tokens so raw JSON SSE chunks and keep-alive ping comments never appear in the chat bubble.
     */
    fun sanitizeChunk(raw: String): String {
        if (raw.contains("Error HTTP 402") || raw.contains("HTTP 402") || raw.contains("insufficient credits", ignoreCase = true)) {
            return ""
        }
        if (raw.startsWith(":") || raw.startsWith("event: ping") || raw.contains("ping -", ignoreCase = true)) {
            return ""
        }
        if (raw.isBlank() && raw.isNotEmpty()) {
            return raw // Preserve whitespace/newlines emitted by offline SLM
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
            .replace(Regex("(?m)^\\s*:\\s*ping.*?$", RegexOption.MULTILINE), "")
            .replace(Regex(":\\s*ping\\s*-\\s*[^\\n]+", RegexOption.IGNORE_CASE), "")
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
        if (userText.isBlank()) return
        if (isGenerating) {
            stopActiveGeneration()
        }
        isEditingPrompt = false
        val currentSessionId = activeSessionId.ifBlank { "default" }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = userText.trim(),
            isUser = true,
            timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
            userId = userProfile.userId,
            sessionId = currentSessionId,
            createdAt = System.currentTimeMillis()
        )
        messages.add(userMessage)
        scope.launch {
            withContext(NonCancellable + Dispatchers.IO) {
                dbHelper.saveMessage(userMessage, userMessage.text.length / 4, userId = userProfile.userId, sessionId = currentSessionId)
                if (userProfile.userId.isNotBlank()) {
                    syncService.syncMessagesToCloud(userProfile.userId, listOf(userMessage))
                }
            }
        }

        val assistantMessageId = UUID.randomUUID().toString()
        val assistantMessage = ChatMessage(
            id = assistantMessageId,
            text = "",
            isUser = false,
            timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
            userId = userProfile.userId,
            sessionId = currentSessionId,
            createdAt = System.currentTimeMillis()
        )
        messages.add(assistantMessage)
        isGenerating = true

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }

        generationJob = scope.launch {
            val history = messages
                .filter { it.id != assistantMessageId && it.text.isNotBlank() }
                .takeLast(6)
                .map { (if (it.isUser) "user" else "assistant") to it.text }

            val currentFreshWeather = withContext(Dispatchers.IO) {
                weatherRepo.ensureFreshWeather(locationData.latitude, locationData.longitude)
            }
            val weatherContext = currentFreshWeather.toDenseMeteorologicalContext()

            var accumulatedResponse = ""
            var lastPersistedLength = 0

            try {
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
                        withContext(NonCancellable + Dispatchers.IO) {
                            dbHelper.saveMessage(messages[index], 0, userId = userProfile.userId, sessionId = currentSessionId)
                        }
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

                        // Periodic incremental checkpoint every ~40 chars to protect against sudden exit
                        if (accumulatedResponse.length - lastPersistedLength >= 40) {
                            lastPersistedLength = accumulatedResponse.length
                            val checkpointText = cleanFull
                            withContext(NonCancellable + Dispatchers.IO) {
                                val checkpointMsg = ChatMessage(
                                    id = assistantMessageId,
                                    text = checkpointText,
                                    isUser = false,
                                    timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                                    userId = userProfile.userId,
                                    sessionId = currentSessionId,
                                    createdAt = assistantMessage.createdAt
                                )
                                dbHelper.saveMessage(checkpointMsg, checkpointText.length / 4, userId = userProfile.userId, sessionId = currentSessionId)
                            }
                        }
                    }
                }
            } finally {
                withContext(NonCancellable + Dispatchers.IO) {
                    val finalizedText = stripRawJsonArtifacts(accumulatedResponse).trim()
                    if (finalizedText.isNotBlank()) {
                        val finalMsg = ChatMessage(
                            id = assistantMessageId,
                            text = finalizedText,
                            isUser = false,
                            timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                            userId = userProfile.userId,
                            sessionId = currentSessionId,
                            createdAt = assistantMessage.createdAt
                        )
                        val index = messages.indexOfFirst { it.id == assistantMessageId }
                        if (index != -1) {
                            messages[index] = finalMsg
                        }
                        dbHelper.saveMessage(finalMsg, finalizedText.length / 4, userId = userProfile.userId, sessionId = currentSessionId)
                        if (userProfile.userId.isNotBlank()) {
                            syncService.syncMessagesToCloud(userProfile.userId, listOf(finalMsg))
                        }
                    } else {
                        // Stopped / cancelled while thinking: clean up the blank bubble
                        val index = messages.indexOfFirst { it.id == assistantMessageId }
                        if (index != -1) {
                            messages.removeAt(index)
                        }
                        dbHelper.deleteMessage(assistantMessageId)
                    }
                }
                isGenerating = false
                generationJob = null
            }
        }
    }

    // Load persisted chat history from SQLite / Cloud on launch, then consume initial prompt
    LaunchedEffect(activeSessionId, userProfile.userId) {
        val targetSessionId = activeSessionId.ifBlank { "default" }
        dbHelper.purgeBlankMessages()
        val savedHistory = dbHelper.getAllMessages(userProfile.userId, targetSessionId)
        messages.clear()
        if (savedHistory.isNotEmpty()) {
            messages.addAll(savedHistory.filter { it.text.isNotBlank() })
        } else if (userProfile.userId.isNotBlank()) {
            // Check cloud if local is empty (e.g. after reinstall)
            val cloudRes = syncService.fetchCloudHistory(userProfile.userId)
            cloudRes.onSuccess { cloudMsgs ->
                if (cloudMsgs.isNotEmpty()) {
                    dbHelper.insertBatchFromCloud(cloudMsgs, userProfile.userId)
                    val restored = dbHelper.getAllMessages(userProfile.userId, targetSessionId)
                    messages.addAll(restored.filter { it.text.isNotBlank() })
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
                sessionId = targetSessionId
            )
            messages.add(welcomeMsg)
            scope.launch {
                withContext(NonCancellable + Dispatchers.IO) {
                    dbHelper.saveMessage(welcomeMsg, 0, userId = userProfile.userId, sessionId = targetSessionId)
                }
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
                    onClick = handleBack,
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
                    val activeMode by inferenceRouter.getActiveModeFlow().collectAsState(initial = inferenceRouter.getActiveMode())
                    val (modeLabel, modeColor) = when (activeMode) {
                        InferenceMode.ON_DEVICE -> "📱 On-Device (Offline)" to Color(0xFFF59E0B)
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

                    // Clear Active Chat Button
                    FrostedIconButton(
                        icon = Icons.Rounded.DeleteOutline,
                        onClick = {
                            stopActiveGeneration()
                            messages.clear()
                            scope.launch {
                                withContext(NonCancellable + Dispatchers.IO) {
                                    dbHelper.clearHistory(userProfile.userId)
                                }
                            }
                            val reset = ChatMessage(
                                id = UUID.randomUUID().toString(),
                                text = "Conversation reset. How can WeatherGPT assist your sector today?",
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
                        },
                        onCopy = { copyToClipboard(msg.text) },
                        onEdit = { editPrompt(msg.text) },
                        onLongPress = { selectedMessageForOptions = msg }
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

            // Editing indicator banner
            AnimatedVisibility(
                visible = isEditingPrompt,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = FrostedGlassTokens.surfaceRaised(isDark),
                    border = BorderStroke(1.dp, accentBeige.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = accentBeige,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Editing prompt • Tap Send to submit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cancel edit",
                            tint = subtitleColor,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .clickable {
                                    isEditingPrompt = false
                                    inputText = ""
                                }
                        )
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
                                if (isGenerating) {
                                    stopActiveGeneration()
                                } else if (inputText.isNotBlank()) {
                                    val t = inputText
                                    inputText = ""
                                    sendMessage(t)
                                }
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(inputFocusRequester)
                        )
                    }

                    // Send / Stop Button with Monochromatic Champagne / High-contrast Charcoal Gradient
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
                            .clickable {
                                if (isGenerating) {
                                    stopActiveGeneration()
                                } else if (inputText.isNotBlank()) {
                                    val t = inputText
                                    inputText = ""
                                    sendMessage(t)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating) {
                            // Sleek Stop square (ChatGPT / Claude style terminate button)
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(sendTint)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) sendTint else sendTint.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Touch & hold message options modal dialog
            if (selectedMessageForOptions != null) {
                val targetMsg = selectedMessageForOptions!!
                MessageOptionsModal(
                    message = targetMsg,
                    isDark = isDark,
                    onDismiss = { selectedMessageForOptions = null },
                    onCopy = {
                        copyToClipboard(targetMsg.text)
                        selectedMessageForOptions = null
                    },
                    onEdit = if (targetMsg.isUser) {
                        {
                            selectedMessageForOptions = null
                            editPrompt(targetMsg.text)
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    isDark: Boolean,
    isSpeaking: Boolean = false,
    onToggleSpeak: () -> Unit = {},
    onCopy: () -> Unit = {},
    onEdit: () -> Unit = {},
    onLongPress: () -> Unit = {}
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
                    .shadow(FrostedGlassTokens.ElevationDefault, RoundedCornerShape(22.dp, 22.dp, 4.dp, 22.dp), ambientColor = FrostedGlassTokens.ShadowColor, spotColor = FrostedGlassTokens.ShadowColor)
                    .pointerInput(message.id) {
                        detectTapGestures(onLongPress = { onLongPress() })
                    },
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

            // Action row under user bubble: timestamp + Edit + Copy
            Row(
                modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.timestamp,
                    color = if (isDark) Color.White.copy(alpha = 0.40f) else Color(0xFF94A3B8),
                    fontSize = 10.sp
                )

                // Edit Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onEdit)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit Prompt",
                        tint = accentBeige.copy(alpha = 0.85f),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Edit",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentBeige.copy(alpha = 0.85f)
                    )
                }

                // Copy Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onCopy)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy Prompt",
                        tint = accentBeige.copy(alpha = 0.85f),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Copy",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentBeige.copy(alpha = 0.85f)
                    )
                }
            }
        } else {
            // Assistant frosted glass message bubble with monochromatic/beige accents
            Surface(
                modifier = Modifier
                    .widthIn(max = 330.dp)
                    .shadow(FrostedGlassTokens.ElevationDefault, RoundedCornerShape(4.dp, 22.dp, 22.dp, 22.dp), ambientColor = FrostedGlassTokens.ShadowColor, spotColor = FrostedGlassTokens.ShadowColor)
                    .pointerInput(message.id) {
                        detectTapGestures(onLongPress = { onLongPress() })
                    },
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Copy button in header
                            if (message.text.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(onClick = onCopy),
                                    shape = RoundedCornerShape(12.dp),
                                    color = FrostedGlassTokens.surfaceSubtle(isDark),
                                    border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentCopy,
                                            contentDescription = "Copy Response",
                                            tint = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "Copy",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B)
                                        )
                                    }
                                }
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
                    }

                    if (message.text.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            ThinkingDotsAnimation(isDark = isDark)
                            Text(
                                text = "Thinking...",
                                color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    } else {
                        Text(
                            text = message.text,
                            color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }

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

@Composable
private fun MessageOptionsModal(
    message: ChatMessage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    val accentBeige = if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF)
    val textColor = if (isDark) Color.White else Color(0xFF111113)
    val subtitleColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(26.dp),
                color = FrostedGlassTokens.surfaceRaised(isDark),
                border = BorderStroke(1.dp, FrostedGlassTokens.border(isDark))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Pill handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(subtitleColor.copy(alpha = 0.4f))
                    )

                    // Message preview snippet
                    val snippet = message.text.take(90).let { if (message.text.length > 90) "$it..." else it }
                    if (snippet.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = FrostedGlassTokens.surfaceSubtle(isDark)
                        ) {
                            Text(
                                text = "\"$snippet\"",
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = subtitleColor,
                                maxLines = 2,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Edit & Resend Option (User Message)
                    if (message.isUser && onEdit != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(onClick = onEdit),
                            shape = RoundedCornerShape(16.dp),
                            color = FrostedGlassTokens.surfaceSubtle(isDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accentBeige.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit Prompt",
                                        tint = if (isDark) accentBeige else Color(0xFF18181B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Edit & Resend",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = textColor
                                    )
                                    Text(
                                        text = "Stops AI thinking and prepares prompt for resending",
                                        fontSize = 11.sp,
                                        color = subtitleColor
                                    )
                                }
                            }
                        }
                    }

                    // Copy Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onCopy),
                        shape = RoundedCornerShape(16.dp),
                        color = FrostedGlassTokens.surfaceSubtle(isDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FrostedGlassTokens.surface(isDark)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = textColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (message.isUser) "Copy Message" else "Copy Response",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                                Text(
                                    text = "Copy text to clipboard",
                                    fontSize = 11.sp,
                                    color = subtitleColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingDotsAnimation(isDark: Boolean) {
    val transition = rememberInfiniteTransition(label = "thinking_dots")
    val dotColor = if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)

    val scale1 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val scale2 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 180, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val scale3 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 360, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .graphicsLayer { scaleX = scale1; scaleY = scale1; alpha = scale1 }
                .clip(CircleShape)
                .background(dotColor)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .graphicsLayer { scaleX = scale2; scaleY = scale2; alpha = scale2 }
                .clip(CircleShape)
                .background(dotColor)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .graphicsLayer { scaleX = scale3; scaleY = scale3; alpha = scale3 }
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

