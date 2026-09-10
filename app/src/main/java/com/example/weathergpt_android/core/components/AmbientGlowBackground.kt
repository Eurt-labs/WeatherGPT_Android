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

    val baseColor = if (isDark) Color(0xFF000000) else Color(0xFFFBFBFA)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        // 1. Monochromatic Atmospheric Shading Wash
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Soft radial ambient light pool at upper center (monochromatic with subtle champagne beige touch)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0x15FFFFFF) else Color(0x12C4BCAF),
                        if (isDark) Color(0x0CE8E3D5) else Color(0x08ECE8E1),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.25f),
                    radius = width * 0.9f
                ),
                radius = width * 0.9f,
                center = Offset(width * 0.5f, height * 0.25f)
            )

            // Lower subtle floor pool
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0x0CE8E3D5) else Color(0x0A9E9585),
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
            speed = 0.35f,
            nodeCount = 42,
            linkDistanceDp = 145f,
            strokeWidthDp = 0.85f,
            opacity = if (isDark) 0.95f else 0.65f
        )

        // 3. Page / Screen Content Overlay
        content()
    }
}
