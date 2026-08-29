package com.example.weathergpt_android.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.ui.theme.AiIndigo
import com.example.weathergpt_android.ui.theme.AiPurple
import com.example.weathergpt_android.ui.theme.AlertRed
import com.example.weathergpt_android.ui.theme.IslandBackground
import com.example.weathergpt_android.ui.theme.SkyBlue
import com.example.weathergpt_android.ui.theme.SubtleSurface
import com.example.weathergpt_android.ui.theme.TextPrimary
import com.example.weathergpt_android.ui.theme.TextSecondary

@Composable
fun TopIslandHeader(
    modifier: Modifier = Modifier,
    notificationCount: Int = 2,
    userName: String = "Dhruv",
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(30.dp),
                    spotColor = Color(0x18000000),
                    ambientColor = Color(0x10000000)
                ),
            shape = RoundedCornerShape(30.dp),
            color = IslandBackground,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp // handled by Modifier.shadow for softer radius
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Notification Button with badge
                val notifSource = remember { MutableInteractionSource() }
                val notifPressed by notifSource.collectIsPressedAsState()
                val notifScale by animateFloatAsState(
                    targetValue = if (notifPressed) 0.88f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "notif_scale"
                )

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .scale(notifScale)
                        .clip(CircleShape)
                        .background(SubtleSurface)
                        .clickable(
                            interactionSource = notifSource,
                            indication = null,
                            onClick = onNotificationClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (notificationCount > 0) {
                                Badge(
                                    containerColor = AlertRed,
                                    contentColor = Color.White,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Notifications",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Center: Application Title (WeatherGPT)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Weather",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "GPT",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = SkyBlue,
                        letterSpacing = (-0.3).sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp, bottom = 8.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(SkyBlue, AiPurple)
                                )
                            )
                    )
                }

                // Right: User Profile Avatar
                val profileSource = remember { MutableInteractionSource() }
                val profilePressed by profileSource.collectIsPressedAsState()
                val profileScale by animateFloatAsState(
                    targetValue = if (profilePressed) 0.88f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "profile_scale"
                )

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .scale(profileScale)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SkyBlue, AiIndigo)
                            )
                        )
                        .clickable(
                            interactionSource = profileSource,
                            indication = null,
                            onClick = onProfileClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
