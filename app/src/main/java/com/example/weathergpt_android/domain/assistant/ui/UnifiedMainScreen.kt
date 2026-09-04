package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.weathergpt_android.domain.assistant.network.ChatSyncService
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.AmbientGlowBackground
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.core.theme.FrostedGlassTokens
import com.example.weathergpt_android.domain.assistant.data.ChatDatabaseHelper
import com.example.weathergpt_android.domain.auth.model.UserProfile
import com.example.weathergpt_android.domain.auth.model.UserSector
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData

/**
 * Single Unified Main Screen personalized to the user's role.
 * Tapping bottom bar opens Chat; tapping mic opens Voice.
 * Kisan AI card displays Chat History.
 */
@Composable
fun UnifiedMainScreen(
    currentTheme: AppThemeMode,
    liveWeatherData: LiveWeatherData,
    userProfile: UserProfile,
    onLaunchVoice: () -> Unit,
    onLaunchChatWithPrompt: (String) -> Unit,
    onOpenPreviousChats: () -> Unit = {},
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dbHelper = remember { ChatDatabaseHelper.getInstance(context) }
    val syncService = remember { ChatSyncService(context) }
    var totalHistoryCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(userProfile.userId) {
        val messages = dbHelper.getAllMessages(userProfile.userId)
        totalHistoryCount = messages.size

        // Background cloud restore if needed
        if (userProfile.userId.isNotBlank()) {
            syncService.fetchCloudHistory(userProfile.userId).onSuccess { cloudMsgs ->
                if (cloudMsgs.isNotEmpty()) {
                    dbHelper.insertBatchFromCloud(cloudMsgs, userProfile.userId)
                    totalHistoryCount = dbHelper.getAllMessages(userProfile.userId).size
                }
            }
        }
    }

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

    val dynamicSubtitle = when (userProfile.sector) {
        UserSector.FARMER -> "🌾 Kisan AI Active • Tailored for ${userProfile.crops} & soil moisture"
        UserSector.DISASTER_OFFICER -> "🚨 Disaster Radar Active • River discharge & flash flood monitoring"
        UserSector.COMMUTER -> "🏙️ Commuter AI Active • Rain windows, AQI & transit advisory"
        UserSector.AVIATION_LOGISTICS -> "✈️ Flight & Logistics Active • Wind gusts, visibility & cloud ceilings"
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
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Glowing Live Sector Pill & Avatar/Settings Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top Left Sector Pill - Opens Previous Chats
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(onClick = onOpenPreviousChats),
                    shape = RoundedCornerShape(22.dp),
                    color = FrostedGlassTokens.surfaceSubtle(isDark),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(neonPurple, neonMagenta)))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = userProfile.sector.tag,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "Previous Chats",
                            tint = neonMagenta,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Top Right Avatar with Neon Ring
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

            // Personalized Greeting Headline
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Hi ${userProfile.name},",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = dynamicSubtitle,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = subtitleColor
                )
            }

            // 2x2 Bento Action Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Row 1
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
                        subtitle = "Instant Voice AI",
                        icon = Icons.Rounded.Mic,
                        iconTint = neonMagenta,
                        onClick = onLaunchVoice,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }

                // Row 2: Kisan AI Box displays Chat History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (userProfile.sector) {
                        UserSector.FARMER -> {
                            BentoHubCard(
                                title = "Kisan AI",
                                subtitle = if (totalHistoryCount > 0) "Previous Chats ($totalHistoryCount)" else "Previous Chats",
                                icon = Icons.Rounded.History,
                                iconTint = Color(0xFF10B981),
                                onClick = onOpenPreviousChats,
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

                        UserSector.DISASTER_OFFICER -> {
                            BentoHubCard(
                                title = "Disaster Radar",
                                subtitle = "Flood Risk: ${liveWeatherData.floodRiskLevel}",
                                icon = Icons.Rounded.Thunderstorm,
                                iconTint = Color(0xFFEF4444),
                                onClick = { onLaunchChatWithPrompt("Give a deep emergency report on river discharge, storm surges, and flood alerts.") },
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )

                            BentoHubCard(
                                title = "Chat History",
                                subtitle = if (totalHistoryCount > 0) "History ($totalHistoryCount)" else "Previous Chats",
                                icon = Icons.Rounded.History,
                                iconTint = neonPurple,
                                onClick = onOpenPreviousChats,
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                        }

                        UserSector.COMMUTER -> {
                            BentoHubCard(
                                title = "Commute AQI",
                                subtitle = "AQI: ${liveWeatherData.aqi}",
                                icon = Icons.Rounded.DirectionsWalk,
                                iconTint = Color(0xFF38BDF8),
                                onClick = { onLaunchChatWithPrompt("Will it rain during evening commute? Give hourly rain and AQI breakdown.") },
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )

                            BentoHubCard(
                                title = "Chat History",
                                subtitle = if (totalHistoryCount > 0) "History ($totalHistoryCount)" else "Previous Chats",
                                icon = Icons.Rounded.History,
                                iconTint = Color(0xFFF59E0B),
                                onClick = onOpenPreviousChats,
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                        }

                        UserSector.AVIATION_LOGISTICS -> {
                            BentoHubCard(
                                title = "Flight Weather",
                                subtitle = "Wind: ${liveWeatherData.windSpeed}",
                                icon = Icons.Rounded.Flight,
                                iconTint = Color(0xFF6366F1),
                                onClick = { onLaunchChatWithPrompt("Provide aviation METAR style briefing: crosswinds, visibility, and cloud ceilings.") },
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )

                            BentoHubCard(
                                title = "Chat History",
                                subtitle = if (totalHistoryCount > 0) "History ($totalHistoryCount)" else "Previous Chats",
                                icon = Icons.Rounded.History,
                                iconTint = Color(0xFFEF4444),
                                onClick = onOpenPreviousChats,
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                        }
                    }
                }
            }

            // Bottom Capsule: Tapping the bar opens Chat; tapping mic opens Voice!
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(FrostedGlassTokens.ElevationRaised, RoundedCornerShape(32.dp), ambientColor = FrostedGlassTokens.ShadowColor, spotColor = FrostedGlassTokens.ShadowColor)
                    .clip(RoundedCornerShape(32.dp))
                    .clickable(onClick = { onLaunchChatWithPrompt("") }), // Tapping capsule opens Chat window
                shape = RoundedCornerShape(32.dp),
                color = FrostedGlassTokens.surfaceRaised(isDark),
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
                        text = "Tap here to chat with WeatherGPT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.9f),
                        modifier = Modifier.weight(1f)
                    )

                    // Glowing Circular Mic Icon: Tapping specifically opens Voice!
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFE879F9), Color(0xFFC026D3), Color(0xFF7C3AED))
                                )
                            )
                            .clickable(onClick = onLaunchVoice),
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
    val cardBg = FrostedGlassTokens.surface(isDark)
    val cardBorder = FrostedGlassTokens.border(isDark)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF64748B)

    Surface(
        modifier = modifier
            .aspectRatio(1.10f)
            .shadow(FrostedGlassTokens.ElevationDefault, RoundedCornerShape(22.dp), ambientColor = FrostedGlassTokens.ShadowColor, spotColor = FrostedGlassTokens.ShadowColor)
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
