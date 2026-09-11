package com.vmeduri.fintrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = BrandSecondary,
    tertiary = WarningColor,
    error = DangerColor
)

private val DarkColors = darkColorScheme(
    primary = BrandSecondary,
    secondary = BrandPrimary,
    tertiary = WarningColor,
    error = DangerColor
)

@Composable
fun SpendTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = Typography, content = content)
}
