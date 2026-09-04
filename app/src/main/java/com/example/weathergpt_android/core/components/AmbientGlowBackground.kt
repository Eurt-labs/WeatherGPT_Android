package com.example.weathergpt_android.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.weathergpt_android.core.theme.AppThemeMode

/**
 * ThreeUI Constellation Field Shader Background.
 * Replaces the previous ribbon streak with the canonical ThreeUI <ConstellationField />
 * particle network. Features drifting celestial nodes, proximity link lines, and
 * soft atmospheric glows tailored to the WeatherGPT Obsidian / Neon Purple & Magenta theme.
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

    val baseColor = if (isDark) Color(0xFF07080B) else Color(0xFFF1F5F9)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        // 1. Deep Space Atmospheric Gradient Wash
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Soft radial ambient light pool at upper center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0x359333EA) else Color(0x18A855F7),
                        if (isDark) Color(0x20C026D3) else Color(0x10C026D3),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.25f),
                    radius = width * 0.9f
                ),
                radius = width * 0.9f,
                center = Offset(width * 0.5f, height * 0.25f)
            )

            // Lower subtle cyan/indigo floor pool
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0x2006B6D4) else Color(0x0C38BDF8),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.65f, height * 0.85f),
                    radius = width * 0.8f
                ),
                radius = width * 0.8f,
                center = Offset(width * 0.65f, height * 0.85f)
            )
        }

        // 2. ThreeUI <ConstellationField /> Drifting Star & Link Network
        ConstellationField(
            isDark = isDark,
            speed = 0.5f,
            nodeCount = 42,
            linkDistanceDp = 145f,
            strokeWidthDp = 0.85f,
            opacity = if (isDark) 0.95f else 0.65f
        )

        // 3. Page / Screen Content Overlay
        content()
    }
}
