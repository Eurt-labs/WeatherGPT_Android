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

    val denseLocationContext: String
        get() = buildString {
            append(cityName)
            if (region.isNotBlank() && !cityName.contains(region)) {
                append(", $region")
            }
            if (country.isNotBlank()) {
                append(", $country")
            }
            if (latitude != 0.0 || longitude != 0.0) {
                append(" (Lat: ${"%.4f".format(latitude)}, Lon: ${"%.4f".format(longitude)})")
            }
        }

    companion object {
        val DEFAULT = LocationData(
            cityName = "New Delhi",
            region = "Delhi",
            country = "India",
            latitude = 28.6139,
            longitude = 77.2090,
            isFromCache = true
        )
    }
}
