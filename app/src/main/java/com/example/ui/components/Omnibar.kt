package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebTab
import java.util.Locale

@Composable
fun Omnibar(
    activeTab: WebTab?,
    tabCount: Int,
    isFocused: Boolean,
    queryText: String,
    isBookmarked: Boolean,
    onQueryChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onSubmitUrl: (String) -> Unit,
    onOpenAiSheet: () -> Unit,
    onToggleTabs: () -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onReload: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleDesktop: () -> Unit,
    onToggleReaderMode: () -> Unit,
    onOpenQrCode: () -> Unit,
    onOpenNotes: () -> Unit,
    onNextTab: () -> Unit,
    onPrevTab: () -> Unit,
    onShare: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onOpenPasswordManager: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenThemeSelector: () -> Unit,
    onOpenWelcome: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenClearData: () -> Unit,
    textZoom: Int,
    onCycleZoom: () -> Unit,
    isAdBlockEnabled: Boolean,
    onToggleAdBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var showMenu by remember { mutableStateOf(false) }

    val isIncognito = activeTab?.isIncognito == true

    // Math evaluation check
    val mathResult = remember(queryText) { tryEvaluateSimpleMath(queryText) }

    // Swipe tracking for tab switching
    var totalDrag by remember { mutableFloatStateOf(0f) }

    // Breathing animation for AI spark button
    val infiniteTransition = rememberInfiniteTransition(label = "spark_glow")
    val sparkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkAlpha"
    )

    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Slim Loading Progress Bar
        if (activeTab?.isLoading == true) {
            LinearProgressIndicator(
                progress = { activeTab.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (isIncognito) Color(0xFFC084FC) else Color(0xFF38BDF8),
                trackColor = Color.Transparent
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Inline Math Result Pill (when focused and arithmetic detected)
        if (isFocused && mathResult != null) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onQueryChange(mathResult)
                    },
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "= $mathResult",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "• Нажмите, чтобы подставить",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Omnibar Main Container (Frosted floating pill)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        when {
                            isFocused -> listOf(Color(0xFF6366F1), Color(0xFF06B6D4))
                            isIncognito -> listOf(Color(0xFF9333EA), Color(0xFF4C1D95))
                            else -> listOf(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            color = if (isIncognito) Color(0xFF130D20).copy(alpha = 0.95f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isFocused) {
                    // Search icon
                    Icon(
                        imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isIncognito) Color(0xFFC084FC) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 4.dp)
                            .size(20.dp)
                    )

                    // Text input field
                    BasicTextField(
                        value = queryText,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .focusRequester(focusRequester)
                            .testTag("omnibar_input"),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(if (isIncognito) Color(0xFFC084FC) else MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                focusManager.clearFocus()
                                onFocusChange(false)
                                onSubmitUrl(queryText)
                            }
                        ),
                        decorationBox = { innerTextField ->
                            if (queryText.isEmpty()) {
                                Text(
                                    text = if (isIncognito) "Анонимный поиск или URL..." else "Поиск, URL или пример 12*4...",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 15.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Clear / Close focus button
                    IconButton(
                        onClick = {
                            if (queryText.isNotEmpty()) {
                                onQueryChange("")
                            } else {
                                focusManager.clearFocus()
                                onFocusChange(false)
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Direct Search Submit Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                focusManager.clearFocus()
                                onFocusChange(false)
                                onSubmitUrl(queryText)
                            }
                            .testTag("omnibar_submit_button"),
                        color = if (isIncognito) Color(0xFF9333EA) else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Перейти",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    // Normal Browsing State
                    val isHttps = activeTab?.url?.startsWith("https://") == true

                    Icon(
                        imageVector = when {
                            isIncognito -> Icons.Default.VisibilityOff
                            isHttps -> Icons.Default.Lock
                            else -> Icons.Default.Search
                        },
                        contentDescription = "Status",
                        tint = when {
                            isIncognito -> Color(0xFFC084FC)
                            isHttps -> Color(0xFF10B981)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .padding(start = 8.dp, end = 6.dp)
                            .size(16.dp)
                    )

                    // Clickable Domain Title with Horizontal Swipe gesture to switch tabs
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onFocusChange(true) }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (totalDrag > 80f) {
                                            onPrevTab()
                                        } else if (totalDrag < -80f) {
                                            onNextTab()
                                        }
                                        totalDrag = 0f
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        totalDrag += dragAmount
                                    }
                                )
                            }
                            .padding(vertical = 4.dp)
                            .testTag("omnibar_display")
                    ) {
                        val displayTitle = when {
                            isIncognito && activeTab?.url == "lumina://newtab" -> "Инкогнито"
                            activeTab == null || activeTab.url == "lumina://newtab" -> "Стартовая страница"
                            activeTab.title.isNotBlank() && activeTab.title != "Загрузка..." -> activeTab.title
                            else -> cleanDomainFromUrl(activeTab.url)
                        }

                        val displaySub = when {
                            isIncognito -> "Приватный режим • Следы не сохраняются"
                            activeTab?.url != "lumina://newtab" && activeTab != null -> cleanDomainFromUrl(activeTab.url)
                            else -> "Умный поиск или URL • Свайп для смены вкладок"
                        }

                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = displaySub,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIncognito) Color(0xFFC084FC).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // LUMINA GEMINI SPARK BUTTON
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFFA855F7))
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable(onClick = onOpenAiSheet)
                            .testTag("gemini_spark_button"),
                        color = Color(0xFF1E1B4B).copy(alpha = sparkAlpha),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini Context AI",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Lumina",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Tab Count Badge Button
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                if (isIncognito) Color(0xFFC084FC).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(onClick = onToggleTabs)
                            .testTag("tab_switcher_button"),
                        color = if (isIncognito) Color(0xFF2E1065) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$tabCount",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isIncognito) Color(0xFFE9D5FF) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // More Menu Button
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("omnibar_more_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Navigation items
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                IconButton(
                                    onClick = {
                                        showMenu = false
                                        onGoBack()
                                    },
                                    enabled = activeTab?.canGoBack == true
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        showMenu = false
                                        onGoForward()
                                    },
                                    enabled = activeTab?.canGoForward == true
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Forward"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        showMenu = false
                                        onReload()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reload"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        showMenu = false
                                        onToggleBookmark()
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isBookmarked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = { Text("Новая вкладка") },
                                onClick = {
                                    showMenu = false
                                    onNewTab()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Новая вкладка инкогнито") },
                                onClick = {
                                    showMenu = false
                                    onNewIncognitoTab()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color(0xFFC084FC)
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Режим чтения") },
                                onClick = {
                                    showMenu = false
                                    onToggleReaderMode()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.ChromeReaderMode, contentDescription = null, tint = Color(0xFF10B981))
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("QR-код страницы") },
                                onClick = {
                                    showMenu = false
                                    onOpenQrCode()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF6366F1))
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Заметки к сайту") },
                                onClick = {
                                    showMenu = false
                                    onOpenNotes()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFFF59E0B))
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Найти на странице") },
                                onClick = {
                                    showMenu = false
                                    onOpenFindInPage()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FindInPage, contentDescription = null, tint = Color(0xFF38BDF8))
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = { Text("История") },
                                onClick = {
                                    showMenu = false
                                    onOpenHistory()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.History, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Закладки") },
                                onClick = {
                                    showMenu = false
                                    onOpenBookmarks()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFFF59E0B))
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Менеджер паролей") },
                                onClick = {
                                    showMenu = false
                                    onOpenPasswordManager()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Key,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8)
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Загрузки") },
                                onClick = {
                                    showMenu = false
                                    onOpenDownloads()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = { Text("Масштаб текста: $textZoom%") },
                                onClick = onCycleZoom,
                                leadingIcon = {
                                    Icon(Icons.Default.ZoomIn, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isAdBlockEnabled) "Защита от трекеров: Вкл" else "Защита от трекеров: Выкл"
                                    )
                                },
                                onClick = onToggleAdBlock,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = if (isAdBlockEnabled) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Темы оформления") },
                                onClick = {
                                    showMenu = false
                                    onOpenThemeSelector()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = Color(0xFFF43F5E)
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("О браузере / Тур") },
                                onClick = {
                                    showMenu = false
                                    onOpenWelcome()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Очистить данные...") },
                                onClick = {
                                    showMenu = false
                                    onOpenClearData()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (activeTab?.isDesktopMode == true) "Мобильная версия" else "Версия для ПК"
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onToggleDesktop()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Computer, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Поделиться страницей") },
                                onClick = {
                                    showMenu = false
                                    onShare()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Анализ страницы (Gemini)") },
                                onClick = {
                                    showMenu = false
                                    onOpenAiSheet()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun tryEvaluateSimpleMath(input: String): String? {
    val clean = input.trim().replace(" ", "").replace(",", ".")
    val mathRegex = Regex("""^(\d+(?:\.\d+)?)([\+\-\*\/])(\d+(?:\.\d+)?)$""")
    val match = mathRegex.find(clean) ?: return null
    val (num1Str, op, num2Str) = match.destructured
    val num1 = num1Str.toDoubleOrNull() ?: return null
    val num2 = num2Str.toDoubleOrNull() ?: return null
    if (op == "/" && num2 == 0.0) return "Деление на 0"
    val res = when (op) {
        "+" -> num1 + num2
        "-" -> num1 - num2
        "*" -> num1 * num2
        "/" -> num1 / num2
        else -> return null
    }
    return if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.2f", res)
}

private fun cleanDomainFromUrl(url: String): String {
    return try {
        val uri = android.net.Uri.parse(url)
        uri.host ?: url
    } catch (e: Exception) {
        url
    }
}
