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
    isDark: Boolean = true,
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
            colors = if (isDark) {
                listOf(Color(0xFF52525B), Color(0xFF3F3F46), Color(0xFF27272A), Color(0xFF18181B))
            } else {
                listOf(Color(0xFFD4D4D8), Color(0xFFA1A1AA), Color(0xFF71717A))
            }
        )
    } else if (isDark) {
        // OLED Dark: Radiant champagne beige core with specular silver halo
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFE8E3D5),
                Color(0xFFC4BCAF),
                Color(0xFF8E8B85)
            )
        )
    } else {
        // Clean Minimal Light: Crisp charcoal / warm stone core
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF2C2A29),
                Color(0xFF1A1918),
                Color(0xFF111113),
                Color(0xFF09090B)
            )
        )
    }

    val shadowColor = if (isMuted) {
        Color(0x30000000)
    } else if (isDark) {
        Color(0x55E8E3D5)
    } else {
        Color(0x40000000)
    }

    val iconColor = if (isMuted) {
        Color.White
    } else if (isDark) {
        Color(0xFF111113) // High-contrast deep charcoal icon on radiant champagne orb
    } else {
        Color(0xFFFFFFFF) // Crisp white icon on deep charcoal orb
    }

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
                    .background(
                        (if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF))
                            .copy(alpha = ripple1Alpha * if (isDark) 0.35f else 0.40f)
                    )
            )

            // Middle Expanding Ripple 2
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(ripple2Scale)
                    .clip(CircleShape)
                    .background(
                        (if (isDark) Color(0xFFFFFFFF) else Color(0xFF111113))
                            .copy(alpha = ripple2Alpha * if (isDark) 0.30f else 0.20f)
                    )
            )
        }

        // Center Radiant Glowing Button
        Surface(
            modifier = Modifier
                .size(size)
                .shadow(20.dp, CircleShape, ambientColor = shadowColor, spotColor = shadowColor)
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
                    tint = iconColor,
                    modifier = Modifier.size(size * 0.44f)
                )
            }
        }
    }
}
