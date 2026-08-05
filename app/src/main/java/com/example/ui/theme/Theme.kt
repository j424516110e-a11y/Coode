package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IdePrimarySky,
    secondary = IdeSecondaryIndigo,
    tertiary = IdeAccentRun,
    background = IdeDarkBg,
    surface = IdeDarkSurface,
    surfaceVariant = IdeDarkSurfaceVariant,
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color(0xFF0F172A),
    onBackground = IdeDarkTextPrimary,
    onSurface = IdeDarkTextPrimary,
    outline = IdeDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = IdePrimarySky,
    secondary = IdeSecondaryIndigo,
    tertiary = IdeAccentRun,
    background = IdeLightBg,
    surface = IdeLightSurface,
    surfaceVariant = IdeLightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = IdeLightTextPrimary,
    onSurface = IdeLightTextPrimary,
    outline = IdeLightBorder
)

@Composable
fun CodeCanvasTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
