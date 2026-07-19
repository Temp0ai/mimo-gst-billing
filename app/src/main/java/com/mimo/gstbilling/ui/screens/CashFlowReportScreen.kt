package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
fun CashFlowReportScreen(
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
        "This Quarter" -> { cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3); cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0); cal.timeInMillis }
        "This Year" -> { cal.set(Calendar.MONTH, Calendar.JANUARY); cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0); cal.timeInMillis }
        else -> 0L
    }

    val filteredSales = sales.filter { it.invoiceDate >= filterStart }
    val filteredPurchases = purchases.filter { it.invoiceDate >= filterStart }
    val filteredExpenses = expenses.filter { it.date >= filterStart }

    val totalInflow = filteredSales.sumOf { it.amountPaid }
    val totalOutflow = filteredPurchases.sumOf { it.amountPaid } + filteredExpenses.sumOf { it.amount }
    val netCashFlow = totalInflow - totalOutflow

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cash Flow", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A)))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("This Month", "This Quarter", "This Year", "All Time").forEach { period ->
                        FilterChip(selected = selectedPeriod == period, onClick = { selectedPeriod = period }, label = { Text(period, fontSize = 11.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.12f), selectedLabelColor = Primary))
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Cash Flow Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) { Text("Inflow", fontSize = 11.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalInflow), fontWeight = FontWeight.Bold, color = GreenBalance) }
                            Column(modifier = Modifier.weight(1f)) { Text("Outflow", fontSize = 11.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalOutflow), fontWeight = FontWeight.Bold, color = RedAccent) }
                            Column(modifier = Modifier.weight(1f)) { Text("Net", fontSize = 11.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", netCashFlow), fontWeight = FontWeight.Bold, color = if (netCashFlow >= 0) GreenBalance else RedAccent) }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Simple bar visualization
                        Row(modifier = Modifier.fillMaxWidth().height(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val maxAmount = maxOf(totalInflow, totalOutflow, 1.0)
                            Box(modifier = Modifier.weight((totalInflow / maxAmount).toFloat().coerceIn(0.05f, 1f)).fillMaxHeight().background(GreenBalance, RoundedCornerShape(4.dp)))
                            Box(modifier = Modifier.weight((totalOutflow / maxAmount).toFloat().coerceIn(0.05f, 1f)).fillMaxHeight().background(RedAccent, RoundedCornerShape(4.dp)))
                        }
                    }
                }
            }
            item { Text("Inflow Sources", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
            items(filteredSales) { invoice ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("Sale", fontSize = 11.sp, color = TextSecondary) }
                        Text(String.format(Locale.US, "+\u20B9%,.2f", invoice.amountPaid), fontWeight = FontWeight.Bold, color = GreenBalance)
                    }
                }
            }
            item { Text("Outflow Sources", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
            items(filteredPurchases) { invoice ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("Purchase", fontSize = 11.sp, color = TextSecondary) }
                        Text(String.format(Locale.US, "-\u20B9%,.2f", invoice.amountPaid), fontWeight = FontWeight.Bold, color = RedAccent)
                    }
                }
            }
            items(filteredExpenses) { expense ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text(expense.category, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(expense.description ?: "Expense", fontSize = 11.sp, color = TextSecondary) }
                        Text(String.format(Locale.US, "-\u20B9%,.2f", expense.amount), fontWeight = FontWeight.Bold, color = RedAccent)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
