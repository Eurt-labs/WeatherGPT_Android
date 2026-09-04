package com.example.weathergpt_android.domain.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.weathergpt_android.domain.weather.repository.UnifiedWeatherRepository
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.FrostedIconButton
import com.example.weathergpt_android.core.network.OpenRouterService
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.domain.assistant.data.ChatDatabaseHelper
import com.example.weathergpt_android.domain.auth.data.UserPreferences
import com.example.weathergpt_android.domain.auth.model.UserProfile
import com.example.weathergpt_android.core.theme.FrostedGlassTokens
import kotlinx.coroutines.launch

enum class DiagnosticStatus {
    NOT_RUN,
    RUNNING,
    SUCCESS,
    FAILED
}

/**
 * Ultra-high contrast Frosted Settings Modal.
 * Cloud-managed Supabase backend architecture, Persona switcher, and Token Stats.
 */
@Composable
fun FrostedSettingsSheet(
    currentTheme: AppThemeMode,
    userProfile: UserProfile,
    onThemeSelected: (AppThemeMode) -> Unit,
    onEditPersona: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { ChatDatabaseHelper.getInstance(context) }
    val openRouterService = remember { OpenRouterService(context) }
    val weatherRepo = remember { UnifiedWeatherRepository(context) }

    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    var totalTokens by remember { mutableIntStateOf(0) }
    var isTestingAll by remember { mutableStateOf(false) }
    var geminiStatus by remember { mutableStateOf(DiagnosticStatus.NOT_RUN) }
    var weatherStatus by remember { mutableStateOf(DiagnosticStatus.NOT_RUN) }
    var dbStatus by remember { mutableStateOf(DiagnosticStatus.NOT_RUN) }
    var geminiDetail by remember { mutableStateOf<String?>(null) }
    var weatherDetail by remember { mutableStateOf<String?>(null) }
    var dbDetail by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        totalTokens = dbHelper.getTotalTokens()
    }

    val cardBackground = FrostedGlassTokens.surfaceRaised(isDark)
    val cardBorder = FrostedGlassTokens.border(isDark)
    val textColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFFB4B9C8) else Color(0xFF475569)
    val accentColor = if (isDark) Color(0xFFC026D3) else Color(0xFF9333EA)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(28.dp, RoundedCornerShape(28.dp), ambientColor = FrostedGlassTokens.ShadowColor, spotColor = FrostedGlassTokens.ShadowColor),
        shape = RoundedCornerShape(28.dp),
        color = cardBackground,
        border = BorderStroke(1.2.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag handle / sheet indicator pill
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(subtitleColor.copy(alpha = 0.35f))
                    .align(Alignment.CenterHorizontally)
            )

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                FrostedIconButton(
                    icon = Icons.Rounded.Close,
                    onClick = onDismiss,
                    size = 36.dp,
                    iconSize = 18.dp,
                    isDark = isDark
                )
            }

            // 1. User Profile & Persona Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "USER PERSONA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor,
                    letterSpacing = 1.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = FrostedGlassTokens.surface(isDark),
                    border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = userProfile.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text(
                                        text = "${userProfile.sector.title} (${userProfile.contact})",
                                        fontSize = 11.sp,
                                        color = subtitleColor
                                    )
                                }
                            }

                            // Edit Persona Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accentColor.copy(alpha = 0.2f))
                                    .clickable(onClick = onEditPersona)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit",
                                        tint = accentColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Appearance Mode Switcher
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "APPEARANCE MODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        title = "System",
                        icon = Icons.Rounded.PhoneAndroid,
                        isSelected = currentTheme == AppThemeMode.SYSTEM,
                        onClick = { onThemeSelected(AppThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    ThemeOptionButton(
                        title = "Dark",
                        icon = Icons.Rounded.DarkMode,
                        isSelected = currentTheme == AppThemeMode.DARK,
                        onClick = { onThemeSelected(AppThemeMode.DARK) },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    ThemeOptionButton(
                        title = "Light",
                        icon = Icons.Rounded.LightMode,
                        isSelected = currentTheme == AppThemeMode.LIGHT,
                        onClick = { onThemeSelected(AppThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }
            }

            // 3. System Diagnostics & All-in-One Health Check
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SYSTEM HEALTH & DIAGNOSTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor,
                    letterSpacing = 1.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = FrostedGlassTokens.surface(isDark),
                    border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Diagnostic Row 1: AI Engine
                        DiagnosticItemRow(
                            title = "AI Intelligence Engine",
                            subtitle = geminiDetail ?: "Cloud Reasoning Engine",
                            icon = Icons.Rounded.AutoAwesome,
                            status = geminiStatus,
                            accentColor = accentColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor
                        )

                        // Diagnostic Row 2: Live Weather
                        DiagnosticItemRow(
                            title = "Live Weather Service",
                            subtitle = weatherDetail ?: "Real-Time Atmospheric Feed",
                            icon = Icons.Rounded.WbSunny,
                            status = weatherStatus,
                            accentColor = Color(0xFFF59E0B),
                            textColor = textColor,
                            subtitleColor = subtitleColor
                        )

                        // Diagnostic Row 3: Local Database
                        DiagnosticItemRow(
                            title = "Device Offline Storage",
                            subtitle = dbDetail ?: "$totalTokens items stored locally",
                            icon = Icons.Rounded.Storage,
                            status = dbStatus,
                            accentColor = Color(0xFF10B981),
                            textColor = textColor,
                            subtitleColor = subtitleColor
                        )

                        // Live Verdict Banner
                        if (geminiStatus != DiagnosticStatus.NOT_RUN ||
                            weatherStatus != DiagnosticStatus.NOT_RUN ||
                            dbStatus != DiagnosticStatus.NOT_RUN
                        ) {
                            val isRunning = geminiStatus == DiagnosticStatus.RUNNING ||
                                            weatherStatus == DiagnosticStatus.RUNNING ||
                                            dbStatus == DiagnosticStatus.RUNNING

                            if (!isRunning) {
                                val allSuccess = geminiStatus == DiagnosticStatus.SUCCESS &&
                                                 weatherStatus == DiagnosticStatus.SUCCESS &&
                                                 dbStatus == DiagnosticStatus.SUCCESS

                                if (allSuccess) {
                                    Surface(
                                        color = Color(0x2010B981),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "All Systems Operational (AI, Weather, Storage) ✓",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }
                                } else {
                                    val failedList = mutableListOf<String>()
                                    if (geminiStatus == DiagnosticStatus.FAILED) failedList.add("AI Intelligence (${geminiDetail ?: "Failed"})")
                                    if (weatherStatus == DiagnosticStatus.FAILED) failedList.add("Weather (${weatherDetail ?: "Failed"})")
                                    if (dbStatus == DiagnosticStatus.FAILED) failedList.add("Storage (${dbDetail ?: "Failed"})")

                                    Surface(
                                        color = Color(0x25EF4444),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.ErrorOutline,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Service Issue Detected!",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFEF4444)
                                                )
                                            }
                                            Text(
                                                text = "The following is not working: ${failedList.joinToString(" • ")}",
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                                color = Color(0xFFFCA5A5)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Test All Connections Button
                        Button(
                            onClick = {
                                isTestingAll = true
                                geminiStatus = DiagnosticStatus.RUNNING
                                weatherStatus = DiagnosticStatus.RUNNING
                                dbStatus = DiagnosticStatus.RUNNING
                                geminiDetail = "Testing connection..."
                                weatherDetail = "Fetching live coordinates..."
                                dbDetail = "Checking token tables..."

                                scope.launch {
                                    // 1. Database Check
                                    try {
                                        val count = dbHelper.getTotalTokens()
                                        totalTokens = count
                                        dbStatus = DiagnosticStatus.SUCCESS
                                        dbDetail = "$count tokens stored"
                                    } catch (e: Exception) {
                                        dbStatus = DiagnosticStatus.FAILED
                                        dbDetail = e.localizedMessage ?: "Database query failed"
                                    }

                                    // 2. Weather API Check
                                    try {
                                        val weatherRes = weatherRepo.fetchLiveWeather(28.6139, 77.2090)
                                        if (weatherRes.isSuccess) {
                                            val weather = weatherRes.getOrNull()
                                            weatherStatus = DiagnosticStatus.SUCCESS
                                            weatherDetail = "${weather?.temperature ?: "Online"}, ${weather?.condition ?: "OK"}"
                                        } else {
                                            weatherStatus = DiagnosticStatus.FAILED
                                            weatherDetail = weatherRes.exceptionOrNull()?.localizedMessage ?: "Weather API failed"
                                        }
                                    } catch (e: Exception) {
                                        weatherStatus = DiagnosticStatus.FAILED
                                        weatherDetail = e.localizedMessage ?: "Network error"
                                    }

                                    // 3. AI Intelligence Check
                                    try {
                                        val geminiRes = openRouterService.generateChatCompletion("Ping test: confirm connection")
                                        if (geminiRes.isSuccess) {
                                            geminiStatus = DiagnosticStatus.SUCCESS
                                            geminiDetail = "AI Core Online"
                                        } else {
                                            geminiStatus = DiagnosticStatus.FAILED
                                            geminiDetail = geminiRes.exceptionOrNull()?.localizedMessage ?: "AI Service unavailable"
                                        }
                                    } catch (e: Exception) {
                                        geminiStatus = DiagnosticStatus.FAILED
                                        geminiDetail = e.localizedMessage ?: "Network error"
                                    }

                                    isTestingAll = false
                                }
                            },
                            enabled = !isTestingAll,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color.White
                            )
                        ) {
                            if (isTestingAll) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Text("Testing All Services...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Test All Connections", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Local Database Tokens & Management
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LOCAL CHAT DATA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor,
                    letterSpacing = 1.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = FrostedGlassTokens.surface(isDark),
                    border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QueryStats,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Total Tokens Processed",
                                    fontSize = 12.sp,
                                    color = subtitleColor
                                )
                                Text(
                                    text = "$totalTokens tokens",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }

                        // Clear Database Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0x20EF4444) else Color(0x15EF4444))
                                .clickable {
                                    scope.launch {
                                        dbHelper.clearHistory()
                                        totalTokens = 0
                                        dbDetail = "0 tokens stored"
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Clear",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Clear",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Sign Out Button
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0x30EF4444) else Color(0x18EF4444),
                    contentColor = Color(0xFFEF4444)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Sign Out",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    val activeBg = if (isDark) Color(0xFFC026D3).copy(alpha = 0.35f) else Color(0xFF9333EA).copy(alpha = 0.20f)
    val activeBorder = if (isDark) Color(0xFFE879F9) else Color(0xFF9333EA)
    val inactiveBg = FrostedGlassTokens.surfaceSubtle(isDark)
    val inactiveBorder = FrostedGlassTokens.borderSubtle(isDark)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) activeBg else inactiveBg,
        border = BorderStroke(1.2.dp, if (isSelected) activeBorder else inactiveBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) (if (isDark) Color(0xFFE879F9) else Color(0xFF9333EA))
                       else (if (isDark) Color(0xFFB4B9C8) else Color(0xFF64748B)),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) (if (isDark) Color.White else Color(0xFF0F172A))
                        else (if (isDark) Color(0xFFB4B9C8) else Color(0xFF64748B))
            )
        }
    }
}

@Composable
private fun DiagnosticItemRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    status: DiagnosticStatus,
    accentColor: Color,
    textColor: Color,
    subtitleColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(17.dp)
                )
            }
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = subtitleColor
                )
            }
        }

        when (status) {
            DiagnosticStatus.NOT_RUN -> {
                Text(
                    text = "Ready",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = subtitleColor
                )
            }
            DiagnosticStatus.RUNNING -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = accentColor,
                        strokeWidth = 1.5.dp
                    )
                    Text(
                        text = "Testing...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentColor
                    )
                }
            }
            DiagnosticStatus.SUCCESS -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Online",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
            DiagnosticStatus.FAILED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Not Working",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}
