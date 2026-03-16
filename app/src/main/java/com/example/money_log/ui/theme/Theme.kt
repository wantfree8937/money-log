package com.example.money_log.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MainGreen,
    onPrimary = Color.Black,
    primaryContainer = PrimaryGreen.copy(alpha = 0.3f),
    onPrimaryContainer = MainGreen,
    secondary = MainGreen,
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    onSurfaceVariant = Color.LightGray,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = MainGreen,
    onPrimary = Color.White,
    primaryContainer = LightGreen,
    onPrimaryContainer = PrimaryGreen,
    secondary = PrimaryGreen,
    onSecondary = Color.White,
    background = BackgroundGray,
    surface = SurfaceWhite,
    onSurface = TextDark,
    onSurfaceVariant = TextGray,
    error = ErrorRed
)

@Composable
fun MoneyLogTheme(
    darkMode: String = "system",
    // 프리미엄 브랜드 룩의 일관성을 위해 dynamicColor 비활성화
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (darkMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}