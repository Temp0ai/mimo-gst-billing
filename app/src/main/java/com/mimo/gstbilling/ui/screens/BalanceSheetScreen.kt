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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSheetScreen(
    navController: NavController,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    val sales by invoiceViewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val purchases by invoiceViewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    val expenses by expenseViewModel.expenses.collectAsState()

    val totalSales = sales.sumOf { it.totalAmount }
    val totalPurchases = purchases.sumOf { it.totalAmount }
    val totalExpenses = expenses.sumOf { it.amount }
    val grossProfit = totalSales - totalPurchases
    val netProfit = grossProfit - totalExpenses
    val receivable = sales.sumOf { it.totalAmount - it.amountPaid }
    val payable = purchases.sumOf { it.totalAmount - it.amountPaid }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Balance Sheet", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A)))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Financial Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        BalanceRow("Total Sales (Revenue)", totalSales, GreenBalance)
                        BalanceRow("Total Purchases (COGS)", totalPurchases, RedAccent)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BalanceRow("Gross Profit", grossProfit, if (grossProfit >= 0) GreenBalance else RedAccent)
                        BalanceRow("Total Expenses", totalExpenses, RedAccent)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BalanceRow("Net Profit/Loss", netProfit, if (netProfit >= 0) GreenBalance else RedAccent)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Receivables & Payables", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        BalanceRow("Accounts Receivable", receivable, Primary)
                        BalanceRow("Accounts Payable", payable, RedAccent)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BalanceRow("Net Position", receivable - payable, if (receivable >= payable) GreenBalance else RedAccent)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        BreakdownItem("Sales (${sales.size} invoices)", totalSales, GreenBalance)
                        BreakdownItem("Purchases (${purchases.size} invoices)", totalPurchases, RedAccent)
                        BreakdownItem("Expenses (${expenses.size} entries)", totalExpenses, RedAccent)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun BalanceRow(label: String, amount: Double, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(String.format(Locale.US, "\u20B9%,.2f", amount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
    }
}

@Composable
fun BreakdownItem(label: String, amount: Double, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextPrimary)
        Text(String.format(Locale.US, "\u20B9%,.2f", amount), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = color)
    }
}
