package com.example.weathergpt_android.domain.auth.model

enum class UserSector(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String
) {
    FARMER(
        id = "farmer",
        title = "Farmer / Kisan",
        subtitle = "Soil moisture, irrigation timing, and crop advisory",
        tag = "🌾 Kisan AI"
    ),
    DISASTER_OFFICER(
        id = "disaster",
        title = "Disaster Relief Officer",
        subtitle = "River discharge, flash floods, and severe storm radar",
        tag = "🚨 Disaster Radar"
    ),
    COMMUTER(
        id = "commuter",
        title = "Daily Commuter",
        subtitle = "Hourly precipitation, air quality index, and transit alerts",
        tag = "🏙️ City Commute"
    ),
    AVIATION_LOGISTICS(
        id = "aviation",
        title = "Aviation & Logistics",
        subtitle = "Crosswind gusts, flight visibility, and cloud ceilings",
        tag = "✈️ Flight & Cargo"
    );

    companion object {
        fun fromId(id: String): UserSector =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FARMER
    }
}

data class UserProfile(
    val userId: String = "",
    val name: String = "Dhruv",
    val contact: String = "",
    val contactType: String = "email", // "email" or "phone"
    val sector: UserSector = UserSector.FARMER,
    val preferredLanguage: String = "en", // "en", "hi", "mr", etc.
    val crops: String = "Wheat, Mustard",
    val landArea: String = "5 Acres",
    val monitoredRegion: String = "Hathras, Uttar Pradesh",
    val isOnboarded: Boolean = false
)
