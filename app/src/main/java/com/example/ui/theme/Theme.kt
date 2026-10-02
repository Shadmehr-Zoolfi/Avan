package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AvanDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = ObsidianDeep,
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = CyanAccent,
    onSecondary = ObsidianDeep,
    secondaryContainer = IndigoDark,
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = IndigoSecondary,
    onTertiary = ObsidianDeep,
    background = ObsidianDeep,
    onBackground = TextPrimary,
    surface = MidnightDark,
    onSurface = TextPrimary,
    surfaceVariant = StudioCardBg,
    onSurfaceVariant = TextSecondary,
    outline = StudioCardStroke,
    error = NoteWrongRed,
    onError = Color.White
)

@Composable
fun AvanTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AvanDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AvanTheme(content = content)
}

