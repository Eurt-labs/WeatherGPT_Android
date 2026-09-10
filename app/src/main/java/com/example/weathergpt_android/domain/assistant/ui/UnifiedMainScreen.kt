package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.example.weathergpt_android.domain.assistant.network.ChatSyncService
import com.example.weathergpt_android.domain.location.model.LocationData
import java.util.Calendar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedMainScreen(
    currentTheme: AppThemeMode,
    liveWeatherData: LiveWeatherData,
    userProfile: UserProfile,
    locationData: LocationData = LocationData.DEFAULT,
    onLaunchVoice: () -> Unit,
    onLaunchChatWithPrompt: (String) -> Unit,
    onOpenPreviousChats: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onRefresh: suspend () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { ChatDatabaseHelper.getInstance(context) }
    val syncService = remember { ChatSyncService(context) }
    var totalHistoryCount by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

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

    val textColor = if (isDark) Color.White else Color(0xFF111113)
    val subtitleColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF575553)
    val accentBeige = if (isDark) Color(0xFFE8E3D5) else Color(0xFF8E8B85)
    val primaryAccent = if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113)

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val timeGreeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Hello"
    }
    val displayName = userProfile.name.ifBlank { "there" }

    val displayLocation = when {
        locationData.cityName.isNotBlank() && locationData.region.isNotBlank() ->
            "${locationData.cityName}, ${locationData.region}"
        locationData.cityName.isNotBlank() -> locationData.cityName
        userProfile.monitoredRegion.isNotBlank() -> userProfile.monitoredRegion
        else -> "Live Location"
    }

    val naturalHumanSubtitle = when (userProfile.sector) {
        UserSector.FARMER ->
            "Taking care of your ${userProfile.crops} today. Keeping a close watch on rain & soil moisture."
        UserSector.AVIATION_LOGISTICS ->
            "Monitoring flight ceilings and crosswinds today. Keeping your cargo & travel routes safe."
        UserSector.DISASTER_OFFICER ->
            "Emergency radar surveillance active. Tracking local river discharge and precipitation."
        UserSector.COMMUTER ->
            "Clear commute advisory active. Keeping you ahead of weather shifts and air quality."
    }

    AmbientGlowBackground(
        currentTheme = currentTheme,
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        try {
                            val messages = dbHelper.getAllMessages(userProfile.userId)
                            totalHistoryCount = messages.size
                            if (userProfile.userId.isNotBlank()) {
                                syncService.fetchCloudHistory(userProfile.userId).onSuccess { cloudMsgs ->
                                    if (cloudMsgs.isNotEmpty()) {
                                        dbHelper.insertBatchFromCloud(cloudMsgs, userProfile.userId)
                                        totalHistoryCount = dbHelper.getAllMessages(userProfile.userId).size
                                    }
                                }
                            }
                            onRefresh()
                        } finally {
                            isRefreshing = false
                        }
                    }
                },
                state = pullRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullRefreshState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 8.dp),
                        containerColor = if (isDark) Color(0xFF27272A) else Color(0xFFF4EFE6),
                        color = if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 22.dp, vertical = 14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Row: History Icon Button & Avatar/Settings Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top Left: Clean History Icon Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(FrostedGlassTokens.surfaceSubtle(isDark))
                        .border(
                            1.dp,
                            FrostedGlassTokens.border(isDark),
                            CircleShape
                        )
                        .clickable(onClick = onOpenPreviousChats),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "Chat History",
                        tint = textColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Top Right Avatar with Monochromatic / Champagne Ring
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                if (isDark) listOf(Color(0xFFE8E3D5), Color.White, Color(0xFFA1A1AA), Color(0xFFE8E3D5))
                                else listOf(Color(0xFF111113), Color(0xFFC4BCAF), Color(0xFF575553), Color(0xFF111113))
                            )
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF09090B) else Color.White)
                        .clickable(onClick = onOpenSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile & Settings",
                        tint = if (isDark) Color.White else Color(0xFF111113),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Personalized Greeting Headline & Location
            Column(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "$timeGreeting, $displayName",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        letterSpacing = (-0.5).sp
                    )

                    // Attractive Location & Weather Status Line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = accentBeige,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$displayLocation • ${liveWeatherData.temperature} ${liveWeatherData.condition}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFFE8E3D5) else Color(0xFF575553)
                        )
                    }
                }

                Text(
                    text = naturalHumanSubtitle,
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
                        iconTint = primaryAccent,
                        onClick = { onLaunchChatWithPrompt("Provide full live weather details and hourly breakdown.") },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )

                    BentoHubCard(
                        title = "Voice AI",
                        subtitle = "Instant Voice AI",
                        icon = Icons.Rounded.Mic,
                        iconTint = primaryAccent,
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
                                iconTint = primaryAccent,
                                onClick = onOpenPreviousChats,
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )

                            BentoHubCard(
                                title = "Disaster Radar",
                                subtitle = "Severe Alerts: ${if (liveWeatherData.floodRiskLevel.contains("Low", true)) "Clear" else "Active"}",
                                icon = Icons.Rounded.Thunderstorm,
                                iconTint = primaryAccent,
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
                                iconTint = primaryAccent,
                                onClick = { onLaunchChatWithPrompt("Give a deep emergency report on river discharge, storm surges, and flood alerts.") },
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )

                            BentoHubCard(
                                title = "Chat History",
                                subtitle = if (totalHistoryCount > 0) "History ($totalHistoryCount)" else "Previous Chats",
                                icon = Icons.Rounded.History,
                                iconTint = primaryAccent,
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
                                iconTint = primaryAccent,
                                onClick = { onLaunchChatWithPrompt("Will it rain during evening commute? Give hourly rain and AQI breakdown.") },
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )

                            BentoHubCard(
                                title = "Chat History",
                                subtitle = if (totalHistoryCount > 0) "History ($totalHistoryCount)" else "Previous Chats",
                                icon = Icons.Rounded.History,
                                iconTint = primaryAccent,
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
                                iconTint = primaryAccent,
                                onClick = { onLaunchChatWithPrompt("Provide aviation METAR style briefing: crosswinds, visibility, and cloud ceilings.") },
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )

                            BentoHubCard(
                                title = "Chat History",
                                subtitle = if (totalHistoryCount > 0) "History ($totalHistoryCount)" else "Previous Chats",
                                icon = Icons.Rounded.History,
                                iconTint = primaryAccent,
                                onClick = onOpenPreviousChats,
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                        }
                    }
                }
            }

            // Smart Context-Aware Quick Action Pills
            SmartWeatherPills(
                liveWeatherData = liveWeatherData,
                isDark = isDark,
                onLaunchChatWithPrompt = onLaunchChatWithPrompt
            )

            // "View in Detail" Expandable Card
            DetailedWeatherCard(
                liveWeatherData = liveWeatherData,
                isDark = isDark
            )

            // Space at bottom so scrolling content is never obstructed by bottom bar
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

            // Bottom Capsule: Animated bar opening Chat, animated glowing mic opening Voice!
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                if (isDark) Color(0xCC09090B) else Color(0xCCF7F6F2),
                                if (isDark) Color(0xF209090B) else Color(0xF2F7F6F2)
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                AnimatedChatCapsuleBar(
                    isDark = isDark,
                    textColor = textColor,
                    onLaunchChat = { onLaunchChatWithPrompt("") },
                    onLaunchVoice = onLaunchVoice
                )
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
    val textColor = if (isDark) Color.White else Color(0xFF111113)
    val subtitleColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF575553)

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
                            listOf(iconTint.copy(alpha = if (isDark) 0.25f else 0.12f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x26E8E3D5) else Color(0x18111113)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
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

/**
 * Animated Chat & Voice Capsule Bar with moving liquid monochromatic gradient border,
 * glowing breathing mic button, and expanding ripple halo.
 */
@Composable
private fun AnimatedChatCapsuleBar(
    isDark: Boolean,
    textColor: Color,
    onLaunchChat: () -> Unit,
    onLaunchVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "capsule_ambient_anim")

    // 1. Flowing monochromatic border shimmer
    val borderCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "border_shimmer"
    )

    // 2. Gentle mic breathing scale
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    // 3. Mic ripple halo
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mic_ripple"
    )

    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.50f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mic_ripple_alpha"
    )

    // 4. Subtle text glow/alpha wave
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.80f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )

    val dynamicBorderBrush = Brush.horizontalGradient(
        colors = if (isDark) {
            listOf(Color(0xFF27272A), Color(0xFFE8E3D5), Color(0xFFFFFFFF), Color(0xFFBFB8A5), Color(0xFF27272A))
        } else {
            listOf(Color(0xFFE4E4E7), Color(0xFF575553), Color(0xFF111113), Color(0xFFC4BCAF), Color(0xFFE4E4E7))
        },
        startX = -600f + borderCycle * 1200f,
        endX = 600f + borderCycle * 1200f,
        tileMode = TileMode.Repeated
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                FrostedGlassTokens.ElevationRaised,
                RoundedCornerShape(32.dp),
                ambientColor = FrostedGlassTokens.ShadowColor,
                spotColor = FrostedGlassTokens.ShadowColor
            )
            .clip(RoundedCornerShape(32.dp))
            .clickable(onClick = onLaunchChat),
        shape = RoundedCornerShape(32.dp),
        color = FrostedGlassTokens.surfaceRaised(isDark),
        border = BorderStroke(1.5.dp, dynamicBorderBrush)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = (if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113)).copy(alpha = textAlpha),
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Tap here to chat with WeatherGPT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = textAlpha)
                )
            }

            // Glowing Animated Circular Mic Icon: Tapping specifically opens Voice!
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding ripple halo ring behind the mic button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(rippleScale)
                        .clip(CircleShape)
                        .background((if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF)).copy(alpha = rippleAlpha * 0.40f))
                )

                // Pulsing Center Radiant Mic Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(micScale)
                        .shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            ambientColor = if (isDark) Color(0x50E8E3D5) else Color(0x30000000),
                            spotColor = if (isDark) Color(0x70E8E3D5) else Color(0x40000000)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                if (isDark) listOf(Color(0xFFFFFFFF), Color(0xFFE8E3D5), Color(0xFFC4BCAF), Color(0xFF8E8B85))
                                else listOf(Color(0xFF2C2A29), Color(0xFF1A1918), Color(0xFF111113))
                            )
                        )
                        .clickable(onClick = onLaunchVoice),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = "Start Voice",
                        tint = if (isDark) Color(0xFF111113) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Smart Context-Aware Weather Quick Action Pills.
 * Dynamically shows relevant suggestions based on current weather conditions.
 */
@Composable
private fun SmartWeatherPills(
    liveWeatherData: LiveWeatherData,
    isDark: Boolean,
    onLaunchChatWithPrompt: (String) -> Unit
) {
    val pills = buildList {
        // Parse rain probability from peakRainTiming
        if (liveWeatherData.rainNext24h != "0.0 mm" && !liveWeatherData.rainNext24h.startsWith("0.")) {
            add(Triple("☔ Rain advisory", if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113), "Will it rain today? Give me a detailed prediction with timing windows."))
        }
        // High UV
        val uvNumStr = liveWeatherData.uvIndex.substringBefore(" ").substringBefore("(")
        val uvVal = uvNumStr.trim().toIntOrNull() ?: 0
        if (uvVal >= 7) {
            add(Triple("☀️ UV Protection", if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113), "The UV index is high. What precautions should I take today?"))
        }
        // AQI Warning
        val aqiNumStr = liveWeatherData.aqi.substringBefore(" ").substringBefore("(")
        val aqiVal = aqiNumStr.trim().toIntOrNull() ?: 0
        if (aqiVal > 150) {
            add(Triple("😷 Air Quality", if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113), "Air quality is poor. Is it safe to go outside today?"))
        }
        // Heatwave
        if (!liveWeatherData.heatwaveAlert.equals("None", ignoreCase = true)) {
            add(Triple("🌡️ Heatwave", if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113), "There's a heatwave alert. What should I do to stay safe?"))
        }
        // Pressure dropping (storm risk)
        if (liveWeatherData.pressureTrend.contains("Falling", ignoreCase = true)) {
            add(Triple("🌀 Storm risk", if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113), "Barometric pressure is falling. What weather change should I expect?"))
        }
        // Fog risk
        if (liveWeatherData.dewPointProximity.contains("Critical", ignoreCase = true) ||
            liveWeatherData.dewPointProximity.contains("Warning", ignoreCase = true)) {
            add(Triple("🌫️ Fog alert", if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113), "There's a fog risk. How will visibility be affected?"))
        }
        // Default: always show "Ask anything"
        if (isEmpty()) {
            add(Triple("✨ Today's outlook", if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113), "Give me a quick weather summary for today."))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        pills.forEach { (label, _, prompt) ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onLaunchChatWithPrompt(prompt) },
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0x18E8E3D5) else Color(0x35ECE8E1),
                border = BorderStroke(1.dp, if (isDark) Color(0x28E8E3D5) else Color(0x28B8AE9C))
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White.copy(alpha = 0.92f) else Color(0xFF111113),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Expandable "View in Detail" card showing full meteorological data sections.
 */
@Composable
private fun DetailedWeatherCard(
    liveWeatherData: LiveWeatherData,
    isDark: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }
    val textColor = if (isDark) Color.White else Color(0xFF111113)
    val subtitleColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF575553)
    val accentColor = if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(18.dp),
        color = FrostedGlassTokens.surface(isDark),
        border = BorderStroke(1.dp, FrostedGlassTokens.border(isDark))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📊 View in Detail",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Tap to see full weather data & trend analysis",
                        fontSize = 11.sp,
                        color = subtitleColor
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = "Toggle Details",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Expandable Detail Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val sections = liveWeatherData.toDetailedDataView()
                    sections.forEach { (sectionTitle, items) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = sectionTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            items.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        color = subtitleColor,
                                        modifier = Modifier.weight(0.45f)
                                    )
                                    Text(
                                        text = value,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor,
                                        modifier = Modifier.weight(0.55f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
