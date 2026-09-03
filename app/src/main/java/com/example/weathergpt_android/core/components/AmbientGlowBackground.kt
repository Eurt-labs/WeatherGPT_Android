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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.weathergpt_android.core.theme.AppThemeMode

/**
 * Immersive Edge-to-Edge Background matching the user's reference screenshot (Screenshot 2).
 * Eliminates all circular boundary flaws and renders a seamless deep obsidian canvas with
 * a diagonal luminous neon purple/magenta light wave.
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

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_light_anim")
    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_pulse"
    )

    val baseColor = if (isDark) Color(0xFF07080B) else Color(0xFFF1F5F9)
    val neonPurple = if (isDark) Color(0xFF9333EA) else Color(0xFFA855F7)
    val neonMagenta = if (isDark) Color(0xFFC026D3) else Color(0xFFE879F9)
    val neonBlue = if (isDark) Color(0xFF3B82F6) else Color(0xFF60A5FA)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Full Screen Linear Glow Bleed (Top to Bottom edge-to-edge, NO hard circle)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseColor,
                        if (isDark) Color(0x189333EA) else Color(0x10A855F7),
                        if (isDark) Color(0x25C026D3) else Color(0x15E879F9),
                        baseColor
                    ),
                    startY = height * 0.1f,
                    endY = height * 0.7f
                )
            )

            // 2. Diagonal Luminous Light Wave (Matching the radiant streak in Screenshot 2)
            val wavePath = Path().apply {
                moveTo(0f, height * 0.44f)
                cubicTo(
                    width * 0.35f, height * 0.38f * wavePulse,
                    width * 0.7f, height * 0.48f,
                    width, height * 0.34f
                )
            }

            // Glow Stroke 1 (Broad Soft Ambient)
            drawPath(
                path = wavePath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        neonBlue.copy(alpha = 0.20f * wavePulse),
                        neonPurple.copy(alpha = 0.35f * wavePulse),
                        neonMagenta.copy(alpha = 0.28f * wavePulse)
                    ),
                    start = Offset(0f, height * 0.4f),
                    end = Offset(width, height * 0.35f)
                ),
                style = Stroke(width = width * 0.28f)
            )

            // Glow Stroke 2 (Vibrant Focused Core)
            drawPath(
                path = wavePath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        neonBlue.copy(alpha = 0.35f * wavePulse),
                        neonMagenta.copy(alpha = 0.65f * wavePulse),
                        neonPurple.copy(alpha = 0.45f * wavePulse)
                    ),
                    start = Offset(0f, height * 0.4f),
                    end = Offset(width, height * 0.35f)
                ),
                style = Stroke(width = 42f)
            )
        }

        content()
    }
}
