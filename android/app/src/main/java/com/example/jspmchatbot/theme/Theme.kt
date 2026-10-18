package com.example.jspmchatbot.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PortalPrimary,
    onPrimary = Color.White,
    primaryContainer = PortalSurfaceVariant,
    onPrimaryContainer = PortalPrimaryDark,
    secondary = PortalSecondary,
    onSecondary = Color.White,
    tertiary = PortalAccent,
    background = PortalBg,
    onBackground = TextPrimary,
    surface = PortalSurface,
    onSurface = TextPrimary,
    surfaceVariant = PortalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = StatusRed,
    onError = Color.White,
    outline = PortalCardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = PortalPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PortalPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = PortalSecondary,
    onSecondary = Color.White,
    tertiary = PortalAccent,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = StatusRed,
    onError = Color.White,
    outline = Color(0xFF475569)
)

@Composable
fun JSPMChatbotTheme(
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
