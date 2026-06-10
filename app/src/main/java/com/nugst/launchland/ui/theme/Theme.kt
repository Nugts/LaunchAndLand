package com.nugst.launchland.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurpleMain,
    onPrimary = Color.Black,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkBackground,
    onSurface = Color.White,
    primaryContainer = PurpleDark,
    onPrimaryContainer = Color.White,
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LavenderDark, // Using the darker purple as primary for better visibility
    onPrimary = Color.White,
    background = LightBackground,
    onBackground = Color.Black,
    surface = White,
    onSurface = Color.Black,
    primaryContainer = LavenderMain,
    onPrimaryContainer = Color.Black,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color.Black
)

@Composable
fun LaunchLandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
