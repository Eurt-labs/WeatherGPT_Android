package com.example.weathergpt_android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weathergpt_android.ui.components.GreetingHeroCard
import com.example.weathergpt_android.ui.components.HourlyForecastSection
import com.example.weathergpt_android.ui.components.PrimaryWeatherCard
import com.example.weathergpt_android.ui.components.SevenDayForecastCard
import com.example.weathergpt_android.ui.components.WeatherMetricsGrid

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "Dhruv",
    onNavigateToGpt: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 84.dp, // Clearance for floating top island
            bottom = 100.dp // Clearance for floating bottom island
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Wireframe Hero Greeting: "Hi Name, todays weather is good. Suggestion go for a walk"
        item {
            GreetingHeroCard(
                userName = userName,
                weatherStatus = "today's weather is good",
                aiSuggestion = "Go for a walk around the park. Evening temperature will be a pleasant 22°C.",
                onAiSuggestionClick = onNavigateToGpt
            )
        }

        // Real-time Weather Overview Card
        item {
            PrimaryWeatherCard(
                temperature = "24°",
                condition = "Clear Sky & Gentle Breeze",
                highLow = "H: 26°  L: 17°",
                location = "San Francisco, CA"
            )
        }

        // Hourly Horizontal Carousel
        item {
            HourlyForecastSection()
        }

        // Detailed 2x2 Metric Cards (Wind, Humidity, UV, AQI)
        item {
            WeatherMetricsGrid(
                windSpeed = "14 km/h",
                humidity = "48%",
                uvIndex = "2 (Low)",
                airQuality = "34 (Good)"
            )
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
