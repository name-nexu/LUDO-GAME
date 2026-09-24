package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NexuDarkColorScheme = darkColorScheme(
    primary = NexuElectricCyan,
    onPrimary = NexuDarkBackground,
    primaryContainer = NexuSurfaceVariant,
    onPrimaryContainer = NexuElectricCyan,
    secondary = NexuAmberGold,
    onSecondary = NexuDarkBackground,
    secondaryContainer = NexuSurfaceVariant,
    onSecondaryContainer = NexuAmberGold,
    tertiary = NexuEmeraldGreen,
    onTertiary = NexuDarkBackground,
    background = NexuDarkBackground,
    onBackground = NexuTextPrimary,
    surface = NexuSurface,
    onSurface = NexuTextPrimary,
    surfaceVariant = NexuSurfaceVariant,
    onSurfaceVariant = NexuTextSecondary,
    error = NexuDanger,
    onError = NexuTextPrimary
)

@Composable
fun NexuLudoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NexuDarkColorScheme,
        typography = Typography,
        content = content
    )
}
