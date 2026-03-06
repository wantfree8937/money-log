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
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamicColor for premium branded look consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Simplified for this redesign to focus on the provided light theme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}