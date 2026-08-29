package com.example.weathergpt_android.domain.weather.repository

import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class OpenMeteoRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchWeather(latitude: Double, longitude: Double): Result<LiveWeatherData> =
        withContext(Dispatchers.IO) {
            val forecastUrl =
                "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min,uv_index_max&timezone=auto"
            val airQualityUrl =
                "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$latitude&longitude=$longitude&current=us_aqi,pm2_5,pm10"

            try {
                // 1. Fetch Forecast from Open-Meteo
                val request = Request.Builder().url(forecastUrl).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Open-Meteo HTTP ${response.code}: $body"))
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

                // Daily High / Low & UV
                var highLowStr = "H: ${tempVal + 2}°  L: ${tempVal - 4}°"
                var uvStr = "3 (Mod)"
                val daily = json.optJSONObject("daily")
                if (daily != null) {
                    val maxList = daily.optJSONArray("temperature_2m_max")
                    val minList = daily.optJSONArray("temperature_2m_min")
                    val uvList = daily.optJSONArray("uv_index_max")
                    if (maxList != null && minList != null && maxList.length() > 0) {
                        val maxT = maxList.getDouble(0).roundToInt()
                        val minT = minList.getDouble(0).roundToInt()
                        highLowStr = "H: $maxT°  L: $minT°"
                    }
                    if (uvList != null && uvList.length() > 0) {
                        val uvVal = uvList.getDouble(0).roundToInt()
                        uvStr = when {
                            uvVal <= 2 -> "$uvVal (Low)"
                            uvVal <= 5 -> "$uvVal (Mod)"
                            uvVal <= 7 -> "$uvVal (High)"
                            else -> "$uvVal (Very High)"
                        }
                    }
                }

                // 2. Fetch Live Air Quality (AQI)
                var aqiStr = "34 (Good)"
                try {
                    val aqiRequest = Request.Builder().url(airQualityUrl).build()
                    val aqiResponse = client.newCall(aqiRequest).execute()
                    val aqiBody = aqiResponse.body?.string() ?: ""
                    if (aqiResponse.isSuccessful) {
                        val aqiJson = JSONObject(aqiBody)
                        val aqiCurrent = aqiJson.optJSONObject("current")
                        if (aqiCurrent != null) {
                            val usAqi = aqiCurrent.optInt("us_aqi", 34)
                            aqiStr = when {
                                usAqi <= 50 -> "$usAqi (Good)"
                                usAqi <= 100 -> "$usAqi (Moderate)"
                                usAqi <= 150 -> "$usAqi (Unhealthy)"
                                else -> "$usAqi (Poor)"
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback default AQI
                }

                val resolved = LiveWeatherData(
                    temperature = tempStr,
                    condition = conditionStr,
                    highLow = highLowStr,
                    windSpeed = windSpeedStr,
                    humidity = humidityStr,
                    uvIndex = uvStr,
                    aqi = aqiStr,
                    isLive = true
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
            3 -> "Overcast Clouds"
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
}
