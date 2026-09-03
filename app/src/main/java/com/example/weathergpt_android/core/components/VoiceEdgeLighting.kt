package com.example.weathergpt_android.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
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

/**
 * Animated Full-Screen Edge Lighting as shown in user's Voice Mode Screenshot (Screenshot 3).
 * Renders a glowing neon purple/magenta/coral border around the entire display perimeter,
 * dynamically breathing and pulsing based on voice activity.
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
        initialValue = if (isActive) 0.75f else 0.40f,
        targetValue = if (isActive) 1.25f else 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 800 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edge_pulse"
    )

    // Dynamic rotation offset for living light
    val offsetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "light_flow"
    )

    val neonPurple = Color(0xFF9333EA)
    val neonMagenta = Color(0xFFD946EF)
    val neonCoral = Color(0xFFFF5722)
    val neonBlue = Color(0xFF38BDF8)

    Box(modifier = modifier.fillMaxSize()) {
        content()

        // Full Screen Edge Glow Border Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val strokeWidth = (if (isActive) 8f else 4.5f) * pulse
            val cornerRadius = 46f

            val edgeGradient = Brush.sweepGradient(
                colors = listOf(
                    neonPurple.copy(alpha = 0.90f * pulse),
                    neonMagenta.copy(alpha = 0.95f * pulse),
                    neonCoral.copy(alpha = 0.85f * pulse),
                    neonBlue.copy(alpha = 0.75f * pulse),
                    neonPurple.copy(alpha = 0.90f * pulse)
                ),
                center = Offset(width * 0.5f, height * 0.5f)
            )

            // Outer Soft Diffused Glow
            drawRoundRect(
                brush = edgeGradient,
                topLeft = Offset(strokeWidth * 0.5f, strokeWidth * 0.5f),
                size = Size(width - strokeWidth, height - strokeWidth),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidth * 2.5f)
            )

            // Crisp Core Neon Border
            drawRoundRect(
                brush = edgeGradient,
                topLeft = Offset(strokeWidth * 0.5f, strokeWidth * 0.5f),
                size = Size(width - strokeWidth, height - strokeWidth),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}
