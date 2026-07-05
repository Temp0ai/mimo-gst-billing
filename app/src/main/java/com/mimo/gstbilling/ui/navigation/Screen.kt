package com.mimo.gstbilling.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object CreateInvoice : Screen("create_invoice")
    object Parties : Screen("parties")
    object PartyDetail : Screen("party_detail/{partyId}") {
        fun createRoute(partyId: Long) = "party_detail/$partyId"
    }
    object Items : Screen("items")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object BusinessProfile : Screen("business_profile")
    object Sales : Screen("sales")
    object Purchases : Screen("purchases")
    object Expenses : Screen("expenses")
    object CashBank : Screen("cash_bank")
    object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: Long) = "item_detail/$itemId"
    }
    object AddParty : Screen("add_party")
    object AddItem : Screen("add_item")
    object InvoiceDetail : Screen("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_detail/$invoiceId"
    }
    object Manufacturing : Screen("manufacturing")
    object StoreManagement : Screen("store_management")
    object BarcodeScanner : Screen("barcode_scanner")
    object StockTransfer : Screen("stock_transfer")
    object Orders : Screen("orders")
    object PartyGroups : Screen("party_groups")
    object PartyStatement : Screen("party_statement/{partyId}") {
        fun createRoute(partyId: Long) = "party_statement/$partyId"
    }
    object PaymentReminders : Screen("payment_reminders")
    object Gstr1Report : Screen("gstr1_report")
    object Gstr3bReport : Screen("gstr3b_report")
    object DayBookReport : Screen("day_book_report")
    object CashFlowReport : Screen("cash_flow_report")
    object BalanceSheet : Screen("balance_sheet")
    object ProfitLossReport : Screen("profit_loss_report")
    object ExpenseCategoryReport : Screen("expense_category_report")
    object ItemBatchTracking : Screen("item_batch_tracking")
    object ImportData : Screen("import_data")
    object ExportData : Screen("export_data")
    object ThermalPrinter : Screen("thermal_printer")
    object BackupRestore : Screen("backup_restore")
    object EditInvoice : Screen("edit_invoice/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "edit_invoice/$invoiceId"
    }
    object EditParty : Screen("edit_party/{partyId}") {
        fun createRoute(partyId: Long) = "edit_party/$partyId"
    }
    object InvoiceTemplates : Screen("invoice_templates")
}
