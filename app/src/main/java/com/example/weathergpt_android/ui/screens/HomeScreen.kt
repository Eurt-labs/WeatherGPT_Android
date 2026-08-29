package com.example.weathergpt_android.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.weathergpt_android.ui.components.GreetingHeroCard
import com.example.weathergpt_android.ui.components.HourlyForecastSection
import com.example.weathergpt_android.ui.components.PrimaryWeatherCard
import com.example.weathergpt_android.ui.components.SevenDayForecastCard
import com.example.weathergpt_android.ui.components.WeatherMetricsGrid
import com.example.weathergpt_android.ui.theme.SkyBlue
import com.example.weathergpt_android.ui.theme.SubtleSurface
import com.example.weathergpt_android.ui.theme.TextPrimary

enum class WeatherScenario(
    val label: String,
    val status: String,
    val suggestion: String,
    val temp: String,
    val condition: String,
    val highLow: String,
    val wind: String,
    val humidity: String,
    val uv: String,
    val aqi: String
) {
    SUNNY(
        label = "☀️ Sunny (Default)",
        status = "today's weather is good",
        suggestion = "Go for a walk around the park. Evening temperature will be a pleasant 22°C.",
        temp = "24°",
        condition = "Clear Sky & Gentle Breeze",
        highLow = "H: 26°  L: 17°",
        wind = "14 km/h",
        humidity = "48%",
        uv = "2 (Low)",
        aqi = "34 (Good)"
    ),
    BREEZY(
        label = "🍃 Mild Breeze",
        status = "today's weather is fresh & clear",
        suggestion = "Great conditions for cycling or jogging along the coastline before 6 PM.",
        temp = "21°",
        condition = "Partly Cloudy with Coastal Breeze",
        highLow = "H: 23°  L: 16°",
        wind = "22 km/h",
        humidity = "55%",
        uv = "3 (Moderate)",
        aqi = "28 (Excellent)"
    ),
    RAIN(
        label = "🌧️ Light Showers",
        status = "showers expected in afternoon",
        suggestion = "Carry an umbrella if heading out. Enjoy indoor activities and coffee!",
        temp = "18°",
        condition = "Passing Rain Showers",
        highLow = "H: 20°  L: 15°",
        wind = "18 km/h",
        humidity = "82%",
        uv = "1 (Low)",
        aqi = "22 (Clean)"
    ),
    SUNSET(
        label = "🌇 Golden Hour",
        status = "peaceful golden evening",
        suggestion = "Perfect rooftop view or sunset stroll. Warm light until 7:12 PM.",
        temp = "23°",
        condition = "Golden Twilight & Calm Air",
        highLow = "H: 25°  L: 18°",
        wind = "10 km/h",
        humidity = "50%",
        uv = "1 (Low)",
        aqi = "30 (Good)"
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "Dhruv",
    onNavigateToGpt: () -> Unit = {}
) {
    var selectedScenario by remember { mutableStateOf(WeatherScenario.SUNNY) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 84.dp, // Clearance for floating top island
            bottom = 100.dp // Clearance for floating bottom island
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Interactive Weather Scenario Switcher Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeatherScenario.entries.forEach { scenario ->
                    val isSelected = selectedScenario == scenario
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedScenario = scenario },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) SkyBlue else SubtleSurface
                    ) {
                        Text(
                            text = scenario.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // Wireframe Hero Greeting with dynamic content transition
        item {
            AnimatedContent(
                targetState = selectedScenario,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "hero_greeting_anim"
            ) { current ->
                GreetingHeroCard(
                    userName = userName,
                    weatherStatus = current.status,
                    aiSuggestion = current.suggestion,
                    onAiSuggestionClick = onNavigateToGpt
                )
            }
        }

        // Real-time Weather Overview Card
        item {
            AnimatedContent(
                targetState = selectedScenario,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "primary_weather_anim"
            ) { current ->
                PrimaryWeatherCard(
                    temperature = current.temp,
                    condition = current.condition,
                    highLow = current.highLow,
                    location = "San Francisco, CA"
                )
            }
        }

        // Hourly Horizontal Carousel
        item {
            HourlyForecastSection()
        }

        // Detailed 2x2 Metric Cards (Wind, Humidity, UV, AQI)
        item {
            AnimatedContent(
                targetState = selectedScenario,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "metrics_anim"
            ) { current ->
                WeatherMetricsGrid(
                    windSpeed = current.wind,
                    humidity = current.humidity,
                    uvIndex = current.uv,
                    airQuality = current.aqi
                )
            }
        }

        // 7-Day Extended Forecast Card
        item {
            SevenDayForecastCard()
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
