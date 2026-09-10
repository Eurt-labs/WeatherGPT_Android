package com.example.weathergpt_android.domain.weather.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny

data class LiveWeatherData(
    val temperature: String = "32°",
    val condition: String = "Mostly Cloudy",
    val highLow: String = "H: 34°  L: 27°",
    val windSpeed: String = "6 km/h",
    val humidity: String = "59%",
    val uvIndex: String = "7 (High)",
    val aqi: String = "64 (Moderate)",
    val isLive: Boolean = false,
    val lastUpdatedTime: String = "Live",
    val hourlyList: List<HourlyForecast> = defaultHourlyList(),
    val dailyList: List<DayForecast> = defaultDailyList(),
    // Multi-Sector Meteorological Intelligence
    val soilMoisture: String = "0.33 m³/m³",
    val rootZoneSoilMoisture: String = "0.35 m³/m³",
    val soilTemperature: String = "28°C",
    val irrigationAdvice: String = "Adequate soil moisture. Irrigation not required today.",
    val evapotranspiration: String = "4.5 mm/day",
    val floodRiskLevel: String = "Normal (Low Risk)",
    val waterloggingRisk: String = "Low",
    val riverDischarge: String = "12.5 m³/s",
    val visibilityKm: String = "10.0 km",
    val pastRainfallTrend: String = "21.7 mm in past 3 days",
    val heatwaveAlert: String = "None",
    val apparentTemperature: String = "34°",
    val dewPoint: String = "22°C",
    val surfacePressure: String = "1010 hPa",
    val windGusts: String = "12 km/h",
    val rainNext24h: String = "0.0 mm",
    val rainNext48h: String = "0.0 mm",
    val peakRainTiming: String = "No severe rain spells expected.",
    // Predictive Trend Analysis Layer (computed from hourly time-series)
    val pressureTrend: String = "Steady",
    val windShiftSummary: String = "No significant wind direction change.",
    val cloudTrend: String = "Stable cloud cover.",
    val dewPointProximity: String = "Safe gap — low condensation risk.",
    val rainfallPatternSummary: String = "No significant recent rainfall.",
    val precipitationWindows: String = "No rain windows detected."
) {
    /**
     * Dense, high-information meteorological context with predictive trend analysis,
     * formatted for Gemini 3.6 Flash reasoning. Supplies atmospheric snapshots, precipitation
     * forecasts, soil saturation, risk metrics, AND computed trend patterns so the LLM
     * can reason like a professional meteorologist.
     */
    fun toDenseMeteorologicalContext(): String {
        return buildString {
            append("Live Atmosphere: $temperature ($condition, Feels $apparentTemperature) | Humidity: $humidity | Dew Point: $dewPoint | Surface Pressure: $surfacePressure | Wind: $windSpeed (Peak Gusts: $windGusts) | Air Quality: $aqi | Visibility: $visibilityKm.\n")
            append("Precipitation Outlook: Next 24h Rain: $rainNext24h | Next 48h Rain: $rainNext48h | Timing Window: $peakRainTiming\n")
            append("Agriculture & Soil Layer: Surface Moisture: $soilMoisture | Root Zone Moisture (1-9cm): $rootZoneSoilMoisture | Soil Temp: $soilTemperature | Evapotranspiration (ET0): $evapotranspiration | Irrigation Advisory: $irrigationAdvice\n")
            append("Risk & Disaster Layer: Waterlogging Risk: $waterloggingRisk | River Discharge / Flood: $floodRiskLevel ($riverDischarge) | Heatwave: $heatwaveAlert | Past Rain: $pastRainfallTrend\n")
            append("Predictive Analysis Layer (USE THIS FOR REASONING):\n")
            append("  - Barometric Pressure Trend: $pressureTrend\n")
            append("  - Wind Pattern Shift: $windShiftSummary\n")
            append("  - Cloud Cover Progression: $cloudTrend\n")
            append("  - Dew Point Gap: $dewPointProximity\n")
            append("  - Rainfall History Pattern: $rainfallPatternSummary\n")
            append("  - Upcoming Rain Windows: $precipitationWindows")
        }
    }

    /**
     * Human-readable detailed data view for "View in Detail" UI card.
     * Returns formatted sections with actual numbers for users who want to see raw data.
     */
    fun toDetailedDataView(): List<Pair<String, List<Pair<String, String>>>> {
        return listOf(
            "🌡️ Atmosphere" to listOf(
                "Temperature" to temperature,
                "Feels Like" to apparentTemperature,
                "Condition" to condition,
                "High / Low" to highLow,
                "Humidity" to humidity,
                "Dew Point" to dewPoint,
                "Surface Pressure" to surfacePressure,
                "Visibility" to visibilityKm
            ),
            "💨 Wind" to listOf(
                "Wind Speed" to windSpeed,
                "Wind Gusts" to windGusts,
                "Wind Shift" to windShiftSummary
            ),
            "🌧️ Precipitation" to listOf(
                "Next 24h Rain" to rainNext24h,
                "Next 48h Rain" to rainNext48h,
                "Peak Rain Window" to peakRainTiming,
                "Rain Windows" to precipitationWindows,
                "Past Rainfall" to pastRainfallTrend
            ),
            "📊 Trend Analysis" to listOf(
                "Pressure Trend" to pressureTrend,
                "Cloud Progression" to cloudTrend,
                "Dew Point Gap" to dewPointProximity,
                "Rainfall Pattern" to rainfallPatternSummary
            ),
            "🌾 Agriculture" to listOf(
                "Surface Soil Moisture" to soilMoisture,
                "Root Zone Moisture" to rootZoneSoilMoisture,
                "Soil Temperature" to soilTemperature,
                "Evapotranspiration" to evapotranspiration,
                "Irrigation Advice" to irrigationAdvice
            ),
            "⚠️ Risk & Alerts" to listOf(
                "Waterlogging Risk" to waterloggingRisk,
                "Flood Risk" to floodRiskLevel,
                "Heatwave Alert" to heatwaveAlert,
                "UV Index" to uvIndex,
                "Air Quality" to aqi
            )
        )
    }

    companion object {
        val DEFAULT = LiveWeatherData()

        fun defaultHourlyList(): List<HourlyForecast> = listOf(
            HourlyForecast("Now", "32°", Icons.Rounded.Cloud, isNow = true),
            HourlyForecast("8:00 pm", "31°", Icons.Rounded.Cloud),
            HourlyForecast("9:00 pm", "31°", Icons.Rounded.Cloud),
            HourlyForecast("10:00 pm", "30°", Icons.Rounded.NightsStay),
            HourlyForecast("11:00 pm", "30°", Icons.Rounded.NightsStay),
            HourlyForecast("12:00 am", "29°", Icons.Rounded.NightsStay),
            HourlyForecast("1:00 am", "28°", Icons.Rounded.NightsStay)
        )

        fun defaultDailyList(): List<DayForecast> = listOf(
            DayForecast("Today", Icons.Rounded.Cloud, "27°", "34°", 0.75f),
            DayForecast("Tomorrow", Icons.Rounded.Cloud, "27°", "34°", 0.75f),
            DayForecast("Mon", Icons.Rounded.WbSunny, "27°", "35°", 0.80f),
            DayForecast("Tue", Icons.Rounded.Cloud, "26°", "31°", 0.60f),
            DayForecast("Wed", Icons.Rounded.Cloud, "26°", "30°", 0.55f),
            DayForecast("Thu", Icons.Rounded.WbSunny, "26°", "31°", 0.60f),
            DayForecast("Fri", Icons.Rounded.Cloud, "25°", "30°", 0.55f)
        )
    }
}
