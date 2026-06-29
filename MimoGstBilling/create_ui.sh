#!/bin/bash
PROJECT="/root/Downloads/mimo ai/MimoGstBilling/app/src/main/java/com/mimo/gstbilling"

# 1. Color.kt
cat > "$PROJECT/ui/theme/Color.kt" << 'KTOFF'
package com.mimo.gstbilling.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF1976D2)
val PrimaryDark = Color(0xFF1565C0)
val Secondary = Color(0xFFFF9800)
val Background = Color(0xFFF5F5F5)
val Surface = Color(0xFFFFFFFF)
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFF000000)
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFA726)
val Error = Color(0xFFE53935)
val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)
val Divider = Color(0xFFE0E0E0)
KTOFF

# 2. Theme.kt
cat > "$PROJECT/ui/theme/Theme.kt" << 'KTOFF'
package com.mimo.gstbilling.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = Background,
    surface = Surface,
    onSurface = TextPrimary,
    error = Error,
    onError = OnPrimary
)

@Composable
fun MimoGstBillingTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = Typography, content = content)
}
KTOFF

# 3. Type.kt
cat > "$PROJECT/ui/theme/Type.kt" << 'KTOFF'
package com.mimo.gstbilling.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography()
KTOFF

# 4. Navigation
cat > "$PROJECT/ui/navigation/MimoNavHost.kt" << 'KTOFF'
package com.mimo.gstbilling.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mimo.gstbilling.ui.screens.*

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object CreateInvoice : Screen("create_invoice")
    object Parties : Screen("parties")
    object Items : Screen("items")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}

@Composable
fun MimoNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.CreateInvoice.route) { CreateInvoiceScreen(navController) }
        composable(Screen.Parties.route) { PartiesScreen(navController) }
        composable(Screen.Items.route) { ItemsScreen(navController) }
        composable(Screen.Reports.route) { ReportsScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
    }
}
KTOFF

echo "UI foundation created!"
