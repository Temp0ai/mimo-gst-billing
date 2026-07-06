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
import com.mimo.gstbilling.ui.viewmodel.CashBankViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayBookReportScreen(
    navController: NavController,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    cashBankViewModel: CashBankViewModel = hiltViewModel()
) {
    val invoices by invoiceViewModel.getInvoices().collectAsState(initial = emptyList())
    val expenses by expenseViewModel.expenses.collectAsState()
    val transactions by cashBankViewModel.transactions.collectAsState()
    val today = SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date())
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayEnd = todayStart + 86400000L

    val todayInvoices = invoices.filter { it.invoiceDate in todayStart until todayEnd }
    val todayExpenses = expenses.filter { it.date in todayStart until todayEnd }
    val todayTransactions = transactions.filter { it.date in todayStart until todayEnd }

    val totalSale = todayInvoices.filter { it.invoiceType == "sales" }.sumOf { it.totalAmount }
    val totalPurchase = todayInvoices.filter { it.invoiceType == "purchase" }.sumOf { it.totalAmount }
    val totalExpensesAmt = todayExpenses.sumOf { it.amount }
    val totalCashIn = todayTransactions.filter { it.type == "credit" }.sumOf { it.amount }
    val totalCashOut = todayTransactions.filter { it.type == "debit" }.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Day Book", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BlueHeader)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Day Book", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(today, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Sales", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.0f", totalSale), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                            Column { Text("Purchases", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.0f", totalPurchase), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                            Column { Text("Expenses", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.0f", totalExpensesAmt), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                            Column { Text("Cash", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.0f", totalCashIn - totalCashOut), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                        }
                    }
                }
            }

            if (todayInvoices.isNotEmpty()) {
                item { Text("Sales & Purchases", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
                items(todayInvoices) { invoice ->
                    val isSale = invoice.invoiceType == "sales"
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(if (isSale) GreenBalance.copy(alpha = 0.1f) else Color(0xFF2196F3).copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Icon(if (isSale) Icons.Filled.TrendingUp else Icons.Filled.ShoppingCart, contentDescription = null, tint = if (isSale) GreenBalance else Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${if (isSale) "Sale" else "Purchase"} - ${invoice.invoiceNumber}", fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 14.sp)
                                Text("Party #${invoice.partyId}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Text(String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount), fontWeight = FontWeight.Bold, color = if (isSale) GreenBalance else RedAccent, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (todayExpenses.isNotEmpty()) {
                item { Text("Expenses", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
                items(todayExpenses) { expense ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(RedAccent.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Note, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.category, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 14.sp)
                                Text(expense.description ?: expense.category, fontSize = 12.sp, color = TextSecondary)
                            }
                            Text(String.format(Locale.US, "\u20B9%,.2f", expense.amount), fontWeight = FontWeight.Bold, color = RedAccent, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (todayTransactions.isNotEmpty()) {
                item { Text("Cash & Bank", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
                items(todayTransactions) { txn ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(if (txn.type == "credit") GreenBalance.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Icon(if (txn.type == "credit") Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward, contentDescription = null, tint = if (txn.type == "credit") GreenBalance else RedAccent, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(txn.description ?: txn.mode, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 14.sp)
                                Text(txn.mode, fontSize = 12.sp, color = TextSecondary)
                            }
                            Text("${if (txn.type == "credit") "+" else "-"}${String.format(Locale.US, "\u20B9%,.2f", txn.amount)}", fontWeight = FontWeight.Bold, color = if (txn.type == "credit") GreenBalance else RedAccent, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (todayInvoices.isEmpty() && todayExpenses.isEmpty() && todayTransactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.EventNote, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No transactions today", fontSize = 16.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
