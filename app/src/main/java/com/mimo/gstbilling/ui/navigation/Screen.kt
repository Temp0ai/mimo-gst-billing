package com.mimo.gstbilling.ui.navigation

sealed class Screen(val route: String) {
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
    object AddItemToSale : Screen("add_item_to_sale?invoiceType={invoiceType}") {
        fun createRoute(invoiceType: String = "sales") = "add_item_to_sale?invoiceType=$invoiceType"
    }
    object InvoiceDetail : Screen("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_detail/$invoiceId"
    }
    object Manufacturing : Screen("manufacturing")
    object StoreManagement : Screen("store_management")
    object BarcodeScanner : Screen("barcode_scanner")
    object StockTransfer : Screen("stock_transfer")
    object Orders : Screen("orders")
    object CreateOrder : Screen("create_order?orderType={orderType}") {
        fun createRoute(orderType: String = "sale") = "create_order?orderType=$orderType"
    }
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "order_detail/$orderId"
    }
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
    object StockTransferReport : Screen("stock_transfer_report")
    object StockTransferDetailReport : Screen("stock_transfer_detail_report")
    object SummaryByHsnReport : Screen("summary_by_hsn_report")
    object SalePurchaseAmountReport : Screen("sale_purchase_amount_report")
    object SalePurchaseExpenseReport : Screen("sale_purchase_expense_report")
    object PartyReport : Screen("party_report")
    object PartyReportByItem : Screen("party_report_by_item")
    object PartyGroupSalePurchaseReport : Screen("party_group_sale_purchase_report")
    object ItemWiseProfitLossReport : Screen("item_wise_profit_loss_report")
    object BillWiseProfitLossReport : Screen("bill_wise_profit_loss_report")
    object TaxRateReport : Screen("tax_rate_report")
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
    object StaffManagement : Screen("staff_management")
    object RecurringInvoices : Screen("recurring_invoices")
    object DiscountConfig : Screen("discount_config")
    object ProformaInvoice : Screen("proforma_invoice/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "proforma_invoice/$invoiceId"
    }
    object Quotation : Screen("quotation/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "quotation/$invoiceId"
    }
    object DeliveryNote : Screen("delivery_note/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "delivery_note/$invoiceId"
    }
    object NotificationSettings : Screen("notification_settings")
    object AppLockSettings : Screen("app_lock_settings")
    object PrintSettings : Screen("print_settings")
    object EmailSettings : Screen("email_settings")
    object BankReconciliation : Screen("bank_reconciliation")
    object TdsReceivableDetail : Screen("tds_receivable_detail")
    object Gstr9 : Screen("gstr9")
    object PaymentReceived : Screen("payment_received")
    object PaymentMade : Screen("payment_made")
    object GoodsReceiptNote : Screen("goods_receipt_note")
    object TdsTcsPayment : Screen("tds_tcs_payment")
    object AdvancePayment : Screen("advance_payment")
    object UserProfile : Screen("user_profile")
    object RoleManagement : Screen("role_management")
    object DeliveryTracking : Screen("delivery_tracking")
    object ExpenseApproval : Screen("expense_approval")
    object Subscription : Screen("subscription")
    object AnalyticsDashboard : Screen("analytics_dashboard")
    object ItemPriceList : Screen("item_price_list")
    object InvoicePreview : Screen("invoice_preview/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_preview/$invoiceId"
    }
    object KycIntro : Screen("kyc_intro")
    object KycVerification : Screen("kyc_verification")
    object ItemBulkOperations : Screen("item_bulk_operations")
    object BarcodeManagement : Screen("barcode_management")
    object ContinuousScanning : Screen("continuous_scanning")
    object UnitMapping : Screen("unit_mapping")
    object CustomReportBuilder : Screen("custom_report_builder")
    object ReportSchedule : Screen("report_schedule")
    object About : Screen("about")
    object RemindersHub : Screen("reminders_hub")
    object ProfitOnInvoice : Screen("profit_on_invoice")
    object WhatsAppPreview : Screen("whatsapp_preview")
    object TrendingItems : Screen("trending_items")
    object TransactionSelect : Screen("transaction_select")
    object MultiFirmSettings : Screen("multi_firm_settings")

    object FixedAssetsList : Screen("fixed_assets_list")
    object FixedAssetDetail : Screen("fixed_asset_detail/{assetId}") {
        fun createRoute(assetId: Long) = "fixed_asset_detail/$assetId"
    }
    object AddFixedAsset : Screen("add_fixed_asset")
    object Catalogue : Screen("catalogue")
    object ViewStore : Screen("view_store")
    object CatalogueItemDetail : Screen("catalogue_item_detail/{itemId}") {
        fun createRoute(itemId: Long) = "catalogue_item_detail/$itemId"
    }
    object OnlineOrderList : Screen("online_order_list")
    object SmsList : Screen("sms_list")
    object MultiplePartyReminder : Screen("multiple_party_reminder")
    object InputReminderMessage : Screen("input_reminder_message")
    object MessageToParty : Screen("message_to_party")
    object Gstr2Report : Screen("gstr2_report")
    object TcsManagement : Screen("tcs_management")
    object TcsReport : Screen("tcs_report")
    object SecurityLog : Screen("security_log")
    object CashInHandAdjustment : Screen("cash_in_hand_adjustment")
    object CashInHandDetail : Screen("cash_in_hand_detail")
    object TransferMoney : Screen("transfer_money")
    object BankStatementDetail : Screen("bank_statement_detail")
    object BankDetail : Screen("bank_detail/{bankId}") {
        fun createRoute(bankId: Long) = "bank_detail/$bankId"
    }
    object ChequeList : Screen("cheque_list")
    object ChequeDetail : Screen("cheque_detail")
    object ChequeClose : Screen("cheque_close")
    object LoanAccountsList : Screen("loan_accounts_list")
    object AddLoan : Screen("add_loan")
    object LoanDetail : Screen("loan_detail/{loanId}") {
        fun createRoute(loanId: Long) = "loan_detail/$loanId"
    }
    object LoanTransaction : Screen("loan_transaction")
    object LoanExpense : Screen("loan_expense")
    object CashBook : Screen("cash_book")
    object BankReconciliationDetail : Screen("bank_reconciliation_detail")
}
