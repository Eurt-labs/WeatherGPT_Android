package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.AmbientGlowBackground
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData

/**
 * Single Unified Main Screen matching the exact UI in user's reference (Screenshot 2).
 * Features Obsidian base with diagonal neon purple light streak, top live pill, avatar,
 * 2x2 Bento Action Cards, and bottom glowing voice launcher bar.
 */
@Composable
fun UnifiedMainScreen(
    currentTheme: AppThemeMode,
    liveWeatherData: LiveWeatherData,
    userName: String = "Dhruv",
    onLaunchVoice: () -> Unit,
    onLaunchChatWithPrompt: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)

    val neonPurple = Color(0xFF9333EA)
    val neonMagenta = Color(0xFFC026D3)
    val neonCoral = Color(0xFFFF5722)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_bento")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "weather_bento_pulse"
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
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Glowing Live Pill & Avatar/Settings Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top Left Pill Button (e.g. "WeatherGPT Live ✨")
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(onClick = { onLaunchChatWithPrompt("Give me a comprehensive weather report.") }),
                    shape = RoundedCornerShape(22.dp),
                    color = if (isDark) Color(0x301E1035) else Color(0xFFEDE9FE),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(neonPurple, neonMagenta)))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "WeatherGPT Live",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = neonMagenta,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Top Right Avatar with Neon Magenta/Purple Glowing Ring
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.sweepGradient(listOf(neonMagenta, neonPurple, neonCoral, neonMagenta)))
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF0F172A) else Color.White)
                        .clickable(onClick = onOpenSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile & Settings",
                        tint = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Greeting Headline (Matching Screenshot 2)
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Hi $userName,",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Give any command naturally, from live forecasts to crop advisories",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = subtitleColor
                )
            }

            // 2x2 Bento Action Cards (Matching Screenshot 2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Row 1: Live Weather & Voice AI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BentoHubCard(
                        title = "Live Weather",
                        subtitle = "${liveWeatherData.temperature} • ${liveWeatherData.condition}",
                        icon = Icons.Rounded.WbSunny,
                        iconTint = Color(0xFFFF9E64),
                        onClick = { onLaunchChatWithPrompt("Provide full live weather details and hourly breakdown.") },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )

                    BentoHubCard(
                        title = "Voice AI",
                        subtitle = "Instant Gemini Puck",
                        icon = Icons.Rounded.Mic,
                        iconTint = neonMagenta,
                        onClick = onLaunchVoice,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }

                // Row 2: Kisan Advisory & Disaster Radar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BentoHubCard(
                        title = "Kisan Advisory",
                        subtitle = "Soil & Irrigation",
                        icon = Icons.Rounded.Grass,
                        iconTint = Color(0xFF10B981),
                        onClick = { onLaunchChatWithPrompt("Explain current soil moisture and crop irrigation advisory.") },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )

                    BentoHubCard(
                        title = "Disaster Radar",
                        subtitle = "Severe Alerts: ${if (liveWeatherData.floodRiskLevel.contains("Low", true)) "Clear" else "Active"}",
                        icon = Icons.Rounded.Thunderstorm,
                        iconTint = Color(0xFFEF4444),
                        onClick = { onLaunchChatWithPrompt("Check for any flash flood, heatwave, or severe weather alerts.") },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }
            }

            // Bottom Glowing Launcher Bar (Matching Screenshot 2)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .clickable(onClick = onLaunchVoice),
                shape = RoundedCornerShape(32.dp),
                color = if (isDark) Color(0x35160A2A) else Color(0xE0FFFFFF),
                border = BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(neonPurple, neonMagenta, neonCoral))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tap here to start with WeatherGPT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.9f)
                    )

                    // Glowing Circular Mic Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFE879F9), Color(0xFFC026D3), Color(0xFF7C3AED))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Start Voice",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    val cardBg = if (isDark) Color(0x22181824) else Color(0x80FFFFFF)
    val cardBorder = if (isDark) Color(0x28FFFFFF) else Color(0x60FFFFFF)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF64748B)

    Surface(
        modifier = modifier
            .aspectRatio(1.10f)
            .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x20000000), spotColor = Color(0x20000000))
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Glowing Round Badge at Top-Left
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(iconTint.copy(alpha = 0.4f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Title and Subtitle at Bottom
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtitleColor,
                    maxLines = 1
                )
            }
        }
    }
}
