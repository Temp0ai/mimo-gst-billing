package com.mimo.gstbilling.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.mimo.gstbilling.ui.screens.*
import java.net.URLDecoder

@Composable
fun MimoNavHost(navController: NavHostController, startDestination: String = Screen.Dashboard.route) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(
            route = Screen.CreateInvoice.route,
            arguments = listOf(
                navArgument("partyId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("invoiceType") { type = NavType.StringType; defaultValue = "sales" }
            )
        ) { backStackEntry ->
            val partyId = backStackEntry.arguments?.getLong("partyId") ?: -1L
            val invoiceType = backStackEntry.arguments?.getString("invoiceType") ?: "sales"
            CreateInvoiceScreen(navController, preselectedPartyId = if (partyId > 0) partyId else null, invoiceType = invoiceType)
        }
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
        composable(
            route = Screen.AddItemToSale.route,
            arguments = listOf(navArgument("invoiceType") { type = NavType.StringType; defaultValue = "sales" })
        ) { backStackEntry ->
            val invoiceType = backStackEntry.arguments?.getString("invoiceType") ?: "sales"
            AddItemToSaleScreen(navController, invoiceType = invoiceType)
        }
        composable(route = Screen.InvoiceDetail.route, arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            InvoiceDetailScreen(navController, invoiceId)
        }
        composable(route = Screen.InvoicePreview.route, arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            InvoicePreviewScreen(navController, invoiceId)
        }
        composable(Screen.Manufacturing.route) { ManufacturingScreen(navController) }
        composable(Screen.StoreManagement.route) { StoreManagementScreen(navController) }
        composable(Screen.BarcodeScanner.route) { BarcodeScannerScreen(navController) }
        composable(Screen.StockTransfer.route) { StockTransferScreen(navController) }
        composable(Screen.Orders.route) { OrdersScreen(navController) }
        composable(
            route = Screen.CreateOrder.route,
            arguments = listOf(navArgument("orderType") { type = NavType.StringType; defaultValue = "sales_order" })
        ) { backStackEntry ->
            val orderType = backStackEntry.arguments?.getString("orderType") ?: "sales_order"
            CreateOrderScreen(navController, orderType = orderType)
        }
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            OrderDetailScreen(navController, orderId)
        }
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
        composable(Screen.StockTransferReport.route) { StockTransferReportScreen(navController) }
        composable(Screen.StockTransferDetailReport.route) { StockTransferDetailReportScreen(navController) }
        composable(Screen.SummaryByHsnReport.route) { SummaryByHsnReportScreen(navController) }
        composable(Screen.SalePurchaseAmountReport.route) { SalePurchaseAmountReportScreen(navController) }
        composable(Screen.SalePurchaseExpenseReport.route) { SalePurchaseExpenseReportScreen(navController) }
        composable(Screen.PartyReport.route) { PartyReportScreen(navController) }
        composable(Screen.PartyReportByItem.route) { PartyReportByItemScreen(navController) }
        composable(Screen.PartyGroupSalePurchaseReport.route) { PartyGroupSalePurchaseReportScreen(navController) }
        composable(Screen.ItemWiseProfitLossReport.route) { ItemWiseProfitLossReportScreen(navController) }
        composable(Screen.BillWiseProfitLossReport.route) { BillWiseProfitLossReportScreen(navController) }
        composable(Screen.TaxRateReport.route) { TaxRateReportScreen(navController) }
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
        composable(Screen.Gstr1Filing.route) { Gstr1FilingScreen(navController) }
        composable(Screen.Gstr3bFiling.route) { Gstr3bFilingScreen(navController) }
        composable(Screen.EWayBill.route) { EWayBillScreen(navController) }
        composable(Screen.ItemSettings.route) { ItemSettingsScreen(navController) }
        composable(Screen.BusinessCardDesigner.route) { BusinessCardDesignerScreen(navController) }
        composable(Screen.PartySettings.route) { PartySettingsScreen(navController) }
        composable(Screen.TransactionSettings.route) { TransactionSettingsScreen(navController) }
        composable(Screen.TaxSettings.route) { TaxSettingsScreen(navController) }
        composable(Screen.BankAccounts.route) { BankAccountScreen(navController) }
        composable(Screen.RecycleBin.route) { RecycleBinScreen(navController) }
        composable(Screen.AgingReport.route) { AgingReportScreen(navController, hiltViewModel()) }
        composable(Screen.PaymentTerms.route) { PaymentTermsScreen(navController) }
        composable(Screen.TransactionPrefixes.route) { TransactionPrefixScreen(navController) }
        composable(Screen.WhatsAppCards.route) { WhatsAppCardsScreen(navController) }
        composable(Screen.Gstr2Filing.route) { Gstr2FilingScreen(navController) }
        composable(Screen.Gstr4Filing.route) { Gstr4FilingScreen(navController) }
        composable(Screen.Gstr9aFiling.route) { Gstr9aFilingScreen(navController) }
        composable(Screen.HsnSummary.route) { HsnSummaryScreen(navController) }
        composable(Screen.BillWiseProfitLoss.route) { BillWiseProfitLossScreen(navController) }
        composable(Screen.PartyWiseProfitLoss.route) { PartyWiseProfitLossScreen(navController) }
        composable(Screen.InvoiceSettings.route) { InvoiceSettingsScreen(navController) }
        composable(Screen.PrintFormat.route) { PrintFormatScreen(navController) }
        composable(Screen.StaffSettings.route) { StaffSettingsScreen(navController) }
        composable(Screen.Permissions.route) { PermissionsScreen(navController) }
        composable(Screen.SmsTemplates.route) { SmsTemplatesScreen(navController) }
        composable(Screen.AutoSend.route) { AutoSendScreen(navController) }
        composable(Screen.StockAlerts.route) { StockAlertsScreen(navController) }
        composable(Screen.UnitsCategories.route) { UnitsCategoriesScreen(navController) }
        composable(Screen.CurrencySettings.route) { CurrencySettingsScreen(navController) }
        composable(Screen.StockSummaryReport.route) { StockSummaryReportScreen(navController) }
        composable(Screen.LowStockReport.route) { LowStockReportScreen(navController) }
        composable(Screen.CategoryStockReport.route) { CategoryStockReportScreen(navController) }
        composable(Screen.CategorySalePurchase.route) { CategorySalePurchaseScreen(navController) }
        composable(Screen.SerialReport.route) { SerialReportScreen(navController) }
        composable(Screen.BankStatementReport.route) { BankStatementReportScreen(navController) }
        composable(Screen.GstrSummary.route) { GstrSummaryScreen(navController) }
        composable(Screen.Form27Eq.route) { Form27EqScreen(navController) }
        composable(Screen.TcsReceivable.route) { TcsReceivableScreen(navController) }
        composable(Screen.TdsPayable.route) { TdsPayableScreen(navController) }
        composable(Screen.TdsReceivable.route) { TdsReceivableScreen(navController) }
        composable(Screen.SacReport.route) { SacReportScreen(navController) }
        composable(Screen.AllTransactionsReport.route) { AllTransactionsReportScreen(navController) }
        composable(Screen.ExpenseTransactionReport.route) { ExpenseTransactionReportScreen(navController) }
        composable(Screen.AllPartiesReport.route) { AllPartiesReportScreen(navController) }
        composable(Screen.VyaparDataImport.route) { VyaparDataImportScreen(navController) }
        composable(Screen.CreditNote.route) { CreditNoteScreen(navController) }
        composable(Screen.DebitNote.route) { DebitNoteScreen(navController) }
        composable(Screen.DeliveryChallan.route) { DeliveryChallanScreen(navController) }
        composable(Screen.CompanySwitch.route) { CompanySwitchScreen(navController) }
        composable(Screen.BiometricLock.route) { BiometricLockScreen(navController) }
        composable(Screen.BiometricSettings.route) { BiometricSettingsScreen(navController) }
        composable(Screen.StaffManagement.route) { StaffManagementScreen(navController) }
        composable(Screen.RecurringInvoices.route) { RecurringInvoicesScreen(navController) }
        composable(Screen.DiscountConfig.route) { DiscountConfigScreen(navController) }
        composable(Screen.ProformaInvoice.route, arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            ProformaInvoiceScreen(navController, invoiceId)
        }
        composable(Screen.Quotation.route, arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            QuotationScreen(navController, invoiceId)
        }
        composable(Screen.DeliveryNote.route, arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            DeliveryNoteScreen(navController, invoiceId)
        }
        composable(Screen.PaymentReceived.route) { PaymentReceivedScreen(navController) }
        composable(Screen.PaymentMade.route) { PaymentMadeScreen(navController) }
        composable(Screen.GoodsReceiptNote.route) { GoodsReceiptNoteScreen(navController) }
        composable(Screen.TdsTcsPayment.route) { TdsTcsPaymentScreen(navController) }
        composable(Screen.AdvancePayment.route) { AdvancePaymentScreen(navController) }
        composable(Screen.NotificationSettings.route) { NotificationSettingsScreen(navController) }
        composable(Screen.AppLockSettings.route) { AppLockSettingsScreen(navController) }
        composable(Screen.PrintSettings.route) { PrintSettingsScreen(navController) }
        composable(Screen.EmailSettings.route) { EmailSettingsScreen(navController) }
        composable(Screen.BankReconciliation.route) { BankReconciliationScreen(navController) }
        composable(Screen.TdsReceivableDetail.route) { TdsReceivableDetailScreen(navController) }
        composable(Screen.Gstr9.route) { Gstr9Screen(navController) }
        composable(Screen.UserProfile.route) { UserProfileScreen(navController) }
        composable(Screen.RoleManagement.route) { RoleManagementScreen(navController) }
        composable(Screen.DeliveryTracking.route) { DeliveryTrackingScreen(navController) }
        composable(Screen.ExpenseApproval.route) { ExpenseApprovalScreen(navController) }
        composable(Screen.Subscription.route) { SubscriptionScreen(navController) }
        composable(Screen.AnalyticsDashboard.route) { AnalyticsDashboardScreen(navController) }
        composable(Screen.ItemPriceList.route) { ItemPriceListScreen(navController) }
        composable(Screen.FixedAssetsList.route) { FixedAssetsListScreen(navController) }
        composable(Screen.FixedAssetDetail.route, arguments = listOf(navArgument("assetId") { type = NavType.LongType })) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getLong("assetId") ?: 0L
            FixedAssetDetailScreen(navController, assetId)
        }
        composable(Screen.AddFixedAsset.route) { AddFixedAssetScreen(navController) }
        composable(Screen.Catalogue.route) { CatalogueScreen(navController) }
        composable(Screen.ViewStore.route) { ViewStoreScreen(navController) }
        composable(Screen.CatalogueItemDetail.route, arguments = listOf(navArgument("itemId") { type = NavType.LongType })) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
            CatalogueItemDetailScreen(navController, itemId)
        }
        composable(Screen.OnlineOrderList.route) { OnlineOrderListScreen(navController) }
        composable(Screen.SmsList.route) { SmsListScreen(navController) }
        composable(Screen.MultiplePartyReminder.route) { MultiplePartyReminderScreen(navController) }
        composable(Screen.InputReminderMessage.route) { InputReminderMessageScreen(navController) }
        composable(Screen.MessageToParty.route) { MessageToPartyScreen(navController) }
        composable(Screen.Gstr2Report.route) { Gstr2ReportScreen(navController) }
        composable(Screen.TcsManagement.route) { TcsManagementScreen(navController) }
        composable(Screen.TcsReport.route) { TcsReportScreen(navController) }
        composable(Screen.SecurityLog.route) { SecurityLogScreen(navController) }
        composable(Screen.About.route) { AboutScreen(navController) }
        composable(Screen.BarcodeManagement.route) { BarcodeManagementScreen(navController) }
        composable(Screen.ContinuousScanning.route) { ContinuousScanningScreen(navController) }
        composable(Screen.CustomReportBuilder.route) { CustomReportBuilderScreen(navController) }
        composable(Screen.ItemBulkOperations.route) { ItemBulkOperationsScreen(navController) }
        composable(Screen.KycIntro.route) { KycIntroScreen(navController) }
        composable(Screen.KycVerification.route) { KycVerificationScreen(navController) }
        composable(Screen.MultiFirmSettings.route) { MultiFirmSettingsScreen(navController) }
        composable(Screen.ProfitOnInvoice.route) { ProfitOnInvoiceScreen(navController) }
        composable(Screen.RemindersHub.route) { RemindersHubScreen(navController) }
        composable(Screen.ReportSchedule.route) { ReportScheduleScreen(navController) }
        composable(Screen.TransactionSelect.route) { TransactionSelectScreen(navController) }
        composable(Screen.TrendingItems.route) { TrendingItemsScreen(navController) }
        composable(Screen.UnitMapping.route) { UnitMappingScreen(navController) }
        composable(Screen.WhatsAppPreview.route) { WhatsAppPreviewScreen(navController) }
        composable(Screen.CashInHandAdjustment.route) { CashInHandAdjustmentScreen(navController) }
        composable(Screen.CashInHandDetail.route) { CashInHandDetailScreen(navController) }
        composable(Screen.TransferMoney.route) { TransferMoneyScreen(navController) }
        composable(Screen.BankStatementDetail.route) { BankStatementScreen(navController) }
        composable(Screen.BankDetail.route) { BankDetailScreen(navController) }
        composable(Screen.ChequeList.route) { ChequeListScreen(navController) }
        composable(Screen.ChequeDetail.route) { ChequeDetailScreen(navController) }
        composable(Screen.ChequeClose.route) { ChequeCloseScreen(navController) }
        composable(Screen.LoanAccountsList.route) { LoanAccountsListScreen(navController) }
        composable(Screen.AddLoan.route) { AddLoanScreen(navController) }
        composable(
            route = Screen.LoanDetail.route,
            arguments = listOf(navArgument("loanId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments?.getLong("loanId") ?: 0L
            LoanDetailScreen(navController)
        }
        composable(Screen.LoanTransaction.route) { LoanTransactionScreen(navController) }
        composable(Screen.LoanExpense.route) { LoanExpenseScreen(navController) }
        composable(Screen.CashBook.route) { CashBookScreen(navController) }
        composable(Screen.BankReconciliationDetail.route) { BankReconciliationDetailScreen(navController) }
    }
}
