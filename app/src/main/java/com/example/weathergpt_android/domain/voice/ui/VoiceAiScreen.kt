package com.example.weathergpt_android.domain.voice.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.theme.AiIndigo
import com.example.weathergpt_android.core.theme.AiPurple
import com.example.weathergpt_android.core.theme.AiPurpleLight
import com.example.weathergpt_android.core.theme.CardBackground
import com.example.weathergpt_android.core.theme.SkyBlue
import com.example.weathergpt_android.core.theme.SkyBlueLight
import com.example.weathergpt_android.core.theme.SubtleSurface
import com.example.weathergpt_android.core.theme.TextPrimary
import com.example.weathergpt_android.core.theme.TextSecondary

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun VoiceAiScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var currentPrompt by remember { mutableStateOf("Tap the microphone or say 'Hey WeatherGPT'") }
    var currentResponse by remember {
        mutableStateOf("WeatherGPT Voice Assistant is ready. Ask anything about forecasts, outfit recommendations, or travel weather.")
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            currentPrompt = "Listening: 'What should I wear for an evening walk?'"
            currentResponse = "At 24°C with mild 14 km/h breeze, a breathable t-shirt with light shorts or chinos is ideal. No rain jacket needed!"
        }
    }

    val quickPrompts = listOf(
        "Is it going to rain this evening?",
        "What should I wear for a 5 PM walk?",
        "Check UV index for tomorrow morning",
        "Will the wind pick up by sunset?"
    )

    // Animated ripple wave pulses
    val infiniteTransition = rememberInfiniteTransition(label = "voice_pulses")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 84.dp,
            bottom = 100.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Voice AI Assistant",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    text = if (isListening) "Listening to your voice..." else "Hands-free natural weather intelligence",
                    fontSize = 14.sp,
                    color = if (isListening) SkyBlue else TextSecondary
                )
            }
        }

        // Voice Orb Visualizer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(pulse2)
                            .clip(CircleShape)
                            .background(SkyBlueLight.copy(alpha = 0.4f))
                    )
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(pulse1)
                            .clip(CircleShape)
                            .background(AiPurpleLight.copy(alpha = 0.6f))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = if (isListening) SkyBlue else Color(0x20000000)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (isListening) listOf(SkyBlue, AiIndigo)
                                else listOf(SkyBlue, AiPurple)
                            )
                        )
                        .clickable {
                            val hasAudioPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!hasAudioPermission) {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                isListening = !isListening
                                if (isListening) {
                                    currentPrompt = "Listening: 'What should I wear for an evening walk?'"
                                    currentResponse = "At 24°C with mild 14 km/h breeze, a breathable t-shirt with light shorts or chinos is ideal. No rain jacket needed!"
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = "Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }

        // Real-time Soundwave Bars
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(12) { index ->
                    val barHeight = if (isListening) {
                        (16 + (index * 7) % 28).dp
                    } else {
                        8.dp
                    }
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isListening) SkyBlue else SubtleSurface)
                    )
                }
            }
        }

        // Voice Response Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0x10000000),
                        ambientColor = Color(0x06000000)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = CardBackground
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AiPurpleLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = AiIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Voice Interaction",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AiIndigo
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Play Audio",
                            tint = SkyBlue,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .clickable { }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentPrompt,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentResponse,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Quick Suggestions
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Try asking",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                quickPrompts.forEach { prompt ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                isListening = true
                                currentPrompt = prompt
                                currentResponse = "Analyzing live meteorological data... Conditions are optimal with 24°C, 48% humidity, and calm winds."
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = SubtleSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = SkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "\"$prompt\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
