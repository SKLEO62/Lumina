package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebTab

/**
 * LuminaBottomBar: Modern Neon Glass floating bottom navigation bar with 5 primary items.
 * 1. Back
 * 2. Forward
 * 3. Home (highlighted with neon glow)
 * 4. Tabs (with active count badge)
 * 5. Menu (additional functions & settings)
 */
@Composable
fun LuminaBottomBar(
    activeTab: WebTab?,
    tabCount: Int,
    isHomeSelected: Boolean,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onGoHome: () -> Unit,
    onToggleTabs: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canGoBack = activeTab?.canGoBack == true
    val canGoForward = activeTab?.canGoForward == true

    // Neon glow pulse for Home button
    val infiniteTransition = rememberInfiniteTransition(label = "home_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF0B0F19).copy(alpha = 0.85f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.5f),
                    Color(0xFF8B5CF6).copy(alpha = 0.3f),
                    Color(0xFFEC4899).copy(alpha = 0.4f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Back
            BottomBarItem(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                label = "Назад",
                enabled = canGoBack,
                onClick = onGoBack,
                testTag = "bottom_bar_back"
            )

            // 2. Forward
            BottomBarItem(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                label = "Вперед",
                enabled = canGoForward,
                onClick = onGoForward,
                testTag = "bottom_bar_forward"
            )

            // 3. Home (Highlighted Neon Glow Button)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF38BDF8).copy(alpha = glowAlpha),
                                Color(0xFF6366F1).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF8B5CF6))
                        ),
                        CircleShape
                    )
                    .clickable { onGoHome() }
                    .testTag("bottom_bar_home"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Главная",
                    tint = Color(0xFFE2E8F0),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 4. Tabs with Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onToggleTabs() }
                    .testTag("bottom_bar_tabs"),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (tabCount > 0) {
                            Badge(
                                containerColor = Color(0xFF38BDF8),
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = tabCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Tab,
                        contentDescription = "Вкладки",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 5. Menu
            BottomBarItem(
                icon = Icons.Default.Menu,
                label = "Меню",
                enabled = true,
                onClick = onOpenMenu,
                testTag = "bottom_bar_menu"
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled) Color(0xFFE2E8F0) else Color(0xFF475569),
            modifier = Modifier.size(22.dp)
        )
    }
}
