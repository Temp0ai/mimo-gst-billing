package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitLossReportScreen(
    navController: NavController,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    val sales by invoiceViewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val purchases by invoiceViewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    val expenses by expenseViewModel.expenses.collectAsState()
    var selectedPeriod by remember { mutableStateOf("This Month") }

    val cal = Calendar.getInstance()
    val filterStart = when (selectedPeriod) {
        "This Month" -> { cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0); cal.timeInMillis }
        "This Year" -> { cal.set(Calendar.MONTH, Calendar.JANUARY); cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0); cal.timeInMillis }
        else -> 0L
    }

    val filteredSales = sales.filter { it.invoiceDate >= filterStart }
    val filteredPurchases = purchases.filter { it.invoiceDate >= filterStart }
    val filteredExpenses = expenses.filter { it.date >= filterStart }

    val totalRevenue = filteredSales.sumOf { it.totalAmount }
    val totalCOGS = filteredPurchases.sumOf { it.totalAmount }
    val grossProfit = totalRevenue - totalCOGS
    val totalExpenses = filteredExpenses.sumOf { it.amount }
    val netProfit = grossProfit - totalExpenses

    val expensesByCategory = filteredExpenses.groupBy { it.category }.map { (cat, list) -> cat to list.sumOf { it.amount } }.sortedByDescending { it.second }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profit & Loss", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A)))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("This Month", "This Year", "All Time").forEach { period ->
                        FilterChip(selected = selectedPeriod == period, onClick = { selectedPeriod = period }, label = { Text(period, fontSize = 12.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.12f), selectedLabelColor = Primary))
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Profit & Loss Statement", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        PLRow("Revenue (Sales)", totalRevenue, GreenBalance)
                        PLRow("Cost of Goods (Purchases)", totalCOGS, RedAccent)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PLRow("Gross Profit", grossProfit, if (grossProfit >= 0) GreenBalance else RedAccent)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Operating Expenses", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        expensesByCategory.forEach { (cat, amt) ->
                            PLSubRow(cat, amt)
                        }
                        if (expensesByCategory.isEmpty()) {
                            PLSubRow("No expenses recorded", 0.0)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PLRow("Net Profit/Loss", netProfit, if (netProfit >= 0) GreenBalance else RedAccent)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun PLRow(label: String, amount: Double, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        Text(String.format(Locale.US, "\u20B9%,.2f", amount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
    }
}

@Composable
fun PLSubRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(String.format(Locale.US, "\u20B9%,.2f", amount), fontSize = 13.sp, color = RedAccent)
    }
}
