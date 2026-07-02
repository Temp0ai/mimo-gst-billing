package com.mimo.gstbilling.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val DarkPrimary = Color(0xFF90CAF9)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkOnPrimary = Color(0xFF000000)
val DarkTextPrimary = Color(0xFFE0E0E0)
val DarkTextSecondary = Color(0xFF9E9E9E)
val DarkGreenBalance = Color(0xFF66BB6A)
val DarkRedAccent = Color(0xFFEF5350)
val DarkBlueHeader = Color(0xFF42A5F5)

object ThemeManager {
    var isDarkMode by mutableStateOf(false)
}

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = Background,
    surface = Surface,
    error = Error
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkRedAccent,
    onSecondary = Color.White,
    background = DarkBackground,
    surface = DarkSurface,
    error = DarkRedAccent
)

@Composable
fun MimoGstBillingTheme(content: @Composable () -> Unit) {
    val colorScheme = if (ThemeManager.isDarkMode) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !ThemeManager.isDarkMode
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
