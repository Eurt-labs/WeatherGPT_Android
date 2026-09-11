package com.example.weathergpt_android.domain.auth.data

import android.content.Context
import android.content.SharedPreferences
import com.example.weathergpt_android.domain.auth.model.UserProfile
import com.example.weathergpt_android.domain.auth.model.UserSector

object UserPreferences {
    private const val PREFS_NAME = "weathergpt_user_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_TOKEN = "session_token"
    private const val KEY_CONTACT = "user_contact"
    private const val KEY_CONTACT_TYPE = "user_contact_type"
    private const val KEY_NAME = "user_name"
    private const val KEY_SECTOR = "user_sector"
    private const val KEY_LANGUAGE = "user_language"
    private const val KEY_CROPS = "user_crops"
    private const val KEY_LAND_AREA = "user_land_area"
    private const val KEY_MONITORED_REGION = "user_monitored_region"
    private const val KEY_IS_ONBOARDED = "is_onboarded"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isLoggedIn(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)

    fun isOnboarded(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_IS_ONBOARDED, false) || isLoggedIn(context)

    fun getSessionToken(context: Context): String =
        getPrefs(context).getString(KEY_TOKEN, "") ?: ""

    fun saveAuthSession(
        context: Context,
        userId: String,
        token: String,
        contact: String,
        contactType: String,
        isOnboarded: Boolean = true
    ) {
        getPrefs(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_ONBOARDED, isOnboarded)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_TOKEN, token)
            .putString(KEY_CONTACT, contact)
            .putString(KEY_CONTACT_TYPE, contactType)
            .apply()
    }

    fun saveProfile(context: Context, profile: UserProfile) {
        getPrefs(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_ONBOARDED, true)
            .putString(KEY_USER_ID, profile.userId)
            .putString(KEY_NAME, profile.name)
            .putString(KEY_CONTACT, profile.contact)
            .putString(KEY_CONTACT_TYPE, profile.contactType)
            .putString(KEY_SECTOR, profile.sector.id)
            .putString(KEY_LANGUAGE, profile.preferredLanguage)
            .putString(KEY_CROPS, profile.crops)
            .putString(KEY_LAND_AREA, profile.landArea)
            .putString(KEY_MONITORED_REGION, profile.monitoredRegion)
            .apply()
    }

    fun getProfile(context: Context): UserProfile {
        val prefs = getPrefs(context)
        return UserProfile(
            userId = prefs.getString(KEY_USER_ID, "") ?: "",
            name = prefs.getString(KEY_NAME, "Dhruv") ?: "Dhruv",
            contact = prefs.getString(KEY_CONTACT, "") ?: "",
            contactType = prefs.getString(KEY_CONTACT_TYPE, "email") ?: "email",
            sector = UserSector.fromId(prefs.getString(KEY_SECTOR, "farmer") ?: "farmer"),
            preferredLanguage = prefs.getString(KEY_LANGUAGE, "en") ?: "en",
            crops = prefs.getString(KEY_CROPS, "Wheat, Mustard") ?: "Wheat, Mustard",
            landArea = prefs.getString(KEY_LAND_AREA, "5 Acres") ?: "5 Acres",
            monitoredRegion = prefs.getString(KEY_MONITORED_REGION, "Hathras, Uttar Pradesh") ?: "Hathras, Uttar Pradesh",
            isOnboarded = prefs.getBoolean(KEY_IS_ONBOARDED, false)
        )
    }

    fun updateSector(context: Context, sector: UserSector) {
        getPrefs(context).edit()
            .putString(KEY_SECTOR, sector.id)
            .apply()
    }

    fun updateLanguage(context: Context, languageCode: String) {
        getPrefs(context).edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }

    fun getSelectedVoice(context: Context, langCode: String): String {
        val defaultVoice = com.example.weathergpt_android.domain.voice.model.VoicePersonaCatalog.getOptimalVoiceForLanguage(langCode).id
        return getPrefs(context).getString("user_voice_$langCode", defaultVoice) ?: defaultVoice
    }

    fun setSelectedVoice(context: Context, langCode: String, voiceId: String) {
        getPrefs(context).edit()
            .putString("user_voice_$langCode", voiceId)
            .apply()
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
