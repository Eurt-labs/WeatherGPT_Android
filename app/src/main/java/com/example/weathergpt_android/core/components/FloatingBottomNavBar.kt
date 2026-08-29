package com.example.weathergpt_android.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weathergpt_android.core.navigation.NavTab

@Composable
fun FloatingBottomNavBar(
    modifier: Modifier = Modifier,
    currentTab: NavTab,
    isAiSpeaking: Boolean = false,
    onTabSelected: (NavTab) -> Unit
) {
    // Breathing Aura Animation for Gemini Live AI Voice Mode
    val infiniteTransition = rememberInfiniteTransition(label = "nav_breathing_glow")

    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_alpha"
    )

    val breathingBorderWidth by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_border_width"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Attached Bottom Bar Surface with rounded top corners, shadow, and dynamic breathing border
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isAiSpeaking) 24.dp else 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    spotColor = if (isAiSpeaking) MaterialTheme.colorScheme.primary.copy(alpha = breathingAlpha) else Color(0x40000000),
                    ambientColor = Color(0x25000000)
                )
                .then(
                    if (isAiSpeaking) {
                        Modifier.border(
                            width = breathingBorderWidth.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = breathingAlpha),
                                    Color(0xFF38BDF8).copy(alpha = breathingAlpha),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = breathingAlpha),
                                    MaterialTheme.colorScheme.primary.copy(alpha = breathingAlpha)
                                )
                            ),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Weather Tab (Left 1)
                NavItem(
                    tab = NavTab.WEATHER,
                    isSelected = currentTab == NavTab.WEATHER,
                    onClick = { onTabSelected(NavTab.WEATHER) }
                )

                // 2. News Tab (Left 2)
                NavItem(
                    tab = NavTab.NEWS,
                    isSelected = currentTab == NavTab.NEWS,
                    onClick = { onTabSelected(NavTab.NEWS) }
                )

                // Center placeholder spacer for the raised Voice AI Mic FAB
                Spacer(modifier = Modifier.size(56.dp))

                // 3. WeatherGPT Assistant Tab (Right 1)
                NavItem(
                    tab = NavTab.GPT,
                    isSelected = currentTab == NavTab.GPT,
                    onClick = { onTabSelected(NavTab.GPT) }
                )

                // 4. Settings Tab (Right 2)
                NavItem(
                    tab = NavTab.SETTINGS,
                    isSelected = currentTab == NavTab.SETTINGS,
                    onClick = { onTabSelected(NavTab.SETTINGS) }
                )
            }
        }

        // Center Elevated Voice AI Mic FAB Cradle with Breathing Aura Rings
        CenterRaisedMicFab(
            isSelected = currentTab == NavTab.VOICE_AI,
            isAiSpeaking = isAiSpeaking,
            onClick = { onTabSelected(NavTab.VOICE_AI) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        )
    }
}

@Composable
private fun CenterRaisedMicFab(
    isSelected: Boolean,
    isAiSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isSelected -> 1.08f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "mic_fab_scale"
    )

    Box(
        modifier = modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Breathing Aura Rings when AI is speaking in Immersive Mode
        if (isAiSpeaking || isSelected) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .scale(if (isAiSpeaking) pulseScale else 1.15f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isAiSpeaking) 0.5f else 0.25f),
                                Color(0xFF38BDF8).copy(alpha = if (isAiSpeaking) 0.35f else 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Main Mic FAB Body
        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(
                    elevation = if (isAiSpeaking) 20.dp else 14.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                    ambientColor = Color(0x30000000)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (isAiSpeaking) {
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                Color(0xFF38BDF8)
                            )
                        } else {
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAiSpeaking) Icons.Rounded.GraphicEq else Icons.Rounded.Mic,
                contentDescription = "Voice AI Assistant",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NavItem(
    tab: NavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.85f
            isSelected -> 1.05f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_item_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "icon_color"
    )

    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pill_bg"
    )

    val pillWidth by animateDpAsState(
        targetValue = if (isSelected) 46.dp else 38.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "pill_width"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = pillWidth, height = 34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(pillBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        // Dot indicator
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
        )
    }
}
