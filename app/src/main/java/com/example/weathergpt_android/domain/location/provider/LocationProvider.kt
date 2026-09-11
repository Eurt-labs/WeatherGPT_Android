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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

class LocationProvider(
    private val context: Context,
    private val cache: LocationCache = LocationCache(context)
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _locationFlow = MutableStateFlow(cache.getCachedLocation())
    val locationFlow: StateFlow<LocationData> = _locationFlow.asStateFlow()

    private var continuousListener: LocationListener? = null
    private var isTracking = false

    fun getInitialCachedLocation(): LocationData {
        return cache.getCachedLocation()
    }

    /**
     * Start continuous real-time location monitoring on GPS & Network providers.
     * Updates [locationFlow] and updates on-device [LocationCache] every time coordinates shift.
     */
    @SuppressLint("MissingPermission")
    fun startContinuousLocationUpdates(scope: CoroutineScope) {
        if (locationManager == null || isTracking) return

        try {
            continuousListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val current = _locationFlow.value
                    // Only re-geocode if distance > 100 meters or location was from cache
                    val dist = FloatArray(1)
                    Location.distanceBetween(
                        current.latitude, current.longitude,
                        location.latitude, location.longitude,
                        dist
                    )
                    if (current.isFromCache || dist[0] > 100f) {
                        reverseGeocodeLocation(location, scope) { resolved ->
                            _locationFlow.value = resolved
                        }
                    }
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

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
                    15000L, // 15 seconds
                    25f,    // 25 meters
                    continuousListener!!,
                    context.mainLooper
                )
            }
            isTracking = true
        } catch (_: SecurityException) {
            // Permission denied or missing
        } catch (_: Exception) {}
    }

    fun stopLocationUpdates() {
        continuousListener?.let {
            try {
                locationManager?.removeUpdates(it)
            } catch (_: Exception) {}
        }
        continuousListener = null
        isTracking = false
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
                reverseGeocodeLocation(lastBestLocation, scope) { resolved ->
                    _locationFlow.value = resolved
                    onLocationResolved(resolved)
                }
            }

            // Register single-update listener for fresh GPS/Network fix
            val singleUpdateListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    reverseGeocodeLocation(location, scope) { resolved ->
                        _locationFlow.value = resolved
                        onLocationResolved(resolved)
                    }
                    try {
                        locationManager.removeUpdates(this)
                    } catch (_: Exception) {}
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

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
        } catch (_: SecurityException) {
            val cached = cache.getCachedLocation()
            _locationFlow.value = cached
            onLocationResolved(cached)
        } catch (_: Exception) {
            val cached = cache.getCachedLocation()
            _locationFlow.value = cached
            onLocationResolved(cached)
        }
    }

    /**
     * Request a fresh location asynchronously. Always updates disk cache and location flow.
     */
    @SuppressLint("MissingPermission")
    suspend fun requestFreshLocation(): LocationData = withContext(Dispatchers.IO) {
        if (locationManager == null) return@withContext cache.getCachedLocation()

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
                val resolved = resolveLocationSync(lastBestLocation)
                cache.saveLocation(resolved)
                _locationFlow.value = resolved
                return@withContext resolved
            }
        } catch (_: Exception) {}

        cache.getCachedLocation()
    }

    private fun reverseGeocodeLocation(
        location: Location,
        scope: CoroutineScope,
        onLocationResolved: (LocationData) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            val resolved = resolveLocationSync(location)
            cache.saveLocation(resolved)
            _locationFlow.value = resolved
            withContext(Dispatchers.Main) {
                onLocationResolved(resolved)
            }
        }
    }

    private suspend fun resolveLocationSync(location: Location): LocationData {
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
                    val address = withTimeoutOrNull(2500L) {
                        suspendCancellableCoroutine<Address?> { cont ->
                            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                cont.resume(addresses.firstOrNull())
                            }
                        }
                    }
                    if (address != null) {
                        cityName = extractCityName(address)
                        regionName = address.adminArea ?: address.subAdminArea ?: ""
                        countryName = address.countryName ?: ""
                    }
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
        } catch (_: Exception) {
            cityName = ""
        }

        val fallbackName = "Loc (${"%.2f".format(location.latitude)}, ${"%.2f".format(location.longitude)})"
        return LocationData(
            cityName = cityName.ifBlank { fallbackName },
            region = regionName,
            country = countryName,
            latitude = location.latitude,
            longitude = location.longitude,
            isFromCache = false,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }
}
