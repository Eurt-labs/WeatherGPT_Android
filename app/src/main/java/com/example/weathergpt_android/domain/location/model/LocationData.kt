package com.example.weathergpt_android.domain.location.model

data class LocationData(
    val cityName: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isFromCache: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
) {
    val formattedLocation: String
        get() = when {
            region.isNotBlank() && cityName.isNotBlank() -> "$cityName, $region"
            cityName.isNotBlank() -> cityName
            else -> "Detecting Location..."
        }

    companion object {
        val DEFAULT = LocationData(
            cityName = "San Francisco",
            region = "CA",
            country = "USA",
            latitude = 37.7749,
            longitude = -122.4194,
            isFromCache = true
        )
    }
}
