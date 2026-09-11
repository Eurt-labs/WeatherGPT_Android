package com.example.weathergpt_android.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reactive network connectivity monitor for WeatherGPT.
 * Emits real-time online/offline status changes to trigger immediate context refresh.
 */
class NetworkMonitor private constructor(context: Context) {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInitialConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        registerNetworkCallback()
    }

    private fun checkInitialConnectivity(): Boolean {
        return try {
            val cm = connectivityManager ?: return true
            val active = cm.activeNetwork ?: return true
            val caps = cm.getNetworkCapabilities(active) ?: return true
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Throwable) {
            android.util.Log.w("NetworkMonitor", "Could not query network capabilities", e)
            true
        }
    }

    private fun registerNetworkCallback() {
        val cm = connectivityManager ?: return
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                }

                override fun onLost(network: Network) {
                    _isOnline.value = checkInitialConnectivity()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    _isOnline.value = hasInternet
                }
            })
        } catch (e: Throwable) {
            android.util.Log.w("NetworkMonitor", "Could not register network callback", e)
        }
    }

    companion object {
        @Volatile
        private var instance: NetworkMonitor? = null

        fun getInstance(context: Context): NetworkMonitor {
            return instance ?: synchronized(this) {
                instance ?: NetworkMonitor(context.applicationContext).also { instance = it }
            }
        }

        fun isOnline(context: Context): Boolean {
            return getInstance(context).isOnline.value
        }

        fun isOnlineFlow(context: Context): StateFlow<Boolean> {
            return getInstance(context).isOnline
        }
    }
}
