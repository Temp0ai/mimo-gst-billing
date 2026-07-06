package com.mimo.gstbilling.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mimo.gstbilling.ui.screens.*
import java.net.URLDecoder

@Composable
fun MimoNavHost(navController: NavHostController, startDestination: String = Screen.Dashboard.route) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.CreateInvoice.route) { CreateInvoiceScreen(navController) }
        composable(Screen.Parties.route) { PartiesScreen(navController) }
        composable(route = Screen.PartyDetail.route, arguments = listOf(navArgument("partyId") { type = NavType.LongType })) { backStackEntry ->
            val partyId = backStackEntry.arguments?.getLong("partyId") ?: 0L
            PartyDetailScreen(navController, partyId)
        }
        composable(Screen.Items.route) { ItemsScreen(navController) }
        composable(route = Screen.ItemDetail.route, arguments = listOf(navArgument("itemId") { type = NavType.LongType })) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
            ItemDetailScreen(navController, itemId)
        }
        composable(Screen.Reports.route) { ReportsScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.BusinessProfile.route) { BusinessProfileScreen(navController) }
        composable(Screen.Sales.route) { SalesScreen(navController) }
        composable(Screen.Purchases.route) { PurchasesScreen(navController) }
        composable(Screen.Expenses.route) { ExpensesScreen(navController) }
        composable(Screen.CashBank.route) { CashBankScreen(navController) }
        composable(Screen.AddParty.route) { AddPartyScreen(navController) }
        composable(Screen.AddItem.route) { AddItemScreen(navController) }
        composable(route = Screen.InvoiceDetail.route, arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            InvoiceDetailScreen(navController, invoiceId)
        }
        composable(Screen.Manufacturing.route) { ManufacturingScreen(navController) }
        composable(Screen.StoreManagement.route) { StoreManagementScreen(navController) }
        composable(Screen.BarcodeScanner.route) { BarcodeScannerScreen(navController) }
        composable(Screen.StockTransfer.route) { StockTransferScreen(navController) }
        composable(Screen.Orders.route) { OrdersScreen(navController) }
        composable(Screen.PartyGroups.route) { PartyGroupsScreen(navController) }
        composable(route = Screen.PartyStatement.route, arguments = listOf(navArgument("partyId") { type = NavType.LongType })) { backStackEntry ->
            val partyId = backStackEntry.arguments?.getLong("partyId") ?: 0L
            PartyStatementScreen(navController, partyId)
        }
        composable(Screen.PaymentReminders.route) { PaymentRemindersScreen(navController) }
        composable(Screen.Gstr1Report.route) { Gstr1ReportScreen(navController) }
        composable(Screen.Gstr3bReport.route) { Gstr3bReportScreen(navController) }
        composable(Screen.DayBookReport.route) { DayBookReportScreen(navController) }
        composable(Screen.CashFlowReport.route) { CashFlowReportScreen(navController) }
        composable(Screen.BalanceSheet.route) { BalanceSheetScreen(navController) }
        composable(Screen.ProfitLossReport.route) { ProfitLossReportScreen(navController) }
        composable(Screen.ExpenseCategoryReport.route) { ExpenseCategoryReportScreen(navController) }
        composable(Screen.ItemBatchTracking.route) { ItemBatchScreen(navController) }
        composable(Screen.ImportData.route) { ImportDataScreen(navController) }
        composable(Screen.ExportData.route) { ExportDataScreen(navController) }
        composable(Screen.InvoiceTemplates.route) { InvoiceTemplatesScreen(navController) }
        composable(Screen.ThermalPrinter.route) { ThermalPrinterScreen(navController) }
        composable(Screen.BackupRestore.route) { BackupRestoreScreen(navController) }
        composable(route = Screen.EditParty.route, arguments = listOf(navArgument("partyId") { type = NavType.LongType })) { backStackEntry ->
            val partyId = backStackEntry.arguments?.getLong("partyId") ?: 0L
            EditPartyScreen(navController, partyId)
        }
        composable(route = Screen.EditInvoice.route, arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            EditInvoiceScreen(navController, invoiceId)
        }
        composable(route = Screen.SettingsDetail.route, arguments = listOf(navArgument("title") { type = NavType.StringType })) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Settings"
            SettingsDetailScreen(navController, URLDecoder.decode(title, "UTF-8"))
        }
        composable(Screen.PartyReportByItems.route) { PartyReportByItemsScreen(navController) }
        composable(Screen.SalePurchaseByParty.route) { SalePurchaseByPartyScreen(navController) }
        composable(Screen.ItemReportByParty.route) { ItemReportByPartyScreen(navController) }
        composable(Screen.ItemWiseProfitLoss.route) { ItemWiseProfitLossScreen(navController) }
        composable(Screen.StockDetailReport.route) { StockDetailReportScreen(navController) }
        composable(Screen.ItemWiseDiscount.route) { ItemWiseDiscountScreen(navController) }
        composable(Screen.DiscountReport.route) { DiscountReportScreen(navController) }
        composable(Screen.TaxReport.route) { TaxReportScreen(navController) }
        composable(Screen.ExpenseItemReport.route) { ExpenseItemReportScreen(navController) }
        composable(Screen.OrderItemReport.route) { OrderItemReportScreen(navController) }
        composable(Screen.LoanStatement.route) { LoanStatementScreen(navController) }
        composable(Screen.VyaparImport.route) { VyaparImportScreen(navController) }
        composable(Screen.Gstr1Filing.route) { Gstr1FilingScreen(navController) }
        composable(Screen.Gstr3bFiling.route) { Gstr3bFilingScreen(navController) }
        composable(Screen.EWayBill.route) { EWayBillScreen(navController) }
        composable(Screen.ItemSettings.route) { ItemSettingsScreen(navController) }
    }
}
