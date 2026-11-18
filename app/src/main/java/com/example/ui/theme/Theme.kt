package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StudioColorScheme = darkColorScheme(
    primary = StudioCyan,
    onPrimary = StudioBackground,
    primaryContainer = StudioCyanContainer,
    onPrimaryContainer = StudioCyan,
    secondary = StudioCoral,
    onSecondary = StudioBackground,
    tertiary = StudioAmber,
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = StudioSurfaceSolid,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    error = DangerRed
)

@Composable
fun MagicPoserTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = MagicPoserTypography,
        content = content
    )
}
