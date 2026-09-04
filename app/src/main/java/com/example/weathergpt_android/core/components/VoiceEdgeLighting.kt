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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Dual-Side Reactive Edge Lighting.
 * Strictly limited to the Left and Right screen bezels, dynamically reactive to talking.
 * Completely hides when idle and pulses smoothly when speaking or listening.
 */
@Composable
fun VoiceEdgeLighting(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "edge_lighting_anim")

    // Dynamic voice breathing pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice_edge_pulse"
    )

    // Smooth visibility fade when talking begins / stops (only reactive to talking!)
    val alphaAnim by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "edge_active_alpha"
    )

    val neonPurple = Color(0xFF9333EA)
    val neonMagenta = Color(0xFFD946EF)
    val neonCyan = Color(0xFF06B6D4)

    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (alphaAnim > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Vertical active range (keeps top status bar and bottom home bar clean)
                val topPadding = height * 0.07f
                val bottomPadding = height * 0.07f
                val activeHeight = height - topPadding - bottomPadding

                // Dynamic beam width reactive to voice
                val coreWidth = 7.dp.toPx() * pulse
                val ambientGlowWidth = 26.dp.toPx() * pulse

                // 1. LEFT SIDE EDGE LIGHTING
                // Left ambient diffused glow
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            neonMagenta.copy(alpha = 0.55f * alphaAnim * pulse),
                            neonPurple.copy(alpha = 0.22f * alphaAnim * pulse),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = ambientGlowWidth
                    ),
                    topLeft = Offset(0f, topPadding),
                    size = Size(ambientGlowWidth, activeHeight)
                )
                // Left intense core edge ray
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            neonMagenta.copy(alpha = 0.95f * alphaAnim),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = coreWidth
                    ),
                    topLeft = Offset(0f, topPadding + 20f),
                    size = Size(coreWidth, activeHeight - 40f)
                )

                // 2. RIGHT SIDE EDGE LIGHTING
                // Right ambient diffused glow
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            neonPurple.copy(alpha = 0.22f * alphaAnim * pulse),
                            neonCyan.copy(alpha = 0.55f * alphaAnim * pulse)
                        ),
                        startX = width - ambientGlowWidth,
                        endX = width
                    ),
                    topLeft = Offset(width - ambientGlowWidth, topPadding),
                    size = Size(ambientGlowWidth, activeHeight)
                )
                // Right intense core edge ray
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            neonCyan.copy(alpha = 0.95f * alphaAnim)
                        ),
                        startX = width - coreWidth,
                        endX = width
                    ),
                    topLeft = Offset(width - coreWidth, topPadding + 20f),
                    size = Size(coreWidth, activeHeight - 40f)
                )
            }
        }
    }
}
