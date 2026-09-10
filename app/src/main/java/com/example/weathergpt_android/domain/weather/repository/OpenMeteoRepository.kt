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
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m,wind_gusts_10m,precipitation,rain,showers,visibility,dew_point_2m,surface_pressure" +
                "&hourly=temperature_2m,relative_humidity_2m,dew_point_2m,weather_code,precipitation_probability,precipitation,rain,soil_moisture_0_to_1cm,soil_moisture_1_to_3cm,soil_moisture_3_to_9cm,soil_temperature_0cm,evapotranspiration,wind_gusts_10m,wind_direction_10m,cloud_cover,visibility,surface_pressure" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,precipitation_hours,wind_gusts_10m_max,wind_direction_10m_dominant,uv_index_max,et0_fao_evapotranspiration" +
                "&past_days=7&forecast_days=7&timezone=auto"
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
                val apparentVal = current.optDouble("apparent_temperature", tempVal.toDouble()).roundToInt()
                val apparentStr = "$apparentVal°"
                val humidityStr = "${current.getInt("relative_humidity_2m")}%"
                val windSpeedVal = current.getDouble("wind_speed_10m").roundToInt()
                val windSpeedStr = "$windSpeedVal km/h"
                val windGustsVal = current.optDouble("wind_gusts_10m", windSpeedVal * 1.5).roundToInt()
                val windGustsStr = "$windGustsVal km/h"
                val dewPointVal = current.optDouble("dew_point_2m", 22.0).roundToInt()
                val dewPointStr = "$dewPointVal°C"
                val surfacePressureVal = current.optDouble("surface_pressure", 1010.0).roundToInt()
                val surfacePressureStr = "$surfacePressureVal hPa"
                val weatherCode = current.getInt("weather_code")
                val conditionStr = decodeWmoWeatherCode(weatherCode)

                // Visibility
                val visMeters = current.optDouble("visibility", 10000.0)
                val visKmStr = "${"%.1f".format(visMeters / 1000.0)} km"

                // Parse Daily High / Low, 7-Day List & Evapotranspiration
                val pastDaysCount = 7
                var highLowStr = "H: ${tempVal + 2}°  L: ${tempVal - 4}°"
                var uvStr = "7 (High)"
                var et0Str = "4.5 mm/day"
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
                    val et0List = daily.optJSONArray("et0_fao_evapotranspiration")

                    val todayIdx = if ((maxList?.length() ?: 0) > pastDaysCount) pastDaysCount else 0

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

                    if (et0List != null && et0List.length() > todayIdx) {
                        val etVal = et0List.optDouble(todayIdx, 4.5)
                        et0Str = "${"%.1f".format(etVal)} mm/day"
                    }
                }

                // Parse Hourly Forecast, Dense Precipitation & Multi-Depth Soil Moisture
                val hourlyList = mutableListOf<HourlyForecast>()
                val hourly = json.optJSONObject("hourly")
                var currentSoilMoisture = 0.33
                var currentRootSoilMoisture = 0.35
                var currentSoilTemp = 28
                var rainNext24hVal = 0.0
                var rainNext48hVal = 0.0
                var peakTimingStr = "No severe rain spells expected."
                val nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val baseIdx = pastDaysCount * 24 + nowHour

                if (hourly != null) {
                    val hTimes = hourly.optJSONArray("time")
                    val hTemps = hourly.optJSONArray("temperature_2m")
                    val hCodes = hourly.optJSONArray("weather_code")
                    val hMoistSurface = hourly.optJSONArray("soil_moisture_0_to_1cm")
                    val hMoistRoot = hourly.optJSONArray("soil_moisture_3_to_9cm")
                    val hSoilT = hourly.optJSONArray("soil_temperature_0cm")
                    val hPrecip = hourly.optJSONArray("precipitation")
                    val hProb = hourly.optJSONArray("precipitation_probability")

                    hourlyList.add(
                        HourlyForecast(
                            time = "Now",
                            temp = tempStr,
                            icon = mapWmoToIcon(weatherCode, isCurrentNight()),
                            isNow = true
                        )
                    )

                    if (hMoistSurface != null && hMoistSurface.length() > baseIdx) {
                        currentSoilMoisture = hMoistSurface.optDouble(baseIdx, 0.33)
                    }
                    if (hMoistRoot != null && hMoistRoot.length() > baseIdx) {
                        currentRootSoilMoisture = hMoistRoot.optDouble(baseIdx, 0.35)
                    }
                    if (hSoilT != null && hSoilT.length() > baseIdx) {
                        currentSoilTemp = hSoilT.optDouble(baseIdx, 28.0).roundToInt()
                    }

                    // Compute upcoming 24h and 48h rain sums
                    if (hPrecip != null) {
                        val end24 = min(baseIdx + 24, hPrecip.length())
                        for (idx in baseIdx until end24) {
                            rainNext24hVal += hPrecip.optDouble(idx, 0.0)
                        }
                        val end48 = min(baseIdx + 48, hPrecip.length())
                        for (idx in baseIdx until end48) {
                            rainNext48hVal += hPrecip.optDouble(idx, 0.0)
                        }
                    }

                    // Peak precipitation probability & timing window
                    if (hProb != null && hProb.length() > baseIdx) {
                        var maxProbVal = 0
                        var peakOffset = 0
                        val endScan = min(baseIdx + 24, hProb.length())
                        for (idx in baseIdx until endScan) {
                            val p = hProb.optInt(idx, 0)
                            if (p > maxProbVal) {
                                maxProbVal = p
                                peakOffset = idx - baseIdx
                            }
                        }
                        if (maxProbVal >= 35) {
                            val peakHour = (nowHour + peakOffset) % 24
                            val peakHourStr = formatHourLabel(peakHour)
                            peakTimingStr = "Strongest spells likely around $peakHourStr ($maxProbVal% chance)."
                        }
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

                // ═══════════════════════════════════════════════════════════════
                // PREDICTIVE TREND ANALYSIS ENGINE
                // Computes meteorological patterns from hourly time-series data
                // ═══════════════════════════════════════════════════════════════

                // 1. Barometric Pressure Trend Analysis (6h lookback)
                var pressureTrendStr = "Steady"
                if (hourly != null) {
                    val hPressure = hourly.optJSONArray("surface_pressure")
                    if (hPressure != null && hPressure.length() > baseIdx) {
                        val pNow = hPressure.optDouble(baseIdx, 1010.0)
                        val p3hAgo = if (baseIdx >= 3) hPressure.optDouble(baseIdx - 3, pNow) else pNow
                        val p6hAgo = if (baseIdx >= 6) hPressure.optDouble(baseIdx - 6, pNow) else pNow
                        val delta6h = pNow - p6hAgo
                        pressureTrendStr = when {
                            delta6h < -4.0 -> "Falling rapidly (${"%.1f".format(delta6h)} hPa in 6h) — incoming low-pressure system, storm likely"
                            delta6h < -2.0 -> "Falling (${"%.1f".format(delta6h)} hPa in 6h) — weather deteriorating, rain probable"
                            delta6h < -0.5 -> "Slightly falling (${"%.1f".format(delta6h)} hPa in 6h) — gradual change possible"
                            delta6h > 4.0 -> "Rising rapidly (+${"%.1f".format(delta6h)} hPa in 6h) — clearing fast, fair weather ahead"
                            delta6h > 2.0 -> "Rising (+${"%.1f".format(delta6h)} hPa in 6h) — improving conditions"
                            delta6h > 0.5 -> "Slightly rising (+${"%.1f".format(delta6h)} hPa in 6h) — stable outlook"
                            else -> "Steady (±${"%.1f".format(kotlin.math.abs(delta6h))} hPa in 6h) — no significant change"
                        }
                    }
                }

                // 2. Wind Direction Shift Detection (6h pattern)
                var windShiftStr = "No significant wind direction change."
                if (hourly != null) {
                    val hWindDir = hourly.optJSONArray("wind_direction_10m")
                    if (hWindDir != null && hWindDir.length() > baseIdx) {
                        val dirNow = hWindDir.optDouble(baseIdx, 180.0)
                        val dir6hAgo = if (baseIdx >= 6) hWindDir.optDouble(baseIdx - 6, dirNow) else dirNow
                        val dir6hAhead = if (baseIdx + 6 < hWindDir.length()) hWindDir.optDouble(baseIdx + 6, dirNow) else dirNow
                        val dirNameNow = degreesToCompass(dirNow)
                        val dirName6hAgo = degreesToCompass(dir6hAgo)
                        val dirName6hAhead = degreesToCompass(dir6hAhead)
                        val shift = angleDiff(dir6hAgo, dirNow)
                        windShiftStr = when {
                            shift > 60 -> "Significant shift from $dirName6hAgo to $dirNameNow (${shift.roundToInt()}° change). Upcoming: $dirName6hAhead — weather pattern change detected."
                            shift > 30 -> "Moderate shift from $dirName6hAgo to $dirNameNow. Upcoming trend: $dirName6hAhead."
                            else -> "Stable wind from $dirNameNow. No major directional change expected."
                        }
                    }
                }

                // 3. Cloud Cover Progression Analysis (6h lookahead)
                var cloudTrendStr = "Stable cloud cover."
                if (hourly != null) {
                    val hCloud = hourly.optJSONArray("cloud_cover")
                    if (hCloud != null && hCloud.length() > baseIdx) {
                        val cloudNow = hCloud.optInt(baseIdx, 50)
                        val cloud3hAhead = if (baseIdx + 3 < hCloud.length()) hCloud.optInt(baseIdx + 3, cloudNow) else cloudNow
                        val cloud6hAhead = if (baseIdx + 6 < hCloud.length()) hCloud.optInt(baseIdx + 6, cloudNow) else cloudNow
                        val cloudDelta = cloud6hAhead - cloudNow
                        cloudTrendStr = when {
                            cloudDelta > 40 -> "Rapid cloud buildup: $cloudNow% → $cloud3hAhead% → $cloud6hAhead% over next 6h — overcast/rain conditions developing."
                            cloudDelta > 20 -> "Increasing cloud cover: $cloudNow% → $cloud6hAhead% over next 6h — partly cloudy turning overcast."
                            cloudDelta < -40 -> "Clearing rapidly: $cloudNow% → $cloud6hAhead% over next 6h — skies opening up."
                            cloudDelta < -20 -> "Gradual clearing: $cloudNow% → $cloud6hAhead% over next 6h."
                            cloudNow > 80 -> "Thick overcast ($cloudNow%) holding steady."
                            cloudNow < 20 -> "Clear skies ($cloudNow%) remaining stable."
                            else -> "Moderate cloud cover ($cloudNow%) with minor fluctuations."
                        }
                    }
                }

                // 4. Dew Point Proximity (Fog/Condensation Risk)
                var dewPointProxStr = "Safe gap — low condensation risk."
                val tempDouble = current.getDouble("temperature_2m")
                val dewDouble = current.optDouble("dew_point_2m", tempDouble - 5.0)
                val dewGap = tempDouble - dewDouble
                dewPointProxStr = when {
                    dewGap <= 1.0 -> "Critical: Temperature and dew point nearly equal (gap ${"%.1f".format(dewGap)}°C) — fog/mist formation imminent."
                    dewGap <= 2.5 -> "Warning: Small gap (${"%.1f".format(dewGap)}°C) — high moisture condensation risk, fog possible overnight."
                    dewGap <= 5.0 -> "Moderate gap (${"%.1f".format(dewGap)}°C) — comfortable humidity, slight overnight fog risk."
                    else -> "Wide gap (${"%.1f".format(dewGap)}°C) — dry conditions, no condensation risk."
                }

                // 5. Rainfall Pattern Summary (past 7 days)
                var rainfallPatternStr = "No significant recent rainfall."
                if (daily != null) {
                    val rainList = daily.optJSONArray("precipitation_sum")
                    if (rainList != null) {
                        val todayI = if ((rainList.length()) > pastDaysCount) pastDaysCount else 0
                        var totalPast7 = 0.0
                        var rainyDays = 0
                        var consecutiveRainDays = 0
                        var maxConsecutive = 0
                        for (i in 0 until todayI) {
                            val dayRain = rainList.optDouble(i, 0.0)
                            totalPast7 += dayRain
                            if (dayRain > 1.0) {
                                rainyDays++
                                consecutiveRainDays++
                                maxConsecutive = max(maxConsecutive, consecutiveRainDays)
                            } else {
                                consecutiveRainDays = 0
                            }
                        }
                        pastRainSum = totalPast7
                        rainfallPatternStr = when {
                            totalPast7 > 60 && maxConsecutive >= 4 -> "Heavy sustained rainfall: ${"%.0f".format(totalPast7)}mm over past 7 days ($rainyDays rainy days, $maxConsecutive consecutive). Soil heavily saturated — flooding/waterlogging very likely with any additional rain."
                            totalPast7 > 30 -> "Significant rainfall: ${"%.0f".format(totalPast7)}mm over past 7 days ($rainyDays rainy days). Soil moisture elevated — drainage stressed."
                            totalPast7 > 10 -> "Moderate rainfall: ${"%.0f".format(totalPast7)}mm over past 7 days ($rainyDays rainy days). Soil adequately moist."
                            totalPast7 > 2 -> "Light rainfall: ${"%.0f".format(totalPast7)}mm over past 7 days. Normal moisture levels."
                            else -> "Dry spell: Less than 2mm rainfall in past 7 days. Soil drying out — irrigation may be needed."
                        }
                    }
                }

                // 6. Precipitation Intensity Windows (next 12h hourly breakdown)
                var precipWindowsStr = "No rain windows detected."
                if (hourly != null) {
                    val hPrecipArr = hourly.optJSONArray("precipitation")
                    if (hPrecipArr != null && hPrecipArr.length() > baseIdx) {
                        val windows = mutableListOf<String>()
                        var windowStart = -1
                        var windowTotal = 0.0
                        var windowIntensity = ""

                        for (offset in 0 until min(12, hPrecipArr.length() - baseIdx)) {
                            val rainMm = hPrecipArr.optDouble(baseIdx + offset, 0.0)
                            if (rainMm > 0.1) {
                                if (windowStart < 0) windowStart = offset
                                windowTotal += rainMm
                            } else if (windowStart >= 0) {
                                // Window ended
                                windowIntensity = when {
                                    windowTotal > 10.0 -> "Heavy"
                                    windowTotal > 3.0 -> "Moderate"
                                    else -> "Light"
                                }
                                val startHr = formatHourLabel((nowHour + windowStart) % 24)
                                val endHr = formatHourLabel((nowHour + offset) % 24)
                                windows.add("$windowIntensity $startHr-$endHr (${"%.1f".format(windowTotal)}mm)")
                                windowStart = -1
                                windowTotal = 0.0
                            }
                        }
                        // Close open window
                        if (windowStart >= 0 && windowTotal > 0.1) {
                            windowIntensity = when {
                                windowTotal > 10.0 -> "Heavy"
                                windowTotal > 3.0 -> "Moderate"
                                else -> "Light"
                            }
                            val startHr = formatHourLabel((nowHour + windowStart) % 24)
                            val endHr = formatHourLabel((nowHour + 12) % 24)
                            windows.add("$windowIntensity $startHr-$endHr (${"%.1f".format(windowTotal)}mm)")
                        }
                        if (windows.isNotEmpty()) {
                            precipWindowsStr = windows.joinToString(", ")
                        }
                    }
                }

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

                val waterloggingRiskStr = when {
                    rainNext48hVal > 40.0 || (rainNext24hVal > 20.0 && currentSoilMoisture > 0.32) -> "High Waterlogging Risk"
                    rainNext24hVal > 10.0 -> "Moderate Waterlogging Risk"
                    else -> "Low Risk"
                }

                val irrigStr = when {
                    rainNext24hVal > 15.0 || currentSoilMoisture > 0.36 ->
                        "Heavy rainfall or saturated soil detected. Suspend irrigation and clear field drainage channels."
                    currentSoilMoisture > 0.28 ->
                        "Adequate soil moisture present. Irrigation not required today."
                    else ->
                        "Low root-zone moisture detected. Controlled morning irrigation advised."
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
                    rootZoneSoilMoisture = "${"%.3f".format(currentRootSoilMoisture)} m³/m³",
                    soilTemperature = "$currentSoilTemp°C",
                    irrigationAdvice = irrigStr,
                    evapotranspiration = et0Str,
                    waterloggingRisk = waterloggingRiskStr,
                    visibilityKm = visKmStr,
                    pastRainfallTrend = "${"%.1f".format(pastRainSum)} mm in past 7 days",
                    apparentTemperature = apparentStr,
                    dewPoint = dewPointStr,
                    surfacePressure = surfacePressureStr,
                    windGusts = windGustsStr,
                    rainNext24h = "${"%.1f".format(rainNext24hVal)} mm",
                    rainNext48h = "${"%.1f".format(rainNext48hVal)} mm",
                    peakRainTiming = peakTimingStr,
                    // Predictive Trend Analysis
                    pressureTrend = pressureTrendStr,
                    windShiftSummary = windShiftStr,
                    cloudTrend = cloudTrendStr,
                    dewPointProximity = dewPointProxStr,
                    rainfallPatternSummary = rainfallPatternStr,
                    precipitationWindows = precipWindowsStr
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

    /**
     * Converts wind direction in degrees to 16-point compass direction.
     */
    private fun degreesToCompass(degrees: Double): String {
        val dirs = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((degrees % 360 + 360) % 360 / 22.5 + 0.5).toInt() % 16
        return dirs[index]
    }

    /**
     * Computes the absolute angular difference between two compass bearings.
     */
    private fun angleDiff(a: Double, b: Double): Double {
        val diff = kotlin.math.abs((b % 360) - (a % 360))
        return if (diff > 180) 360 - diff else diff
    }
}
