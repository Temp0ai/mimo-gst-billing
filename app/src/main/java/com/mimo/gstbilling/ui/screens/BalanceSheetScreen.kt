package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSheetScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val sales by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val purchases by viewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    val totalSales = sales.sumOf { it.totalAmount }
    val totalPurchases = purchases.sumOf { it.totalAmount }
    val totalReceivable = sales.filter { it.paymentStatus != "paid" }.sumOf { it.totalAmount - it.amountPaid }
    val totalPayable = purchases.filter { it.paymentStatus != "paid" }.sumOf { it.totalAmount - it.amountPaid }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Balance Sheet", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF795548))) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Balance Sheet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }}
            item { Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("ASSETS", fontWeight = FontWeight.Bold, color = GreenBalance, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportDetailRow("Total Sales (Revenue)", String.format(Locale.US, "\u20B9%,.2f", totalSales))
                    ReportDetailRow("Accounts Receivable", String.format(Locale.US, "\u20B9%,.2f", totalReceivable))
                }
            }}
            item { Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("LIABILITIES", fontWeight = FontWeight.Bold, color = RedAccent, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportDetailRow("Total Purchases", String.format(Locale.US, "\u20B9%,.2f", totalPurchases))
                    ReportDetailRow("Accounts Payable", String.format(Locale.US, "\u20B9%,.2f", totalPayable))
                }
            }}
        }
    }
}
