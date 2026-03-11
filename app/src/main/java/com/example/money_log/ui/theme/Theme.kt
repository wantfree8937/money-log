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
    // 프리미엄 브랜드 룩의 일관성을 위해 dynamicColor 비활성화
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 제공된 라이트 테마에 집중하기 위해 단순화됨
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}