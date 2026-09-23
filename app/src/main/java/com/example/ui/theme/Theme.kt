package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class TritonThemeMode(val displayName: String, val description: String) {
    LIGHT("Shimmering Gold (Light)", "Warm parchment ivory with radiant gold accents (Default)"),
    DARK("Dark Mode", "Dark grey canvas with high-contrast lighter text for accessibility")
}

val LocalIsDarkTheme = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = ClaudeBgDark,
    primaryContainer = GoldContainerDark,
    onPrimaryContainer = GoldOnContainerDark,
    secondary = GoldAmber,
    onSecondary = ClaudeBgDark,
    secondaryContainer = ClaudeSurfaceVariantDark,
    onSecondaryContainer = GoldHighlight,
    tertiary = GoldHighlight,
    background = ClaudeBgDark,
    onBackground = ClaudeTextPrimaryDark,
    surface = ClaudeSurfaceDark,
    onSurface = ClaudeTextPrimaryDark,
    surfaceVariant = ClaudeSurfaceVariantDark,
    onSurfaceVariant = ClaudeTextSecondaryDark,
    outline = ClaudeBorderDark,
    outlineVariant = GoldBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = GoldDeep,
    onPrimary = ClaudeSurfaceLight,
    primaryContainer = GoldContainerLight,
    onPrimaryContainer = GoldOnContainerLight,
    secondary = GoldAmber,
    onSecondary = ClaudeSurfaceLight,
    secondaryContainer = ClaudeSurfaceVariantLight,
    onSecondaryContainer = GoldDeep,
    tertiary = GoldPrimary,
    background = ClaudeBgLight,
    onBackground = ClaudeTextPrimaryLight,
    surface = ClaudeSurfaceLight,
    onSurface = ClaudeTextPrimaryLight,
    surfaceVariant = ClaudeSurfaceVariantLight,
    onSurfaceVariant = ClaudeTextSecondaryLight,
    outline = ClaudeBorderLight,
    outlineVariant = GoldBorderLight
)

@Composable
fun TritonTheme(
    darkTheme: Boolean = false, // Default is Light Mode!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Backwards-compatibility alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    TritonTheme(darkTheme = darkTheme, content = content)
}
