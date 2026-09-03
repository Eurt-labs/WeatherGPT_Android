package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.AmbientGlowBackground
import com.example.weathergpt_android.core.components.FrostedBottomInputBar
import com.example.weathergpt_android.core.components.FrostedGlassChip
import com.example.weathergpt_android.core.components.FrostedIconButton
import com.example.weathergpt_android.core.components.VoiceWaveformPills
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData

/**
 * Smart Assistant & Voice Screen matching the Center Phone Mockup from the user's reference image.
 */
@Composable
fun SmartAssistantScreen(
    currentTheme: AppThemeMode,
    liveWeatherData: LiveWeatherData,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onStartChatWithPrompt: (String) -> Unit,
    onLaunchVoiceAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    var inputText by remember { mutableStateOf("") }
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)

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
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation Bar (Back, Title, Close)
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
                    text = "Smart Assistant",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                FrostedIconButton(
                    icon = Icons.Rounded.Close,
                    onClick = onClose,
                    isDark = isDark
                )
            }

            // Center Content (Breathing Waveform, Title, Chips)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Animated Breathing Waveform Centerpiece
                VoiceWaveformPills(
                    isActive = false,
                    isDark = isDark
                )

                Text(
                    text = "Ask Me Anything!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Smart Weather & Climate Intelligence - Anytime.",
                    fontSize = 13.sp,
                    color = subtitleColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Question Chips (Row 1)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FrostedGlassChip(
                        text = "Will it rain today?",
                        icon = Icons.Rounded.Grain,
                        onClick = { onStartChatWithPrompt("Will it rain today at my location?") },
                        isDark = isDark
                    )
                    FrostedGlassChip(
                        text = "Current AQI: ${liveWeatherData.aqi.take(3)}",
                        icon = Icons.Rounded.Speed,
                        onClick = { onStartChatWithPrompt("What is the current air quality and health advisory?") },
                        isDark = isDark
                    )
                }

                // Quick Question Chips (Row 2)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FrostedGlassChip(
                        text = "Soil & Irrigation",
                        icon = Icons.Rounded.Grass,
                        onClick = { onStartChatWithPrompt("What is my soil moisture and irrigation recommendation?") },
                        isDark = isDark
                    )
                    FrostedGlassChip(
                        text = "Severe Alerts",
                        icon = Icons.Rounded.ElectricBolt,
                        onClick = { onStartChatWithPrompt("Are there any severe weather, flood, or storm alerts today?") },
                        isDark = isDark
                    )
                }
            }

            // Bottom Section: Horizontal Tool Ribbon & Glass Input Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Horizontal Tools Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FrostedGlassChip(
                        text = "Aa Translation",
                        icon = Icons.Rounded.Translate,
                        onClick = { onStartChatWithPrompt("Translate the forecast into Hindi and regional Indian languages.") },
                        isDark = isDark
                    )
                    FrostedGlassChip(
                        text = "7-Day Outlook",
                        icon = Icons.Rounded.WbSunny,
                        onClick = { onStartChatWithPrompt("Give me a comprehensive 7-day weather outlook.") },
                        isDark = isDark
                    )
                    FrostedGlassChip(
                        text = "Evapotranspiration",
                        icon = Icons.Rounded.WaterDrop,
                        onClick = { onStartChatWithPrompt("Explain the FAO evapotranspiration and water loss for crops today.") },
                        isDark = isDark
                    )
                }

                // Frosted Bottom Input Bar
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
                    onMicClick = onLaunchVoiceAi,
                    onVoiceWaveformClick = onLaunchVoiceAi,
                    placeholderText = "Ask me anything...",
                    isDark = isDark
                )
            }
        }
    }
}
