package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("transaction_settings", Context.MODE_PRIVATE)

    var invoiceBillNumber by remember { mutableStateOf(prefs.getBoolean("invoice_bill_number", true)) }
    var cashSaleDefault by remember { mutableStateOf(prefs.getBoolean("cash_sale_default", false)) }
    var billingNameParties by remember { mutableStateOf(prefs.getBoolean("billing_name_parties", false)) }
    var poDetails by remember { mutableStateOf(prefs.getBoolean("po_details", false)) }
    var addTimeTransactions by remember { mutableStateOf(prefs.getBoolean("add_time_transactions", false)) }
    var inclusiveExclusiveTax by remember { mutableStateOf(prefs.getBoolean("inclusive_exclusive_tax", true)) }
    var displayPurchasePrice by remember { mutableStateOf(prefs.getBoolean("display_purchase_price", true)) }
    var showLast5SalePrice by remember { mutableStateOf(prefs.getBoolean("show_last5_sale_price", false)) }
    var freeItemQuantity by remember { mutableStateOf(prefs.getBoolean("free_item_quantity", false)) }
    var count by remember { mutableStateOf(prefs.getBoolean("count", false)) }
    var barcodeScanning by remember { mutableStateOf(prefs.getBoolean("barcode_scanning", false)) }
    var transactionWiseTax by remember { mutableStateOf(prefs.getBoolean("transaction_wise_tax", false)) }
    var transactionWiseDiscount by remember { mutableStateOf(prefs.getBoolean("transaction_wise_discount", false)) }
    var roundOff by remember { mutableStateOf(prefs.getBoolean("round_off", false)) }
    var shareTransactionAs by remember { mutableStateOf(prefs.getString("share_transaction_as", "Share as PDF") ?: "Share as PDF") }
    var passcodeEditDelete by remember { mutableStateOf(prefs.getBoolean("passcode_edit_delete", false)) }
    var discountDuringPayment by remember { mutableStateOf(prefs.getBoolean("discount_during_payment", false)) }
    var linkPaymentsToInvoices by remember { mutableStateOf(prefs.getBoolean("link_payments_invoices", false)) }
    var enableInvoicePreview by remember { mutableStateOf(prefs.getBoolean("enable_invoice_preview", true)) }
    var termsConditions by remember { mutableStateOf(prefs.getBoolean("terms_conditions", true)) }
    var reverseCharge by remember { mutableStateOf(prefs.getBoolean("reverse_charge", false)) }
    var stateOfSupply by remember { mutableStateOf(prefs.getBoolean("state_of_supply", true)) }
    var ewayBillNo by remember { mutableStateOf(prefs.getBoolean("eway_bill_no", true)) }

    var showShareDropdown by remember { mutableStateOf(false) }
    val shareOptions = listOf("Share as PDF", "Share as Image", "Share as Text")

    fun save(key: String, value: Any) {
        prefs.edit().apply {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
            }
            apply()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Transaction Header
            SectionHeader("Transaction Header")
            SettingToggleRow("Invoice/Bill Number", invoiceBillNumber) { invoiceBillNumber = it; save("invoice_bill_number", it) }
            SettingToggleRow("Cash Sale by default", cashSaleDefault) { cashSaleDefault = it; save("cash_sale_default", it) }
            SettingToggleRow("Billing name of Parties", billingNameParties) { billingNameParties = it; save("billing_name_parties", it) }
            SettingToggleRow("PO Details(of customer)", poDetails) { poDetails = it; save("po_details", it) }
            SettingToggleRow("Add Time On Transactions", addTimeTransactions) { addTimeTransactions = it; save("add_time_transactions", it) }

            // Items Table
            SectionHeader("Items Table")
            SettingToggleRow("Allow Inclusive/Exclusive tax on Rate(Price/unit)", inclusiveExclusiveTax) { inclusiveExclusiveTax = it; save("inclusive_exclusive_tax", it) }
            SettingToggleRow("Display Purchase Price", displayPurchasePrice) { displayPurchasePrice = it; save("display_purchase_price", it) }
            SettingToggleRow("Show Last 5 Sale Price of Items", showLast5SalePrice) { showLast5SalePrice = it; save("show_last5_sale_price", it) }
            SettingToggleRow("Free Item quantity", freeItemQuantity) { freeItemQuantity = it; save("free_item_quantity", it) }
            SettingToggleRow("Count", count) { count = it; save("count", it) }
            SettingToggleRow("Barcode scanning for items", barcodeScanning) { barcodeScanning = it; save("barcode_scanning", it) }

            // Taxes, Discount & Total
            SectionHeader("Taxes, Discount & Total")
            SettingToggleRow("Transaction wise Tax", transactionWiseTax) { transactionWiseTax = it; save("transaction_wise_tax", it) }
            SettingToggleRow("Transaction wise Discount", transactionWiseDiscount) { transactionWiseDiscount = it; save("transaction_wise_discount", it) }
            SettingToggleRow("Round Off Transaction amount", roundOff) { roundOff = it; save("round_off", it) }

            // GST
            SectionHeader("GST")
            SettingToggleRow("Reverse Charge", reverseCharge) { reverseCharge = it; save("reverse_charge", it) }
            SettingToggleRow("State of Supply", stateOfSupply) { stateOfSupply = it; save("state_of_supply", it) }
            SettingToggleRow("E-Way Bill No.", ewayBillNo) { ewayBillNo = it; save("eway_bill_no", it) }

            // Transportation & Additional
            SettingNavigationRow("Transportation Details", false) { navController.navigate(Screen.SettingsDetail.createRoute("Transportation Details")) }
            SettingNavigationRow("Additional Charges", false) { navController.navigate(Screen.SettingsDetail.createRoute("Additional Charges")) }

            // Transaction Prefixes
            SectionHeader("Transaction Prefixes")
            SettingNavigationRow("Transaction Prefixes", false) { navController.navigate(Screen.SettingsDetail.createRoute("Transaction Prefixes")) }

            // More Transaction Features
            SectionHeader("More Transaction Features")

            // Share Transaction as
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Share Transaction as", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                Box {
                    Row(modifier = Modifier.clickable { showShareDropdown = true }, verticalAlignment = Alignment.CenterVertically) {
                        Text(shareTransactionAs, fontSize = 14.sp, color = TextSecondary)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }
                    DropdownMenu(expanded = showShareDropdown, onDismissRequest = { showShareDropdown = false }) {
                        shareOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                shareTransactionAs = option; save("share_transaction_as", option); showShareDropdown = false
                            })
                        }
                    }
                }
            }
            HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))

            SettingToggleRow("Passcode for edit/delete", passcodeEditDelete) { passcodeEditDelete = it; save("passcode_edit_delete", it) }
            SettingToggleRow("Discount during Payment", discountDuringPayment) { discountDuringPayment = it; save("discount_during_payment", it) }
            SettingToggleRow("Link Payments to Invoices", linkPaymentsToInvoices) { linkPaymentsToInvoices = it; save("link_payments_invoices", it) }

            SettingNavigationRow("Due Dates and Payment terms", false) { navController.navigate(Screen.SettingsDetail.createRoute("Due Dates and Payment terms")) }

            SettingToggleRow("Enable Invoice Preview", enableInvoicePreview) { enableInvoicePreview = it; save("enable_invoice_preview", it) }
            SettingToggleRow("Terms & Conditions", termsConditions) { termsConditions = it; save("terms_conditions", it) }

            SettingNavigationRow("Set Terms & Conditions", false) { navController.navigate(Screen.SettingsDetail.createRoute("Set Terms & Conditions")) }
            SettingNavigationRow("Additional Fields", false) { navController.navigate(Screen.SettingsDetail.createRoute("Additional Fields")) }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier.fillMaxWidth().background(LightBlueBg).padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
    }
}
