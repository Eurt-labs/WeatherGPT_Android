package com.example.weathergpt_android.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.unit.dp

/**
 * Linearly Expandable Dual-Side Reactive Edge Lighting.
 *
 * Distinctive Mechanics:
 * - Originates strictly near the vertical CENTER of the left and right bezels.
 * - Expands linearly up and down along the screen edges as conversation progresses.
 * - Stays strictly linear and hugs the bezel borders (NOT radial, NOT expanding horizontally).
 * - Smoothly collapses back to the center origin point when conversation stops.
 */
@Composable
fun VoiceEdgeLighting(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    speechEnergy: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    // 1. Linear Expansion Progression:
    // When active, the beam linearly elongates vertically from center outwards along the bezel
    val linearExpansion by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isActive) 1800 else 320,
            easing = FastOutSlowInEasing
        ),
        label = "edge_linear_expansion"
    )

    // Smooth visibility fade
    val alphaAnim by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "edge_active_alpha"
    )

    // Gentle conversational wave modulation (breathing effect along the linear beam)
    val infiniteTransition = rememberInfiniteTransition(label = "edge_lighting_wave")
    val waveModulation by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice_edge_wave_pulse"
    )

    val neonPurple = Color(0xFF9333EA)
    val neonMagenta = Color(0xFFD946EF)
    val neonCyan = Color(0xFF06B6D4)
    val neonSky = Color(0xFF38BDF8)

    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (alphaAnim > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerY = height * 0.5f

                // Maximum vertical half-span (keeps system status and navigation bars clean)
                val maxHalfHeight = height * 0.40f
                // Initial compact origin length near center (originating point)
                val minHalfHeight = 28.dp.toPx()

                // Calculate current half-height expanding linearly as conversation continues
                val reactiveBoost = (speechEnergy.coerceIn(0f, 1f) * 0.15f)
                val currentExpansion = (linearExpansion * (0.85f + reactiveBoost) * waveModulation).coerceIn(0f, 1f)
                val currentHalfHeight = minHalfHeight + (maxHalfHeight - minHalfHeight) * currentExpansion

                val topY = (centerY - currentHalfHeight).coerceAtLeast(height * 0.08f)
                val bottomY = (centerY + currentHalfHeight).coerceAtMost(height * 0.92f)
                val beamHeight = bottomY - topY

                // Slender bezel-hugging widths (strictly linear, NOT expanding horizontally across screen)
                val coreLineWidth = 3.5.dp.toPx()
                val subtleGlowWidth = 10.dp.toPx()
                val cornerRadius = CornerRadius(coreLineWidth / 2f, coreLineWidth / 2f)

                // =========================================================================
                // 1. LEFT EDGE: Originating near center, expanding linearly up & down
                // =========================================================================
                val leftLinearBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        neonPurple.copy(alpha = 0.35f * alphaAnim),
                        neonMagenta.copy(alpha = 0.85f * alphaAnim),
                        Color.White.copy(alpha = 0.95f * alphaAnim), // Brilliant center origin spark
                        neonMagenta.copy(alpha = 0.85f * alphaAnim),
                        neonPurple.copy(alpha = 0.35f * alphaAnim),
                        Color.Transparent
                    ),
                    startY = topY,
                    endY = bottomY
                )

                // Subtle bezel glow
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            neonMagenta.copy(alpha = 0.32f * alphaAnim),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = subtleGlowWidth
                    ),
                    topLeft = Offset(0f, topY),
                    size = Size(subtleGlowWidth, beamHeight),
                    cornerRadius = CornerRadius(subtleGlowWidth / 2f, subtleGlowWidth / 2f)
                )

                // Core linear beam
                drawRoundRect(
                    brush = leftLinearBrush,
                    topLeft = Offset(0f, topY),
                    size = Size(coreLineWidth, beamHeight),
                    cornerRadius = cornerRadius
                )

                // =========================================================================
                // 2. RIGHT EDGE: Originating near center, expanding linearly up & down
                // =========================================================================
                val rightLinearBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        neonCyan.copy(alpha = 0.35f * alphaAnim),
                        neonSky.copy(alpha = 0.85f * alphaAnim),
                        Color.White.copy(alpha = 0.95f * alphaAnim), // Brilliant center origin spark
                        neonSky.copy(alpha = 0.85f * alphaAnim),
                        neonCyan.copy(alpha = 0.35f * alphaAnim),
                        Color.Transparent
                    ),
                    startY = topY,
                    endY = bottomY
                )

                // Subtle bezel glow
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            neonSky.copy(alpha = 0.32f * alphaAnim)
                        ),
                        startX = width - subtleGlowWidth,
                        endX = width
                    ),
                    topLeft = Offset(width - subtleGlowWidth, topY),
                    size = Size(subtleGlowWidth, beamHeight),
                    cornerRadius = CornerRadius(subtleGlowWidth / 2f, subtleGlowWidth / 2f)
                )

                // Core linear beam
                drawRoundRect(
                    brush = rightLinearBrush,
                    topLeft = Offset(width - coreLineWidth, topY),
                    size = Size(coreLineWidth, beamHeight),
                    cornerRadius = cornerRadius
                )
            }
        }
    }
}
