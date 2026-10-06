package com.renger.system.note.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Purple = Color(0xFF6C63FF)

private val DarkColors = darkColorScheme(
    primary = Purple,
    background = Color(0xFF0F0F14),
    surface = Color(0xFF1A1A22),
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED)
)

private val LightColors = lightColorScheme(
    primary = Purple,
    background = Color(0xFFF5F5FA),
    surface = Color.White,
    onBackground = Color(0xFF1A1A22),
    onSurface = Color(0xFF1A1A22)
)

@Composable
fun RengerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}