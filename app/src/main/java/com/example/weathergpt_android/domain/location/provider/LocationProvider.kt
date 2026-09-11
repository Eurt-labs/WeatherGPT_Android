package com.example.weathergpt_android.domain.location.provider

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import com.example.weathergpt_android.domain.location.cache.LocationCache
import com.example.weathergpt_android.domain.location.model.LocationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationProvider(
    private val context: Context,
    private val cache: LocationCache = LocationCache(context)
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun getInitialCachedLocation(): LocationData {
        return cache.getCachedLocation()
    }

    @SuppressLint("MissingPermission")
    fun fetchRealtimeLocation(
        scope: CoroutineScope,
        onLocationResolved: (LocationData) -> Unit
    ) {
        if (locationManager == null) return

        try {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            var lastBestLocation: Location? = null

            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null && (lastBestLocation == null || loc.accuracy < lastBestLocation.accuracy)) {
                        lastBestLocation = loc
                    }
                }
            }

            if (lastBestLocation != null) {
                reverseGeocodeLocation(lastBestLocation, scope, onLocationResolved)
            }

            // Register single-update listener for fresh GPS/Network fix
            val singleUpdateListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    reverseGeocodeLocation(location, scope, onLocationResolved)
                    locationManager.removeUpdates(this)
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            // Register listeners on both GPS (pinpoint meter accuracy) and Network (fast initial fix)
            val activeProviders = mutableListOf<String>()
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                activeProviders.add(LocationManager.GPS_PROVIDER)
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                activeProviders.add(LocationManager.NETWORK_PROVIDER)
            }

            for (prov in activeProviders) {
                locationManager.requestLocationUpdates(
                    prov,
                    3000L,
                    5f,
                    singleUpdateListener,
                    context.mainLooper
                )
            }
        } catch (e: SecurityException) {
            // Permissions not yet granted, fallback to cached
            onLocationResolved(cache.getCachedLocation())
        } catch (e: Exception) {
            onLocationResolved(cache.getCachedLocation())
        }
    }

    private fun reverseGeocodeLocation(
        location: Location,
        scope: CoroutineScope,
        onLocationResolved: (LocationData) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            var cityName = ""
            var regionName = ""
            var countryName = ""

            fun extractCityName(address: Address?): String {
                if (address == null) return ""
                return address.locality
                    ?: address.subLocality
                    ?: address.subAdminArea
                    ?: address.adminArea
                    ?: address.featureName
                    ?: ""
            }

            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            val address = addresses.firstOrNull()
                            if (address != null) {
                                cityName = extractCityName(address)
                                regionName = address.adminArea ?: address.subAdminArea ?: ""
                                countryName = address.countryName ?: ""
                            }
                            val fallbackName = "Loc (${"%.2f".format(location.latitude)}, ${"%.2f".format(location.longitude)})"
                            val resolved = LocationData(
                                cityName = cityName.ifBlank { fallbackName },
                                region = regionName,
                                country = countryName,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                isFromCache = false,
                                lastUpdatedTimestamp = System.currentTimeMillis()
                            )
                            cache.saveLocation(resolved)
                            scope.launch(Dispatchers.Main) {
                                onLocationResolved(resolved)
                            }
                        }
                        return@launch
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val address = addresses?.firstOrNull()
                        if (address != null) {
                            cityName = extractCityName(address)
                            regionName = address.adminArea ?: address.subAdminArea ?: ""
                            countryName = address.countryName ?: ""
                        }
                    }
                }
            } catch (e: Exception) {
                cityName = ""
            }

            val fallbackName = "Loc (${"%.2f".format(location.latitude)}, ${"%.2f".format(location.longitude)})"
            val resolved = LocationData(
                cityName = cityName.ifBlank { fallbackName },
                region = regionName,
                country = countryName,
                latitude = location.latitude,
                longitude = location.longitude,
                isFromCache = false,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )

            cache.saveLocation(resolved)

            withContext(Dispatchers.Main) {
                onLocationResolved(resolved)
            }
        }
    }
}
