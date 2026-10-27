package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Bookmark
import com.example.data.model.ExtractedTask
import com.example.data.model.LuminaWallpaper
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

data class QuickShortcut(
    val title: String,
    val url: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewTabScreen(
    bookmarks: List<Bookmark>,
    tasks: List<ExtractedTask>,
    selectedWallpaper: LuminaWallpaper,
    customWallpaperPath: String? = null,
    wallpaperDimming: Float,
    wallpaperBlur: Float = 0f,
    activeBookmarkCategory: String,
    isBookmarkGridView: Boolean,
    onOpenUrl: (String) -> Unit,
    onOpenAiWithPrompt: (String) -> Unit,
    onOpenTasksSheet: () -> Unit,
    onAddBookmark: (Bookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onTogglePinBookmark: (String) -> Unit,
    onEditBookmark: (String, String, String, String) -> Unit,
    onSelectBookmarkCategory: (String) -> Unit,
    onToggleBookmarkViewMode: () -> Unit,
    onSelectWallpaper: (LuminaWallpaper) -> Unit,
    onSetCustomWallpaper: (String) -> Unit,
    onRemoveCustomWallpaper: () -> Unit,
    onSetWallpaperDimming: (Float) -> Unit,
    onSetWallpaperBlur: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isAddBookmarkDialogOpen by remember { mutableStateOf(false) }
    var isWallpaperSheetOpen by remember { mutableStateOf(false) }
    var bookmarkToEdit by remember { mutableStateOf<Bookmark?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val destinationFile = File(context.filesDir, "lumina_custom_wallpaper.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onSetCustomWallpaper(destinationFile.absolutePath)
            } catch (e: Exception) {
                android.util.Log.e("NewTabScreen", "Failed to save custom wallpaper", e)
            }
        }
    }

    // Quick shortcuts
    val shortcuts = remember {
        listOf(
            QuickShortcut("Google", "https://www.google.com", Icons.Default.Search, Color(0xFF4285F4)),
            QuickShortcut("Wikipedia", "https://ru.wikipedia.org", Icons.Default.Language, Color(0xFF64748B)),
            QuickShortcut("GitHub", "https://github.com", Icons.Default.Code, Color(0xFF8B5CF6)),
            QuickShortcut("Новости", "https://news.ycombinator.com", Icons.Default.Newspaper, Color(0xFFF97316)),
            QuickShortcut("Habr", "https://habr.com", Icons.Default.TrendingUp, Color(0xFF06B6D4)),
            QuickShortcut("YouTube", "https://youtube.com", Icons.Default.Language, Color(0xFFEF4444))
        )
    }

    // Dynamic greeting based on current hour
    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Доброе утро"
            in 12..17 -> "Добрый день"
            in 18..22 -> "Добрый вечер"
            else -> "Доброй ночи"
        }
    }

    // Available categories from existing bookmarks + standard ones
    val categories = remember(bookmarks) {
        val standard = listOf("Все", "★ Избранное", "Поиск", "Работа", "Учеба", "Медиа", "Инструменты")
        val custom = bookmarks.map { it.category }.distinct().filterNot { standard.contains(it) }
        standard + custom
    }

    // Filter bookmarks by category & search query
    val filteredBookmarks = remember(bookmarks, activeBookmarkCategory, searchQuery) {
        var list = bookmarks
        if (activeBookmarkCategory == "★ Избранное") {
            list = list.filter { it.isPinned }
        } else if (activeBookmarkCategory != "Все") {
            list = list.filter { it.category.equals(activeBookmarkCategory, ignoreCase = true) }
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.url.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }

        // Sort: pinned first, then newest
        list.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.addedAt })
    }

    Box(modifier = modifier.fillMaxSize()) {
        // --- 1. WALLPAPER BACKGROUND LAYER ---
        val blurModifier = if (wallpaperBlur > 0.5f) Modifier.blur(wallpaperBlur.dp) else Modifier

        if (selectedWallpaper == LuminaWallpaper.CUSTOM && !customWallpaperPath.isNullOrBlank()) {
            val customFile = remember(customWallpaperPath) { File(customWallpaperPath) }
            if (customFile.exists()) {
                AsyncImage(
                    model = customFile,
                    contentDescription = "Пользовательский фон",
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = wallpaperDimming))
            )
        } else if (selectedWallpaper.drawableResId != null) {
            Image(
                painter = painterResource(id = selectedWallpaper.drawableResId),
                contentDescription = "Фон браузера",
                modifier = Modifier
                    .fillMaxSize()
                    .then(blurModifier),
                contentScale = ContentScale.Crop
            )
            // Dimming scrim overlay for text contrast and readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = wallpaperDimming))
            )
        } else {
            // Minimal theme gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )
        }

        // --- 2. START SCREEN CONTENT ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Bar: Wallpaper Pill + Quick Tools
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wallpaper Selector Button Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { isWallpaperSheetOpen = true }
                            .testTag("wallpaper_selector_pill"),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = "Обои",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Обои: ${selectedWallpaper.title}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Bookmarks Counter Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(20.dp)
                            ),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${bookmarks.size} закладок",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFCD34D)
                            )
                        }
                    }
                }
            }

            // Hero Brand Header & Search Bar
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF6366F1),
                                        Color(0xFF06B6D4),
                                        Color(0xFFA855F7)
                                    )
                                )
                            )
                            .padding(2.5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Lumina",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF38BDF8),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Lumina",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "$greetingText • Быстрый поиск и закладки",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Start Screen Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_page_search_input"),
                        placeholder = {
                            Text(
                                text = "Поиск в закладках или ввести URL...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Поиск",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Очистить",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    onOpenUrl(searchQuery)
                                }
                            }
                        ),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            // --- 3. BOOKMARKS HUB (REDESIGNED CORE) ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Bookmarks Header Row with Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Мои Закладки",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // View Mode Toggle (Grid vs List)
                            IconButton(
                                onClick = onToggleBookmarkViewMode,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                    .testTag("toggle_bookmarks_view_mode")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarkGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                    contentDescription = "Сменить вид",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Add Bookmark Button
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { isAddBookmarkDialogOpen = true }
                                    .testTag("start_page_add_bookmark_btn"),
                                color = Color(0xFFF59E0B),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Добавить",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Добавить",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // Category Filter Scrollable Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = category == activeBookmarkCategory
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onSelectBookmarkCategory(category) }
                                    .testTag("bookmark_cat_${category.lowercase().replace(" ", "_")}"),
                                color = if (isSelected) Color(0xFFF59E0B).copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color(0xFFFCD34D) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Bookmarks Content (Grid or List)
                    if (filteredBookmarks.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp)),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 28.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B).copy(alpha = 0.7f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Ничего не найдено" else "В этой категории нет закладок",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(onClick = { isAddBookmarkDialogOpen = true }) {
                                    Text("+ Добавить первую закладку", color = Color(0xFF38BDF8))
                                }
                            }
                        }
                    } else if (isBookmarkGridView) {
                        // 2-Column Grid Layout for Bookmarks
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            maxItemsInEachRow = 2
                        ) {
                            filteredBookmarks.forEach { bookmark ->
                                BookmarkGridCard(
                                    bookmark = bookmark,
                                    onOpen = { onOpenUrl(bookmark.url) },
                                    onTogglePin = { onTogglePinBookmark(bookmark.id) },
                                    onEdit = { bookmarkToEdit = bookmark },
                                    onDelete = { onDeleteBookmark(bookmark.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        // Detailed List Layout for Bookmarks
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredBookmarks.forEach { bookmark ->
                                BookmarkListRow(
                                    bookmark = bookmark,
                                    onOpen = { onOpenUrl(bookmark.url) },
                                    onTogglePin = { onTogglePinBookmark(bookmark.id) },
                                    onEdit = { bookmarkToEdit = bookmark },
                                    onDelete = { onDeleteBookmark(bookmark.id) }
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. QUICK SHORTCUTS ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Быстрый запуск",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = 3
                    ) {
                        shortcuts.forEach { shortcut ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f, fill = true)
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onOpenUrl(shortcut.url) }
                                    .testTag("shortcut_${shortcut.title.lowercase()}"),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = shortcut.icon,
                                        contentDescription = shortcut.title,
                                        tint = shortcut.accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = shortcut.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 5. AI GEMINI SHORTCUTS & CONTEXT CARDS ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(Color(0xFF6366F1).copy(alpha = 0.4f), Color(0xFF06B6D4).copy(alpha = 0.4f))
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Интеллектуальный контекст Gemini",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Открывайте страницы и вызывайте ассистента ✦ для краткой выжимки, анализа данных и перевода.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AiQuickChip(
                                label = "⚡ IT-новости дня",
                                onClick = { onOpenUrl("https://news.ycombinator.com") }
                            )
                            AiQuickChip(
                                label = "🔍 Поиск в Perplexity",
                                onClick = { onOpenUrl("https://www.perplexity.ai") }
                            )
                            AiQuickChip(
                                label = "🎲 Случайная статья Wiki",
                                onClick = { onOpenUrl("https://ru.wikipedia.org/wiki/Специальная:Случайная_страница") }
                            )
                            if (tasks.isNotEmpty()) {
                                AiQuickChip(
                                    label = "📋 Извлеченные дела (${tasks.count { !it.isDone }})",
                                    onClick = onOpenTasksSheet
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- 6. ADD BOOKMARK DIALOG ---
    if (isAddBookmarkDialogOpen) {
        var newTitle by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf(if (activeBookmarkCategory != "Все" && activeBookmarkCategory != "★ Избранное") activeBookmarkCategory else "Общие") }
        var isPinned by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { isAddBookmarkDialogOpen = false },
            title = {
                Text(
                    text = "Новая закладка",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Название сайта") },
                        placeholder = { Text("Например: GitHub или Хабр") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_bookmark_title_input")
                    )

                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("Адрес (URL)") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_bookmark_url_input")
                    )

                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Категория / Папка") },
                        placeholder = { Text("Работа, Учеба, Медиа, Шопинг...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_bookmark_category_input")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isPinned = !isPinned }
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Text(
                            text = "Добавить в Избранное ★",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = if (isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (isPinned) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUrl.isNotBlank()) {
                            val title = if (newTitle.isNotBlank()) newTitle else newUrl
                            onAddBookmark(
                                Bookmark(
                                    title = title.trim(),
                                    url = newUrl.trim(),
                                    category = newCategory.trim().ifBlank { "Общие" },
                                    isPinned = isPinned
                                )
                            )
                            isAddBookmarkDialogOpen = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_bookmark_btn")
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddBookmarkDialogOpen = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // --- 7. EDIT BOOKMARK DIALOG ---
    bookmarkToEdit?.let { bookmark ->
        var editTitle by remember(bookmark) { mutableStateOf(bookmark.title) }
        var editUrl by remember(bookmark) { mutableStateOf(bookmark.url) }
        var editCategory by remember(bookmark) { mutableStateOf(bookmark.category) }

        AlertDialog(
            onDismissRequest = { bookmarkToEdit = null },
            title = {
                Text(
                    text = "Редактировать закладку",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Название") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editUrl,
                        onValueChange = { editUrl = it },
                        label = { Text("Адрес (URL)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        label = { Text("Категория") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editUrl.isNotBlank()) {
                            onEditBookmark(bookmark.id, editTitle, editUrl, editCategory)
                            bookmarkToEdit = null
                        }
                    }
                ) {
                    Text("Обновить")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookmarkToEdit = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // --- 8. WALLPAPER SELECTOR BOTTOM SHEET ---
    if (isWallpaperSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { isWallpaperSheetOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Surface(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(42.dp)
                        .height(4.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                ) {}
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Фон браузера",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Выберите фоновое изображение для стартовой страницы",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { isWallpaperSheetOpen = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --- CUSTOM USER WALLPAPER FROM GALLERY ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            1.dp,
                            if (selectedWallpaper == LuminaWallpaper.CUSTOM) Color(0xFF38BDF8)
                            else Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(18.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Свое фото из галереи",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!customWallpaperPath.isNullOrBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (selectedWallpaper != LuminaWallpaper.CUSTOM) {
                                        TextButton(
                                            onClick = { onSelectWallpaper(LuminaWallpaper.CUSTOM) }
                                        ) {
                                            Text("Выбрать", color = Color(0xFF38BDF8))
                                        }
                                    }
                                    IconButton(
                                        onClick = onRemoveCustomWallpaper,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Удалить свое фото",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (!customWallpaperPath.isNullOrBlank()) {
                            val customFile = remember(customWallpaperPath) { File(customWallpaperPath) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectWallpaper(LuminaWallpaper.CUSTOM) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp, 56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (selectedWallpaper == LuminaWallpaper.CUSTOM) 2.dp else 1.dp,
                                            color = if (selectedWallpaper == LuminaWallpaper.CUSTOM) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    AsyncImage(
                                        model = customFile,
                                        contentDescription = "Превью вашего фото",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (selectedWallpaper == LuminaWallpaper.CUSTOM) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(3.dp)
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF38BDF8)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedWallpaper == LuminaWallpaper.CUSTOM) "★ Текущий активный фон" else "Пользовательское фото сохранено",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (selectedWallpaper == LuminaWallpaper.CUSTOM) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Нажмите, чтобы применить, или выберите другое фото",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.testTag("replace_custom_wallpaper_btn")
                                ) {
                                    Text("Сменить")
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        1.dp,
                                        Color(0xFF38BDF8).copy(alpha = 0.35f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .testTag("pick_custom_wallpaper_btn"),
                                color = Color(0xFF38BDF8).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Выбрать картинку из галереи",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Поддерживаются любые JPEG, PNG, WEBP фотографии",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- SLIDERS FOR DIMMING AND BLUR ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Dimming Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Затемнение фона",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(wallpaperDimming * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Slider(
                            value = wallpaperDimming,
                            onValueChange = onSetWallpaperDimming,
                            valueRange = 0.15f..0.85f,
                            modifier = Modifier.fillMaxWidth().testTag("wallpaper_dimming_slider")
                        )
                    }

                    // Blur Slider (Frosted Glass Effect)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Размытие фона (Frosted Glass)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${wallpaperBlur.toInt()} dp",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Slider(
                            value = wallpaperBlur,
                            onValueChange = onSetWallpaperBlur,
                            valueRange = 0f..25f,
                            modifier = Modifier.fillMaxWidth().testTag("wallpaper_blur_slider")
                        )
                    }
                }

                Text(
                    text = "Коллекция встроенных тем",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Standard Built-in Wallpapers List
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(LuminaWallpaper.entries.filterNot { it == LuminaWallpaper.CUSTOM }) { wallpaper ->
                        val isSelected = wallpaper == selectedWallpaper

                        Surface(
                            modifier = Modifier
                                .width(120.dp)
                                .height(170.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { onSelectWallpaper(wallpaper) }
                                .testTag("wp_tile_${wallpaper.id}"),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (wallpaper.drawableResId != null) {
                                    Image(
                                        painter = painterResource(id = wallpaper.drawableResId),
                                        contentDescription = wallpaper.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF0F172A), Color(0xFF020617))
                                                )
                                            )
                                    )
                                }

                                // Dark vignette at bottom for label
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                            )
                                        )
                                    )

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = wallpaper.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = wallpaper.description,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = Color.White.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS: BOOKMARK CARDS & CHIPS ---

@Composable
private fun BookmarkGridCard(
    bookmark: Bookmark,
    onOpen: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val domain = remember(bookmark.url) {
        try {
            val uri = android.net.Uri.parse(bookmark.url)
            uri.host?.replace("www.", "") ?: bookmark.url
        } catch (_: Exception) {
            bookmark.url
        }
    }

    val monogram = remember(bookmark.title) {
        bookmark.title.take(2).uppercase()
    }

    val avatarBrush = remember(bookmark.title) {
        val h = bookmark.title.hashCode()
        val colors = listOf(
            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
            listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
            listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
            listOf(Color(0xFF10B981), Color(0xFF06B6D4)),
            listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
        )
        val selected = colors[kotlin.math.abs(h) % colors.size]
        Brush.linearGradient(selected)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (bookmark.isPinned) Color(0xFFF59E0B).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onOpen)
            .testTag("bookmark_grid_${bookmark.id}"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Monogram Avatar
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(avatarBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = monogram,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                // Pin / Star Button
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = if (bookmark.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (bookmark.isPinned) "Открепить" else "Закрепить",
                        tint = if (bookmark.isPinned) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = domain,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = bookmark.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }

                // Actions: Edit and Delete
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkListRow(
    bookmark: Bookmark,
    onOpen: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val domain = remember(bookmark.url) {
        try {
            val uri = android.net.Uri.parse(bookmark.url)
            uri.host?.replace("www.", "") ?: bookmark.url
        } catch (_: Exception) {
            bookmark.url
        }
    }

    val monogram = remember(bookmark.title) {
        bookmark.title.take(2).uppercase()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (bookmark.isPinned) Color(0xFFF59E0B).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onOpen)
            .testTag("bookmark_list_${bookmark.id}"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Monogram Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (bookmark.isPinned) Color(0xFFF59E0B).copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = monogram,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (bookmark.isPinned) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = bookmark.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (bookmark.isPinned) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Закреплено",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Text(
                    text = "$domain • ${bookmark.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (bookmark.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Избранное",
                        tint = if (bookmark.isPinned) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AiQuickChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                Color(0xFF6366F1).copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            ),
        color = Color(0xFF6366F1).copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
