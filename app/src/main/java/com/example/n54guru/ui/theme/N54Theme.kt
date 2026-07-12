package com.example.n54guru.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * N54 Guru design system, ported from the Base44 web app.
 *
 * The Base44 app uses a dark automotive theme:
 *   - background:  ~#0c0f14  (hsl 220 20% 6%)
 *   - card surface: ~#13161d  (hsl 220 18% 10%)
 *   - primary:      ~#ff7f11  (hsl 25 100% 55%) — turbo/orange
 *   - accent:       ~#29b6f6  (hsl 200 80% 50%) — diagnostics/cyan
 *   - muted text:   ~#7f8c9a  (hsl 215 15% 55%)
 *   - destructive:  ~#ef4444  (red)
 *
 * Only the dark scheme is used for the Android app. A light scheme is kept
 * for preview/semantics but will still look dark-ish because the brand is
 * built around the dark garage-dash aesthetic.
 */

private val Background = Color(0xFF0C0F14)
private val Surface = Color(0xFF13161D)
private val SurfaceVariant = Color(0xFF1A1E27)
private val Primary = Color(0xFFFF7F11)
private val OnPrimary = Color(0xFFFFFFFF)
private val Secondary = Color(0xFF1E232C)
private val OnSecondary = Color(0xFFE5E8ED)
private val Accent = Color(0xFF29B6F6)
private val OnAccent = Color(0xFFFFFFFF)
private val Muted = Color(0xFF151922)
private val MutedForeground = Color(0xFF7F8C9A)
private val Border = Color(0xFF2A2F3A)
private val Destructive = Color(0xFFEF4444)
private val DestructiveForeground = Color(0xFFFEF2F2)
private val Emerald = Color(0xFF34D399)
private val Yellow = Color(0xFFFACC15)
private val Violet = Color(0xFFA78BFA)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.15f),
    onPrimaryContainer = Primary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = Secondary,
    onSecondaryContainer = OnSecondary,
    tertiary = Accent,
    onTertiary = OnAccent,
    tertiaryContainer = Accent.copy(alpha = 0.15f),
    onTertiaryContainer = Accent,
    background = Background,
    onBackground = Color(0xFFF1F2F5),
    surface = Surface,
    onSurface = Color(0xFFF1F2F5),
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = MutedForeground,
    error = Destructive,
    onError = DestructiveForeground,
    errorContainer = Destructive.copy(alpha = 0.15f),
    onErrorContainer = Destructive,
    outline = Border,
    outlineVariant = Border.copy(alpha = 0.6f),
    scrim = Color.Black.copy(alpha = 0.7f),
    inverseSurface = Color(0xFFE5E8ED),
    inverseOnSurface = Background,
    inversePrimary = Primary
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.12f),
    onPrimaryContainer = Color(0xFF7A3D0C),
    secondary = Color(0xFFE5E8ED),
    onSecondary = Color(0xFF1A1E27),
    secondaryContainer = Color(0xFFDEE2E8),
    onSecondaryContainer = Color(0xFF1A1E27),
    tertiary = Accent,
    onTertiary = OnAccent,
    tertiaryContainer = Accent.copy(alpha = 0.12f),
    onTertiaryContainer = Color(0xFF0C4A6E),
    background = Color(0xFFF8F9FB),
    onBackground = Color(0xFF0C0F14),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0C0F14),
    surfaceVariant = Color(0xFFF1F3F6),
    onSurfaceVariant = Color(0xFF4B5563),
    error = Destructive,
    onError = DestructiveForeground,
    errorContainer = Destructive.copy(alpha = 0.12f),
    onErrorContainer = Destructive,
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
    scrim = Color.Black.copy(alpha = 0.5f),
    inverseSurface = Color(0xFF1A1E27),
    inverseOnSurface = Color(0xFFF1F2F5),
    inversePrimary = Primary
)

/**
 * Extended brand colors that are not part of Material3's standard color
 * scheme but are used repeatedly across N54 screens.
 */
object N54Colors {
    val background: Color get() = Background
    val surface: Color get() = Surface
    val surfaceVariant: Color get() = SurfaceVariant
    val primary: Color get() = Primary
    val accent: Color get() = Accent
    val mutedForeground: Color get() = MutedForeground
    val border: Color get() = Border
    val destructive: Color get() = Destructive
    val emerald: Color get() = Emerald
    val yellow: Color get() = Yellow
    val violet: Color get() = Violet
    val muted: Color get() = Muted
}

@Composable
fun N54GuruTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = N54Typography,
        content = content
    )
}
