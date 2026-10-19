package com.example.posrf.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,

    background = CharcoalDark,
    onBackground = CustomTextPrimary,

    surface = SlateSurface,
    onSurface = CustomTextPrimary,
    surfaceVariant = SlateSurfaceLight,
    onSurfaceVariant = CustomTextSecondary,

    error = StatusCritical,
    onError = Color(0xFFFFFFFF),
)

@Composable
fun POSRFTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true, // Force Dark Mode
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false, // Disable system dynamic tinting
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
