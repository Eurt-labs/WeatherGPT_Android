package com.example.weathergpt_android.domain.weather.ui

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
import com.example.weathergpt_android.core.theme.SkyBlue
import com.example.weathergpt_android.core.theme.SubtleSurface
import com.example.weathergpt_android.core.theme.TextPrimary
import com.example.weathergpt_android.domain.weather.model.WeatherScenario

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
            top = 84.dp,
            bottom = 100.dp
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

        // Wireframe Hero Greeting with dynamic animation
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
