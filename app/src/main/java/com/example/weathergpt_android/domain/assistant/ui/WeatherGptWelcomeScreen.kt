package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.AmbientGlowBackground
import com.example.weathergpt_android.core.components.FrostedGlassCard
import com.example.weathergpt_android.core.components.FrostedIconButton
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData

/**
 * Welcome & Overview Screen matching the Left Phone Mockup from the user's reference image.
 */
@Composable
fun WeatherGptWelcomeScreen(
    currentTheme: AppThemeMode,
    liveWeatherData: LiveWeatherData,
    onNavigateToAssistant: () -> Unit,
    onNavigateToBento: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.72f) else Color(0xFF475569)

    AmbientGlowBackground(
        currentTheme = currentTheme,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Brand Badge & Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Frosted Brand Badge Pill
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0x28FFFFFF) else Color(0x80FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) Color(0x35FFFFFF) else Color(0x75FFFFFF)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFFFF8A65) else Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "WeatherGPT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }

                // Settings Frosted Icon Button
                FrostedIconButton(
                    icon = Icons.Rounded.Settings,
                    onClick = onOpenSettings,
                    isDark = isDark
                )
            }

            // Headline Typography (Hello I'm WeatherGPT)
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Hello I'm WeatherGPT",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "How Can I Help You Today?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = subtitleColor
                )
            }

            // Vertical Frosted Action Capsules (Matching Left Phone Mockup)
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ActionCapsuleCard(
                    title = "Explore Live Weather & Radar",
                    subtitle = "Real-time forecast: ${liveWeatherData.temperature}, ${liveWeatherData.condition}",
                    icon = Icons.Rounded.WbSunny,
                    iconBg = if (isDark) Color(0xFFDF5B18) else Color(0xFFFF9E64),
                    onClick = onNavigateToBento,
                    isDark = isDark
                )

                ActionCapsuleCard(
                    title = "Conversational AI Intelligence",
                    subtitle = "Powered by Google Gemini 2.5 Flash",
                    icon = Icons.Rounded.AutoAwesome,
                    iconBg = if (isDark) Color(0xFF0284C7) else Color(0xFF38BDF8),
                    onClick = onNavigateToAssistant,
                    isDark = isDark
                )

                ActionCapsuleCard(
                    title = "Hands-Free Voice AI Mode",
                    subtitle = "Speak naturally in Hindi, English & Dialects",
                    icon = Icons.Rounded.Mic,
                    iconBg = if (isDark) Color(0xFF10B981) else Color(0xFF34D399),
                    onClick = onNavigateToVoice,
                    isDark = isDark
                )
            }

            // Bottom Action Launcher Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToAssistant,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0x35FFFFFF) else Color(0x85FFFFFF),
                        contentColor = textColor
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) Color(0x40FFFFFF) else Color(0x80FFFFFF)
                    )
                ) {
                    Text(
                        text = "Smart Assistant",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onNavigateToBento,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFFDF5B18) else Color(0xFF0284C7),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "New Chat",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCapsuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit,
    isDark: Boolean
) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        onClick = onClick,
        isDark = isDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg.copy(alpha = if (isDark) 0.85f else 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B),
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Rounded.OpenInNew,
                contentDescription = null,
                tint = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
