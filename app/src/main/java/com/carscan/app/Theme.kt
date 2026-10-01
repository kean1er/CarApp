package com.carscan.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CarTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) {
        darkColorScheme(
            primary = Color(0xFF0A84FF),
            background = Color(0xFF000000),
            surface = Color(0xFF1C1C1E),
            surfaceVariant = Color(0xFF2C2C2E),
            onBackground = Color.White,
            onSurface = Color.White,
            onSurfaceVariant = Color(0xFF8E8E93)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF007AFF),
            background = Color(0xFFF2F2F7),
            surface = Color.White,
            surfaceVariant = Color(0xFFE5E5EA),
            onBackground = Color.Black,
            onSurface = Color.Black,
            onSurfaceVariant = Color(0xFF8E8E93)
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
