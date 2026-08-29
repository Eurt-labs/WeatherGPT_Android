package com.example.weathergpt_android.domain.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.theme.AiIndigo
import com.example.weathergpt_android.core.theme.AiPurple
import com.example.weathergpt_android.core.theme.AiPurpleLight
import com.example.weathergpt_android.core.theme.CardBackground
import com.example.weathergpt_android.core.theme.SkyBlue
import com.example.weathergpt_android.core.theme.SubtleSurface
import com.example.weathergpt_android.core.theme.TextPrimary
import com.example.weathergpt_android.core.theme.TextSecondary
import com.example.weathergpt_android.core.theme.TextTertiary
import com.example.weathergpt_android.domain.assistant.model.ChatMessage
import kotlinx.coroutines.launch

@Composable
fun GptChatScreen(
    modifier: Modifier = Modifier
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "1",
                text = "Hello Dhruv! I'm your WeatherGPT Assistant. Ask me anything regarding micro-climates, activity timing, or clothing advice.",
                isUser = false,
                timestamp = "2:30 PM"
            ),
            ChatMessage(
                id = "2",
                text = "Is today good for a walk outside around sunset?",
                isUser = true,
                timestamp = "2:31 PM"
            ),
            ChatMessage(
                id = "3",
                text = "Today's weather is good! Temperatures will hover around 22°C with light 12 km/h breeze and 0% chance of rain until late night.",
                isUser = false,
                timestamp = "2:31 PM",
                weatherHighlight = "Optimal walk window: 5:30 PM - 7:00 PM (Sunset @ 6:48 PM)"
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val suggestionChips = listOf(
        "Suggest outfit for today",
        "Weekend rain probability?",
        "Best time for running tomorrow",
        "Air quality breakdown"
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 84.dp,
                bottom = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "WeatherGPT Intelligence",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Powered by real-time spatial weather modeling",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }
        }

        // Suggestions horizontal row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestionChips.forEach { suggestion ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            messages.add(
                                ChatMessage(
                                    id = System.currentTimeMillis().toString(),
                                    text = suggestion,
                                    isUser = true,
                                    timestamp = "Just now"
                                )
                            )
                            messages.add(
                                ChatMessage(
                                    id = (System.currentTimeMillis() + 1).toString(),
                                    text = "Based on our latest hyper-local forecast for San Francisco, tomorrow features clear skies (24°C), low humidity (45%), and UV index 3.",
                                    isUser = false,
                                    timestamp = "Just now",
                                    weatherHighlight = "Recommended: Light cotton wear & sunglasses"
                                )
                            )
                            scope.launch {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = SubtleSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = SkyBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = suggestion,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Input pill container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 92.dp, top = 6.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0x14000000)
                    ),
                shape = RoundedCornerShape(28.dp),
                color = CardBackground
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask WeatherGPT anything...",
                                fontSize = 14.sp,
                                color = TextTertiary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(SkyBlue, AiIndigo)
                                )
                            )
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    val query = inputText.trim()
                                    inputText = ""
                                    messages.add(
                                        ChatMessage(
                                            id = System.currentTimeMillis().toString(),
                                            text = query,
                                            isUser = true,
                                            timestamp = "Just now"
                                        )
                                    )
                                    messages.add(
                                        ChatMessage(
                                            id = (System.currentTimeMillis() + 1).toString(),
                                            text = "Analyzing query '$query'... Local temperature is 24°C with pleasant clear skies and mild wind speeds (14 km/h).",
                                            isUser = false,
                                            timestamp = "Just now",
                                            weatherHighlight = "Comfort Index: 9.2/10"
                                        )
                                    )
                                    scope.launch {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    if (message.isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)),
                shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
                color = SkyBlue
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.timestamp,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
                        spotColor = Color(0x10000000)
                    ),
                shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
                color = CardBackground
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(AiPurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = AiIndigo,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WeatherGPT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AiIndigo
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )

                    if (message.weatherHighlight != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SubtleSurface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Thermostat,
                                    contentDescription = null,
                                    tint = SkyBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = message.weatherHighlight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.timestamp,
                        fontSize = 10.sp,
                        color = TextTertiary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
