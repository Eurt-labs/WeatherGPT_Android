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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.collectAsState
import com.example.weathergpt_android.domain.inference.download.ModelDownloadManager
import com.example.weathergpt_android.domain.inference.download.ModelDownloadService
import com.example.weathergpt_android.domain.inference.download.DownloadState
import com.example.weathergpt_android.domain.inference.engine.OnDeviceEngine
import com.example.weathergpt_android.domain.inference.model.OnDeviceModelConfig
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import com.example.weathergpt_android.core.network.BackendConfig
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
 * Cloud-managed backend architecture, Persona switcher, Language selector, and Token Stats.
 */
@Composable
fun FrostedSettingsSheet(
    currentTheme: AppThemeMode,
    userProfile: UserProfile,
    onThemeSelected: (AppThemeMode) -> Unit,
    onLanguageSelected: (String) -> Unit = {},
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
    var currentLanguageCode by remember(userProfile.preferredLanguage) { mutableStateOf(userProfile.preferredLanguage) }
    var keyStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingAiKey by remember { mutableStateOf(false) }
    var backendMode by remember { mutableStateOf(BackendConfig.getBackendMode(context)) }
    var customLocalUrl by remember { mutableStateOf(BackendConfig.getCustomUrl(context)) }
    val downloadManager = remember { ModelDownloadManager.getInstance(context) }
    val onDeviceEngine = remember { OnDeviceEngine.getInstance(context) }
    val downloadState by downloadManager.downloadState.collectAsState()
    var isMemoryLoaded by remember { mutableStateOf(onDeviceEngine.isMemoryLoaded) }
    var isAutoFallback by remember { mutableStateOf(BackendConfig.isAutoFallbackEnabled(context)) }

    LaunchedEffect(Unit) {
        totalTokens = dbHelper.getTotalTokens()
    }

    val cardBackground = FrostedGlassTokens.surfaceRaised(isDark)
    val cardBorder = FrostedGlassTokens.border(isDark)
    val textColor = if (isDark) Color.White else Color(0xFF111113)
    val subtitleColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
    val accentColor = if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)

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

            // 2. App & Voice Language Switcher
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APP & VOICE LANGUAGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtitleColor,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Native Voice Tuning",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = accentColor
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = FrostedGlassTokens.surface(isDark),
                    border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val languages = listOf(
                            Pair("en" to "English", "Native Accent"),
                            Pair("hi" to "हिन्दी", "Hindi"),
                            Pair("mr" to "मराठी", "Marathi"),
                            Pair("bn" to "বাংলা", "Bengali"),
                            Pair("ta" to "தமிழ்", "Tamil"),
                            Pair("te" to "తెలుగు", "Telugu"),
                            Pair("gu" to "ગુજરાતી", "Gujarati")
                        )

                        for (rowItems in languages.chunked(2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for ((pair, english) in rowItems) {
                                    val (code, native) = pair
                                    LanguageOptionCard(
                                        code = code,
                                        nativeName = native,
                                        englishName = english,
                                        isSelected = currentLanguageCode.equals(code, ignoreCase = true),
                                        onClick = {
                                            currentLanguageCode = code
                                            onLanguageSelected(code)
                                        },
                                        modifier = Modifier.weight(1f),
                                        isDark = isDark
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 3. Appearance Mode Switcher
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

            // 3. FastAPI AI Engine (Render Hosted - Exclusively Connected)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FASTAPI AI ENGINE (RENDER HOSTED)",
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
                        // Title & Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val (headerIcon, headerTint, headerTitle) = when (backendMode) {
                                    BackendConfig.MODE_CLOUD -> Triple(Icons.Rounded.CloudDone, Color(0xFF10B981), "FastAPI on Render")
                                    BackendConfig.MODE_ON_DEVICE -> Triple(Icons.Rounded.PhoneAndroid, Color(0xFFF59E0B), "On-Device SLM (Phone)")
                                    else -> Triple(Icons.Rounded.Memory, Color(0xFF6366F1), "WeatherGPT Local PC")
                                }
                                Icon(
                                    imageVector = headerIcon,
                                    contentDescription = null,
                                    tint = headerTint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = headerTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                            val badgeBg = when (backendMode) {
                                BackendConfig.MODE_CLOUD -> Color(0x2010B981)
                                BackendConfig.MODE_ON_DEVICE -> Color(0x20F59E0B)
                                else -> Color(0x206366F1)
                            }
                            val badgeBorder = when (backendMode) {
                                BackendConfig.MODE_CLOUD -> Color(0xFF10B981).copy(alpha = 0.4f)
                                BackendConfig.MODE_ON_DEVICE -> Color(0xFFF59E0B).copy(alpha = 0.4f)
                                else -> Color(0xFF6366F1).copy(alpha = 0.4f)
                            }
                            val badgeText = when (backendMode) {
                                BackendConfig.MODE_CLOUD -> "Cloud Active"
                                BackendConfig.MODE_ON_DEVICE -> "100% Offline"
                                else -> "Offline Edge PC"
                            }
                            val badgeTextColor = when (backendMode) {
                                BackendConfig.MODE_CLOUD -> Color(0xFF10B981)
                                BackendConfig.MODE_ON_DEVICE -> Color(0xFFF59E0B)
                                else -> Color(0xFF818CF8)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeBg,
                                border = BorderStroke(1.dp, badgeBorder)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = badgeTextColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = when (backendMode) {
                                BackendConfig.MODE_CLOUD -> "Streaming via high-speed Render cloud backend with Google Gemini reasoning."
                                BackendConfig.MODE_ON_DEVICE -> "Running 100% offline directly on phone ARM64 silicon via llama.cpp (zero network dependency)."
                                else -> "Streaming via offline local PC AI engine (Qwen 2.5 GGUF) with zero cloud dependencies."
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = subtitleColor
                        )

                        // Mode Selector Chips - Row 1: Primary Production Modes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Option 1: Cloud
                            val isCloud = backendMode == BackendConfig.MODE_CLOUD
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        backendMode = BackendConfig.MODE_CLOUD
                                        BackendConfig.setBackendMode(context, BackendConfig.MODE_CLOUD)
                                        keyStatusMessage = null
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCloud) accentColor else FrostedGlassTokens.surfaceSubtle(isDark),
                                border = BorderStroke(1.dp, if (isCloud) accentColor else FrostedGlassTokens.borderSubtle(isDark))
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CloudQueue,
                                        contentDescription = null,
                                        tint = if (isCloud) (if (isDark) Color(0xFF121214) else Color.White) else subtitleColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Cloud (Gemini)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isCloud) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCloud) (if (isDark) Color(0xFF121214) else Color.White) else textColor
                                    )
                                }
                            }

                            // Option 2: On-Device (Offline Mobile)
                            val isOnDevice = backendMode == BackendConfig.MODE_ON_DEVICE
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        backendMode = BackendConfig.MODE_ON_DEVICE
                                        BackendConfig.setBackendMode(context, BackendConfig.MODE_ON_DEVICE)
                                        keyStatusMessage = null
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isOnDevice) Color(0xFFF59E0B) else FrostedGlassTokens.surfaceSubtle(isDark),
                                border = BorderStroke(1.dp, if (isOnDevice) Color(0xFFF59E0B) else FrostedGlassTokens.borderSubtle(isDark))
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PhoneAndroid,
                                        contentDescription = null,
                                        tint = if (isOnDevice) Color.Black else subtitleColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "On-Device (Offline)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isOnDevice) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isOnDevice) Color.Black else textColor
                                    )
                                }
                            }
                        }

                        // Mode Selector Chips - Row 2: Developer PC Testing Modes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // PC USB
                            val isUsb = backendMode == BackendConfig.MODE_LOCAL_USB
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        backendMode = BackendConfig.MODE_LOCAL_USB
                                        BackendConfig.setBackendMode(context, BackendConfig.MODE_LOCAL_USB)
                                        keyStatusMessage = null
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isUsb) Color(0xFF6366F1) else FrostedGlassTokens.surfaceSubtle(isDark),
                                border = BorderStroke(1.dp, if (isUsb) Color(0xFF6366F1) else FrostedGlassTokens.borderSubtle(isDark))
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bolt,
                                        contentDescription = null,
                                        tint = if (isUsb) Color.White else subtitleColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Dev: PC (USB)",
                                        fontSize = 10.sp,
                                        fontWeight = if (isUsb) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isUsb) Color.White else textColor
                                    )
                                }
                            }

                            // PC Wi-Fi
                            val isWifi = backendMode == BackendConfig.MODE_LOCAL_CUSTOM
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        backendMode = BackendConfig.MODE_LOCAL_CUSTOM
                                        BackendConfig.setBackendMode(context, BackendConfig.MODE_LOCAL_CUSTOM, customLocalUrl)
                                        keyStatusMessage = null
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isWifi) Color(0xFF8B5CF6) else FrostedGlassTokens.surfaceSubtle(isDark),
                                border = BorderStroke(1.dp, if (isWifi) Color(0xFF8B5CF6) else FrostedGlassTokens.borderSubtle(isDark))
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Memory,
                                        contentDescription = null,
                                        tint = if (isWifi) Color.White else subtitleColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Dev: PC (Wi-Fi)",
                                        fontSize = 10.sp,
                                        fontWeight = if (isWifi) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isWifi) Color.White else textColor
                                    )
                                }
                            }
                        }

                        // ═══════════════════════════════════════════════════════════════
                        // ON-DEVICE SLM ENGINE CARD (Model Management & Offline Controls)
                        // ═══════════════════════════════════════════════════════════════
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) Color(0xFF141417) else Color(0xFFF4F4F6),
                            border = BorderStroke(1.dp, if (backendMode == BackendConfig.MODE_ON_DEVICE) Color(0xFFF59E0B).copy(alpha = 0.5f) else FrostedGlassTokens.borderSubtle(isDark))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "On-Device SLM: Qwen 2.5 1.5B",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Text(
                                            text = "Quantized GGUF Q4_K_M (Runs 100% offline on phone ARM64 silicon)",
                                            fontSize = 10.sp,
                                            color = subtitleColor
                                        )
                                    }
                                    if (downloadState is DownloadState.Completed) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0x2510B981)
                                        ) {
                                            Text(
                                                text = "Ready ✓",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Download Manager Status & Controls
                                when (val state = downloadState) {
                                    is DownloadState.Completed -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = "Model file: ${downloadManager.getModelFileSizeMB()} MB in internal storage",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF10B981),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = if (isMemoryLoaded) "Status: Loaded in Memory (~1.3 GB RAM)" else "Status: In Storage (Loads on demand)",
                                                        fontSize = 10.sp,
                                                        color = if (isMemoryLoaded) Color(0xFFF59E0B) else subtitleColor
                                                    )
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        onDeviceEngine.unloadModel()
                                                        isMemoryLoaded = false
                                                        downloadManager.deleteModel()
                                                        if (backendMode == BackendConfig.MODE_ON_DEVICE) {
                                                            backendMode = BackendConfig.MODE_CLOUD
                                                            BackendConfig.setBackendMode(context, BackendConfig.MODE_CLOUD)
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Delete,
                                                        contentDescription = "Delete",
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Delete", fontSize = 10.sp)
                                                }
                                            }

                                            if (isMemoryLoaded) {
                                                OutlinedButton(
                                                    onClick = {
                                                        onDeviceEngine.unloadModel()
                                                        isMemoryLoaded = false
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B))
                                                ) {
                                                    Text("Unload Model to Free RAM", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                    is DownloadState.Downloading -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Downloading: ${state.downloadedBytes / (1024 * 1024)} MB / ${state.totalBytes / (1024 * 1024)} MB (${state.progressPercent}%)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = textColor
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0x30F59E0B)
                                                ) {
                                                    Text(
                                                        text = "${"%.1f".format(state.downloadSpeedMBs)} MB/s",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFF59E0B),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            LinearProgressIndicator(
                                                progress = { state.progressPercent / 100f },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                                color = Color(0xFFF59E0B),
                                                trackColor = Color(0x30F59E0B)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { ModelDownloadService.pause(context) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f),
                                                    border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                                                ) {
                                                    Icon(Icons.Rounded.Pause, contentDescription = null, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Pause", fontSize = 11.sp, color = textColor)
                                                }
                                                OutlinedButton(
                                                    onClick = { ModelDownloadService.cancel(context) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f),
                                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                                ) {
                                                    Text("Cancel", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                    is DownloadState.Paused -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Paused: ${state.downloadedBytes / (1024 * 1024)} MB / ${state.totalBytes / (1024 * 1024)} MB (${state.progressPercent}%)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFFF59E0B)
                                                )
                                                Text(
                                                    text = "Paused",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = subtitleColor
                                                )
                                            }

                                            LinearProgressIndicator(
                                                progress = { state.progressPercent / 100f },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                                color = Color(0xFFF59E0B),
                                                trackColor = Color(0x30F59E0B)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { ModelDownloadService.resume(context) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                                                ) {
                                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Resume", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                }
                                                OutlinedButton(
                                                    onClick = { ModelDownloadService.cancel(context) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f),
                                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                                ) {
                                                    Text("Cancel", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                    is DownloadState.CheckingSpace, is DownloadState.Verifying -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFFF59E0B))
                                            Text(
                                                text = if (state is DownloadState.CheckingSpace) "Verifying internal storage..." else "Verifying model file integrity...",
                                                fontSize = 11.sp,
                                                color = textColor
                                            )
                                        }
                                    }
                                    is DownloadState.Failed -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "⚠️ ${state.error}",
                                                fontSize = 11.sp,
                                                color = Color(0xFFEF4444)
                                            )
                                            Button(
                                                onClick = { ModelDownloadService.start(context) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                                            ) {
                                                Text("Retry Download", fontSize = 11.sp, color = Color.Black)
                                            }
                                        }
                                    }
                                    is DownloadState.Idle -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "Model not downloaded (~1.1 GB required for 100% offline inference on phone).",
                                                fontSize = 11.sp,
                                                color = subtitleColor
                                            )
                                            Button(
                                                onClick = { ModelDownloadService.start(context) },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Download,
                                                    contentDescription = null,
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Download On-Device Model (1.1 GB)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }
                                }

                                // Auto-Fallback Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = "Auto-fallback to On-Device",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textColor
                                        )
                                        Text(
                                            text = "Transparently switches from Cloud to Phone SLM if network drops.",
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp,
                                            color = subtitleColor
                                        )
                                    }
                                    Switch(
                                        checked = isAutoFallback,
                                        onCheckedChange = {
                                            isAutoFallback = it
                                            BackendConfig.setAutoFallback(context, it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFFF59E0B)
                                        )
                                    )
                                }
                            }
                        }

                        // If Local PC USB selected: Show quick command hint
                        if (backendMode == BackendConfig.MODE_LOCAL_USB) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0x156366F1),
                                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    Text(
                                        text = "USB Zero-Latency Setup:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF818CF8)
                                    )
                                    Text(
                                        text = "Run: adb reverse tcp:8000 tcp:8000 on your PC terminal, then start run_pc_server.bat",
                                        fontSize = 10.sp,
                                        color = subtitleColor
                                    )
                                }
                            }
                        }

                        // If Local PC Wi-Fi selected: Show IP editor
                        if (backendMode == BackendConfig.MODE_LOCAL_CUSTOM) {
                            OutlinedTextField(
                                value = customLocalUrl,
                                onValueChange = {
                                    customLocalUrl = it
                                    BackendConfig.setBackendMode(context, BackendConfig.MODE_LOCAL_CUSTOM, it)
                                },
                                label = { Text("PC Local URL (e.g. http://192.168.1.15:8000)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = FrostedGlassTokens.borderSubtle(isDark),
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                )
                            )
                        }

                        // Endpoint & Protocol Info Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FrostedGlassTokens.surfaceSubtle(isDark),
                            border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Target Endpoint",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = subtitleColor
                                    )
                                    Text(
                                        text = BackendConfig.getBaseUrl(context),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Protocol / Security",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = subtitleColor
                                    )
                                    Text(
                                        text = if (backendMode == BackendConfig.MODE_CLOUD) "Dynamic HMAC-SHA256" else "Direct Local Subnet",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (backendMode == BackendConfig.MODE_CLOUD) Color(0xFF10B981) else Color(0xFF818CF8)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target AI Model",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = subtitleColor
                                    )
                                    Text(
                                        text = if (backendMode == BackendConfig.MODE_CLOUD) "Google Gemini 3.6 Flash" else "Qwen 2.5 GGUF (Local Engine)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor
                                    )
                                }
                            }
                        }

                        // Test Backend Connection Button
                        Button(
                            onClick = {
                                isTestingAiKey = true
                                val targetName = if (backendMode == BackendConfig.MODE_CLOUD) "Render Cloud" else "Local PC Server"
                                keyStatusMessage = "Testing $targetName connection..."
                                scope.launch {
                                    val res = openRouterService.generateChatCompletion("Ping test: confirm connection")
                                    isTestingAiKey = false
                                    keyStatusMessage = if (res.isSuccess) {
                                        "✓ $targetName Online! Response: ${res.getOrNull()}"
                                    } else {
                                        "✗ ${res.exceptionOrNull()?.localizedMessage}"
                                    }
                                }
                            },
                            enabled = !isTestingAiKey,
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (backendMode == BackendConfig.MODE_CLOUD) accentColor else Color(0xFF6366F1),
                                contentColor = if (backendMode == BackendConfig.MODE_CLOUD) (if (isDark) Color(0xFF121214) else Color.White) else Color.White
                            )
                        ) {
                            if (isTestingAiKey) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (backendMode == BackendConfig.MODE_CLOUD) "Connecting to Render..." else "Connecting to Local PC...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (backendMode == BackendConfig.MODE_CLOUD) "Test Render Backend Connection" else "Test Local PC Server Connection",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (keyStatusMessage != null) {
                            val isOk = keyStatusMessage!!.startsWith("✓")
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isOk) Color(0x2010B981) else Color(0x25EF4444),
                                border = BorderStroke(1.dp, if (isOk) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = keyStatusMessage!!,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isOk) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. System Diagnostics & All-in-One Health Check
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
                            subtitle = geminiDetail ?: if (BackendConfig.isLocalMode(context)) "Local PC Qwen Engine" else "Cloud Reasoning Engine",
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
                                            val target = if (BackendConfig.isLocalMode(context)) "Local PC Engine" else "FastAPI Render"
                                            geminiDetail = "$target Online ✓"
                                        } else {
                                            val err = geminiRes.exceptionOrNull()?.localizedMessage ?: "AI Service unavailable"
                                            geminiStatus = DiagnosticStatus.FAILED
                                            geminiDetail = err
                                        }
                                    } catch (e: Exception) {
                                        geminiStatus = DiagnosticStatus.FAILED
                                        geminiDetail = e.localizedMessage ?: "AI connection failed"
                                    }

                                    isTestingAll = false
                                }
                            },
                            enabled = !isTestingAll,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = if (isDark) Color(0xFF121214) else Color.White
                            )
                        ) {
                            val btnContentColor = if (isDark) Color(0xFF121214) else Color.White
                            if (isTestingAll) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = btnContentColor,
                                        strokeWidth = 2.dp
                                    )
                                    Text("Testing All Services...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = btnContentColor)
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null,
                                        tint = btnContentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Test All Connections", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = btnContentColor)
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
    val activeBg = if (isDark) Color(0x35E8E3D5) else Color(0xFF18181B)
    val activeBorder = if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)
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
                tint = if (isSelected) (if (isDark) Color(0xFFE8E3D5) else Color.White)
                       else (if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) (if (isDark) Color.White else Color.White)
                        else (if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A))
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

@Composable
private fun LanguageOptionCard(
    code: String,
    nativeName: String,
    englishName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    val activeBorder = if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)
    val accentBeige = if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            if (isDark) Color(0x28E8E3D5) else Color(0xFF18181B)
        } else {
            FrostedGlassTokens.surfaceSubtle(isDark)
        },
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) activeBorder else FrostedGlassTokens.borderSubtle(isDark)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = nativeName,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) (if (isDark) Color.White else Color.White)
                            else (if (isDark) Color(0xFFE4E4E7) else Color(0xFF27272A))
                )
                Text(
                    text = englishName,
                    fontSize = 10.sp,
                    color = if (isSelected) accentBeige else (if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A))
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(accentBeige),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF121214) else Color(0xFF18181B),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}



