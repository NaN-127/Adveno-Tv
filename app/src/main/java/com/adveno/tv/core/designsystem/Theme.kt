package com.adveno.tv.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TvPrimary,
    onPrimary = TvTextPrimary,
    primaryContainer = TvPrimaryVariant,
    onPrimaryContainer = TvTextPrimary,
    secondary = TvAccentCyan,
    onSecondary = TvBackgroundDark,
    background = TvBackgroundDark,
    onBackground = TvTextPrimary,
    surface = TvSurfaceDark,
    onSurface = TvTextPrimary,
    surfaceVariant = TvSurfaceElevated,
    onSurfaceVariant = TvTextSecondary,
    outline = TvSurfaceBorder,
    error = TvStatusError
)

@Composable
fun AdvenoTvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = TvTypography,
        content = content
    )
}

