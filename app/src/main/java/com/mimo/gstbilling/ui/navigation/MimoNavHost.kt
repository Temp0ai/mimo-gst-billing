package com.mimo.gstbilling.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mimo.gstbilling.ui.screens.AddItemScreen
import com.mimo.gstbilling.ui.screens.AddPartyScreen
import com.mimo.gstbilling.ui.screens.CashBankScreen
import com.mimo.gstbilling.ui.screens.CreateInvoiceScreen
import com.mimo.gstbilling.ui.screens.DashboardScreen
import com.mimo.gstbilling.ui.screens.ExpensesScreen
import com.mimo.gstbilling.ui.screens.InvoiceDetailScreen
import com.mimo.gstbilling.ui.screens.ItemsScreen
import com.mimo.gstbilling.ui.screens.PartiesScreen
import com.mimo.gstbilling.ui.screens.PartyDetailScreen
import com.mimo.gstbilling.ui.screens.PurchasesScreen
import com.mimo.gstbilling.ui.screens.ReportsScreen
import com.mimo.gstbilling.ui.screens.SalesScreen
import com.mimo.gstbilling.ui.screens.SettingsScreen

@Composable
fun MimoNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.CreateInvoice.route) {
            CreateInvoiceScreen(navController)
        }
        composable(Screen.Parties.route) {
            PartiesScreen(navController)
        }
        composable(
            route = Screen.PartyDetail.route,
            arguments = listOf(navArgument("partyId") { type = NavType.LongType })
        ) {
            PartyDetailScreen(navController)
        }
        composable(Screen.Items.route) {
            ItemsScreen(navController)
        }
        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) {
            ItemsScreen(navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        composable(Screen.Sales.route) {
            SalesScreen(navController)
        }
        composable(Screen.Purchases.route) {
            PurchasesScreen(navController)
        }
        composable(Screen.Expenses.route) {
            ExpensesScreen(navController)
        }
        composable(Screen.CashBank.route) {
            CashBankScreen(navController)
        }
        composable(Screen.AddParty.route) {
            AddPartyScreen(navController)
        }
        composable(Screen.AddItem.route) {
            AddItemScreen(navController)
        }
        composable(
            route = Screen.InvoiceDetail.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
        ) {
            InvoiceDetailScreen(navController)
        }
    }
}
