package com.example.weathergpt_android.domain.weather.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.weathergpt_android.domain.weather.model.DayForecast
import com.example.weathergpt_android.domain.weather.model.HourlyForecast
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class OpenMeteoRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(14, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchWeather(latitude: Double, longitude: Double): Result<LiveWeatherData> =
        withContext(Dispatchers.IO) {
            val forecastUrl =
                "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,visibility" +
                "&hourly=temperature_2m,weather_code,precipitation_probability,soil_moisture_0_to_1cm,soil_temperature_0cm" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,uv_index_max,precipitation_sum" +
                "&past_days=3&timezone=auto"
            val airQualityUrl =
                "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$latitude&longitude=$longitude&current=us_aqi,pm2_5,pm10"

            try {
                // 1. Fetch Forecast from Open-Meteo
                val request = Request.Builder().url(forecastUrl).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Weather service error (${response.code})"))
                }

                val json = JSONObject(body)
                val current = json.getJSONObject("current")
                val tempVal = current.getDouble("temperature_2m").roundToInt()
                val tempStr = "$tempVal°"
                val humidityStr = "${current.getInt("relative_humidity_2m")}%"
                val windSpeedVal = current.getDouble("wind_speed_10m").roundToInt()
                val windSpeedStr = "$windSpeedVal km/h"
                val weatherCode = current.getInt("weather_code")
                val conditionStr = decodeWmoWeatherCode(weatherCode)

                // Visibility
                val visMeters = current.optDouble("visibility", 10000.0)
                val visKmStr = "${"%.1f".format(visMeters / 1000.0)} km"

                // Parse Daily High / Low & 7-Day List
                var highLowStr = "H: ${tempVal + 2}°  L: ${tempVal - 4}°"
                var uvStr = "7 (High)"
                val dailyList = mutableListOf<DayForecast>()
                val daily = json.optJSONObject("daily")
                var pastRainSum = 0.0

                if (daily != null) {
                    val maxList = daily.optJSONArray("temperature_2m_max")
                    val minList = daily.optJSONArray("temperature_2m_min")
                    val codeList = daily.optJSONArray("weather_code")
                    val timeList = daily.optJSONArray("time")
                    val uvList = daily.optJSONArray("uv_index_max")
                    val rainList = daily.optJSONArray("precipitation_sum")

                    val todayIdx = if ((maxList?.length() ?: 0) > 3) 3 else 0

                    if (maxList != null && minList != null && maxList.length() > todayIdx) {
                        val maxT = maxList.getDouble(todayIdx).roundToInt()
                        val minT = minList.getDouble(todayIdx).roundToInt()
                        highLowStr = "H: $maxT°  L: $minT°"

                        for (i in todayIdx until min(maxList.length(), todayIdx + 7)) {
                            val maxDay = maxList.getDouble(i).roundToInt()
                            val minDay = minList.getDouble(i).roundToInt()
                            val cCode = codeList?.optInt(i, 3) ?: 3
                            val timeStr = timeList?.optString(i, "") ?: ""
                            val label = when (i - todayIdx) {
                                0 -> "Today"
                                1 -> "Tomorrow"
                                else -> parseDayName(timeStr)
                            }
                            val progress = ((maxDay - 20.0) / 20.0).toFloat().coerceIn(0.3f, 0.95f)
                            dailyList.add(
                                DayForecast(
                                    day = label,
                                    condition = mapWmoToIcon(cCode),
                                    minTemp = "$minDay°",
                                    maxTemp = "$maxDay°",
                                    progress = progress
                                )
                            )
                        }
                    }

                    if (rainList != null) {
                        for (i in 0 until min(todayIdx, rainList.length())) {
                            pastRainSum += rainList.optDouble(i, 0.0)
                        }
                    }

                    if (uvList != null && uvList.length() > todayIdx) {
                        val uvVal = uvList.getDouble(todayIdx).roundToInt()
                        uvStr = when {
                            uvVal <= 2 -> "$uvVal (Low)"
                            uvVal <= 5 -> "$uvVal (Mod)"
                            uvVal <= 7 -> "$uvVal (High)"
                            else -> "$uvVal (Very High)"
                        }
                    }
                }

                // Parse Hourly Forecast & Soil Moisture
                val hourlyList = mutableListOf<HourlyForecast>()
                val hourly = json.optJSONObject("hourly")
                var currentSoilMoisture = 0.33
                var currentSoilTemp = 28

                if (hourly != null) {
                    val hTimes = hourly.optJSONArray("time")
                    val hTemps = hourly.optJSONArray("temperature_2m")
                    val hCodes = hourly.optJSONArray("weather_code")
                    val hMoist = hourly.optJSONArray("soil_moisture_0_to_1cm")
                    val hSoilT = hourly.optJSONArray("soil_temperature_0cm")

                    hourlyList.add(
                        HourlyForecast(
                            time = "Now",
                            temp = tempStr,
                            icon = mapWmoToIcon(weatherCode, isCurrentNight()),
                            isNow = true
                        )
                    )

                    val nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val baseIdx = 3 * 24 + nowHour // Account for past_days=3 offset
                    if (hMoist != null && hMoist.length() > baseIdx) {
                        currentSoilMoisture = hMoist.optDouble(baseIdx, 0.33)
                    }
                    if (hSoilT != null && hSoilT.length() > baseIdx) {
                        currentSoilTemp = hSoilT.optDouble(baseIdx, 28.0).roundToInt()
                    }

                    if (hTimes != null && hTemps != null && hCodes != null) {
                        for (i in (baseIdx + 1) until min(baseIdx + 8, hTimes.length())) {
                            val tVal = hTemps.getDouble(i).roundToInt()
                            val cVal = hCodes.getInt(i)
                            val hourStr = formatHourLabel(i % 24)
                            hourlyList.add(
                                HourlyForecast(
                                    time = hourStr,
                                    temp = "$tVal°",
                                    icon = mapWmoToIcon(cVal, isHourNight(i % 24))
                                )
                            )
                        }
                    }
                }

                if (hourlyList.isEmpty()) hourlyList.addAll(LiveWeatherData.defaultHourlyList())
                if (dailyList.isEmpty()) dailyList.addAll(LiveWeatherData.defaultDailyList())

                // 2. Fetch Live Air Quality (AQI)
                var aqiStr = "64 (Moderate)"
                try {
                    val aqiRequest = Request.Builder().url(airQualityUrl).build()
                    val aqiResponse = client.newCall(aqiRequest).execute()
                    val aqiBody = aqiResponse.body?.string() ?: ""
                    if (aqiResponse.isSuccessful) {
                        val aqiJson = JSONObject(aqiBody)
                        val aqiCurrent = aqiJson.optJSONObject("current")
                        if (aqiCurrent != null) {
                            val pm25 = aqiCurrent.optDouble("pm2_5", 20.0)
                            val pm10 = aqiCurrent.optDouble("pm10", 60.0)
                            val aqiVal = max((pm25 * 2.5).roundToInt(), (pm10 * 1.0).roundToInt()).coerceIn(15, 500)
                            aqiStr = when {
                                aqiVal <= 50 -> "$aqiVal (Good)"
                                aqiVal <= 100 -> "$aqiVal (Moderate)"
                                aqiVal <= 200 -> "$aqiVal (Unhealthy)"
                                else -> "$aqiVal (Poor)"
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback
                }

                val irrigStr = if (currentSoilMoisture > 0.28) {
                    "Adequate soil moisture. No irrigation needed today."
                } else {
                    "Low soil moisture detected. Controlled irrigation advised."
                }

                val resolved = LiveWeatherData(
                    temperature = tempStr,
                    condition = conditionStr,
                    highLow = highLowStr,
                    windSpeed = windSpeedStr,
                    humidity = humidityStr,
                    uvIndex = uvStr,
                    aqi = aqiStr,
                    isLive = true,
                    hourlyList = hourlyList,
                    dailyList = dailyList,
                    soilMoisture = "${"%.3f".format(currentSoilMoisture)} m³/m³",
                    soilTemperature = "$currentSoilTemp°C",
                    irrigationAdvice = irrigStr,
                    visibilityKm = visKmStr,
                    pastRainfallTrend = "${"%.1f".format(pastRainSum)} mm in past 3 days"
                )

                Result.success(resolved)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun decodeWmoWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Mostly Cloudy"
            45, 48 -> "Foggy Atmosphere"
            51, 53, 55 -> "Light Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63 -> "Moderate Rain"
            65 -> "Heavy Rain"
            71, 73, 75 -> "Snow Fall"
            77 -> "Snow Grains"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Partly Cloudy"
        }
    }

    private fun mapWmoToIcon(code: Int, isNight: Boolean = false): ImageVector {
        return when (code) {
            0, 1 -> if (isNight) Icons.Rounded.NightsStay else Icons.Rounded.WbSunny
            2, 3 -> Icons.Rounded.Cloud
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Rounded.WaterDrop
            else -> Icons.Rounded.Cloud
        }
    }

    private fun parseDayName(dateStr: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            date?.let { SimpleDateFormat("EEE", Locale.getDefault()).format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatHourLabel(hour: Int): String {
        val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val ampm = if (hour < 12) "am" else "pm"
        return "$h:00 $ampm"
    }

    private fun isCurrentNight(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour < 6 || hour >= 19
    }

    private fun isHourNight(hour: Int): Boolean {
        return hour < 6 || hour >= 19
    }
}
