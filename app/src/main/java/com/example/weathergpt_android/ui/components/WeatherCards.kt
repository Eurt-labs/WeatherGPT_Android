package com.example.weathergpt_android.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.ui.theme.AiIndigo
import com.example.weathergpt_android.ui.theme.AiPurple
import com.example.weathergpt_android.ui.theme.AiPurpleLight
import com.example.weathergpt_android.ui.theme.CardBackground
import com.example.weathergpt_android.ui.theme.SkyBlue
import com.example.weathergpt_android.ui.theme.SkyBlueLight
import com.example.weathergpt_android.ui.theme.SubtleSurface
import com.example.weathergpt_android.ui.theme.TextPrimary
import com.example.weathergpt_android.ui.theme.TextSecondary
import com.example.weathergpt_android.ui.theme.TextTertiary
import com.example.weathergpt_android.ui.theme.WeatherAmber
import com.example.weathergpt_android.ui.theme.WeatherAmberLight
import com.example.weathergpt_android.ui.theme.WeatherEmerald
import com.example.weathergpt_android.ui.theme.WeatherEmeraldLight
import kotlin.math.roundToInt

/**
 * Main wireframe greeting banner:
 * "Hi Name, todays weather is good. Suggestion go for a walk"
 */
@Composable
fun GreetingHeroCard(
    userName: String = "Dhruv",
    weatherStatus: String = "today's weather is good",
    aiSuggestion: String = "Go for a pleasant evening walk around the park.",
    onAiSuggestionClick: () -> Unit = {}
) {
    // Subtle floating animation for weather emblem
    val infiniteTransition = rememberInfiniteTransition(label = "floating_hero")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_float"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x12000000),
                ambientColor = Color(0x08000000)
            ),
        shape = RoundedCornerShape(26.dp),
        color = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hi $userName 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.4).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(WeatherEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = weatherStatus.replaceFirstChar { it.uppercase() },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = WeatherEmerald
                        )
                    }
                }

                // Animated Sun & Cloud Emblem
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, floatOffset.roundToInt()) }
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(WeatherAmberLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = "Sunny Weather",
                        tint = WeatherAmber,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // AI Suggestion Box (Interactive capsule matching "Suggestion go for a walk")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onAiSuggestionClick),
                shape = RoundedCornerShape(18.dp),
                color = SubtleSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AiPurple, AiIndigo)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI Suggestion",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WeatherGPT Suggestion",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AiIndigo
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.DirectionsWalk,
                                contentDescription = null,
                                tint = AiIndigo,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = aiSuggestion,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Primary Weather Summary Card
 */
@Composable
fun PrimaryWeatherCard(
    temperature: String = "24°",
    condition: String = "Mostly Clear & Mild",
    highLow: String = "H: 26°  L: 17°",
    location: String = "San Francisco, CA"
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x12000000),
                ambientColor = Color(0x08000000)
            ),
        shape = RoundedCornerShape(26.dp),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = location,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = temperature,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-1.5).sp
                )
                Text(
                    text = condition,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = highLow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SkyBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cloud,
                        contentDescription = "Cloudy Weather",
                        tint = SkyBlue,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WeatherEmeraldLight
                ) {
                    Text(
                        text = "Comfortable",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WeatherEmerald,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Hourly Forecast Horizontal Pill Carousel
 */
data class HourlyForecast(
    val time: String,
    val temp: String,
    val icon: ImageVector,
    val isNow: Boolean = false
)

@Composable
fun HourlyForecastSection(
    hourlyList: List<HourlyForecast> = listOf(
        HourlyForecast("Now", "24°", Icons.Rounded.WbSunny, isNow = true),
        HourlyForecast("4 PM", "25°", Icons.Rounded.WbSunny),
        HourlyForecast("5 PM", "24°", Icons.Rounded.Cloud),
        HourlyForecast("6 PM", "23°", Icons.Rounded.WbTwilight),
        HourlyForecast("7 PM", "21°", Icons.Rounded.NightsStay),
        HourlyForecast("8 PM", "20°", Icons.Rounded.NightsStay),
        HourlyForecast("9 PM", "19°", Icons.Rounded.NightsStay)
    )
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Hourly Forecast",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            hourlyList.forEach { forecast ->
                HourlyPill(forecast = forecast)
            }
        }
    }
}

@Composable
private fun HourlyPill(forecast: HourlyForecast) {
    Surface(
        modifier = Modifier
            .width(66.dp)
            .shadow(
                elevation = if (forecast.isNow) 6.dp else 2.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x10000000)
            ),
        shape = RoundedCornerShape(22.dp),
        color = if (forecast.isNow) SkyBlueLight else CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = forecast.time,
                fontSize = 12.sp,
                fontWeight = if (forecast.isNow) FontWeight.Bold else FontWeight.Medium,
                color = if (forecast.isNow) SkyBlue else TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Icon(
                imageVector = forecast.icon,
                contentDescription = null,
                tint = if (forecast.isNow) SkyBlue else WeatherAmber,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = forecast.temp,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

/**
 * 2x2 Weather Metrics Grid
 */
@Composable
fun WeatherMetricsGrid(
    windSpeed: String = "14 km/h",
    humidity: String = "52%",
    uvIndex: String = "3 (Moderate)",
    airQuality: String = "38 (Good)"
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Air,
                title = "Wind",
                value = windSpeed,
                subtitle = "North-West breeze",
                accentColor = SkyBlue
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.WaterDrop,
                title = "Humidity",
                value = humidity,
                subtitle = "Dew point 12°C",
                accentColor = SkyBlue
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.WbSunny,
                title = "UV Index",
                value = uvIndex,
                subtitle = "Sun protection low",
                accentColor = WeatherAmber
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Speed,
                title = "Air Quality",
                value = airQuality,
                subtitle = "Healthy air today",
                accentColor = WeatherEmerald
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x10000000),
                ambientColor = Color(0x06000000)
            ),
        shape = RoundedCornerShape(22.dp),
        color = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextTertiary
            )
        }
    }
}

/**
 * 7-Day Outlook Card
 */
data class DayForecast(
    val day: String,
    val condition: ImageVector,
    val minTemp: String,
    val maxTemp: String,
    val progress: Float
)

@Composable
fun SevenDayForecastCard() {
    val days = listOf(
        DayForecast("Today", Icons.Rounded.WbSunny, "17°", "26°", 0.7f),
        DayForecast("Mon", Icons.Rounded.Cloud, "16°", "24°", 0.6f),
        DayForecast("Tue", Icons.Rounded.WbSunny, "18°", "27°", 0.8f),
        DayForecast("Wed", Icons.Rounded.WaterDrop, "15°", "21°", 0.4f),
        DayForecast("Thu", Icons.Rounded.Cloud, "16°", "23°", 0.5f),
        DayForecast("Fri", Icons.Rounded.WbSunny, "19°", "28°", 0.85f),
        DayForecast("Sat", Icons.Rounded.WbSunny, "20°", "29°", 0.9f)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x12000000),
                ambientColor = Color(0x08000000)
            ),
        shape = RoundedCornerShape(26.dp),
        color = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "7-Day Forecast",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                days.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.day,
                            fontSize = 14.sp,
                            fontWeight = if (item.day == "Today") FontWeight.Bold else FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier.width(52.dp)
                        )

                        Icon(
                            imageVector = item.condition,
                            contentDescription = null,
                            tint = if (item.condition == Icons.Rounded.WbSunny) WeatherAmber else SkyBlue,
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = item.minTemp,
                            fontSize = 13.sp,
                            color = TextTertiary,
                            modifier = Modifier.width(32.dp)
                        )

                        LinearProgressIndicator(
                            progress = { item.progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SkyBlue,
                            trackColor = SubtleSurface,
                            strokeCap = StrokeCap.Round
                        )

                        Text(
                            text = item.maxTemp,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.width(32.dp)
                        )
                    }
                }
            }
        }
    }
}
