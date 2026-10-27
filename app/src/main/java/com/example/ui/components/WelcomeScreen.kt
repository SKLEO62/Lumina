package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.LuminaThemeMode
import com.example.data.model.LuminaWallpaper
import com.example.data.model.SearchEngine

/**
 * Clean Pure Splash & Welcome Screen showing exclusively the user-provided concept poster image.
 */
@Composable
fun WelcomeScreen(
    currentTheme: LuminaThemeMode = LuminaThemeMode.OBSIDIAN,
    onSelectTheme: (LuminaThemeMode) -> Unit = {},
    searchEngine: SearchEngine = SearchEngine.GOOGLE,
    onSelectSearchEngine: (SearchEngine) -> Unit = {},
    isAdBlockEnabled: Boolean = true,
    onToggleAdBlock: () -> Unit = {},
    selectedWallpaper: LuminaWallpaper = LuminaWallpaper.COSMIC_NEBULA,
    onSelectWallpaper: (LuminaWallpaper) -> Unit = {},
    onDismiss: (dontShowAgain: Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss(true) }
            .testTag("welcome_screen_container")
    ) {
        // Full screen concept poster image
        Image(
            painter = painterResource(id = R.drawable.lumina_splash_bg),
            contentDescription = "Lumina Splash Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Bottom CTA button to enter browser
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { onDismiss(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("welcome_next_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "Начать сёрфинг",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
