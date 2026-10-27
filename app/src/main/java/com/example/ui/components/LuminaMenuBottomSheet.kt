package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.WebTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaMenuBottomSheet(
    activeTab: WebTab?,
    isBookmarked: Boolean,
    textZoom: Int,
    isAdBlockEnabled: Boolean,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onReload: () -> Unit,
    onToggleBookmark: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onToggleReaderMode: () -> Unit,
    onOpenQrCode: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenPasswordManager: () -> Unit,
    onOpenDownloads: () -> Unit,
    onCycleZoom: () -> Unit,
    onToggleAdBlock: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWelcome: () -> Unit,
    onOpenClearData: () -> Unit,
    onToggleDesktop: () -> Unit,
    onShare: () -> Unit,
    onOpenAiSheet: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0B0F19),
        contentColor = Color(0xFFE2E8F0),
        tonalElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header handle & Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFF334155), RoundedCornerShape(2.dp))
                )
            }

            Text(
                text = "Меню Lumina",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF1E293B)
            )

            // Top Quick Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        onDismiss()
                        onGoBack()
                    },
                    enabled = activeTab?.canGoBack == true
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = if (activeTab?.canGoBack == true) Color(0xFFE2E8F0) else Color(0xFF64748B)
                    )
                }
                IconButton(
                    onClick = {
                        onDismiss()
                        onGoForward()
                    },
                    enabled = activeTab?.canGoForward == true
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Вперед",
                        tint = if (activeTab?.canGoForward == true) Color(0xFFE2E8F0) else Color(0xFF64748B)
                    )
                }
                IconButton(
                    onClick = {
                        onDismiss()
                        onReload()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = Color(0xFF38BDF8)
                    )
                }
                IconButton(
                    onClick = {
                        onDismiss()
                        onToggleBookmark()
                    }
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Закладка",
                        tint = if (isBookmarked) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF1E293B)
            )

            // Scrollable Menu Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(horizontal = 12.dp)
            ) {
                item {
                    MenuRowItem(
                        icon = Icons.Default.Add,
                        tint = Color(0xFF38BDF8),
                        title = "Новая вкладка",
                        onClick = { onDismiss(); onNewTab() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.VisibilityOff,
                        tint = Color(0xFFC084FC),
                        title = "Новая вкладка инкогнито",
                        onClick = { onDismiss(); onNewIncognitoTab() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.ChromeReaderMode,
                        tint = Color(0xFF10B981),
                        title = "Режим чтения",
                        onClick = { onDismiss(); onToggleReaderMode() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.QrCode,
                        tint = Color(0xFF6366F1),
                        title = "QR-код страницы",
                        onClick = { onDismiss(); onOpenQrCode() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.EditNote,
                        tint = Color(0xFFF59E0B),
                        title = "Заметки к сайту",
                        onClick = { onDismiss(); onOpenNotes() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.FindInPage,
                        tint = Color(0xFF38BDF8),
                        title = "Найти на странице",
                        onClick = { onDismiss(); onOpenFindInPage() }
                    )
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF1E293B))
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.History,
                        tint = Color(0xFFE2E8F0),
                        title = "История",
                        onClick = { onDismiss(); onOpenHistory() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.Bookmark,
                        tint = Color(0xFFF59E0B),
                        title = "Закладки",
                        onClick = { onDismiss(); onOpenBookmarks() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.Key,
                        tint = Color(0xFF38BDF8),
                        title = "Менеджер паролей",
                        onClick = { onDismiss(); onOpenPasswordManager() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.Download,
                        tint = Color(0xFF10B981),
                        title = "Загрузки",
                        onClick = { onDismiss(); onOpenDownloads() }
                    )
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF1E293B))
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.ZoomIn,
                        tint = Color(0xFFE2E8F0),
                        title = "Масштаб текста: $textZoom%",
                        onClick = { onCycleZoom() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.Security,
                        tint = if (isAdBlockEnabled) Color(0xFF10B981) else Color(0xFF64748B),
                        title = if (isAdBlockEnabled) "Защита от трекеров: Вкл" else "Защита от трекеров: Выкл",
                        onClick = { onToggleAdBlock() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.Settings,
                        tint = Color(0xFF38BDF8),
                        title = "Настройки браузера",
                        onClick = { onDismiss(); onOpenSettings() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.Info,
                        tint = Color(0xFF38BDF8),
                        title = "О браузере / Тур",
                        onClick = { onDismiss(); onOpenWelcome() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.DeleteSweep,
                        tint = Color(0xFFEF4444),
                        title = "Очистить данные...",
                        onClick = { onDismiss(); onOpenClearData() }
                    )
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF1E293B))
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.Computer,
                        tint = Color(0xFFE2E8F0),
                        title = if (activeTab?.isDesktopMode == true) "Мобильная версия" else "Версия для ПК",
                        onClick = { onDismiss(); onToggleDesktop() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.Share,
                        tint = Color(0xFF38BDF8),
                        title = "Поделиться страницей",
                        onClick = { onDismiss(); onShare() }
                    )
                }
                item {
                    MenuRowItem(
                        icon = Icons.Default.AutoAwesome,
                        tint = Color(0xFFC084FC),
                        title = "Анализ страницы (Gemini)",
                        onClick = { onDismiss(); onOpenAiSheet() }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFE2E8F0))
            )
        }
    }
}
