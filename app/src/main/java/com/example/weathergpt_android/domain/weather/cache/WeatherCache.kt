package com.example.weathergpt_android.domain.weather.cache

import android.content.Context
import android.content.SharedPreferences
import com.example.weathergpt_android.domain.weather.model.DayForecast
import com.example.weathergpt_android.domain.weather.model.HourlyForecast
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import org.json.JSONArray
import org.json.JSONObject

/**
 * Offline persistent cache for LiveWeatherData snapshot.
 * Ensures the on-device LLM always has rich meteorological telemetry
 * to ground its reasoning, even with zero network connectivity.
 */
class WeatherCache(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveWeatherSnapshot(data: LiveWeatherData) {
        val json = JSONObject().apply {
            put("temperature", data.temperature)
            put("condition", data.condition)
            put("highLow", data.highLow)
            put("windSpeed", data.windSpeed)
            put("humidity", data.humidity)
            put("uvIndex", data.uvIndex)
            put("aqi", data.aqi)
            put("apparentTemperature", data.apparentTemperature)
            put("dewPoint", data.dewPoint)
            put("surfacePressure", data.surfacePressure)
            put("windGusts", data.windGusts)
            put("visibilityKm", data.visibilityKm)
            put("rainNext24h", data.rainNext24h)
            put("rainNext48h", data.rainNext48h)
            put("peakRainTiming", data.peakRainTiming)
            put("pastRainfallTrend", data.pastRainfallTrend)
            // Multi-Sector Agriculture & Soil
            put("soilMoisture", data.soilMoisture)
            put("rootZoneSoilMoisture", data.rootZoneSoilMoisture)
            put("soilTemperature", data.soilTemperature)
            put("irrigationAdvice", data.irrigationAdvice)
            put("evapotranspiration", data.evapotranspiration)
            // Risk & Disaster
            put("floodRiskLevel", data.floodRiskLevel)
            put("waterloggingRisk", data.waterloggingRisk)
            put("riverDischarge", data.riverDischarge)
            put("heatwaveAlert", data.heatwaveAlert)
            // Predictive Trend Layer
            put("pressureTrend", data.pressureTrend)
            put("windShiftSummary", data.windShiftSummary)
            put("cloudTrend", data.cloudTrend)
            put("dewPointProximity", data.dewPointProximity)
            put("rainfallPatternSummary", data.rainfallPatternSummary)
            put("precipitationWindows", data.precipitationWindows)
            put("savedTimestamp", System.currentTimeMillis())
        }

        prefs.edit()
            .putString(KEY_WEATHER_JSON, json.toString())
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getCachedWeather(): LiveWeatherData? {
        val raw = prefs.getString(KEY_WEATHER_JSON, null) ?: return null
        return try {
            val json = JSONObject(raw)
            LiveWeatherData(
                temperature = json.optString("temperature", "32°"),
                condition = json.optString("condition", "Partly Cloudy"),
                highLow = json.optString("highLow", "H: 34°  L: 27°"),
                windSpeed = json.optString("windSpeed", "6 km/h"),
                humidity = json.optString("humidity", "59%"),
                uvIndex = json.optString("uvIndex", "7 (High)"),
                aqi = json.optString("aqi", "64 (Moderate)"),
                isLive = false,
                lastUpdatedTime = getCacheAgeString(),
                soilMoisture = json.optString("soilMoisture", "0.33 m³/m³"),
                rootZoneSoilMoisture = json.optString("rootZoneSoilMoisture", "0.35 m³/m³"),
                soilTemperature = json.optString("soilTemperature", "28°C"),
                irrigationAdvice = json.optString("irrigationAdvice", "Adequate soil moisture."),
                evapotranspiration = json.optString("evapotranspiration", "4.5 mm/day"),
                floodRiskLevel = json.optString("floodRiskLevel", "Normal (Low Risk)"),
                waterloggingRisk = json.optString("waterloggingRisk", "Low"),
                riverDischarge = json.optString("riverDischarge", "12.5 m³/s"),
                visibilityKm = json.optString("visibilityKm", "10.0 km"),
                pastRainfallTrend = json.optString("pastRainfallTrend", "Normal"),
                heatwaveAlert = json.optString("heatwaveAlert", "None"),
                apparentTemperature = json.optString("apparentTemperature", "34°"),
                dewPoint = json.optString("dewPoint", "22°C"),
                surfacePressure = json.optString("surfacePressure", "1010 hPa"),
                windGusts = json.optString("windGusts", "12 km/h"),
                rainNext24h = json.optString("rainNext24h", "0.0 mm"),
                rainNext48h = json.optString("rainNext48h", "0.0 mm"),
                peakRainTiming = json.optString("peakRainTiming", "No severe rain spells expected."),
                pressureTrend = json.optString("pressureTrend", "Steady"),
                windShiftSummary = json.optString("windShiftSummary", "No significant wind direction change."),
                cloudTrend = json.optString("cloudTrend", "Stable cloud cover."),
                dewPointProximity = json.optString("dewPointProximity", "Safe gap — low condensation risk."),
                rainfallPatternSummary = json.optString("rainfallPatternSummary", "No significant recent rainfall."),
                precipitationWindows = json.optString("precipitationWindows", "No rain windows detected.")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun getCacheAgeMinutes(): Long {
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        if (timestamp == 0L) return Long.MAX_VALUE
        val diffMs = System.currentTimeMillis() - timestamp
        return if (diffMs > 0) diffMs / (1000 * 60) else 0L
    }

    fun isCacheFresh(maxAgeMinutes: Long = 120): Boolean {
        val age = getCacheAgeMinutes()
        return age <= maxAgeMinutes
    }

    fun getCacheAgeString(): String {
        val ageMin = getCacheAgeMinutes()
        return when {
            ageMin == Long.MAX_VALUE -> "Offline (No cache)"
            ageMin < 1 -> "Updated just now"
            ageMin < 60 -> "Updated $ageMin min ago"
            ageMin < 1440 -> "Updated ${ageMin / 60}h ago"
            else -> "Updated ${ageMin / 1440}d ago"
        }
    }

    fun clearCache() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "weathergpt_weather_cache"
        private const val KEY_WEATHER_JSON = "cached_weather_json"
        private const val KEY_TIMESTAMP = "cached_weather_timestamp"
    }
}
