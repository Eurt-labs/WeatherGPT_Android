package com.example.weathergpt_android.domain.weather.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import com.example.weathergpt_android.domain.weather.model.WeatherScenario

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    locationData: LocationData = LocationData.DEFAULT,
    liveWeatherData: LiveWeatherData = LiveWeatherData.DEFAULT
) {
    var selectedScenario by remember { mutableStateOf<WeatherScenario?>(null) }

    val displayTemp = selectedScenario?.temp ?: liveWeatherData.temperature
    val displayCondition = selectedScenario?.condition ?: liveWeatherData.condition
    val displayHighLow = selectedScenario?.highLow ?: liveWeatherData.highLow
    val displayWind = selectedScenario?.wind ?: liveWeatherData.windSpeed
    val displayHumidity = selectedScenario?.humidity ?: liveWeatherData.humidity
    val displayUv = selectedScenario?.uv ?: liveWeatherData.uvIndex
    val displayAqi = selectedScenario?.aqi ?: liveWeatherData.aqi

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 104.dp, // Clearance for top island
            bottom = 110.dp // Clearance for bottom attached nav bar
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Interactive Weather Scenario Switcher Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live Weather Pill
                val isLiveSelected = selectedScenario == null
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedScenario = null },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isLiveSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "● Live Weather",
                        fontSize = 12.sp,
                        fontWeight = if (isLiveSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isLiveSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }

                WeatherScenario.entries.forEach { scenario ->
                    val isSelected = selectedScenario == scenario
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedScenario = scenario },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = scenario.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // Real-time Primary Weather Overview Card (Live OpenWeather Data)
        item {
            AnimatedContent(
                targetState = displayTemp to displayCondition,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "primary_weather_anim"
            ) { (temp, condition) ->
                PrimaryWeatherCard(
                    temperature = temp,
                    condition = condition,
                    highLow = displayHighLow,
                    location = locationData.formattedLocation
                )
            }
        }

        // Hourly Horizontal Carousel
        item {
            HourlyForecastSection(hourlyList = liveWeatherData.hourlyList)
        }

        // Detailed 2x2 Metric Cards (Wind, Humidity, UV, AQI)
        item {
            AnimatedContent(
                targetState = displayWind to displayHumidity,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "metrics_anim"
            ) {
                WeatherMetricsGrid(
                    windSpeed = displayWind,
                    humidity = displayHumidity,
                    uvIndex = displayUv,
                    airQuality = displayAqi
                )
            }
        }

        // 7-Day Extended Forecast Card
        item {
            SevenDayForecastCard(days = liveWeatherData.dailyList)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
