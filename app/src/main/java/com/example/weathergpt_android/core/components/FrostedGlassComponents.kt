package com.example.weathergpt_android.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.weathergpt_android.core.theme.FrostedGlassTokens

/**
 * Frosted Glass Card with translucent surface and subtle specular border.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    onClick: (() -> Unit)? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "glass_press_scale"
    )

    val surfaceColor = FrostedGlassTokens.surface(isDark)
    val borderColor = FrostedGlassTokens.border(isDark)
    val shadowColor = FrostedGlassTokens.ShadowColor

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = shadowColor,
                spotColor = shadowColor
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                    } else Modifier
                )
        ) {
            content()
        }
    }
}

/**
 * Frosted Glass Chip for quick weather questions and suggestions.
 */
@Composable
fun FrostedGlassChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    icon: ImageVector? = null
) {
    val surfaceColor = FrostedGlassTokens.surfaceSubtle(isDark)
    val borderColor = FrostedGlassTokens.borderSubtle(isDark)
    val textColor = if (isDark) Color.White.copy(alpha = 0.92f) else Color(0xFF111113)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Frosted Circular Action Button (Back, Close, Settings, etc.)
 */
@Composable
fun FrostedIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    isDark: Boolean = isSystemInDarkTheme(),
    tint: Color? = null
) {
    val surfaceColor = FrostedGlassTokens.surfaceSubtle(isDark)
    val borderColor = FrostedGlassTokens.border(isDark)
    val iconTint = tint ?: if (isDark) Color.White else Color(0xFF111113)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(surfaceColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

/**
 * Bottom Floating Frosted Glass Input Bar with Add button, text input, Mic, and Waveform launcher.
 */
@Composable
fun FrostedBottomInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    onVoiceWaveformClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Ask me anything...",
    isDark: Boolean = isSystemInDarkTheme()
) {
    val barColor = FrostedGlassTokens.surfaceRaised(isDark)
    val borderColor = FrostedGlassTokens.border(isDark)
    val textColor = if (isDark) Color.White else Color(0xFF111113)
    val hintColor = if (isDark) Color.White.copy(alpha = 0.55f) else Color(0xFF575553)
    val iconColor = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF111113)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = FrostedGlassTokens.ShadowColor, spotColor = FrostedGlassTokens.ShadowColor),
        shape = RoundedCornerShape(32.dp),
        color = barColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Plus / Tools Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x20FFFFFF) else Color(0x30E4E4E7))
                    .clickable(onClick = onVoiceWaveformClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Tools",
                    tint = iconColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Central Basic Text Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = placeholderText,
                        color = hintColor,
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    textStyle = TextStyle(
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113)),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (inputText.isNotBlank()) onSend() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Mic Icon Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable(onClick = onMicClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Voice Input",
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Waveform Voice AI Launcher Pill Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x28E8E3D5) else Color(0x25C4BCAF))
                    .clickable(onClick = onVoiceWaveformClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = "Live Voice AI",
                    tint = if (isDark) Color(0xFFE8E3D5) else Color(0xFF111113),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
