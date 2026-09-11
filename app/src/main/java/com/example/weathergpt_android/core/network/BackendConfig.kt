package com.example.weathergpt_android.core.network

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BackendConfig {
    const val CLOUD_URL = "https://weathergpt-backend-m5kk.onrender.com"
    const val BASE_URL = CLOUD_URL
    const val CLIENT_AUTH_KEY = "weathergpt_prod_client_auth_secret_2026"
    const val AUTH_HEADER_NAME = "X-WeatherGPT-Key"

    const val MODE_CLOUD = "cloud"
    const val MODE_ON_DEVICE = "on_device"

    private const val PREFS_NAME = "weathergpt_backend_prefs"
    private const val KEY_BACKEND_MODE = "selected_backend_mode"
    private const val KEY_AUTO_FALLBACK = "auto_fallback_enabled"

    private val _backendModeFlow = MutableStateFlow(MODE_CLOUD)
    val backendModeFlow: StateFlow<String> = _backendModeFlow.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_BACKEND_MODE, MODE_CLOUD) ?: MODE_CLOUD
        _backendModeFlow.value = if (saved == MODE_ON_DEVICE) MODE_ON_DEVICE else MODE_CLOUD
    }

    fun getBaseUrl(context: Context? = null): String {
        return CLOUD_URL
    }

    fun getBackendMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_BACKEND_MODE, MODE_CLOUD) ?: MODE_CLOUD
        val mode = if (raw == MODE_ON_DEVICE) MODE_ON_DEVICE else MODE_CLOUD
        if (_backendModeFlow.value != mode) {
            _backendModeFlow.value = mode
        }
        return mode
    }

    fun setBackendMode(context: Context, mode: String) {
        val cleanMode = if (mode == MODE_ON_DEVICE) MODE_ON_DEVICE else MODE_CLOUD
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_BACKEND_MODE, cleanMode)
            apply()
        }
        _backendModeFlow.value = cleanMode
    }

    fun isOnDeviceMode(context: Context): Boolean {
        return getBackendMode(context) == MODE_ON_DEVICE
    }

    fun isAutoFallbackEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_FALLBACK, true)
    }

    fun setAutoFallback(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_FALLBACK, enabled).apply()
    }
}


