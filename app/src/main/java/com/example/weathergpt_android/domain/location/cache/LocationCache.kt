package com.example.weathergpt_android.domain.location.cache

import android.content.Context
import android.content.SharedPreferences
import com.example.weathergpt_android.domain.location.model.LocationData

class LocationCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCachedLocation(): LocationData {
        val city = prefs.getString(KEY_CITY, LocationData.DEFAULT.cityName) ?: LocationData.DEFAULT.cityName
        val region = prefs.getString(KEY_REGION, LocationData.DEFAULT.region) ?: LocationData.DEFAULT.region
        val country = prefs.getString(KEY_COUNTRY, LocationData.DEFAULT.country) ?: LocationData.DEFAULT.country
        val lat = prefs.getFloat(KEY_LAT, LocationData.DEFAULT.latitude.toFloat()).toDouble()
        val lon = prefs.getFloat(KEY_LON, LocationData.DEFAULT.longitude.toFloat()).toDouble()
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)

        return LocationData(
            cityName = city,
            region = region,
            country = country,
            latitude = lat,
            longitude = lon,
            isFromCache = true,
            lastUpdatedTimestamp = timestamp
        )
    }

    fun saveLocation(location: LocationData) {
        prefs.edit()
            .putString(KEY_CITY, location.cityName)
            .putString(KEY_REGION, location.region)
            .putString(KEY_COUNTRY, location.country)
            .putFloat(KEY_LAT, location.latitude.toFloat())
            .putFloat(KEY_LON, location.longitude.toFloat())
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "weathergpt_location_cache"
        private const val KEY_CITY = "cached_city"
        private const val KEY_REGION = "cached_region"
        private const val KEY_COUNTRY = "cached_country"
        private const val KEY_LAT = "cached_lat"
        private const val KEY_LON = "cached_lon"
        private const val KEY_TIMESTAMP = "cached_timestamp"
    }
}
