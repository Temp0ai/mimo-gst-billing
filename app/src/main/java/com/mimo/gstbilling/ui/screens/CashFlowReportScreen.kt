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
fun CashFlowReportScreen(navController: NavController, invoiceViewModel: InvoiceViewModel = hiltViewModel(), expenseViewModel: ExpenseViewModel = hiltViewModel()) {
    val sales by invoiceViewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val totalInflow = sales.sumOf { it.amountPaid }
    val totalExpenses by expenseViewModel.totalExpenses.collectAsState()
    val netFlow = totalInflow - totalExpenses

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cash Flow Report", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GreenBalance)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Cash Flow Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }}
            item { ReportDetailRow("Cash Inflow (Received)", String.format(Locale.US, "\u20B9%,.2f", totalInflow)) }
            item { ReportDetailRow("Cash Outflow (Expenses)", String.format(Locale.US, "\u20B9%,.2f", totalExpenses)) }
            item { ReportDetailRow("Net Cash Flow", String.format(Locale.US, "\u20B9%,.2f", netFlow), isBold = true) }
        }
    }
}
