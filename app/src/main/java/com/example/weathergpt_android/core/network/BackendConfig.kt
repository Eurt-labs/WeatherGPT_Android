package com.example.weathergpt_android.core.network

import android.content.Context

object BackendConfig {
    const val CLOUD_URL = "https://weathergpt-backend-m5kk.onrender.com"
    const val LOCAL_USB_URL = "http://localhost:8000"
    const val LOCAL_EMULATOR_URL = "http://10.0.2.2:8000"

    // Default fallback constant kept for backward compatibility
    const val BASE_URL = CLOUD_URL
    const val CLIENT_AUTH_KEY = "weathergpt_prod_client_auth_secret_2026"
    const val AUTH_HEADER_NAME = "X-WeatherGPT-Key"

    const val MODE_CLOUD = "cloud"
    const val MODE_LOCAL_USB = "local_usb"
    const val MODE_LOCAL_EMULATOR = "local_emulator"
    const val MODE_LOCAL_CUSTOM = "local_custom"
    const val MODE_ON_DEVICE = "on_device"

    private const val PREFS_NAME = "weathergpt_backend_prefs"
    private const val KEY_BACKEND_MODE = "selected_backend_mode"
    private const val KEY_CUSTOM_URL = "custom_local_url"
    private const val KEY_AUTO_FALLBACK = "auto_fallback_enabled"

    fun getBaseUrl(context: Context? = null): String {
        if (context == null) return BASE_URL
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return when (prefs.getString(KEY_BACKEND_MODE, MODE_CLOUD)) {
            MODE_LOCAL_USB -> LOCAL_USB_URL
            MODE_LOCAL_EMULATOR -> LOCAL_EMULATOR_URL
            MODE_LOCAL_CUSTOM -> {
                val custom = prefs.getString(KEY_CUSTOM_URL, LOCAL_USB_URL)?.trim()
                if (!custom.isNullOrEmpty()) custom.removeSuffix("/") else LOCAL_USB_URL
            }
            else -> CLOUD_URL
        }
    }

    fun getBackendMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BACKEND_MODE, MODE_CLOUD) ?: MODE_CLOUD
    }

    fun setBackendMode(context: Context, mode: String, customUrl: String? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_BACKEND_MODE, mode)
            if (customUrl != null) {
                putString(KEY_CUSTOM_URL, customUrl.trim().removeSuffix("/"))
            }
            apply()
        }
    }

    fun getCustomUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_URL, "http://192.168.1.100:8000") ?: "http://192.168.1.100:8000"
    }

    fun isLocalMode(context: Context): Boolean {
        val mode = getBackendMode(context)
        return mode != MODE_CLOUD && mode != MODE_ON_DEVICE
    }

    fun isOnDeviceMode(context: Context): Boolean {
        return getBackendMode(context) == MODE_ON_DEVICE
    }

    fun isAutoFallbackEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_FALLBACK, true) // Enabled by default
    }

    fun setAutoFallback(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_FALLBACK, enabled).apply()
    }
}

