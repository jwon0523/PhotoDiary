package com.example.oneframe.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val ColorScheme = lightColorScheme(
    primary = Color(0xFFFBEED7),
    onPrimary = Color(0xFF3E3E3E),
    primaryContainer = Color(0xFFE4B98F),
    onPrimaryContainer = Color(0xFF2C1F12),
    secondary = Color(0xFFA9DDD6),
    onSecondary = Color(0xFF0D4F4D),
    tertiary = Color(0xFFD5C6E0),
    background = Color(0xFFFFFDF9),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2B2B2B),
    error = Color(0xFFD32F2F),
    outline = Color.Gray
)

@Composable
fun OneFrameTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}