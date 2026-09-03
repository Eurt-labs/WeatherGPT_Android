package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.AmbientGlowBackground
import com.example.weathergpt_android.core.components.FrostedBottomInputBar
import com.example.weathergpt_android.core.components.FrostedGlassCard
import com.example.weathergpt_android.core.components.FrostedIconButton
import com.example.weathergpt_android.core.network.OpenRouterPreferences
import com.example.weathergpt_android.core.network.OpenRouterService
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.domain.assistant.model.ChatMessage
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun GptChatScreen(
    currentTheme: AppThemeMode,
    locationData: LocationData = LocationData.DEFAULT,
    liveWeatherData: LiveWeatherData = LiveWeatherData.DEFAULT,
    initialPrompt: String? = null,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onLaunchVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val openRouterService = remember { OpenRouterService(context) }
    val scope = rememberCoroutineScope()
    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)

    var isGenerating by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "1",
                text = "Hello! I'm WeatherGPT powered by Google Gemini 2.5 Flash.\n\nCurrently in ${locationData.cityName}, it's ${liveWeatherData.temperature} with ${liveWeatherData.condition}, humidity at ${liveWeatherData.humidity}, and AQI at ${liveWeatherData.aqi}. Ask me anything!",
                isUser = false,
                timestamp = "Just now"
            )
        )
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || isGenerating) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = userText.trim(),
            isUser = true,
            timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        )
        messages.add(userMessage)

        val assistantMessageId = UUID.randomUUID().toString()
        val assistantMessage = ChatMessage(
            id = assistantMessageId,
            text = "",
            isUser = false,
            timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
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

            val weatherContext = "${liveWeatherData.temperature}, ${liveWeatherData.condition}, Humidity ${liveWeatherData.humidity}, Wind ${liveWeatherData.windSpeed}, AQI ${liveWeatherData.aqi}, Soil ${liveWeatherData.soilMoisture}, Flood ${liveWeatherData.floodRiskLevel}"

            openRouterService.streamChatCompletion(
                userMessage = userText,
                locationContext = "${locationData.cityName}, ${locationData.country}",
                weatherContext = weatherContext,
                history = history
            ).catch { error ->
                val index = messages.indexOfFirst { it.id == assistantMessageId }
                if (index != -1) {
                    messages[index] = messages[index].copy(
                        text = "Connection error: ${error.localizedMessage ?: "Please check your network and OpenRouter API key in Settings."}"
                    )
                }
                isGenerating = false
            }.collect { token ->
                val index = messages.indexOfFirst { it.id == assistantMessageId }
                if (index != -1) {
                    val currentText = messages[index].text
                    messages[index] = messages[index].copy(text = currentText + token)
                    listState.scrollToItem(messages.size - 1)
                }
            }
            isGenerating = false
        }
    }

    // Auto-send initial prompt if provided
    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            sendMessage(initialPrompt)
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
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrostedIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = onBack,
                    size = 38.dp,
                    isDark = isDark
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isGenerating) Color(0xFFFF8A65) else Color(0xFF10B981))
                        )
                        Text(
                            text = "WeatherGPT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    Text(
                        text = "Google: Gemini 2.5 Flash",
                        fontSize = 11.sp,
                        color = subtitleColor
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FrostedIconButton(
                        icon = Icons.Rounded.DeleteOutline,
                        onClick = {
                            messages.clear()
                            messages.add(
                                ChatMessage(
                                    id = "1",
                                    text = "Conversation cleared. How can I help you today?",
                                    isUser = false,
                                    timestamp = "Just now"
                                )
                            )
                        },
                        size = 38.dp,
                        iconSize = 18.dp,
                        isDark = isDark
                    )

                    FrostedIconButton(
                        icon = Icons.Rounded.Settings,
                        onClick = onOpenSettings,
                        size = 38.dp,
                        iconSize = 18.dp,
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
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        isDark = isDark,
                        onSpeak = {
                            // Instant voice synthesis / playback trigger
                        }
                    )
                }
            }

            // Bottom Frosted Input Bar
            FrostedBottomInputBar(
                inputText = inputText,
                onInputChange = { inputText = it },
                onSend = {
                    val text = inputText
                    inputText = ""
                    sendMessage(text)
                },
                onMicClick = onLaunchVoice,
                onVoiceWaveformClick = onLaunchVoice,
                placeholderText = "Ask Gemini 2.5 Flash...",
                isDark = isDark
            )
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    isDark: Boolean,
    onSpeak: () -> Unit
) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (isUser) {
            // User message bubble (Amber/Cyan vibrant gradient)
            Surface(
                modifier = Modifier.widthIn(max = 290.dp),
                shape = RoundedCornerShape(22.dp, 22.dp, 4.dp, 22.dp),
                color = if (isDark) Color(0xFFDF5B18) else Color(0xFF0284C7),
                border = BorderStroke(1.dp, if (isDark) Color(0xFFFF8A65) else Color(0xFF38BDF8))
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        } else {
            // Assistant frosted glass message bubble
            FrostedGlassCard(
                modifier = Modifier.widthIn(max = 320.dp),
                cornerRadius = 22.dp,
                isDark = isDark
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
                                    .background(if (isDark) Color(0x35FF8A65) else Color(0x350284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFFF8A65) else Color(0xFF0284C7),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "Gemini 2.5 Flash",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFFFF9E64) else Color(0xFF0284C7)
                            )
                        }

                        Icon(
                            imageVector = Icons.Rounded.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF94A3B8),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(onClick = onSpeak)
                        )
                    }

                    Text(
                        text = if (message.text.isEmpty()) "..." else message.text,
                        color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF0F172A),
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )

                    Text(
                        text = message.timestamp,
                        color = if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
