package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.AmbientGlowBackground
import com.example.weathergpt_android.core.components.FrostedBottomInputBar
import com.example.weathergpt_android.core.components.FrostedGlassCard
import com.example.weathergpt_android.core.components.FrostedIconButton
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData

/**
 * New Chat Bento Screen matching the Right Phone Mockup from user reference image.
 * Features 2x2 Bento grid with Talk, Write, Animated Live Weather, and Alerts.
 */
@Composable
fun NewChatBentoScreen(
    currentTheme: AppThemeMode,
    liveWeatherData: LiveWeatherData,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartVoiceMode: () -> Unit,
    onStartChatWithPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    var inputText by remember { mutableStateOf("") }
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.72f) else Color(0xFF475569)

    // Breathing scale animation for weather card icon
    val infiniteTransition = rememberInfiniteTransition(label = "weather_pulse")
    val weatherIconScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_pulse"
    )

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
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar (Back, "New Chat", Settings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrostedIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = onBack,
                    isDark = isDark
                )

                Text(
                    text = "New Chat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                FrostedIconButton(
                    icon = Icons.Rounded.Settings,
                    onClick = onOpenSettings,
                    isDark = isDark
                )
            }

            // Headline
            Column(
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Hello!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "How Can I Assist You Today?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = subtitleColor
                )
            }

            // 2x2 Bento Grid (Talk, Write, Weather Animated, Alerts)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Row 1: Talk & Write
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BentoActionCard(
                        title = "Talk",
                        subtitle = "Speak\nWithout Limits",
                        icon = Icons.Rounded.Mic,
                        iconTint = if (isDark) Color(0xFFFF8A65) else Color(0xFF0284C7),
                        onClick = onStartVoiceMode,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )

                    BentoActionCard(
                        title = "Write",
                        subtitle = "Ask\nWithout Limits",
                        icon = Icons.Rounded.Edit,
                        iconTint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0EA5E9),
                        onClick = { onStartChatWithPrompt("") },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }

                // Row 2: Live Weather (Animated) & Alerts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Weather Bento Card with live temperature and animated icon
                    FrostedGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.08f),
                        cornerRadius = 24.dp,
                        onClick = { onStartChatWithPrompt("Give me a complete live weather breakdown and forecast for today.") },
                        isDark = isDark
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .scale(weatherIconScale)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0x35DF5B18) else Color(0x350284C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (liveWeatherData.condition.contains("Rain", ignoreCase = true)) Icons.Rounded.Cloud
                                        else Icons.Rounded.WbSunny,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFFFB088) else Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Text(
                                    text = liveWeatherData.temperature,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Weather",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "${liveWeatherData.condition}\nForecast",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = subtitleColor
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.NorthEast,
                                        contentDescription = null,
                                        tint = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Alerts Bento Card
                    BentoActionCard(
                        title = "Alerts",
                        subtitle = "Radar &\nLive Warnings",
                        icon = Icons.Rounded.NotificationsActive,
                        iconTint = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                        badgeText = if (liveWeatherData.floodRiskLevel.contains("Low", ignoreCase = true)) "Clear" else "Active",
                        onClick = { onStartChatWithPrompt("Are there any storm, flash flood, or air quality alerts active right now?") },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }
            }

            // Bottom Glass Input Bar
            FrostedBottomInputBar(
                inputText = inputText,
                onInputChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        val prompt = inputText
                        inputText = ""
                        onStartChatWithPrompt(prompt)
                    }
                },
                onMicClick = onStartVoiceMode,
                onVoiceWaveformClick = onStartVoiceMode,
                placeholderText = "Ask me anything...",
                isDark = isDark
            )
        }
    }
}

@Composable
private fun BentoActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    isDark: Boolean
) {
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)

    FrostedGlassCard(
        modifier = modifier.aspectRatio(1.08f),
        cornerRadius = 24.dp,
        onClick = onClick,
        isDark = isDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x25FFFFFF) else Color(0x35000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (badgeText == "Clear") Color(0x2510B981) else Color(0x30EF4444)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (badgeText == "Clear") Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = subtitleColor
                    )
                    Icon(
                        imageVector = Icons.Rounded.NorthEast,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF94A3B8),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
