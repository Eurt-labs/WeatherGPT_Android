package com.example.weathergpt_android.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Concentric Pulsing Orb matching the bottom microphone button in Screenshot 3.
 * Features radiating ripple rings and radiant magenta/coral center, with interactive muted state.
 */
@Composable
fun ConcentricPulsingOrb(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    isMuted: Boolean = false,
    onClick: () -> Unit = {},
    size: Dp = 86.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse_anim")

    val ripple1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive && !isMuted) 1200 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )

    val ripple1Alpha by infiniteTransition.animateFloat(
        initialValue = if (!isMuted) 0.45f else 0.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive && !isMuted) 1200 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1_alpha"
    )

    val ripple2Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive && !isMuted) 1200 else 2400, delayMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )

    val ripple2Alpha by infiniteTransition.animateFloat(
        initialValue = if (!isMuted) 0.55f else 0.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive && !isMuted) 1200 else 2400, delayMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2_alpha"
    )

    val centerButtonGradient = if (isMuted) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFF87171),
                Color(0xFFEF4444),
                Color(0xFFB91C1C),
                Color(0xFF7F1D1D)
            )
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFE879F9),
                Color(0xFFC026D3),
                Color(0xFF9333EA),
                Color(0xFFE11D48)
            )
        )
    }

    val shadowColor = if (isMuted) Color(0xFFEF4444) else Color(0xFFC026D3)

    Box(
        modifier = modifier.size(size * 1.8f),
        contentAlignment = Alignment.Center
    ) {
        // Outer Expanding Ripple 1
        if (!isMuted) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(ripple1Scale)
                    .clip(CircleShape)
                    .background(Color(0xFF9333EA).copy(alpha = ripple1Alpha))
            )

            // Middle Expanding Ripple 2
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(ripple2Scale)
                    .clip(CircleShape)
                    .background(Color(0xFFC026D3).copy(alpha = ripple2Alpha))
            )
        }

        // Center Radiant Glowing Button
        Surface(
            modifier = Modifier
                .size(size)
                .shadow(24.dp, CircleShape, ambientColor = shadowColor, spotColor = shadowColor)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(centerButtonGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                    contentDescription = if (isMuted) "Unmute Microphone" else "Mute Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.44f)
                )
            }
        }
    }
}
