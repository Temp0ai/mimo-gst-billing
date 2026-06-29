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
