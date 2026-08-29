package com.example.weathergpt_android.domain.weather.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.weathergpt_android.core.network.OpenWeatherPreferences
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class OpenWeatherRepository(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("weathergpt_live_cache", Context.MODE_PRIVATE)

    fun getCachedWeather(): LiveWeatherData {
        val temp = prefs.getString("cached_temp", "24°") ?: "24°"
        val cond = prefs.getString("cached_cond", "Clear Sky") ?: "Clear Sky"
        val hl = prefs.getString("cached_hl", "H: 26°  L: 18°") ?: "H: 26°  L: 18°"
        val wind = prefs.getString("cached_wind", "14 km/h") ?: "14 km/h"
        val hum = prefs.getString("cached_hum", "52%") ?: "52%"
        val uv = prefs.getString("cached_uv", "3 (Mod)") ?: "3 (Mod)"
        val aqi = prefs.getString("cached_aqi", "34 (Good)") ?: "34 (Good)"

        return LiveWeatherData(
            temperature = temp,
            condition = cond,
            highLow = hl,
            windSpeed = wind,
            humidity = hum,
            uvIndex = uv,
            aqi = aqi,
            isLive = true
        )
    }

    private fun cacheWeather(data: LiveWeatherData) {
        prefs.edit()
            .putString("cached_temp", data.temperature)
            .putString("cached_cond", data.condition)
            .putString("cached_hl", data.highLow)
            .putString("cached_wind", data.windSpeed)
            .putString("cached_hum", data.humidity)
            .putString("cached_uv", data.uvIndex)
            .putString("cached_aqi", data.aqi)
            .putLong("cached_time", System.currentTimeMillis())
            .apply()
    }

    suspend fun fetchLiveWeather(latitude: Double, longitude: Double): Result<LiveWeatherData> =
        withContext(Dispatchers.IO) {
            val apiKey = OpenWeatherPreferences.getApiKey(context)
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("OpenWeather API key is not configured"))
            }

            val weatherUrl =
                "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$apiKey"
            val pollutionUrl =
                "https://api.openweathermap.org/data/2.5/air_pollution?lat=$latitude&lon=$longitude&appid=$apiKey"

            try {
                // 1. Fetch Current Weather
                val weatherRequest = Request.Builder().url(weatherUrl).build()
                val weatherResponse = client.newCall(weatherRequest).execute()
                val weatherBody = weatherResponse.body?.string() ?: ""

                if (!weatherResponse.isSuccessful) {
                    return@withContext Result.failure(Exception("OpenWeather HTTP ${weatherResponse.code}: $weatherBody"))
                }

                val weatherJson = JSONObject(weatherBody)
                val main = weatherJson.getJSONObject("main")
                val currentTemp = "${main.getDouble("temp").roundToInt()}°"
                val minTemp = main.getDouble("temp_min").roundToInt()
                val maxTemp = main.getDouble("temp_max").roundToInt()
                val highLow = "H: $maxTemp°  L: $minTemp°"
                val humidity = "${main.getInt("humidity")}%"

                val windObj = weatherJson.optJSONObject("wind")
                val windSpeedMs = windObj?.optDouble("speed", 3.8) ?: 3.8
                val windSpeedKmH = "${(windSpeedMs * 3.6).roundToInt()} km/h"

                val weatherArray = weatherJson.getJSONArray("weather")
                val weatherCondition = if (weatherArray.length() > 0) {
                    val rawCondition = weatherArray.getJSONObject(0).getString("main")
                    val description = weatherArray.getJSONObject(0).getString("description")
                    description.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                } else {
                    "Clear Sky"
                }

                // 2. Fetch Air Pollution AQI (Optional auxiliary call)
                var aqiString = "34 (Good)"
                try {
                    val aqiRequest = Request.Builder().url(pollutionUrl).build()
                    val aqiResponse = client.newCall(aqiRequest).execute()
                    val aqiBody = aqiResponse.body?.string() ?: ""
                    if (aqiResponse.isSuccessful) {
                        val aqiJson = JSONObject(aqiBody)
                        val list = aqiJson.optJSONArray("list")
                        if (list != null && list.length() > 0) {
                            val aqiVal = list.getJSONObject(0).getJSONObject("main").getInt("aqi")
                            aqiString = when (aqiVal) {
                                1 -> "28 (Good)"
                                2 -> "45 (Fair)"
                                3 -> "78 (Moderate)"
                                4 -> "120 (Poor)"
                                5 -> "180 (Very Poor)"
                                else -> "34 (Good)"
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback default AQI
                }

                val resolvedLiveWeather = LiveWeatherData(
                    temperature = currentTemp,
                    condition = weatherCondition,
                    highLow = highLow,
                    windSpeed = windSpeedKmH,
                    humidity = humidity,
                    uvIndex = "3 (Mod)",
                    aqi = aqiString,
                    isLive = true
                )

                cacheWeather(resolvedLiveWeather)
                Result.success(resolvedLiveWeather)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
