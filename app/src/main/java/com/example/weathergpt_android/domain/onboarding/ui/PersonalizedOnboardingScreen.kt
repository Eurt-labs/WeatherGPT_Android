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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
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
import com.example.weathergpt_android.domain.auth.data.UserPreferences
import com.example.weathergpt_android.domain.auth.model.UserProfile
import com.example.weathergpt_android.domain.auth.model.UserSector
import com.example.weathergpt_android.domain.auth.network.AuthApiService
import kotlinx.coroutines.launch

/**
 * Multi-Step Personalized Onboarding Questionnaire (SIH Problem Statement 26068).
 * Analyzes user type and configures tailored AI recommendations, widgets, and language.
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

    var currentStep by remember { mutableIntStateOf(1) } // 1: Sector, 2: Name/Language, 3: Specifics
    var selectedSector by remember { mutableStateOf(initialProfile.sector) }
    var userName by remember { mutableStateOf(initialProfile.name) }
    var selectedLanguage by remember { mutableStateOf("en") }
    var selectedCrops by remember { mutableStateOf("Wheat, Mustard") }
    var landArea by remember { mutableStateOf("5 Acres") }
    var monitoredRegion by remember { mutableStateOf("Hathras, Uttar Pradesh") }
    var isSaving by remember { mutableStateOf(false) }

    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)
    val neonPurple = Color(0xFF9333EA)
    val neonMagenta = Color(0xFFC026D3)
    val neonCoral = Color(0xFFFF5722)

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
            // Header with Progress Indicator (Step 1 of 3)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step $currentStep of 3",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = neonMagenta,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "SIH-26068 Personalization",
                        fontSize = 11.sp,
                        color = subtitleColor
                    )
                }

                // Visual Step Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StepBarSegment(isActive = currentStep >= 1, modifier = Modifier.weight(1f))
                    StepBarSegment(isActive = currentStep >= 2, modifier = Modifier.weight(1f))
                    StepBarSegment(isActive = currentStep >= 3, modifier = Modifier.weight(1f))
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
                            // Step 1: Sector / Role Selection
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "What is your primary focus?",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "We customize forecasts, advisories, and AI reasoning to your role.",
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
                                icon = Icons.Rounded.DirectionsWalk,
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

                        2 -> {
                            // Step 2: Name & Language
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Personal Details",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "How should WeatherGPT address you and speak with you?",
                                    fontSize = 13.sp,
                                    color = subtitleColor
                                )
                            }

                            // Name Input
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isDark) Color(0x35FFFFFF) else Color(0x60CBD5E1))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = neonMagenta,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(text = "YOUR NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
                                        BasicTextField(
                                            value = userName,
                                            onValueChange = { userName = it },
                                            textStyle = TextStyle(color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                            cursorBrush = SolidColor(neonMagenta),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // Language Selection
                            Text(
                                text = "PREFERRED LANGUAGE FOR VOICE & CHAT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = subtitleColor,
                                letterSpacing = 1.sp
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LanguagePill(code = "en", label = "English", isSelected = selectedLanguage == "en", onSelect = { selectedLanguage = "en" }, isDark = isDark)
                                LanguagePill(code = "hi", label = "हिन्दी (Hindi)", isSelected = selectedLanguage == "hi", onSelect = { selectedLanguage = "hi" }, isDark = isDark)
                                LanguagePill(code = "mr", label = "मराठी (Marathi)", isSelected = selectedLanguage == "mr", onSelect = { selectedLanguage = "mr" }, isDark = isDark)
                                LanguagePill(code = "bn", label = "বাংলা (Bengali)", isSelected = selectedLanguage == "bn", onSelect = { selectedLanguage = "bn" }, isDark = isDark)
                                LanguagePill(code = "ta", label = "தமிழ் (Tamil)", isSelected = selectedLanguage == "ta", onSelect = { selectedLanguage = "ta" }, isDark = isDark)
                            }
                        }

                        3 -> {
                            // Step 3: Sector Specifics
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Tailor Your Advisory",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = when (selectedSector) {
                                        UserSector.FARMER -> "Configure crop types & acreage for precise irrigation models."
                                        UserSector.DISASTER_OFFICER -> "Set high-priority river basins or alert regions."
                                        UserSector.COMMUTER -> "Set primary commute route and travel window."
                                        UserSector.AVIATION_LOGISTICS -> "Set regional hubs for wind and visibility briefs."
                                    },
                                    fontSize = 13.sp,
                                    color = subtitleColor
                                )
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
                                        val commonCrops = listOf("Wheat", "Mustard", "Rice", "Cotton", "Potato", "Sugarcane")
                                        commonCrops.forEach { crop ->
                                            val isSelected = selectedCrops.contains(crop)
                                            CropOptionPill(
                                                crop = crop,
                                                isSelected = isSelected,
                                                onClick = {
                                                    selectedCrops = if (isSelected) {
                                                        selectedCrops.split(", ").filter { it != crop }.joinToString(", ")
                                                    } else {
                                                        if (selectedCrops.isEmpty()) crop else "$selectedCrops, $crop"
                                                    }
                                                },
                                                isDark = isDark
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Land Area Input
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (isDark) Color(0x35FFFFFF) else Color(0x60CBD5E1))
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                            Text(text = "LAND AREA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
                                            BasicTextField(
                                                value = landArea,
                                                onValueChange = { landArea = it },
                                                textStyle = TextStyle(color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                                cursorBrush = SolidColor(neonMagenta),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    // Monitored Region Input for Disaster/Commuter/Aviation
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (isDark) Color(0x35FFFFFF) else Color(0x60CBD5E1))
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                            Text(text = "MONITORED REGION / CITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
                                            BasicTextField(
                                                value = monitoredRegion,
                                                onValueChange = { monitoredRegion = it },
                                                textStyle = TextStyle(color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                                cursorBrush = SolidColor(neonMagenta),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    Button(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF1E2032) else Color(0xFFE2E8F0),
                            contentColor = textColor
                        )
                    ) {
                        Text(text = "Back", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            // Finish and Save Profile to Supabase + Local Cache
                            isSaving = true
                            val finalProfile = initialProfile.copy(
                                name = userName.ifBlank { "Dhruv" },
                                sector = selectedSector,
                                preferredLanguage = selectedLanguage,
                                crops = selectedCrops,
                                landArea = landArea,
                                monitoredRegion = monitoredRegion,
                                isOnboarded = true
                            )

                            scope.launch {
                                UserPreferences.saveProfile(context, finalProfile)
                                authService.saveUserProfile(finalProfile)
                                isSaving = false
                                onComplete(finalProfile)
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(if (currentStep > 1) 1.6f else 1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = neonMagenta,
                        contentColor = Color.White
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = if (currentStep < 3) "Continue" else "Launch WeatherGPT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
            .background(if (isActive) Color(0xFFC026D3) else Color(0x35FFFFFF))
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
    val activeBorder = Color(0xFFC026D3)
    val inactiveBorder = if (isDark) Color(0x28FFFFFF) else Color(0x50CBD5E1)
    val activeBg = if (isDark) Color(0x30C026D3) else Color(0x20C026D3)
    val inactiveBg = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) activeBg else inactiveBg,
        border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, if (isSelected) activeBorder else inactiveBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFFC026D3) else (if (isDark) Color(0xFF282A3E) else Color(0xFFE2E8F0))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else (if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF475569)),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text(text = subtitle, fontSize = 12.sp, color = subtitleColor, lineHeight = 16.sp)
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color(0xFFC026D3),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguagePill(code: String, label: String, isSelected: Boolean, onSelect: () -> Unit, isDark: Boolean) {
    val activeBg = Color(0xFFC026D3)
    val inactiveBg = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9)
    val textColor = if (isSelected) Color.White else (if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF0F172A))

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) activeBg else inactiveBg,
        border = BorderStroke(1.dp, if (isSelected) activeBg else (if (isDark) Color(0x30FFFFFF) else Color(0x50CBD5E1)))
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
    }
}

@Composable
private fun CropOptionPill(crop: String, isSelected: Boolean, onClick: () -> Unit, isDark: Boolean) {
    val activeBg = Color(0xFF10B981)
    val inactiveBg = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9)
    val textColor = if (isSelected) Color.White else (if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF0F172A))

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) activeBg else inactiveBg,
        border = BorderStroke(1.dp, if (isSelected) activeBg else (if (isDark) Color(0x30FFFFFF) else Color(0x50CBD5E1)))
    ) {
        Text(text = crop, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
    }
}
