package com.example.weathergpt_android.domain.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.FrostedIconButton
import com.example.weathergpt_android.core.network.OpenRouterService
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.domain.assistant.data.ChatDatabaseHelper
import com.example.weathergpt_android.domain.auth.data.UserPreferences
import com.example.weathergpt_android.domain.auth.model.UserProfile
import kotlinx.coroutines.launch

/**
 * Ultra-high contrast Frosted Settings Modal.
 * Cloud-managed Supabase backend architecture, SIH26068 Persona switcher, and Token Stats.
 */
@Composable
fun FrostedSettingsSheet(
    currentTheme: AppThemeMode,
    userProfile: UserProfile,
    onThemeSelected: (AppThemeMode) -> Unit,
    onEditPersona: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { ChatDatabaseHelper.getInstance(context) }
    val openRouterService = remember { OpenRouterService(context) }

    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    var totalTokens by remember { mutableIntStateOf(0) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testStatusText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        totalTokens = dbHelper.getTotalTokens()
    }

    val cardBackground = if (isDark) Color(0xF212131F) else Color(0xFAF8FAFC)
    val cardBorder = if (isDark) Color(0x38FFFFFF) else Color(0x80CBD5E1)
    val textColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFFB4B9C8) else Color(0xFF475569)
    val accentColor = if (isDark) Color(0xFFC026D3) else Color(0xFF9333EA)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(18.dp)
            .shadow(28.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x50000000), spotColor = Color(0x50000000)),
        shape = RoundedCornerShape(28.dp),
        color = cardBackground,
        border = BorderStroke(1.2.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                FrostedIconButton(
                    icon = Icons.Rounded.Close,
                    onClick = onDismiss,
                    size = 36.dp,
                    iconSize = 18.dp,
                    isDark = isDark
                )
            }

            // 1. User Profile & SIH26068 Persona Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SIH-26068 USER PERSONA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor,
                    letterSpacing = 1.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isDark) Color(0x30FFFFFF) else Color(0x50CBD5E1))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = userProfile.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text(
                                        text = "${userProfile.sector.title} (${userProfile.contact})",
                                        fontSize = 11.sp,
                                        color = subtitleColor
                                    )
                                }
                            }

                            // Edit Persona Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accentColor.copy(alpha = 0.2f))
                                    .clickable(onClick = onEditPersona)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit",
                                        tint = accentColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Appearance Mode Switcher
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "APPEARANCE MODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        title = "System",
                        icon = Icons.Rounded.PhoneAndroid,
                        isSelected = currentTheme == AppThemeMode.SYSTEM,
                        onClick = { onThemeSelected(AppThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    ThemeOptionButton(
                        title = "Dark",
                        icon = Icons.Rounded.DarkMode,
                        isSelected = currentTheme == AppThemeMode.DARK,
                        onClick = { onThemeSelected(AppThemeMode.DARK) },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    ThemeOptionButton(
                        title = "Light",
                        icon = Icons.Rounded.LightMode,
                        isSelected = currentTheme == AppThemeMode.LIGHT,
                        onClick = { onThemeSelected(AppThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }
            }

            // 3. Cloud Backend & Supabase Architecture Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CLOUD BACKEND & SUPABASE AUTH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor,
                    letterSpacing = 1.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isDark) Color(0x30FFFFFF) else Color(0x50CBD5E1))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudDone,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Render FastAPI + Supabase",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                        Text(
                            text = "Authenticated with Supabase Auth OTP. Keys & user profiles securely managed by the server.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = subtitleColor
                        )
                    }
                }
            }

            // 4. Local Database & Tokens Analytics Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LOCAL DATABASE & TOKENS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor,
                    letterSpacing = 1.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isDark) Color(0x30FFFFFF) else Color(0x50CBD5E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QueryStats,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Total Tokens Processed",
                                    fontSize = 12.sp,
                                    color = subtitleColor
                                )
                                Text(
                                    text = "$totalTokens tokens",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }

                        // Clear Database Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0x20EF4444) else Color(0x15EF4444))
                                .clickable {
                                    scope.launch {
                                        dbHelper.clearHistory()
                                        totalTokens = 0
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Clear",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Clear",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Test Backend Connection Button
            Button(
                onClick = {
                    isTestingConnection = true
                    testStatusText = "Connecting to Cloud FastAPI backend..."
                    scope.launch {
                        val res = openRouterService.generateChatCompletion(
                            userMessage = "Ping test: Confirm connection"
                        )
                        isTestingConnection = false
                        res.onSuccess {
                            testStatusText = "✓ Connected to Cloud Backend!"
                        }.onFailure {
                            testStatusText = "✗ Backend error: ${it.message}"
                        }
                    }
                },
                enabled = !isTestingConnection,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isTestingConnection) "Connecting..." else "Test Cloud Backend Connection",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            testStatusText?.let { status ->
                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = if (status.startsWith("✓")) Color(0xFF10B981) else Color(0xFFEF4444),
                    fontWeight = FontWeight.Medium
                )
            }

            // 6. Sign Out Button
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0x30EF4444) else Color(0x18EF4444),
                    contentColor = Color(0xFFEF4444)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Sign Out",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    val activeBg = if (isDark) Color(0xFFC026D3).copy(alpha = 0.35f) else Color(0xFF9333EA).copy(alpha = 0.20f)
    val activeBorder = if (isDark) Color(0xFFE879F9) else Color(0xFF9333EA)
    val inactiveBg = if (isDark) Color(0xFF1B1D2C) else Color(0xFFF1F5F9)
    val inactiveBorder = if (isDark) Color(0x30FFFFFF) else Color(0x50CBD5E1)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) activeBg else inactiveBg,
        border = BorderStroke(1.2.dp, if (isSelected) activeBorder else inactiveBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) (if (isDark) Color(0xFFE879F9) else Color(0xFF9333EA))
                       else (if (isDark) Color(0xFFB4B9C8) else Color(0xFF64748B)),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) (if (isDark) Color.White else Color(0xFF0F172A))
                        else (if (isDark) Color(0xFFB4B9C8) else Color(0xFF64748B))
            )
        }
    }
}
