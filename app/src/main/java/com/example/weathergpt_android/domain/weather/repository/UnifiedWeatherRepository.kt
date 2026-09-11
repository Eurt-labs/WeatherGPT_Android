package com.example.weathergpt_android.domain.weather.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 100% Pure Open-Meteo Unified Weather Repository.
 * Zero external keys required, 10,000 free high-resolution calls/day,
 * CPCB AQI, soil moisture, flood risk, and aviation metrics.
 */
class UnifiedWeatherRepository(private val context: Context) {
    private val openMeteoRepo = OpenMeteoRepository()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("weathergpt_live_cache", Context.MODE_PRIVATE)

    fun getCachedWeather(): LiveWeatherData {
        val temp = prefs.getString("cached_temp", "29°") ?: "29°"
        val cond = prefs.getString("cached_cond", "Partly Cloudy") ?: "Partly Cloudy"
        val hl = prefs.getString("cached_hl", "H: 32°  L: 24°") ?: "H: 32°  L: 24°"
        val wind = prefs.getString("cached_wind", "12 km/h") ?: "12 km/h"
        val hum = prefs.getString("cached_hum", "65%") ?: "65%"
        val uv = prefs.getString("cached_uv", "6 (High)") ?: "6 (High)"
        val aqi = prefs.getString("cached_aqi", "68 (Moderate)") ?: "68 (Moderate)"
        val soil = prefs.getString("cached_soil", "0.33 m³/m³") ?: "0.33 m³/m³"
        val rootSoil = prefs.getString("cached_root_soil", "0.35 m³/m³") ?: "0.35 m³/m³"
        val soilTemp = prefs.getString("cached_soil_temp", "26°C") ?: "26°C"
        val et0 = prefs.getString("cached_et0", "4.2 mm/day") ?: "4.2 mm/day"
        val irrig = prefs.getString("cached_irrig", "Moderate moisture. Monitor topsoil before irrigating.") ?: "Moderate moisture."
        val flood = prefs.getString("cached_flood", "Normal (Low Risk)") ?: "Normal (Low Risk)"
        val waterlog = prefs.getString("cached_waterlog", "Low") ?: "Low"
        val river = prefs.getString("cached_river", "12.0 m³/s") ?: "12.0 m³/s"
        val vis = prefs.getString("cached_vis", "10.0 km") ?: "10.0 km"
        val rain24h = prefs.getString("cached_rain24h", "0.0 mm") ?: "0.0 mm"
        val rain48h = prefs.getString("cached_rain48h", "0.0 mm") ?: "0.0 mm"
        val peakRain = prefs.getString("cached_peak_rain", "No severe rain spells expected.") ?: "No severe rain spells expected."
        val appTemp = prefs.getString("cached_app_temp", "31°") ?: "31°"
        val dew = prefs.getString("cached_dew", "21°C") ?: "21°C"
        val pressure = prefs.getString("cached_pressure", "1012 hPa") ?: "1012 hPa"
        val gusts = prefs.getString("cached_gusts", "18 km/h") ?: "18 km/h"
        val hasCached = prefs.contains("cached_temp")

        return LiveWeatherData(
            temperature = temp,
            condition = cond,
            highLow = hl,
            windSpeed = wind,
            humidity = hum,
            uvIndex = uv,
            aqi = aqi,
            soilMoisture = soil,
            rootZoneSoilMoisture = rootSoil,
            soilTemperature = soilTemp,
            evapotranspiration = et0,
            irrigationAdvice = irrig,
            floodRiskLevel = flood,
            waterloggingRisk = waterlog,
            riverDischarge = river,
            visibilityKm = vis,
            rainNext24h = rain24h,
            rainNext48h = rain48h,
            peakRainTiming = peakRain,
            apparentTemperature = appTemp,
            dewPoint = dew,
            surfacePressure = pressure,
            windGusts = gusts,
            isLive = hasCached
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
            .putString("cached_soil", data.soilMoisture)
            .putString("cached_root_soil", data.rootZoneSoilMoisture)
            .putString("cached_soil_temp", data.soilTemperature)
            .putString("cached_et0", data.evapotranspiration)
            .putString("cached_irrig", data.irrigationAdvice)
            .putString("cached_flood", data.floodRiskLevel)
            .putString("cached_waterlog", data.waterloggingRisk)
            .putString("cached_river", data.riverDischarge)
            .putString("cached_vis", data.visibilityKm)
            .putString("cached_rain24h", data.rainNext24h)
            .putString("cached_rain48h", data.rainNext48h)
            .putString("cached_peak_rain", data.peakRainTiming)
            .putString("cached_app_temp", data.apparentTemperature)
            .putString("cached_dew", data.dewPoint)
            .putString("cached_pressure", data.surfacePressure)
            .putString("cached_gusts", data.windGusts)
            .putLong("cached_time", System.currentTimeMillis())
            .apply()
    }

    suspend fun fetchLiveWeather(
        latitude: Double,
        longitude: Double
    ): Result<LiveWeatherData> = withContext(Dispatchers.IO) {
        val result = openMeteoRepo.fetchWeather(latitude, longitude)
        result.onSuccess { data ->
            cacheWeather(data)
        }
        result
    }
}
