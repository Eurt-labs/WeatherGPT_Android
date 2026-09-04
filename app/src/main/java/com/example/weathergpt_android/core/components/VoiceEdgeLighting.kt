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
 * - Originates strictly near the vertical CENTER of the left and right bezels (x=0 and x=width at centerY).
 * - Expands linearly (vertically up and down along the bezel) as the conversation goes on.
 * - Stays strictly linear and hugs the bezel borders (NOT radial, NOT expanding horizontally).
 * - Origin point at center acts as the radiant anchor emitter.
 * - Smoothly collapses back to the center origin point when conversation stops.
 */
@Composable
fun VoiceEdgeLighting(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    speechEnergy: Float = 0f,
    conversationProgress: Float = 0.12f,
    content: @Composable BoxScope.() -> Unit
) {
    // 1. Animated Linear Expansion:
    // Tracks conversation progression smoothly, scaling linearly from center origin outwards
    val animatedProgress by animateFloatAsState(
        targetValue = if (isActive) conversationProgress.coerceIn(0.08f, 1f) else 0f,
        animationSpec = tween(
            durationMillis = if (isActive) 350 else 250,
            easing = FastOutSlowInEasing
        ),
        label = "edge_linear_progress"
    )

    // Smooth visibility fade
    val alphaAnim by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "edge_active_alpha"
    )

    // Gentle conversational wave modulation (breathing effect along the linear beam)
    val infiniteTransition = rememberInfiniteTransition(label = "edge_lighting_wave")
    val waveModulation by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
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

                // Minimum origin length: starts as a luminous spark right at the center bezel
                val minHalfHeight = 18.dp.toPx()
                // Maximum vertical half-span: hugs the bezel without covering system icons
                val maxHalfHeight = height * 0.44f

                // Combined linear expansion: conversation progression + instantaneous speech energy bounce
                val dynamicBounce = speechEnergy.coerceIn(0f, 1f) * 0.18f
                val effectiveExpansion = ((animatedProgress + dynamicBounce) * waveModulation).coerceIn(0.06f, 1f)
                val currentHalfHeight = minHalfHeight + (maxHalfHeight - minHalfHeight) * effectiveExpansion

                val topY = (centerY - currentHalfHeight).coerceAtLeast(height * 0.04f)
                val bottomY = (centerY + currentHalfHeight).coerceAtMost(height * 0.96f)
                val beamHeight = bottomY - topY

                // Slender bezel-hugging widths (strictly linear, NOT expanding horizontally across screen)
                val coreLineWidth = 3.dp.toPx()
                val tightGlowWidth = 6.dp.toPx()
                val cornerRadius = CornerRadius(coreLineWidth / 2f, coreLineWidth / 2f)

                // =========================================================================
                // 1. LEFT EDGE: Originating near center, expanding linearly up & down
                // =========================================================================
                val leftLinearBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        neonPurple.copy(alpha = 0.40f * alphaAnim),
                        neonMagenta.copy(alpha = 0.85f * alphaAnim),
                        Color.White.copy(alpha = 1.0f * alphaAnim), // Center origin spark
                        neonMagenta.copy(alpha = 0.85f * alphaAnim),
                        neonPurple.copy(alpha = 0.40f * alphaAnim),
                        Color.Transparent
                    ),
                    startY = topY,
                    endY = bottomY
                )

                // Tight bezel glow (feathered only 6dp from edge, no horizontal wash)
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            neonMagenta.copy(alpha = 0.35f * alphaAnim),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = tightGlowWidth
                    ),
                    topLeft = Offset(0f, topY),
                    size = Size(tightGlowWidth, beamHeight),
                    cornerRadius = CornerRadius(tightGlowWidth / 2f, tightGlowWidth / 2f)
                )

                // Core linear beam (3dp along edge)
                drawRoundRect(
                    brush = leftLinearBrush,
                    topLeft = Offset(0f, topY),
                    size = Size(coreLineWidth, beamHeight),
                    cornerRadius = cornerRadius
                )

                // Left Origin Emitter Node at center (x=0, y=centerY)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.95f * alphaAnim),
                    topLeft = Offset(0f, centerY - 5.dp.toPx()),
                    size = Size(coreLineWidth + 1.dp.toPx(), 10.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // =========================================================================
                // 2. RIGHT EDGE: Originating near center, expanding linearly up & down
                // =========================================================================
                val rightLinearBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        neonCyan.copy(alpha = 0.40f * alphaAnim),
                        neonSky.copy(alpha = 0.85f * alphaAnim),
                        Color.White.copy(alpha = 1.0f * alphaAnim), // Center origin spark
                        neonSky.copy(alpha = 0.85f * alphaAnim),
                        neonCyan.copy(alpha = 0.40f * alphaAnim),
                        Color.Transparent
                    ),
                    startY = topY,
                    endY = bottomY
                )

                // Tight bezel glow (feathered only 6dp from edge, no horizontal wash)
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            neonSky.copy(alpha = 0.35f * alphaAnim)
                        ),
                        startX = width - tightGlowWidth,
                        endX = width
                    ),
                    topLeft = Offset(width - tightGlowWidth, topY),
                    size = Size(tightGlowWidth, beamHeight),
                    cornerRadius = CornerRadius(tightGlowWidth / 2f, tightGlowWidth / 2f)
                )

                // Core linear beam (3dp along edge)
                drawRoundRect(
                    brush = rightLinearBrush,
                    topLeft = Offset(width - coreLineWidth, topY),
                    size = Size(coreLineWidth, beamHeight),
                    cornerRadius = cornerRadius
                )

                // Right Origin Emitter Node at center (x=width, y=centerY)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.95f * alphaAnim),
                    topLeft = Offset(width - (coreLineWidth + 1.dp.toPx()), centerY - 5.dp.toPx()),
                    size = Size(coreLineWidth + 1.dp.toPx(), 10.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}
