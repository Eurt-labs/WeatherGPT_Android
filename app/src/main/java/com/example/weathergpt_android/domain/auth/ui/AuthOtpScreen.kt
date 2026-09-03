package com.example.weathergpt_android.domain.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.AmbientGlowBackground
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.domain.auth.data.UserPreferences
import com.example.weathergpt_android.domain.auth.model.UserProfile
import com.example.weathergpt_android.domain.auth.network.AuthApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Sign In with Email or Phone OTP Screen matching the Obsidian theme.
 * Powered by Supabase Auth via FastAPI backend with Sandbox testing support.
 */
@Composable
fun AuthOtpScreen(
    currentTheme: AppThemeMode,
    onAuthSuccess: (UserProfile, isNewUser: Boolean) -> Unit,
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

    var isPhoneMode by remember { mutableStateOf(false) }
    var contactInput by remember { mutableStateOf("dhruv@example.com") }
    var otpInput by remember { mutableStateOf("") }

    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var timerCountdown by remember { mutableIntStateOf(0) }

    // Countdown Timer for Resending OTP
    LaunchedEffect(timerCountdown) {
        if (timerCountdown > 0) {
            delay(1000)
            timerCountdown--
        }
    }

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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo & Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(listOf(neonMagenta, neonPurple, neonCoral, neonMagenta))
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF0B0C14) else Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = "WeatherGPT",
                        tint = Color(0xFFFF9E64),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Text(
                    text = "WeatherGPT",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Conversational Climate & Weather Intelligence",
                    fontSize = 13.sp,
                    color = subtitleColor
                )
            }

            // Central Frosted Authentication Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(28.dp, RoundedCornerShape(26.dp), ambientColor = Color(0x60000000), spotColor = Color(0x60000000)),
                shape = RoundedCornerShape(26.dp),
                color = if (isDark) Color(0xF212131F) else Color(0xFAFFFFFF),
                border = BorderStroke(1.2.dp, if (isDark) Color(0x35FFFFFF) else Color(0x70CBD5E1))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (!isOtpSent) "Sign In with OTP" else "Enter 6-Digit Code",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    if (!isOtpSent) {
                        // Toggle: Email vs Phone
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isPhoneMode) neonPurple else Color.Transparent)
                                    .clickable {
                                        isPhoneMode = false
                                        contactInput = "dhruv@example.com"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Email,
                                        contentDescription = null,
                                        tint = if (!isPhoneMode) Color.White else subtitleColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Email",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (!isPhoneMode) Color.White else subtitleColor
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isPhoneMode) neonPurple else Color.Transparent)
                                    .clickable {
                                        isPhoneMode = true
                                        contactInput = "+919876543210"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Phone,
                                        contentDescription = null,
                                        tint = if (isPhoneMode) Color.White else subtitleColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Phone (SMS)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isPhoneMode) Color.White else subtitleColor
                                    )
                                }
                            }
                        }

                        // Input Field for Email or Phone
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isDark) Color(0x35FFFFFF) else Color(0x60CBD5E1))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPhoneMode) Icons.Rounded.Phone else Icons.Rounded.Email,
                                    contentDescription = null,
                                    tint = neonMagenta,
                                    modifier = Modifier.size(18.dp)
                                )
                                BasicTextField(
                                    value = contactInput,
                                    onValueChange = { contactInput = it },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = if (isPhoneMode) KeyboardType.Phone else KeyboardType.Email
                                    ),
                                    textStyle = TextStyle(
                                        color = textColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(neonMagenta),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Send OTP Button
                        Button(
                            onClick = {
                                if (contactInput.isBlank()) {
                                    statusMessage = "Please enter your contact."
                                    isError = true
                                    return@Button
                                }
                                isLoading = true
                                statusMessage = null
                                scope.launch {
                                    val res = authService.sendOtp(
                                        contact = contactInput.trim(),
                                        channel = if (isPhoneMode) "phone" else "email"
                                    )
                                    isLoading = false
                                    res.onSuccess { msg ->
                                        isOtpSent = true
                                        timerCountdown = 60
                                        statusMessage = msg
                                        isError = false
                                    }.onFailure { err ->
                                        statusMessage = err.message
                                        isError = true
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = neonPurple,
                                contentColor = Color.White
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text(text = "Send Verification OTP", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Step 2: 6-Digit OTP Code Input
                        Text(
                            text = "Sent to $contactInput",
                            fontSize = 13.sp,
                            color = subtitleColor
                        )

                        // 6-digit boxes
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isDark) Color(0x35FFFFFF) else Color(0x60CBD5E1))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = neonMagenta,
                                    modifier = Modifier.size(18.dp)
                                )
                                BasicTextField(
                                    value = otpInput,
                                    onValueChange = {
                                        if (it.length <= 6) {
                                            otpInput = it
                                            if (it.length == 6) {
                                                // Auto verify upon typing 6th digit
                                                isLoading = true
                                                scope.launch {
                                                    val res = authService.verifyOtp(
                                                        contact = contactInput.trim(),
                                                        token = it,
                                                        channel = if (isPhoneMode) "phone" else "email"
                                                    )
                                                    isLoading = false
                                                    res.onSuccess { verified ->
                                                        UserPreferences.saveAuthSession(
                                                            context,
                                                            userId = verified.userId,
                                                            token = verified.sessionToken,
                                                            contact = contactInput.trim(),
                                                            contactType = if (isPhoneMode) "phone" else "email"
                                                        )
                                                        val profile = verified.profile ?: UserProfile(
                                                            userId = verified.userId,
                                                            contact = contactInput.trim(),
                                                            contactType = if (isPhoneMode) "phone" else "email",
                                                            isOnboarded = !verified.isNewUser
                                                        )
                                                        onAuthSuccess(profile, verified.isNewUser)
                                                    }.onFailure { err ->
                                                        statusMessage = err.message
                                                        isError = true
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    textStyle = TextStyle(
                                        color = textColor,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 4.sp
                                    ),
                                    cursorBrush = SolidColor(neonMagenta),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Verify Button
                        Button(
                            onClick = {
                                if (otpInput.length < 6) {
                                    statusMessage = "Please enter the full 6-digit code."
                                    isError = true
                                    return@Button
                                }
                                isLoading = true
                                scope.launch {
                                    val res = authService.verifyOtp(
                                        contact = contactInput.trim(),
                                        token = otpInput.trim(),
                                        channel = if (isPhoneMode) "phone" else "email"
                                    )
                                    isLoading = false
                                    res.onSuccess { verified ->
                                        UserPreferences.saveAuthSession(
                                            context,
                                            userId = verified.userId,
                                            token = verified.sessionToken,
                                            contact = contactInput.trim(),
                                            contactType = if (isPhoneMode) "phone" else "email"
                                        )
                                        val profile = verified.profile ?: UserProfile(
                                            userId = verified.userId,
                                            contact = contactInput.trim(),
                                            contactType = if (isPhoneMode) "phone" else "email",
                                            isOnboarded = !verified.isNewUser
                                        )
                                        onAuthSuccess(profile, verified.isNewUser)
                                    }.onFailure { err ->
                                        statusMessage = err.message
                                        isError = true
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = neonMagenta,
                                contentColor = Color.White
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text(text = "Verify & Proceed", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Resend OTP / Change Contact Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Change Contact",
                                fontSize = 12.sp,
                                color = neonMagenta,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { isOtpSent = false }
                            )

                            Text(
                                text = if (timerCountdown > 0) "Resend in ${timerCountdown}s" else "Resend OTP",
                                fontSize = 12.sp,
                                color = if (timerCountdown > 0) subtitleColor else neonPurple,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable(enabled = timerCountdown == 0) {
                                    scope.launch {
                                        authService.sendOtp(contactInput.trim(), if (isPhoneMode) "phone" else "email")
                                        timerCountdown = 60
                                    }
                                }
                            )
                        }
                    }

                    // Status / Error message
                    statusMessage?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = if (isError) Color(0xFFEF4444) else Color(0xFF10B981),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Developer Sandbox Mode Hint Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) Color(0x301E1035) else Color(0xFFEDE9FE),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(neonPurple, neonMagenta)))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = neonMagenta,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Supabase Auth • Demo OTP: 123456",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
