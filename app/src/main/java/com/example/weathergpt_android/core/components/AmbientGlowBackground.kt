package com.example.weathergpt_android.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.weathergpt_android.core.theme.AppThemeMode

/**
 * Hardware-accelerated fluid ambient glow background supporting both Dark and Light themes.
 * Renders warm amber/coral glow and oceanic teal/cyan radial gradients with subtle breathing animation.
 */
@Composable
fun AmbientGlowBackground(
    currentTheme: AppThemeMode = AppThemeMode.SYSTEM,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_pulse"
    )

    val baseColor = if (isDark) Color(0xFF090D14) else Color(0xFFF1F5F9)
    val amberGlow = if (isDark) Color(0xFFDF5B18) else Color(0xFFFF9E64)
    val tealGlow = if (isDark) Color(0xFF007A87) else Color(0xFF48CAE4)
    val secondaryGlow = if (isDark) Color(0xFF8B2500) else Color(0xFFFFB4A2)

    val amberAlpha = if (isDark) 0.38f else 0.28f
    val tealAlpha = if (isDark) 0.32f else 0.24f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Fiery Amber / Orange Radial Glow (Top-Left / Mid-Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        amberGlow.copy(alpha = amberAlpha * pulse),
                        secondaryGlow.copy(alpha = amberAlpha * 0.5f * pulse),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.32f),
                    radius = width * 0.9f * pulse
                )
            )

            // 2. Oceanic Teal / Cyan Radial Glow (Mid-Right / Bottom-Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tealGlow.copy(alpha = tealAlpha * pulse),
                        Color(0xFF00B4D8).copy(alpha = tealAlpha * 0.4f * pulse),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.88f, height * 0.65f),
                    radius = width * 0.85f * pulse
                )
            )

            // 3. Subtle center deep illumination
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        amberGlow.copy(alpha = amberAlpha * 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.45f, height * 0.18f),
                    radius = width * 0.6f
                )
            )
        }

        content()
    }
}
