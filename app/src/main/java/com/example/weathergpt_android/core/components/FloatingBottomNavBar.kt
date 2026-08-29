package com.example.weathergpt_android.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weathergpt_android.core.navigation.NavTab
import com.example.weathergpt_android.core.theme.IslandBackground
import com.example.weathergpt_android.core.theme.SkyBlue
import com.example.weathergpt_android.core.theme.SkyBlueLight
import com.example.weathergpt_android.core.theme.TextTertiary

@Composable
fun FloatingBottomNavBar(
    modifier: Modifier = Modifier,
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(34.dp),
                    spotColor = Color(0x22000000),
                    ambientColor = Color(0x12000000)
                ),
            shape = RoundedCornerShape(34.dp),
            color = IslandBackground,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab.entriesList.forEach { tab ->
                    val isSelected = tab == currentTab
                    NavItem(
                        tab = tab,
                        isSelected = isSelected,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: NavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.85f
            isSelected -> 1.08f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_item_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) SkyBlue else TextTertiary,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "icon_color"
    )

    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) SkyBlueLight else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pill_bg"
    )

    val pillWidth by animateDpAsState(
        targetValue = if (isSelected) 52.dp else 44.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "pill_width"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = pillWidth, height = 40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(pillBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        // Subtle dot indicator under active item
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isSelected) SkyBlue else Color.Transparent)
        )
    }
}
