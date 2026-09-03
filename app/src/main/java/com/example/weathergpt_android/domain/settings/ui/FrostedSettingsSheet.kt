package com.example.weathergpt_android.domain.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.FrostedGlassCard
import com.example.weathergpt_android.core.components.FrostedIconButton
import com.example.weathergpt_android.core.network.OpenRouterPreferences
import com.example.weathergpt_android.core.network.OpenRouterService
import com.example.weathergpt_android.core.theme.AppThemeMode
import kotlinx.coroutines.launch

/**
 * Frosted Glass Settings Sheet matching the dark/light ambient glow design system.
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
    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    var apiKey by remember { mutableStateOf(OpenRouterPreferences.getApiKey(context)) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testStatusText by remember { mutableStateOf<String?>(null) }
    val openRouterService = remember { OpenRouterService(context) }

    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        cornerRadius = 28.dp,
        isDark = isDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
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
                    FrostedIconButton(
                        icon = Icons.Rounded.Settings,
                        onClick = {},
                        size = 38.dp,
                        iconSize = 18.dp,
                        isDark = isDark
                    )
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

            // 1. Theme Appearance Mode Selector
            Text(
                text = "APPEARANCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = subtitleColor,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemePillOption(
                    title = "System",
                    icon = Icons.Rounded.PhoneAndroid,
                    isSelected = currentTheme == AppThemeMode.SYSTEM,
                    onClick = { onThemeSelected(AppThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                ThemePillOption(
                    title = "Dark",
                    icon = Icons.Rounded.DarkMode,
                    isSelected = currentTheme == AppThemeMode.DARK,
                    onClick = { onThemeSelected(AppThemeMode.DARK) },
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                ThemePillOption(
                    title = "Light",
                    icon = Icons.Rounded.LightMode,
                    isSelected = currentTheme == AppThemeMode.LIGHT,
                    onClick = { onThemeSelected(AppThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
            }

            // 2. OpenRouter API Key Input
            Text(
                text = "OPENROUTER API KEY",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = subtitleColor,
                letterSpacing = 1.sp
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0x18FFFFFF) else Color(0x75FFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0x24FFFFFF) else Color(0x60FFFFFF))
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
                        tint = if (isDark) Color(0xFFFF8A65) else Color(0xFF0284C7),
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
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(if (isDark) Color(0xFFFF8A65) else Color(0xFF0284C7)),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. AI Engine Status Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) Color(0x22FFFFFF) else Color(0x85FFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0x30FFFFFF) else Color(0x70FFFFFF))
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
                            .background(if (isDark) Color(0x30FF8A65) else Color(0x300284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Memory,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFFFF8A65) else Color(0xFF0284C7),
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
                            text = "1M Token Context • Free Tier • Multimodal",
                            fontSize = 11.sp,
                            color = subtitleColor
                        )
                    }
                }
            }

            // 4. Test Connection Button
            Button(
                onClick = {
                    if (apiKey.isBlank()) {
                        testStatusText = "Please enter your OpenRouter API Key."
                        return@Button
                    }
                    isTestingConnection = true
                    testStatusText = "Testing Gemini 2.5 Flash..."
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
                    containerColor = if (isDark) Color(0xFFDF5B18) else Color(0xFF0284C7),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isTestingConnection) "Testing..." else "Test Connection",
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
private fun ThemePillOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val activeBg = if (isDark) Color(0x40FF8A65) else Color(0x350284C7)
    val activeBorder = if (isDark) Color(0xFFFF8A65) else Color(0xFF0284C7)
    val inactiveBg = if (isDark) Color(0x14FFFFFF) else Color(0x60FFFFFF)
    val inactiveBorder = if (isDark) Color(0x20FFFFFF) else Color(0x50FFFFFF)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) activeBg else inactiveBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) activeBorder else inactiveBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) (if (isDark) Color(0xFFFF9E64) else Color(0xFF0284C7))
                       else (if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) (if (isDark) Color.White else Color(0xFF0F172A))
                        else (if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B))
            )
        }
    }
}
