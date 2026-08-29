package com.example.weathergpt_android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.ui.theme.AiIndigo
import com.example.weathergpt_android.ui.theme.AlertRed
import com.example.weathergpt_android.ui.theme.AlertRedLight
import com.example.weathergpt_android.ui.theme.CardBackground
import com.example.weathergpt_android.ui.theme.SkyBlue
import com.example.weathergpt_android.ui.theme.SkyBlueLight
import com.example.weathergpt_android.ui.theme.SubtleSurface
import com.example.weathergpt_android.ui.theme.TextPrimary
import com.example.weathergpt_android.ui.theme.TextSecondary
import com.example.weathergpt_android.ui.theme.TextTertiary
import com.example.weathergpt_android.ui.theme.WeatherAmber
import com.example.weathergpt_android.ui.theme.WeatherAmberLight

data class WeatherNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val tint: Color,
    val bgTint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSheet(
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val notifications = remember {
        mutableStateListOf(
            WeatherNotification(
                id = "1",
                title = "Rain Alert in 45 Mins",
                message = "Light showers expected near your area. Carry an umbrella if stepping out.",
                time = "10m ago",
                icon = Icons.Rounded.Thunderstorm,
                tint = SkyBlue,
                bgTint = SkyBlueLight
            ),
            WeatherNotification(
                id = "2",
                title = "Perfect Walking Weather",
                message = "Current UV index is low (2) and temperature is a pleasant 24°C.",
                time = "1h ago",
                icon = Icons.Rounded.WbSunny,
                tint = WeatherAmber,
                bgTint = WeatherAmberLight
            ),
            WeatherNotification(
                id = "3",
                title = "Air Quality Advisory",
                message = "AQI improved to 42 (Good). Ideal for outdoor workouts and running.",
                time = "3h ago",
                icon = Icons.Rounded.Air,
                tint = AiIndigo,
                bgTint = AlertRedLight
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = CardBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Notifications",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${notifications.size} unread alerts",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                if (notifications.isNotEmpty()) {
                    Text(
                        text = "Clear all",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SkyBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { notifications.clear() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No new weather notifications",
                        fontSize = 15.sp,
                        color = TextTertiary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationItemCard(
                            notification = notif,
                            onDismiss = { notifications.remove(notif) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    notification: WeatherNotification,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SubtleSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(notification.bgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = notification.tint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = notification.time,
                        fontSize = 12.sp,
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Dismiss",
                tint = TextTertiary,
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDismiss)
            )
        }
    }
}
