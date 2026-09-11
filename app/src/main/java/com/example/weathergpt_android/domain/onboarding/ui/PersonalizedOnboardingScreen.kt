package com.example.weathergpt_android.domain.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.example.weathergpt_android.core.network.BackendConfig
import com.example.weathergpt_android.domain.inference.download.ModelDownloadService
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.AmbientGlowBackground
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.core.theme.FrostedGlassTokens
import com.example.weathergpt_android.domain.auth.data.UserPreferences
import com.example.weathergpt_android.domain.auth.model.UserProfile
import com.example.weathergpt_android.domain.auth.model.UserSector
import com.example.weathergpt_android.domain.auth.network.AuthApiService
import kotlinx.coroutines.launch

/**
 * 3-Step Personalization Startup Questionnaire.
 * Step 1: Language First (English, Hindi, Marathi, Bengali, Tamil, Telugu).
 * Step 2: Primary Sector / Focus (Farmer, Disaster, Commuter, Aviation).
 * Step 3: Name & Custom Sector Attributes (Crops, Land Area, Region).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalizedOnboardingScreen(
    currentTheme: AppThemeMode,
    initialProfile: UserProfile,
    onComplete: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authService = remember { AuthApiService(context) }

    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    var currentStep by remember { mutableIntStateOf(1) } // 1: Language First, 2: Sector, 3: Details, 4: AI Mode
    var selectedLanguage by remember { mutableStateOf(initialProfile.preferredLanguage.ifBlank { "en" }) }
    var selectedSector by remember { mutableStateOf(initialProfile.sector) }
    var userName by remember { mutableStateOf(initialProfile.name.ifBlank { "Dhruv" }) }
    var selectedCrops by remember { mutableStateOf(initialProfile.crops.ifBlank { "Wheat, Mustard" }) }
    var landArea by remember { mutableStateOf(initialProfile.landArea.ifBlank { "5 Acres" }) }
    var monitoredRegion by remember { mutableStateOf(initialProfile.monitoredRegion.ifBlank { "Hathras, Uttar Pradesh" }) }
    var preferOfflineAi by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val textColor = if (isDark) Color.White else Color(0xFF111113)
    val subtitleColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
    val accentBeige = if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF)

    AmbientGlowBackground(
        currentTheme = currentTheme,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Progress Indicator (Step 1 of 4)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step $currentStep of 4",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentBeige,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Personalized Setup",
                        fontSize = 11.sp,
                        color = subtitleColor
                    )
                }

                // Visual Step Progress Bar (4 segments)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StepBarSegment(isActive = currentStep >= 1, modifier = Modifier.weight(1f))
                    StepBarSegment(isActive = currentStep >= 2, modifier = Modifier.weight(1f))
                    StepBarSegment(isActive = currentStep >= 3, modifier = Modifier.weight(1f))
                    StepBarSegment(isActive = currentStep >= 4, modifier = Modifier.weight(1f))
                }
            }

            // Step Body with Smooth Crossfade
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "onboarding_step"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (step) {
                        1 -> {
                            // Step 1: Language First!
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Choose Your Language",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "Choose how WeatherGPT will speak, listen, and explain weather to you.",
                                    fontSize = 13.sp,
                                    color = subtitleColor
                                )
                            }

                            // Interactive Language Selection Cards
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                LanguageSelectCard(
                                    code = "en",
                                    name = "English",
                                    nativeName = "English",
                                    isSelected = selectedLanguage == "en",
                                    onClick = { selectedLanguage = "en" },
                                    isDark = isDark
                                )
                                LanguageSelectCard(
                                    code = "hi",
                                    name = "Hindi",
                                    nativeName = "हिन्दी",
                                    isSelected = selectedLanguage == "hi",
                                    onClick = { selectedLanguage = "hi" },
                                    isDark = isDark
                                )
                                LanguageSelectCard(
                                    code = "mr",
                                    name = "Marathi",
                                    nativeName = "मराठी",
                                    isSelected = selectedLanguage == "mr",
                                    onClick = { selectedLanguage = "mr" },
                                    isDark = isDark
                                )
                                LanguageSelectCard(
                                    code = "bn",
                                    name = "Bengali",
                                    nativeName = "বাংলা",
                                    isSelected = selectedLanguage == "bn",
                                    onClick = { selectedLanguage = "bn" },
                                    isDark = isDark
                                )
                                LanguageSelectCard(
                                    code = "ta",
                                    name = "Tamil",
                                    nativeName = "தமிழ்",
                                    isSelected = selectedLanguage == "ta",
                                    onClick = { selectedLanguage = "ta" },
                                    isDark = isDark
                                )
                                LanguageSelectCard(
                                    code = "te",
                                    name = "Telugu",
                                    nativeName = "తెలుగు",
                                    isSelected = selectedLanguage == "te",
                                    onClick = { selectedLanguage = "te" },
                                    isDark = isDark
                                )
                            }
                        }

                        2 -> {
                            // Step 2: Sector / Role Selection
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "What is your primary focus?",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "We customize forecasts, soil models, and AI reasoning to your role.",
                                    fontSize = 13.sp,
                                    color = subtitleColor
                                )
                            }

                            SectorSelectCard(
                                title = UserSector.FARMER.title,
                                subtitle = UserSector.FARMER.subtitle,
                                icon = Icons.Rounded.Grass,
                                isSelected = selectedSector == UserSector.FARMER,
                                onClick = { selectedSector = UserSector.FARMER },
                                isDark = isDark
                            )

                            SectorSelectCard(
                                title = UserSector.DISASTER_OFFICER.title,
                                subtitle = UserSector.DISASTER_OFFICER.subtitle,
                                icon = Icons.Rounded.Thunderstorm,
                                isSelected = selectedSector == UserSector.DISASTER_OFFICER,
                                onClick = { selectedSector = UserSector.DISASTER_OFFICER },
                                isDark = isDark
                            )

                            SectorSelectCard(
                                title = UserSector.COMMUTER.title,
                                subtitle = UserSector.COMMUTER.subtitle,
                                icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                                isSelected = selectedSector == UserSector.COMMUTER,
                                onClick = { selectedSector = UserSector.COMMUTER },
                                isDark = isDark
                            )

                            SectorSelectCard(
                                title = UserSector.AVIATION_LOGISTICS.title,
                                subtitle = UserSector.AVIATION_LOGISTICS.subtitle,
                                icon = Icons.Rounded.Flight,
                                isSelected = selectedSector == UserSector.AVIATION_LOGISTICS,
                                onClick = { selectedSector = UserSector.AVIATION_LOGISTICS },
                                isDark = isDark
                            )
                        }

                        3 -> {
                            // Step 3: Name & Custom Sector Attributes
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Tell us about yourself",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "Enter your name and details so WeatherGPT can personalize every alert.",
                                    fontSize = 13.sp,
                                    color = subtitleColor
                                )
                            }

                            // Name Input
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = FrostedGlassTokens.surface(isDark),
                                border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = accentBeige,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(text = "YOUR NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
                                        BasicTextField(
                                            value = userName,
                                            onValueChange = { userName = it },
                                            textStyle = TextStyle(color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                            cursorBrush = SolidColor(accentBeige),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            when (selectedSector) {
                                UserSector.FARMER -> {
                                    // Crop Selector Pills
                                    Text(text = "PRIMARY CROPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val commonCrops = listOf("Wheat", "Mustard", "Rice", "Cotton", "Potato", "Sugarcane", "Maize", "Soybean")
                                        val activeList = selectedCrops.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                        commonCrops.forEach { crop ->
                                            val isChosen = activeList.contains(crop)
                                            CropChip(
                                                label = crop,
                                                isSelected = isChosen,
                                                onClick = {
                                                    val updated = if (isChosen) {
                                                        activeList.filter { it != crop }
                                                    } else {
                                                        activeList + crop
                                                    }
                                                    selectedCrops = updated.joinToString(", ")
                                                },
                                                isDark = isDark
                                            )
                                        }
                                    }

                                    // Land Area
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        color = FrostedGlassTokens.surface(isDark),
                                        border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Place,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column {
                                                Text(text = "FARM / LAND AREA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
                                                BasicTextField(
                                                    value = landArea,
                                                    onValueChange = { landArea = it },
                                                    textStyle = TextStyle(color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                                    cursorBrush = SolidColor(accentBeige),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    // Monitored Region Input
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        color = FrostedGlassTokens.surface(isDark),
                                        border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Place,
                                                contentDescription = null,
                                                tint = accentBeige,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column {
                                                Text(text = "MONITORED REGION / CITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
                                                BasicTextField(
                                                    value = monitoredRegion,
                                                    onValueChange = { monitoredRegion = it },
                                                    textStyle = TextStyle(color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                                    cursorBrush = SolidColor(accentBeige),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        4 -> {
                            // Step 4: AI Operating Preference (Offline vs Cloud)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "AI Operating Mode",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "Choose whether you prefer cloud intelligence or offline on-device processing.",
                                    fontSize = 13.sp,
                                    color = subtitleColor
                                )
                            }

                            SectorSelectCard(
                                title = "Cloud AI (Google Gemini 3.6 Flash)",
                                subtitle = "Zero storage required. Ultra-fast real-time inference via cloud API (Recommended if you have internet).",
                                icon = Icons.Rounded.Cloud,
                                isSelected = !preferOfflineAi,
                                onClick = { preferOfflineAi = false },
                                isDark = isDark
                            )

                            SectorSelectCard(
                                title = "100% On-Device Offline AI",
                                subtitle = "Zero network dependency. Downloads compact on-device SLM (~1.1 GB) in the background with live notification.",
                                icon = Icons.Rounded.PhoneAndroid,
                                isSelected = preferOfflineAi,
                                onClick = { preferOfflineAi = true },
                                isDark = isDark
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = { currentStep -= 1 },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(FrostedGlassTokens.surfaceSubtle(isDark))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(50.dp))
                }

                val btnBg = if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)
                val btnContent = if (isDark) Color(0xFF121214) else Color.White

                Button(
                    onClick = {
                        if (currentStep < 4) {
                            currentStep += 1
                        } else {
                            // Finish and Save Profile
                            isSaving = true
                            val completedProfile = initialProfile.copy(
                                name = userName.ifBlank { "Dhruv" },
                                sector = selectedSector,
                                preferredLanguage = selectedLanguage,
                                crops = selectedCrops,
                                landArea = landArea,
                                monitoredRegion = monitoredRegion,
                                isOnboarded = true
                            )
                            if (preferOfflineAi) {
                                BackendConfig.setBackendMode(context, BackendConfig.MODE_ON_DEVICE)
                                ModelDownloadService.start(context)
                            } else {
                                BackendConfig.setBackendMode(context, BackendConfig.MODE_CLOUD)
                            }
                            scope.launch {
                                UserPreferences.saveProfile(context, completedProfile)
                                if (completedProfile.userId.isNotBlank()) {
                                    authService.saveUserProfile(completedProfile)
                                }
                                onComplete(completedProfile)
                            }
                        }
                    },
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .padding(start = 12.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = btnBg,
                        contentColor = btnContent
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = btnContent, modifier = Modifier.size(20.dp))
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (currentStep == 4) "Launch WeatherGPT 🚀" else "Continue",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (currentStep == 4) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageSelectCard(
    code: String,
    name: String,
    nativeName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val bg = if (isSelected) {
        if (isDark) Color(0x28E8E3D5) else Color(0xFF18181B)
    } else {
        FrostedGlassTokens.surfaceSubtle(isDark)
    }

    val border = if (isSelected) {
        if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)
    } else {
        FrostedGlassTokens.borderSubtle(isDark)
    }

    val accent = if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = bg,
        border = BorderStroke(1.5.dp, border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) (if (isDark) Color(0xFFE8E3D5) else Color(0xFF27272A)) else FrostedGlassTokens.surfaceSubtle(isDark)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = null,
                        tint = if (isSelected) (if (isDark) Color(0xFF121214) else Color.White) else if (isDark) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = nativeName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) (if (isDark) Color.White else Color.White) else if (isDark) Color.White else Color(0xFF111113)
                    )
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        color = if (isSelected) accent else if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF121214) else Color(0xFF18181B),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepBarSegment(isActive: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .then(
                if (isActive) {
                    Modifier.background(Color(0xFFE8E3D5))
                } else {
                    Modifier.background(Color(0x25FFFFFF))
                }
            )
    )
}

@Composable
private fun SectorSelectCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val bg = if (isSelected) {
        if (isDark) Color(0x28E8E3D5) else Color(0xFF18181B)
    } else {
        FrostedGlassTokens.surfaceSubtle(isDark)
    }

    val border = if (isSelected) {
        if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)
    } else {
        FrostedGlassTokens.borderSubtle(isDark)
    }

    val accent = if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = BorderStroke(1.5.dp, border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) (if (isDark) Color(0xFFE8E3D5) else Color(0xFF27272A)) else FrostedGlassTokens.surfaceSubtle(isDark)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) (if (isDark) Color(0xFF121214) else Color.White) else if (isDark) Color.White else Color(0xFF334155),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) (if (isDark) Color.White else Color.White) else if (isDark) Color.White else Color(0xFF111113)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isSelected) accent else if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF121214) else Color(0xFF18181B),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CropChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val bg = if (isSelected) Color(0xFF10B981) else FrostedGlassTokens.surfaceSubtle(isDark)
    val textColor = if (isSelected) Color.White else if (isDark) Color.White else Color(0xFF1E293B)

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF10B981) else FrostedGlassTokens.borderSubtle(isDark))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        }
    }
}
