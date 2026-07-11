package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun TransactionPrefixScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("txn_prefixes", Context.MODE_PRIVATE)

    var salePrefix by remember { mutableStateOf(prefs.getString("sale_prefix", "") ?: "") }
    var creditNotePrefix by remember { mutableStateOf(prefs.getString("credit_note_prefix", "") ?: "") }
    var saleOrderPrefix by remember { mutableStateOf(prefs.getString("sale_order_prefix", "") ?: "") }
    var purchaseOrderPrefix by remember { mutableStateOf(prefs.getString("purchase_order_prefix", "") ?: "") }
    var estimatePrefix by remember { mutableStateOf(prefs.getString("estimate_prefix", "") ?: "") }
    var deliveryChallanPrefix by remember { mutableStateOf(prefs.getString("delivery_challan_prefix", "") ?: "") }
    var paymentInPrefix by remember { mutableStateOf(prefs.getString("payment_in_prefix", "") ?: "") }
    var paymentOutPrefix by remember { mutableStateOf(prefs.getString("payment_out_prefix", "") ?: "") }
    var purchasePrefix by remember { mutableStateOf(prefs.getString("purchase_prefix", "") ?: "") }
    var debitNotePrefix by remember { mutableStateOf(prefs.getString("debit_note_prefix", "") ?: "") }

    fun save(key: String, value: String) { prefs.edit().putString(key, value).apply() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Prefixes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp)
        ) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Leave blank for default numbering", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))

                    val prefixes = listOf(
                        Triple("Sale Invoices", salePrefix) { v: String -> salePrefix = v; save("sale_prefix", v) },
                        Triple("Credit Note", creditNotePrefix) { v: String -> creditNotePrefix = v; save("credit_note_prefix", v) },
                        Triple("Sale Order", saleOrderPrefix) { v: String -> saleOrderPrefix = v; save("sale_order_prefix", v) },
                        Triple("Purchase", purchasePrefix) { v: String -> purchasePrefix = v; save("purchase_prefix", v) },
                        Triple("Purchase Order", purchaseOrderPrefix) { v: String -> purchaseOrderPrefix = v; save("purchase_order_prefix", v) },
                        Triple("Debit Note", debitNotePrefix) { v: String -> debitNotePrefix = v; save("debit_note_prefix", v) },
                        Triple("Estimate", estimatePrefix) { v: String -> estimatePrefix = v; save("estimate_prefix", v) },
                        Triple("Delivery Challan", deliveryChallanPrefix) { v: String -> deliveryChallanPrefix = v; save("delivery_challan_prefix", v) },
                        Triple("Payment-In", paymentInPrefix) { v: String -> paymentInPrefix = v; save("payment_in_prefix", v) },
                        Triple("Payment-Out", paymentOutPrefix) { v: String -> paymentOutPrefix = v; save("payment_out_prefix", v) }
                    )

                    prefixes.forEach { (label, value, setter) ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = setter,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            label = { Text(label) },
                            placeholder = { Text("e.g. AE/") },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider)
                        )
                    }
                }
            }
        }
    }
}
