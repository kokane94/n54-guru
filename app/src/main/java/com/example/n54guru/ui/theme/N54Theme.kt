package com.example.n54guru.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object N54Colors {
    // Dark theme (matches Base44 screenshots)
    val background = Color(0xFF0B0E14)
    val surface = Color(0xFF151922)
    val surfaceVariant = Color(0xFF1C2030)
    val border = Color(0xFF2A2F3D)

    val primary = Color(0xFFFF6A2C)
    val primarySoft = Color(0x33FF6A2C)
    val onPrimary = Color(0xFFFFFFFF)

    val accent = Color(0xFFE67E22)
    val accentSoft = Color(0x33E67E22)

    val textPrimary = Color(0xFFF1F2F5)
    val textSecondary = Color(0xFF9AA3B2)
    val textMuted = Color(0xFF6B7280)

    val chipUnselectedBg = Color(0xFF202637)
    val chipUnselectedText = Color(0xFF9AA3B2)

    val badgeCritical = Color(0xFFE53935)
    val badgeCriticalBg = Color(0x26E53935)
    val badgeHigh = Color(0xFFFF6A6A)
    val badgeHighBg = Color(0x26FF6A6A)
    val badgeMedium = Color(0xFFFFB74D)
    val badgeMediumBg = Color(0x26FFB74D)
    val badgeLow = Color(0xFF4FC3F7)
    val badgeLowBg = Color(0x264FC3F7)

    val beginner = Color(0xFF4ADE80)
    val beginnerBg = Color(0x264ADE80)
    val intermediate = Color(0xFFFFB74D)
    val intermediateBg = Color(0x26FFB74D)

    val destructive = Color(0xFFEF5350)
    val emerald = Color(0xFF66BB6A)
    val violet = Color(0xFF9575CD)
}

private val n54DarkScheme = darkColorScheme(
    background = N54Colors.background,
    surface = N54Colors.surface,
    onBackground = N54Colors.textPrimary,
    onSurface = N54Colors.textPrimary,
    primary = N54Colors.primary,
    onPrimary = N54Colors.onPrimary,
    secondary = N54Colors.accent,
    outline = N54Colors.border,
    surfaceVariant = N54Colors.surfaceVariant
)

private val n54LightScheme = lightColorScheme(
    background = Color(0xFFF8F6F4),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1B1C1E),
    onSurface = Color(0xFF1B1C1E),
    primary = Color(0xFFFF6A00),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF3950E6),
    outline = Color(0xFFE9E6E0),
    surfaceVariant = Color(0xFFF1EEEA)
)

@Composable
fun N54GuruTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) n54DarkScheme else n54LightScheme,
        typography = N54Typography,
        content = content
    )
}
