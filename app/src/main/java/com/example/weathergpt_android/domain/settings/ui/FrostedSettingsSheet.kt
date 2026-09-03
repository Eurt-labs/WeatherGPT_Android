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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.FrostedIconButton
import com.example.weathergpt_android.core.network.OpenRouterPreferences
import com.example.weathergpt_android.core.network.OpenRouterService
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.domain.assistant.data.ChatDatabaseHelper
import kotlinx.coroutines.launch

/**
 * Ultra-high contrast Frosted Settings Modal.
 * Guaranteed 100% text readability on all displays, token database analytics,
 * and appearance mode controls.
 */
@Composable
fun FrostedSettingsSheet(
    currentTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { ChatDatabaseHelper.getInstance(context) }
    val openRouterService = remember { OpenRouterService(context) }

    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    var apiKey by remember { mutableStateOf(OpenRouterPreferences.getApiKey(context)) }
    var totalTokens by remember { mutableIntStateOf(0) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testStatusText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        totalTokens = dbHelper.getTotalTokens()
    }

    // High contrast backdrop & text colors
    val cardBackground = if (isDark) Color(0xF212131F) else Color(0xFAF8FAFC)
    val cardBorder = if (isDark) Color(0x38FFFFFF) else Color(0x80CBD5E1)
    val textColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFFB4B9C8) else Color(0xFF475569)
    val accentColor = if (isDark) Color(0xFFC026D3) else Color(0xFF9333EA)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(18.dp)
            .shadow(28.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x50000000), spotColor = Color(0x50000000)),
        shape = RoundedCornerShape(28.dp),
        color = cardBackground,
        border = BorderStroke(1.2.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
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

            // 1. Appearance Mode Switcher
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

            // 2. Chat & Token Database Analytics Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LOCAL DATABASE & TOKENS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor,
                    letterSpacing = 1.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isDark) Color(0x30FFFFFF) else Color(0x50CBD5E1))
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

            // 3. OpenRouter API Key Input (High Contrast)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "OPENROUTER API KEY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor,
                    letterSpacing = 1.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isDark) Color(0x35FFFFFF) else Color(0x50CBD5E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (apiKey.isEmpty()) {
                                Text(
                                    text = "sk-or-v1-...",
                                    color = subtitleColor,
                                    fontSize = 13.sp
                                )
                            }
                            BasicTextField(
                                value = apiKey,
                                onValueChange = {
                                    apiKey = it
                                    OpenRouterPreferences.saveApiKey(context, it)
                                },
                                textStyle = TextStyle(
                                    color = textColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(accentColor),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 4. AI Engine & Voice Profile Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, if (isDark) Color(0x30FFFFFF) else Color(0x50CBD5E1))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Memory,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Google: Gemini 2.5 Flash",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Voice: Puck • Pure Open-Meteo • Free Tier",
                            fontSize = 11.sp,
                            color = subtitleColor
                        )
                    }
                }
            }

            // 5. Test Connection Button
            Button(
                onClick = {
                    if (apiKey.isBlank()) {
                        testStatusText = "Please enter your OpenRouter API Key."
                        return@Button
                    }
                    isTestingConnection = true
                    testStatusText = "Testing Gemini 2.5 Flash connection..."
                    scope.launch {
                        val res = openRouterService.generateChatCompletion(
                            userMessage = "Ping test: Reply with 'Gemini 2.5 Flash Connected!'"
                        )
                        isTestingConnection = false
                        res.onSuccess {
                            testStatusText = "✓ $it"
                        }.onFailure {
                            testStatusText = "✗ Error: ${it.message}"
                        }
                    }
                },
                enabled = !isTestingConnection,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isTestingConnection) "Testing..." else "Test Gemini 2.5 Connection",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            testStatusText?.let { status ->
                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = if (status.startsWith("✓")) Color(0xFF10B981) else Color(0xFFEF4444),
                    fontWeight = FontWeight.Medium
                )
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
    val inactiveBg = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9)
    val inactiveBorder = if (isDark) Color(0x30FFFFFF) else Color(0x50CBD5E1)

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
