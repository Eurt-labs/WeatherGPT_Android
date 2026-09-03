package com.example.weathergpt_android.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Ultra-Immersive Animated Edge Lighting with Enhanced Corner Blooms.
 * Tuned for modern curved display bezels (52dp+ corner curvature) with volumetric
 * 4-corner radial ambient pools and fluid neon perimeter sweeps.
 */
@Composable
fun VoiceEdgeLighting(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "edge_lighting_anim")

    // Dynamic stroke breathing animation
    val pulse by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.85f else 0.45f,
        targetValue = if (isActive) 1.35f else 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 750 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edge_pulse"
    )

    // Fluid rotation for dynamic rainbow/neon circulation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 4500 else 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "edge_rotation"
    )

    // Corner volumetric expansion
    val cornerBloomSize by infiniteTransition.animateFloat(
        initialValue = if (isActive) 140f else 90f,
        targetValue = if (isActive) 210f else 135f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 900 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corner_bloom"
    )

    val neonPurple = Color(0xFF9333EA)
    val neonMagenta = Color(0xFFD946EF)
    val neonCoral = Color(0xFFFF5722)
    val neonCyan = Color(0xFF06B6D4)
    val neonElectric = Color(0xFFA855F7)

    Box(modifier = modifier.fillMaxSize()) {
        content()

        // Full Screen Edge Glow Border Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Modern smartphone corner curve (52.dp translated to device pixels)
            val cornerRadiusPx = 54.dp.toPx()
            val strokeWidth = (if (isActive) 10.dp.toPx() else 5.5.dp.toPx()) * pulse
            val softBloomWidth = strokeWidth * 3.8f

            val edgeGradient = Brush.sweepGradient(
                colors = listOf(
                    neonPurple.copy(alpha = 0.95f * pulse),
                    neonMagenta.copy(alpha = 0.98f * pulse),
                    neonCoral.copy(alpha = 0.90f * pulse),
                    neonCyan.copy(alpha = 0.85f * pulse),
                    neonElectric.copy(alpha = 0.92f * pulse),
                    neonPurple.copy(alpha = 0.95f * pulse)
                ),
                center = Offset(width * 0.5f, height * 0.5f)
            )

            // --- 1. Volumetric 4-Corner Glow Pools (Immersive Corner Accent) ---
            val cornerGlowAlpha = (if (isActive) 0.65f else 0.35f) * pulse

            // Top-Left Corner Bloom (Magenta/Purple)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(neonMagenta.copy(alpha = cornerGlowAlpha), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = cornerBloomSize * 1.8f
                ),
                radius = cornerBloomSize * 1.8f,
                center = Offset(0f, 0f)
            )

            // Top-Right Corner Bloom (Coral/Purple)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(neonCoral.copy(alpha = cornerGlowAlpha), Color.Transparent),
                    center = Offset(width, 0f),
                    radius = cornerBloomSize * 1.8f
                ),
                radius = cornerBloomSize * 1.8f,
                center = Offset(width, 0f)
            )

            // Bottom-Left Corner Bloom (Cyan/Electric)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(neonCyan.copy(alpha = cornerGlowAlpha), Color.Transparent),
                    center = Offset(0f, height),
                    radius = cornerBloomSize * 1.9f
                ),
                radius = cornerBloomSize * 1.9f,
                center = Offset(0f, height)
            )

            // Bottom-Right Corner Bloom (Magenta/Electric)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(neonPurple.copy(alpha = cornerGlowAlpha), Color.Transparent),
                    center = Offset(width, height),
                    radius = cornerBloomSize * 1.9f
                ),
                radius = cornerBloomSize * 1.9f,
                center = Offset(width, height)
            )

            // --- 2. Ultra-Wide Diffused Atmospheric Glow ---
            drawRoundRect(
                brush = edgeGradient,
                topLeft = Offset(strokeWidth * 0.2f, strokeWidth * 0.2f),
                size = Size(width - strokeWidth * 0.4f, height - strokeWidth * 0.4f),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                style = Stroke(width = softBloomWidth)
            )

            // --- 3. Mid-Intensity Ambient Ray ---
            drawRoundRect(
                brush = edgeGradient,
                topLeft = Offset(strokeWidth * 0.4f, strokeWidth * 0.4f),
                size = Size(width - strokeWidth * 0.8f, height - strokeWidth * 0.8f),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                style = Stroke(width = strokeWidth * 1.8f)
            )

            // --- 4. Crisp High-Intensity Core Neon Border ---
            drawRoundRect(
                brush = edgeGradient,
                topLeft = Offset(strokeWidth * 0.5f, strokeWidth * 0.5f),
                size = Size(width - strokeWidth, height - strokeWidth),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}
