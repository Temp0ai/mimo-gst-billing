package com.mimo.gstbilling.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object CreateInvoice : Screen("create_invoice?partyId={partyId}&invoiceType={invoiceType}") {
        fun createRoute(partyId: Long = -1L, invoiceType: String = "sales") = 
            if (partyId > 0) "create_invoice?partyId=$partyId&invoiceType=$invoiceType" 
            else "create_invoice?invoiceType=$invoiceType"
    }
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
    object SettingsDetail : Screen("settings_detail/{title}") {
        fun createRoute(title: String) = "settings_detail/$title"
    }
    object PartyReportByItems : Screen("party_report_by_items")
    object SalePurchaseByParty : Screen("sale_purchase_by_party")
    object ItemReportByParty : Screen("item_report_by_party")
    object ItemWiseProfitLoss : Screen("item_wise_profit_loss")
    object StockDetailReport : Screen("stock_detail_report")
    object ItemWiseDiscount : Screen("item_wise_discount")
    object DiscountReport : Screen("discount_report")
    object TaxReport : Screen("tax_report")
    object ExpenseItemReport : Screen("expense_item_report")
    object OrderItemReport : Screen("order_item_report")
    object LoanStatement : Screen("loan_statement")
    object VyaparImport : Screen("vyapar_import")
    object Gstr1Filing : Screen("gstr1_filing")
    object Gstr3bFiling : Screen("gstr3b_filing")
    object EWayBill : Screen("e_way_bill")
    object ItemSettings : Screen("item_settings")
    object BusinessCardDesigner : Screen("business_card_designer")
    object PartySettings : Screen("party_settings")
    object TransactionSettings : Screen("transaction_settings")
    object TaxSettings : Screen("tax_settings")
    object BankAccounts : Screen("bank_accounts")
    object RecycleBin : Screen("recycle_bin")
    object AgingReport : Screen("aging_report")
    object PaymentTerms : Screen("payment_terms")
    object TransactionPrefixes : Screen("transaction_prefixes")
    object WhatsAppCards : Screen("whatsapp_cards")
    object Gstr2Filing : Screen("gstr2_filing")
    object Gstr4Filing : Screen("gstr4_filing")
    object Gstr9aFiling : Screen("gstr9a_filing")
    object HsnSummary : Screen("hsn_summary")
    object BillWiseProfitLoss : Screen("bill_wise_profit_loss")
    object PartyWiseProfitLoss : Screen("party_wise_profit_loss")
    object InvoiceSettings : Screen("invoice_settings")
    object PrintFormat : Screen("print_format")
    object StaffSettings : Screen("staff_settings")
    object Permissions : Screen("permissions")
    object SmsTemplates : Screen("sms_templates")
    object AutoSend : Screen("auto_send")
    object StockAlerts : Screen("stock_alerts")
    object UnitsCategories : Screen("units_categories")
    object CurrencySettings : Screen("currency_settings")
    object StockSummaryReport : Screen("stock_summary_report")
    object LowStockReport : Screen("low_stock_report")
    object CategoryStockReport : Screen("category_stock_report")
    object CategorySalePurchase : Screen("category_sale_purchase")
    object SerialReport : Screen("serial_report")
    object BankStatementReport : Screen("bank_statement_report")
    object GstrSummary : Screen("gstr_summary")
    object Form27Eq : Screen("form_27eq")
    object TcsReceivable : Screen("tcs_receivable")
    object TdsPayable : Screen("tds_payable")
    object TdsReceivable : Screen("tds_receivable")
    object SacReport : Screen("sac_report")
    object AllTransactionsReport : Screen("all_transactions_report")
    object ExpenseTransactionReport : Screen("expense_transaction_report")
    object AllPartiesReport : Screen("all_parties_report")
    object VyaparDataImport : Screen("vyapar_data_import")
    object CreditNote : Screen("credit_note")
    object DebitNote : Screen("debit_note")
    object DeliveryChallan : Screen("delivery_challan")
    object CompanySwitch : Screen("company_switch")
    object BiometricLock : Screen("biometric_lock")
    object BiometricSettings : Screen("biometric_settings")
}
