package com.example.weathergpt_android.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Minimalist 3-Pill Voice Waveform Visualizer as showcased in the reference design.
 * Features smooth breathing height animation and gentle luminous glow.
 */
@Composable
fun VoiceWaveformPills(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val pill1Height by infiniteTransition.animateFloat(
        initialValue = if (isActive) 32f else 38f,
        targetValue = if (isActive) 68f else 48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 550 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pill1_h"
    )

    val pill2Height by infiniteTransition.animateFloat(
        initialValue = if (isActive) 54f else 58f,
        targetValue = if (isActive) 82f else 68f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 450 else 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pill2_h"
    )

    val pill3Height by infiniteTransition.animateFloat(
        initialValue = if (isActive) 28f else 36f,
        targetValue = if (isActive) 62f else 46f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 600 else 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pill3_h"
    )

    val pillColor = if (isDark) {
        if (isActive) Brush.verticalGradient(listOf(Color(0xFFFF8A65), Color(0xFFFF6F00)))
        else Brush.verticalGradient(listOf(Color(0x35FFFFFF), Color(0x20FFFFFF)))
    } else {
        if (isActive) Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1)))
        else Brush.verticalGradient(listOf(Color(0x70FFFFFF), Color(0x50FFFFFF)))
    }

    val borderColor = if (isDark) Color(0x30FFFFFF) else Color(0x60FFFFFF)

    Row(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pill 1
        Surface(
            modifier = Modifier
                .width(18.dp)
                .height(pill1Height.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x20000000), spotColor = Color(0x20000000)),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(pillColor)
            )
        }

        // Pill 2 (Tallest Center)
        Surface(
            modifier = Modifier
                .width(20.dp)
                .height(pill2Height.dp)
                .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x25000000), spotColor = Color(0x25000000)),
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(pillColor)
            )
        }

        // Pill 3
        Surface(
            modifier = Modifier
                .width(18.dp)
                .height(pill3Height.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x20000000), spotColor = Color(0x20000000)),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(pillColor)
            )
        }
    }
}
