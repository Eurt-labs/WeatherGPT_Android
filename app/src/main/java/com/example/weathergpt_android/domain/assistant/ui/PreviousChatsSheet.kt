package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.core.theme.FrostedGlassTokens
import com.example.weathergpt_android.domain.assistant.data.ChatDatabaseHelper
import com.example.weathergpt_android.domain.assistant.model.ChatSessionSummary
import com.example.weathergpt_android.domain.assistant.network.ChatSyncService
import com.example.weathergpt_android.domain.auth.model.UserProfile
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Translucent Frosted Glass Modal Sheet for Previous Chats.
 * Displays persistent chat history sessions synced across SQLite and the Cloud.
 */
@Composable
fun PreviousChatsSheet(
    currentTheme: AppThemeMode,
    userProfile: UserProfile,
    onSelectSession: (sessionId: String) -> Unit,
    onStartNewChat: (newSessionId: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { ChatDatabaseHelper.getInstance(context) }
    val syncService = remember { ChatSyncService(context) }

    val isDark = when (currentTheme) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val textColor = if (isDark) Color.White else Color(0xFF111113)
    val subtitleColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
    val accentBeige = if (isDark) Color(0xFFE8E3D5) else Color(0xFFC4BCAF)
    val emeraldGreen = Color(0xFF10B981)

    val sessions = remember { mutableStateListOf<ChatSessionSummary>() }
    var isSyncing by remember { mutableStateOf(true) }
    var syncStatusText by remember { mutableStateOf("Syncing cloud history...") }

    fun refreshSessions() {
        scope.launch {
            val localSessions = dbHelper.getSessions(userProfile.userId)
            sessions.clear()
            sessions.addAll(localSessions)
        }
    }

    // On open: load local sessions immediately, then pull from cloud & merge
    LaunchedEffect(userProfile.userId) {
        refreshSessions()

        // Sync from cloud
        if (userProfile.userId.isNotBlank()) {
            val cloudRes = syncService.fetchCloudHistory(userProfile.userId)
            cloudRes.onSuccess { cloudMsgs ->
                if (cloudMsgs.isNotEmpty()) {
                    dbHelper.insertBatchFromCloud(cloudMsgs, userProfile.userId)
                    refreshSessions()
                    syncStatusText = "Cloud Synced (${cloudMsgs.size} messages)"
                } else {
                    syncStatusText = "Cloud Synced"
                }
            }.onFailure {
                syncStatusText = "Local Cache Mode"
            }
        } else {
            syncStatusText = "Local Cache Mode"
        }
        isSyncing = false
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.82f)
            .shadow(
                elevation = FrostedGlassTokens.ElevationRaised,
                shape = RoundedCornerShape(32.dp),
                ambientColor = FrostedGlassTokens.ShadowColor,
                spotColor = FrostedGlassTokens.ShadowColor
            ),
        shape = RoundedCornerShape(32.dp),
        color = FrostedGlassTokens.surfaceRaised(isDark),
        border = BorderStroke(1.2.dp, FrostedGlassTokens.border(isDark))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header Row: Sector Pill / Title, Cloud Badge, and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val historySweep = if (isDark) {
                        Brush.sweepGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE8E3D5), Color(0xFFA1A1AA), Color(0xFFFFFFFF)))
                    } else {
                        Brush.sweepGradient(listOf(Color(0xFF18181B), Color(0xFFC4BCAF), Color(0xFF71717A), Color(0xFF18181B)))
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(historySweep),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF121214) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "${userProfile.sector.title} • Previous Chats",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(11.dp),
                                    strokeWidth = 1.5.dp,
                                    color = accentBeige
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.CloudDone,
                                    contentDescription = null,
                                    tint = emeraldGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = syncStatusText,
                                fontSize = 11.sp,
                                color = subtitleColor
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(FrostedGlassTokens.surfaceSubtle(isDark))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Start New Chat Button
            val newChatBtnBg = if (isDark) Color(0xFFE8E3D5) else Color(0xFF18181B)
            val newChatBtnContent = if (isDark) Color(0xFF121214) else Color.White

            Button(
                onClick = {
                    val newId = "session_" + UUID.randomUUID().toString().take(8)
                    onStartNewChat(newId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = newChatBtnBg,
                    contentColor = newChatBtnContent
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = newChatBtnContent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Start New Chat",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = newChatBtnContent
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Previous Sessions List
            if (sessions.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                            contentDescription = null,
                            tint = subtitleColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Previous Chats Found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        Text(
                            text = "Your conversations with WeatherGPT will appear here\nand sync automatically across app downloads.",
                            fontSize = 12.sp,
                            color = subtitleColor,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sessions, key = { it.sessionId }) { session ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    onSelectSession(session.sessionId)
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = FrostedGlassTokens.surfaceSubtle(isDark),
                            border = BorderStroke(1.dp, FrostedGlassTokens.borderSubtle(isDark))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = session.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isDark) Color(0x25E8E3D5) else Color(0x1518181B),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFFE8E3D5).copy(alpha = 0.35f) else Color(0xFFC4BCAF).copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "${session.messageCount} msgs",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) accentBeige else Color(0xFF18181B),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = session.snippet,
                                    fontSize = 12.sp,
                                    color = subtitleColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Last: ${session.lastTimestamp}",
                                        fontSize = 10.sp,
                                        color = subtitleColor.copy(alpha = 0.8f)
                                    )

                                    Text(
                                        text = "Resume Chat →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) accentBeige else Color(0xFF18181B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Clear All History Row
            if (sessions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Clear All Chat History",
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            scope.launch {
                                dbHelper.clearHistory(userProfile.userId)
                                if (userProfile.userId.isNotBlank()) {
                                    syncService.clearCloudHistory(userProfile.userId)
                                }
                                sessions.clear()
                            }
                        }
                    )
                }
            }
        }
    }
}
