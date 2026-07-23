package com.odysseus.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarmLightColors = lightColorScheme(
    primary = Color(0xFFD96B43), // Claude Warm Terracotta
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3EFE9), // Warm Cream
    onPrimaryContainer = Color(0xFF2C241E),
    secondary = Color(0xFF8A7E72), // Warm Gray/Taupe
    onSecondary = Color.White,
    tertiary = Color(0xFF5C8A75), // Soft Sage Green
    onTertiary = Color.White,
    background = Color(0xFFFAF6F0), // Claude Warm Paper
    onBackground = Color(0xFF1E1B18), // Deep Charcoal Brown
    surface = Color(0xFFFCFAF7), // Slightly Lighter Paper
    onSurface = Color(0xFF1E1B18),
    surfaceVariant = Color(0xFFF0EAE1),
    onSurfaceVariant = Color(0xFF4E4740),
    outline = Color(0xFFC7BDB1)
)

private val WarmDarkColors = darkColorScheme(
    primary = Color(0xFFE28766), // Slightly lighter/brighter terracotta for dark mode
    onPrimary = Color(0xFF3E1C0F),
    primaryContainer = Color(0xFF352B24), // Deep warm brown
    onPrimaryContainer = Color(0xFFF0EAE1),
    secondary = Color(0xFFA59A8E),
    onSecondary = Color(0xFF2C2722),
    tertiary = Color(0xFF75A38D),
    onTertiary = Color(0xFF1D3528),
    background = Color(0xFF12100E), // Soft dark brown/black
    onBackground = Color(0xFFEAE5DE), // Cream text
    surface = Color(0xFF181512),
    onSurface = Color(0xFFEAE5DE),
    surfaceVariant = Color(0xFF2C2520),
    onSurfaceVariant = Color(0xFFC7BDB1),
    outline = Color(0xFF80756B)
)

@Composable
fun OdysseusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) WarmDarkColors else WarmLightColors

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
