package com.example.weathergpt_android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.weathergpt_android.ui.theme.SkyBlueLight
import com.example.weathergpt_android.ui.theme.SubtleSurface
import com.example.weathergpt_android.ui.theme.TextPrimary
import com.example.weathergpt_android.ui.theme.TextSecondary
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
    var isExpanded by remember { mutableStateOf(false) }

    val islandHeight by animateDpAsState(
        targetValue = if (isExpanded) 118.dp else 60.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "island_height"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(islandHeight)
                .shadow(
                    elevation = if (isExpanded) 16.dp else 10.dp,
                    shape = RoundedCornerShape(30.dp),
                    spotColor = Color(0x18000000),
                    ambientColor = Color(0x10000000)
                ),
            shape = RoundedCornerShape(30.dp),
            color = IslandBackground,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
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

                    // Center: Application Title (WeatherGPT) - Tap to expand dynamic island
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
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

                // Expanded Dynamic Island Live Widget
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(150)) + expandVertically(),
                    exit = fadeOut(animationSpec = tween(120)) + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = SubtleSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(SkyBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Cloud,
                                        contentDescription = null,
                                        tint = SkyBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "24° • Clear Sky",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "AQI 34 (Good) • 0% Rain",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { isExpanded = false }
                            ) {
                                Text(
                                    text = "Close",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SkyBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
