package com.example.weathergpt_android.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Audio Waveform for live in-bubble voice dictation.
 * Displays dynamic undulating neon gradient wave bars with tap-to-stop capability.
 */
@Composable
fun VoiceWaveAnimation(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    onStopClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave_motion")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "voice_wave_phase"
    )

    val neonCyan = Color(0xFF06B6D4)
    val neonSky = Color(0xFF38BDF8)
    val neonMagenta = Color(0xFFD946EF)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onStopClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0x2206B6D4) else Color(0x1406B6D4),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    neonCyan.copy(alpha = 0.5f),
                    neonMagenta.copy(alpha = 0.4f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.GraphicEq,
                contentDescription = null,
                tint = neonCyan,
                modifier = Modifier.size(15.dp)
            )

            // Dynamic Undulating Audio Waveform Bars
            Canvas(
                modifier = Modifier
                    .width(110.dp)
                    .height(18.dp)
            ) {
                val barCount = 14
                val spacing = size.width / barCount
                val barWidth = 3.dp.toPx()
                val maxBarHeight = size.height * 0.95f
                val minBarHeight = 3.5.dp.toPx()
                val centerY = size.height / 2f

                for (i in 0 until barCount) {
                    val wave1 = sin(phase + i * 0.52f).toFloat()
                    val wave2 = cos(phase * 1.6f + i * 0.38f).toFloat()
                    val combined = ((wave1 * 0.65f + wave2 * 0.35f) + 1f) / 2f

                    // Natural envelope tapering the edges
                    val envelope = sin((i.toFloat() / (barCount - 1)) * Math.PI.toFloat()).coerceAtLeast(0.35f)
                    val barHeight = (minBarHeight + (maxBarHeight - minBarHeight) * combined * envelope)
                        .coerceIn(minBarHeight, maxBarHeight)

                    val x = i * spacing + (spacing - barWidth) / 2f
                    val yTop = centerY - barHeight / 2f

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(neonCyan, neonSky, neonMagenta),
                            startY = yTop,
                            endY = yTop + barHeight
                        ),
                        topLeft = Offset(x, yTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }

            // Compact Stop pill control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x35EF4444))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Stop,
                    contentDescription = "Stop",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = "Stop",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252)
                )
            }
        }
    }
}
