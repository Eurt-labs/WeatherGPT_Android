package com.example.weathergpt_android.domain.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.weathergpt_android.core.components.AmbientGlowBackground
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.core.theme.FrostedGlassTokens
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
    val focusManager = LocalFocusManager.current
    val contactFocusRequester = remember { FocusRequester() }
    val otpFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    var isPhoneMode by remember { mutableStateOf(false) }
    var contactInput by remember { mutableStateOf("") }
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

    // Auto-focus OTP field when OTP is sent
    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            delay(150)
            try {
                otpFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    val textColor = if (isDark) Color.White else Color(0xFF111113)
    val subtitleColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
    val accentBeige = if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF)

    val executeSendOtp: () -> Unit = {
        if (contactInput.isBlank()) {
            statusMessage = "Please enter your contact."
            isError = true
        } else if (contactInput.trim() == "123456") {
            // Testing bypass for 123456 entered in email/contact
            focusManager.clearFocus()
            val testProfile = UserProfile(
                userId = "test_user_123456",
                name = "Dhruv",
                contact = "dhruv@weathergpt.local",
                contactType = if (isPhoneMode) "phone" else "email",
                isOnboarded = true
            )
            UserPreferences.saveAuthSession(
                context,
                userId = testProfile.userId,
                token = "test_session_token_123456",
                contact = testProfile.contact,
                contactType = testProfile.contactType,
                isOnboarded = true
            )
            UserPreferences.saveProfile(context, testProfile)
            onAuthSuccess(testProfile, false)
        } else {
            focusManager.clearFocus()
            isLoading = true
            statusMessage = null
            scope.launch {
                val res = authService.sendOtp(
                    contact = contactInput.trim(),
                    channel = if (isPhoneMode) "phone" else "email"
                )
                isLoading = false
                res.onSuccess {
                    isOtpSent = true
                    timerCountdown = 60
                    statusMessage = if (isPhoneMode) "OTP sent on phone" else "OTP sent on email"
                    isError = false
                }.onFailure { err ->
                    statusMessage = err.message
                    isError = true
                }
            }
        }
    }

    val executeVerifyOtp: () -> Unit = {
        if (otpInput.length < 6) {
            statusMessage = "Please enter your verification code."
            isError = true
        } else if (otpInput.trim() == "123456") {
            focusManager.clearFocus()
            val testProfile = UserProfile(
                userId = "test_user_123456",
                name = "Dhruv",
                contact = contactInput.trim().ifBlank { "dhruv@weathergpt.local" },
                contactType = if (isPhoneMode) "phone" else "email",
                isOnboarded = true
            )
            UserPreferences.saveAuthSession(
                context,
                userId = testProfile.userId,
                token = "test_session_token_123456",
                contact = testProfile.contact,
                contactType = testProfile.contactType,
                isOnboarded = true
            )
            UserPreferences.saveProfile(context, testProfile)
            onAuthSuccess(testProfile, false)
        } else {
            focusManager.clearFocus()
            isLoading = true
            scope.launch {
                val res = authService.verifyOtp(
                    contact = contactInput.trim(),
                    token = otpInput.trim(),
                    channel = if (isPhoneMode) "phone" else "email"
                )
                isLoading = false
                res.onSuccess { verified ->
                    val profile = verified.profile ?: UserProfile(
                        userId = verified.userId,
                        contact = contactInput.trim(),
                        contactType = if (isPhoneMode) "phone" else "email",
                        isOnboarded = !verified.isNewUser
                    )
                    UserPreferences.saveAuthSession(
                        context,
                        userId = verified.userId,
                        token = verified.sessionToken,
                        contact = contactInput.trim(),
                        contactType = if (isPhoneMode) "phone" else "email",
                        isOnboarded = !verified.isNewUser
                    )
                    UserPreferences.saveProfile(context, profile)
                    onAuthSuccess(profile, verified.isNewUser)
                }.onFailure { err ->
                    statusMessage = err.message
                    isError = true
                }
            }
        }
    }

    AmbientGlowBackground(
        currentTheme = currentTheme,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Logo & Branding
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    val logoRingBrush = if (isDark) {
                        Brush.sweepGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE8E3D5), Color(0xFFA1A1AA), Color(0xFFFFFFFF)))
                    } else {
                        Brush.sweepGradient(listOf(Color(0xFF18181B), Color(0xFFC4BCAF), Color(0xFF71717A), Color(0xFF18181B)))
                    }

                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(logoRingBrush)
                            .padding(2.5.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF000000) else Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = "WeatherGPT",
                            tint = if (isDark) accentBeige else Color(0xFF18181B),
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

                // Central Frosted Authentication Card ("Login dialog box")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(FrostedGlassTokens.ElevationRaised, RoundedCornerShape(26.dp), ambientColor = FrostedGlassTokens.ShadowColor, spotColor = FrostedGlassTokens.ShadowColor),
                    shape = RoundedCornerShape(26.dp),
                    color = FrostedGlassTokens.surfaceRaised(isDark),
                    border = BorderStroke(1.2.dp, FrostedGlassTokens.border(isDark))
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
                            val activeToggleBg = if (isDark) Color(0x35E8E3D5) else Color(0xFF18181B)
                            val activeToggleTint = if (isDark) accentBeige else Color.White

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(FrostedGlassTokens.surfaceSubtle(isDark))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (!isPhoneMode) activeToggleBg else Color.Transparent)
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
                                            tint = if (!isPhoneMode) activeToggleTint else subtitleColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Email",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (!isPhoneMode) activeToggleTint else subtitleColor
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isPhoneMode) activeToggleBg else Color.Transparent)
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
                                            tint = if (isPhoneMode) activeToggleTint else subtitleColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Phone (SMS)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isPhoneMode) activeToggleTint else subtitleColor
                                        )
                                    }
                                }
                            }

                            // Input Field for Email or Phone
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try { contactFocusRequester.requestFocus() } catch (_: Exception) {}
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = FrostedGlassTokens.surface(isDark),
                                border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPhoneMode) Icons.Rounded.Phone else Icons.Rounded.Email,
                                        contentDescription = null,
                                        tint = accentBeige,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    BasicTextField(
                                        value = contactInput,
                                        onValueChange = { contactInput = it },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = if (isPhoneMode) KeyboardType.Phone else KeyboardType.Email,
                                            imeAction = ImeAction.Send
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                executeSendOtp()
                                            }
                                        ),
                                        textStyle = TextStyle(
                                            color = textColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        cursorBrush = SolidColor(accentBeige),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(contactFocusRequester),
                                        decorationBox = { innerTextField ->
                                            if (contactInput.isEmpty()) {
                                                Text(
                                                    text = if (isPhoneMode) "Phone number" else "name@example.com",
                                                    color = textColor.copy(alpha = 0.45f),
                                                    fontSize = 14.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }

                            // Send OTP Button
                            val authBtnBg = if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)
                            val authBtnContent = if (isDark) Color(0xFF121214) else Color.White

                            Button(
                                onClick = executeSendOtp,
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = authBtnBg,
                                    contentColor = authBtnContent
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = authBtnContent, modifier = Modifier.size(20.dp))
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try { otpFocusRequester.requestFocus() } catch (_: Exception) {}
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = FrostedGlassTokens.surface(isDark),
                                border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = accentBeige,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    BasicTextField(
                                        value = otpInput,
                                        onValueChange = {
                                            if (it.length <= 8) {
                                                otpInput = it
                                                if (it.trim() == "123456") {
                                                    // Testing bypass: direct login when 123456 is entered as pass/code
                                                    val testProfile = UserProfile(
                                                        userId = "test_user_123456",
                                                        name = "Dhruv",
                                                        contact = contactInput.trim().ifBlank { "dhruv@weathergpt.local" },
                                                        contactType = if (isPhoneMode) "phone" else "email",
                                                        isOnboarded = true
                                                    )
                                                    UserPreferences.saveAuthSession(
                                                        context,
                                                        userId = testProfile.userId,
                                                        token = "test_session_token_123456",
                                                        contact = testProfile.contact,
                                                        contactType = testProfile.contactType,
                                                        isOnboarded = true
                                                    )
                                                    UserPreferences.saveProfile(context, testProfile)
                                                    onAuthSuccess(testProfile, false)
                                                    return@BasicTextField
                                                }
                                                if (it.length == 6 || it.length == 8) {
                                                    // Auto verify upon entering complete code
                                                    executeVerifyOtp()
                                                }
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.NumberPassword,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                executeVerifyOtp()
                                            }
                                        ),
                                        textStyle = TextStyle(
                                            color = textColor,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 4.sp
                                        ),
                                        cursorBrush = SolidColor(accentBeige),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(otpFocusRequester),
                                        decorationBox = { innerTextField ->
                                            if (otpInput.isEmpty()) {
                                                Text(
                                                    text = "6-digit OTP",
                                                    color = textColor.copy(alpha = 0.45f),
                                                    fontSize = 14.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }

                            // Verify Button
                            val verifyBtnBg = if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)
                            val verifyBtnContent = if (isDark) Color(0xFF121214) else Color.White

                            Button(
                                onClick = executeVerifyOtp,
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = verifyBtnBg,
                                    contentColor = verifyBtnContent
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = verifyBtnContent, modifier = Modifier.size(20.dp))
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
                                    color = accentBeige,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { isOtpSent = false }
                                )

                                Text(
                                    text = if (timerCountdown > 0) "Resend in ${timerCountdown}s" else "Resend OTP",
                                    fontSize = 12.sp,
                                    color = if (timerCountdown > 0) subtitleColor else accentBeige,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable(enabled = timerCountdown == 0) {
                                        scope.launch {
                                            val res = authService.sendOtp(contactInput.trim(), if (isPhoneMode) "phone" else "email")
                                            timerCountdown = 60
                                            res.onSuccess {
                                                statusMessage = if (isPhoneMode) "OTP sent on phone" else "OTP sent on email"
                                                isError = false
                                            }.onFailure { err ->
                                                statusMessage = err.message
                                                isError = true
                                            }
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
            }
        }
    }
}

