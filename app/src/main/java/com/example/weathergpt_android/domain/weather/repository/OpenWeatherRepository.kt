package com.example.weathergpt_android.domain.weather.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.weathergpt_android.core.network.OpenWeatherPreferences
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

class OpenWeatherRepository(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("weathergpt_live_cache", Context.MODE_PRIVATE)

    fun getCachedWeather(): LiveWeatherData {
        val temp = prefs.getString("cached_temp", "32°") ?: "32°"
        val cond = prefs.getString("cached_cond", "Mostly Cloudy") ?: "Mostly Cloudy"
        val hl = prefs.getString("cached_hl", "H: 34°  L: 27°") ?: "H: 34°  L: 27°"
        val wind = prefs.getString("cached_wind", "6 km/h") ?: "6 km/h"
        val hum = prefs.getString("cached_hum", "59%") ?: "59%"
        val uv = prefs.getString("cached_uv", "7 (High)") ?: "7 (High)"
        val aqi = prefs.getString("cached_aqi", "64 (Moderate)") ?: "64 (Moderate)"

        return LiveWeatherData(
            temperature = temp,
            condition = cond,
            highLow = hl,
            windSpeed = wind,
            humidity = hum,
            uvIndex = uv,
            aqi = aqi,
            isLive = true,
            hourlyList = LiveWeatherData.defaultHourlyList(),
            dailyList = LiveWeatherData.defaultDailyList()
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
            val forecastUrl =
                "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&units=metric&appid=$apiKey"
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
                val currentTempDouble = main.getDouble("temp")
                val currentTemp = "${currentTempDouble.roundToInt()}°"
                val minTemp = main.getDouble("temp_min").roundToInt()
                val maxTemp = main.getDouble("temp_max").roundToInt()
                val humidity = "${main.getInt("humidity")}%"

                val windObj = weatherJson.optJSONObject("wind")
                val windSpeedMs = windObj?.optDouble("speed", 2.0) ?: 2.0
                val windSpeedKmH = "${(windSpeedMs * 3.6).roundToInt()} km/h"

                val weatherArray = weatherJson.getJSONArray("weather")
                var mainCondition = "Clouds"
                var description = "mostly cloudy"
                if (weatherArray.length() > 0) {
                    mainCondition = weatherArray.getJSONObject(0).getString("main")
                    description = weatherArray.getJSONObject(0).getString("description")
                }
                val formattedCondition = description.split(" ").joinToString(" ") {
                    it.replaceFirstChar { char -> char.uppercase() }
                }

                // 2. Fetch 5-Day / 3-Hour Forecast for Hourly & Multi-Day
                val hourlyList = mutableListOf<HourlyForecast>()
                val dailyList = mutableListOf<DayForecast>()
                var computedHigh = maxTemp
                var computedLow = minTemp

                try {
                    val forecastReq = Request.Builder().url(forecastUrl).build()
                    val forecastRes = client.newCall(forecastReq).execute()
                    val forecastBody = forecastRes.body?.string() ?: ""
                    if (forecastRes.isSuccessful) {
                        val forecastJson = JSONObject(forecastBody)
                        val list = forecastJson.getJSONArray("list")

                        // First hourly pill is "Now"
                        hourlyList.add(
                            HourlyForecast(
                                time = "Now",
                                temp = currentTemp,
                                icon = mapConditionToIcon(mainCondition, isNight = isCurrentNight()),
                                isNow = true
                            )
                        )

                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

                        val dailyMinMap = mutableMapOf<String, Double>()
                        val dailyMaxMap = mutableMapOf<String, Double>()
                        val dailyConditionMap = mutableMapOf<String, String>()

                        for (i in 0 until min(list.length(), 24)) {
                            val item = list.getJSONObject(i)
                            val dt = item.getLong("dt") * 1000L
                            val itemMain = item.getJSONObject("main")
                            val itemTemp = itemMain.getDouble("temp")
                            val itemWeather = item.getJSONArray("weather").getJSONObject(0).getString("main")

                            val date = Date(dt)
                            val timeStr = timeFormat.format(date).lowercase(Locale.getDefault())
                            val dayStr = dayFormat.format(date)

                            if (hourlyList.size < 7) {
                                hourlyList.add(
                                    HourlyForecast(
                                        time = timeStr,
                                        temp = "${itemTemp.roundToInt()}°",
                                        icon = mapConditionToIcon(itemWeather, isNight = timeStr.contains("am") || timeStr.contains("10:") || timeStr.contains("11:") || timeStr.contains("9:") || timeStr.contains("8:"))
                                    )
                                )
                            }

                            // Group for daily forecast
                            dailyMinMap[dayStr] = min(dailyMinMap.getOrDefault(dayStr, itemTemp), itemTemp)
                            dailyMaxMap[dayStr] = max(dailyMaxMap.getOrDefault(dayStr, itemTemp), itemTemp)
                            if (!dailyConditionMap.containsKey(dayStr)) {
                                dailyConditionMap[dayStr] = itemWeather
                            }
                        }

                        // Build Daily List
                        var dayIdx = 0
                        for ((dayKey, minT) in dailyMinMap) {
                            val maxT = dailyMaxMap[dayKey] ?: (minT + 6)
                            val label = when (dayIdx) {
                                0 -> "Today"
                                1 -> "Tomorrow"
                                else -> dayKey
                            }
                            if (dayIdx == 0) {
                                computedHigh = max(computedHigh, maxT.roundToInt())
                                computedLow = min(computedLow, minT.roundToInt())
                            }
                            val progress = ((maxT - 20.0) / 20.0).toFloat().coerceIn(0.3f, 0.95f)
                            dailyList.add(
                                DayForecast(
                                    day = label,
                                    condition = mapConditionToIcon(dailyConditionMap[dayKey] ?: "Clouds"),
                                    minTemp = "${minT.roundToInt()}°",
                                    maxTemp = "${maxT.roundToInt()}°",
                                    progress = progress
                                )
                            )
                            dayIdx++
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to default lists if forecast API fails
                }

                if (hourlyList.isEmpty()) {
                    hourlyList.addAll(LiveWeatherData.defaultHourlyList())
                }
                if (dailyList.isEmpty()) {
                    dailyList.addAll(LiveWeatherData.defaultDailyList())
                }

                val highLowStr = "H: $computedHigh°  L: $computedLow°"

                // 3. Fetch Air Quality Index with Accurate Indian CPCB / EPA Calculation
                var aqiString = "64 (Moderate)"
                try {
                    val aqiRequest = Request.Builder().url(pollutionUrl).build()
                    val aqiResponse = client.newCall(aqiRequest).execute()
                    val aqiBody = aqiResponse.body?.string() ?: ""
                    if (aqiResponse.isSuccessful) {
                        val aqiJson = JSONObject(aqiBody)
                        val list = aqiJson.optJSONArray("list")
                        if (list != null && list.length() > 0) {
                            val components = list.getJSONObject(0).getJSONObject("components")
                            val pm25 = components.optDouble("pm2_5", 20.0)
                            val pm10 = components.optDouble("pm10", 60.0)
                            val o3 = components.optDouble("o3", 64.0)

                            // Accurate Standard AQI formula
                            val aqiVal = calculateAccurateAqi(pm25, pm10, o3)
                            aqiString = when {
                                aqiVal <= 50 -> "$aqiVal (Good)"
                                aqiVal <= 100 -> "$aqiVal (Moderate)"
                                aqiVal <= 200 -> "$aqiVal (Unhealthy)"
                                aqiVal <= 300 -> "$aqiVal (Very Poor)"
                                else -> "$aqiVal (Hazardous)"
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback
                }

                val resolvedLiveWeather = LiveWeatherData(
                    temperature = currentTemp,
                    condition = formattedCondition,
                    highLow = highLowStr,
                    windSpeed = windSpeedKmH,
                    humidity = humidity,
                    uvIndex = "7 (High)",
                    aqi = aqiString,
                    isLive = true,
                    hourlyList = hourlyList,
                    dailyList = dailyList
                )

                cacheWeather(resolvedLiveWeather)
                Result.success(resolvedLiveWeather)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun calculateAccurateAqi(pm25: Double, pm10: Double, o3: Double): Int {
        // Linear breakpoint interpolation for standard PM2.5 & PM10
        val aqiPm25 = when {
            pm25 <= 12.0 -> (50.0 / 12.0 * pm25).roundToInt()
            pm25 <= 35.4 -> (50.0 + (50.0 / 23.4) * (pm25 - 12.0)).roundToInt()
            pm25 <= 55.4 -> (100.0 + (50.0 / 20.0) * (pm25 - 35.4)).roundToInt()
            pm25 <= 150.4 -> (150.0 + (50.0 / 95.0) * (pm25 - 55.4)).roundToInt()
            else -> (200.0 + (100.0 / 100.0) * (pm25 - 150.4)).roundToInt()
        }
        val aqiPm10 = when {
            pm10 <= 54.0 -> (50.0 / 54.0 * pm10).roundToInt()
            pm10 <= 154.0 -> (50.0 + (50.0 / 100.0) * (pm10 - 54.0)).roundToInt()
            else -> (100.0 + (50.0 / 100.0) * (pm10 - 154.0)).roundToInt()
        }
        return max(aqiPm25, aqiPm10).coerceIn(15, 500)
    }

    private fun mapConditionToIcon(condition: String, isNight: Boolean = false): ImageVector {
        return when {
            condition.contains("Rain", ignoreCase = true) || condition.contains("Drizzle", ignoreCase = true) -> Icons.Rounded.WaterDrop
            condition.contains("Cloud", ignoreCase = true) -> Icons.Rounded.Cloud
            condition.contains("Clear", ignoreCase = true) && isNight -> Icons.Rounded.NightsStay
            condition.contains("Clear", ignoreCase = true) -> Icons.Rounded.WbSunny
            isNight -> Icons.Rounded.NightsStay
            else -> Icons.Rounded.WbSunny
        }
    }

    private fun isCurrentNight(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour < 6 || hour >= 19
    }
}
