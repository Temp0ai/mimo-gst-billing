package com.mimo.gstbilling.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

object ThemeManager {
    var isDarkMode = mutableStateOf(false)
}

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Secondary,
    onSecondary = Color.White,
    background = LightBlueBg,
    surface = Color.White,
    surfaceVariant = Color(0xFFF5F6F6),
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Error,
    outline = Color(0xFFE0E0E0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Secondary,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFAAAAAA),
    error = Error,
    outline = Color(0xFF444444)
)

@Composable
fun MimoGstBillingTheme(content: @Composable () -> Unit) {
    val darkMode by ThemeManager.isDarkMode
    val colorScheme = if (darkMode) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkMode) Color(0xFF121212).toArgb() else Primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkMode
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
