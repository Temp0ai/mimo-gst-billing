package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.util.Locale
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitLossReportScreen(navController: NavController, invoiceViewModel: InvoiceViewModel = hiltViewModel(), expenseViewModel: ExpenseViewModel = hiltViewModel()) {
    val sales by invoiceViewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val purchases by invoiceViewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    val totalRevenue = sales.sumOf { it.totalAmount }
    val totalCost = purchases.sumOf { it.totalAmount }
    val totalExpenses by expenseViewModel.totalExpenses.collectAsState()
    val netProfit = totalRevenue - totalCost - totalExpenses

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profit & Loss", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = GreenBalance)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Profit & Loss Statement", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }}
            item { ReportDetailRow("Revenue (Sales)", String.format(Locale.US, "\u20B9%,.2f", totalRevenue)) }
            item { ReportDetailRow("Cost of Goods (Purchases)", String.format(Locale.US, "\u20B9%,.2f", totalCost)) }
            item { ReportDetailRow("Operating Expenses", String.format(Locale.US, "\u20B9%,.2f", totalExpenses)) }
            item { ReportDetailRow("Net Profit/Loss", String.format(Locale.US, "\u20B9%,.2f", netProfit), isBold = true) }
        }
    }
}
