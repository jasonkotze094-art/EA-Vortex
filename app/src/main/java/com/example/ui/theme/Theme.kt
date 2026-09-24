package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VortexDarkColorScheme = darkColorScheme(
    primary = VortexPrimary,
    onPrimary = Color.White,
    primaryContainer = VortexPrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = VortexSecondary,
    onSecondary = Color.Black,
    secondaryContainer = VortexSurfaceElevated,
    onSecondaryContainer = Color.White,
    tertiary = VortexAccent,
    background = VortexBg,
    onBackground = TextPrimary,
    surface = VortexSurface,
    onSurface = TextPrimary,
    surfaceVariant = VortexSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = VortexBorder,
    outlineVariant = VortexBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // EA Vortex is designed around dark cybernetic fintech
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = VortexDarkColorScheme,
        typography = Typography,
        content = content
    )
}
