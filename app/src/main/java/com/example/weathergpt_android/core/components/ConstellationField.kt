package com.example.weathergpt_android.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * ThreeUI <ConstellationField /> Component (Variant: constellation-field).
 * Drifting celestial particle network with tunable proximity link lines and pulsing star nodes.
 * Authored according to ThreeUI canonical source revision SHA-256 1920ad4fe34f.
 *
 * Theme-adapted for WeatherGPT Obsidian / Neon Purple / Magenta / Cyan vibe.
 */
class ConstellationParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val colorIndex: Int,
    val phase: Float
)

@Composable
fun ConstellationField(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    speed: Float = 1.0f,
    nodeCount: Int = 42,
    linkDistanceDp: Float = 145f,
    strokeWidthDp: Float = 0.85f,
    opacity: Float = 1.0f
) {
    // Theme palette tailored for WeatherGPT Obsidian aesthetic
    val darkPalette = remember {
        listOf(
            Color(0xFFA855F7), // Electric Violet
            Color(0xFFC026D3), // Neon Magenta
            Color(0xFF9333EA), // Deep Neon Purple
            Color(0xFF38BDF8), // Cyan Star
            Color(0xFFE879F9)  // Soft Orchid
        )
    }

    val lightPalette = remember {
        listOf(
            Color(0xFF7C3AED), // Deep Violet
            Color(0xFF2563EB), // Royal Blue
            Color(0xFF64748B), // Slate Ink
            Color(0xFF0D9488)  // Teal
        )
    }

    val palette = if (isDark) darkPalette else lightPalette
    val linkBaseColor = if (isDark) Color(0xFFA855F7) else Color(0xFF64748B)

    // Node collection
    val particles = remember {
        val rand = Random(42)
        List(nodeCount) { i ->
            ConstellationParticle(
                x = rand.nextFloat(),
                y = rand.nextFloat(),
                vx = (rand.nextFloat() - 0.5f) * 0.0075f * speed,
                vy = (rand.nextFloat() - 0.5f) * 0.0075f * speed,
                radius = rand.nextFloat() * 1.8f + 1.6f,
                colorIndex = i % palette.size,
                phase = rand.nextFloat() * 6.28f
            )
        }
    }

    // Animation frame tick
    val animTime = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(speed) {
        var lastNanos = withFrameNanos { it }
        while (true) {
            withFrameNanos { nowNanos ->
                val dt = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = nowNanos
                animTime.floatValue += dt

                // Update particle positions across normalized 0..1 coordinates
                for (p in particles) {
                    p.x += p.vx * dt * 60f
                    p.y += p.vy * dt * 60f

                    // Bounce off screen boundaries
                    if (p.x < 0f) { p.x = 0f; p.vx = -p.vx }
                    else if (p.x > 1f) { p.x = 1f; p.vx = -p.vx }

                    if (p.y < 0f) { p.y = 0f; p.vy = -p.vy }
                    else if (p.y > 1f) { p.y = 1f; p.vy = -p.vy }
                }
            }
        }
    }

    // Gentle, calm global star twinkle transition (slow, tranquil breathing)
    val infiniteTransition = rememberInfiniteTransition(label = "constellation_twinkle")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        val maxLinkPx = linkDistanceDp.dp.toPx()
        val strokeWidthPx = strokeWidthDp.dp.toPx()
        val currentTime = animTime.floatValue

        // 1. Draw Links between close nodes
        val count = particles.size
        for (i in 0 until count) {
            val a = particles[i]
            val ax = a.x * width
            val ay = a.y * height

            for (j in i + 1 until count) {
                val b = particles[j]
                val bx = b.x * width
                val by = b.y * height

                val d = hypot(ax - bx, ay - by)
                if (d < maxLinkPx) {
                    val proximityRatio = 1f - (d / maxLinkPx)
                    val linkAlpha = (0.07f + proximityRatio * 0.32f) * opacity * (if (isDark) 1f else 0.7f)

                    drawLine(
                        color = linkBaseColor.copy(alpha = linkAlpha),
                        start = Offset(ax, ay),
                        end = Offset(bx, by),
                        strokeWidth = strokeWidthPx
                    )
                }
            }
        }

        // 2. Draw Nodes (Dual-pass: Soft Halo + Bright Core)
        for (p in particles) {
            val px = p.x * width
            val py = p.y * height
            val color = palette[p.colorIndex % palette.size]
            val nodePulse = (0.75f + sin(currentTime * 1.5f + p.phase) * 0.25f) * pulse

            // Outer ethereal atmospheric halo
            drawCircle(
                color = color.copy(alpha = 0.22f * nodePulse * opacity),
                radius = p.radius.dp.toPx() * 2.5f,
                center = Offset(px, py)
            )

            // High-intensity star core
            drawCircle(
                color = color.copy(alpha = 0.92f * nodePulse * opacity),
                radius = p.radius.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}
