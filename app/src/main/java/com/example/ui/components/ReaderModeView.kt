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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebTab

enum class ReaderTheme(val bg: Color, val text: Color, val nameLabel: String) {
    CHARCOAL(Color(0xFF131823), Color(0xFFF1F5F9), "Тёмная"),
    SEPIA(Color(0xFFFBF0D9), Color(0xFF2C2416), "Сепия"),
    OLED(Color(0xFF000000), Color(0xFFE2E8F0), "OLED"),
    LIGHT(Color(0xFFFFFFFF), Color(0xFF0F172A), "Светлая")
}

@Composable
fun ReaderModeView(
    tab: WebTab,
    isSpeaking: Boolean,
    onToggleSpeak: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var readerTheme by remember { mutableStateOf(ReaderTheme.CHARCOAL) }
    var isSerif by remember { mutableStateOf(true) }
    var fontSize by remember { mutableFloatStateOf(17f) }

    val wordCount = remember(tab.pageText) {
        tab.pageText.split("\\s+".toRegex()).size
    }
    val readingTimeMin = remember(wordCount) {
        maxOf(1, (wordCount / 180))
    }

    // Animation for TTS wave
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveScale"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(readerTheme.bg),
        color = readerTheme.bg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Reader Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Reader Mode",
                            tint = readerTheme.text
                        )
                    }

                    Column {
                        Text(
                            text = "Режим чтения",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = readerTheme.text
                        )
                        Text(
                            text = "⏱️ ~$readingTimeMin мин чтения • $wordCount слов",
                            style = MaterialTheme.typography.labelSmall,
                            color = readerTheme.text.copy(alpha = 0.7f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Read Aloud TTS button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onToggleSpeak("${tab.title}. ${tab.pageText}")
                            }
                            .testTag("reader_tts_button"),
                        color = if (isSpeaking) Color(0xFF6366F1) else readerTheme.text.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.VolumeUp,
                                contentDescription = "Listen",
                                tint = if (isSpeaking) Color.White else readerTheme.text,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isSpeaking) "Слушаем" else "Озвучить",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSpeaking) Color.White else readerTheme.text
                            )
                        }
                    }

                    // Font Serif/Sans toggle
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { isSerif = !isSerif }
                            .padding(8.dp),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = if (isSerif) "Serif" else "Sans",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = readerTheme.text
                        )
                    }

                    // Font Size toggle
                    IconButton(
                        onClick = {
                            fontSize = when (fontSize) {
                                15f -> 18f
                                18f -> 22f
                                22f -> 15f
                                else -> 18f
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Font Size",
                            tint = readerTheme.text,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Theme Color Swatches Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReaderTheme.entries.forEach { theme ->
                    val isSelected = theme == readerTheme
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(theme.bg)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF6366F1) else Color.Gray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable { readerTheme = theme }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = readerTheme.text.copy(alpha = 0.15f)
            )

            // Article Content Reader Scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                // Title
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = if (isSerif) FontFamily.Serif else FontFamily.Default,
                        lineHeight = (fontSize * 1.35f).sp
                    ),
                    color = readerTheme.text
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = tab.url,
                    style = MaterialTheme.typography.labelSmall,
                    color = readerTheme.text.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Body Text
                val cleanContent = if (tab.pageText.isNotBlank()) {
                    tab.pageText
                } else {
                    "Текстовое содержимое не распознано на данной странице."
                }

                Text(
                    text = cleanContent,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.6f).sp,
                        fontFamily = if (isSerif) FontFamily.Serif else FontFamily.Default
                    ),
                    color = readerTheme.text
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
