package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.LuminaThemeMode

// 1. Obsidian (Default Cosmic Dark)
private val ObsidianColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4F46E5),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF06B6D4),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0E7490),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFF43F5E),
    onTertiary = Color.White,
    background = Color(0xFF0B0E14),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF131823),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1B2333),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF2A364F),
    outlineVariant = Color(0xFF1E293B)
)

// 2. Cyberpunk Neon
private val NeonColorScheme = darkColorScheme(
    primary = Color(0xFFD946EF), // Neon Fuchsia
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFA21CAF),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF06B6D4), // Cyan
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0891B2),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFACC15), // Neon Yellow
    onTertiary = Color.Black,
    background = Color(0xFF07070E),
    onBackground = Color(0xFFFAF5FF),
    surface = Color(0xFF0F0F1D),
    onSurface = Color(0xFFFAF5FF),
    surfaceVariant = Color(0xFF1A1A32),
    onSurfaceVariant = Color(0xFFA855F7),
    outline = Color(0xFF3B1754),
    outlineVariant = Color(0xFF271038)
)

// 3. Deep Space Navy
private val DeepSpaceColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA), // Sapphire blue
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF2563EB),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF38BDF8),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0284C7),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color.White,
    background = Color(0xFF0A0F1D),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF10192D),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF192644),
    onSurfaceVariant = Color(0xFF93C5FD),
    outline = Color(0xFF1E3A8A),
    outlineVariant = Color(0xFF172554)
)

// 4. Emerald Forest
private val EmeraldColorScheme = darkColorScheme(
    primary = Color(0xFF34D399), // Mint green
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF059669),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0F766E),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color.Black,
    background = Color(0xFF0A1310),
    onBackground = Color(0xFFECFDF5),
    surface = Color(0xFF11211C),
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = Color(0xFF193129),
    onSurfaceVariant = Color(0xFF6EE7B7),
    outline = Color(0xFF064E3B),
    outlineVariant = Color(0xFF022C22)
)

// 5. Crisp Minimalist Light
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = Color(0xFFE11D48),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun LuminaTheme(
    themeMode: LuminaThemeMode = LuminaThemeMode.OBSIDIAN,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeMode) {
        LuminaThemeMode.OBSIDIAN -> ObsidianColorScheme
        LuminaThemeMode.NEON -> NeonColorScheme
        LuminaThemeMode.DEEP_SPACE -> DeepSpaceColorScheme
        LuminaThemeMode.EMERALD -> EmeraldColorScheme
        LuminaThemeMode.LIGHT -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
