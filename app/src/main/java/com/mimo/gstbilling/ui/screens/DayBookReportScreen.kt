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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayBookReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val allSales by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val allPurchases by viewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
    val todaySales = allSales.filter { it.invoiceDate >= today }
    val todayPurchases = allPurchases.filter { it.invoiceDate >= today }
    val todaySalesTotal = todaySales.sumOf { it.totalAmount }
    val todayPurchaseTotal = todayPurchases.sumOf { it.totalAmount }
    val todayProfit = todaySalesTotal - todayPurchaseTotal

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Day Book", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A)))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(dateFormat.format(Date()), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) { Text("Sales", fontSize = 11.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", todaySalesTotal), fontWeight = FontWeight.Bold, color = Primary) }
                            Column(modifier = Modifier.weight(1f)) { Text("Purchases", fontSize = 11.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", todayPurchaseTotal), fontWeight = FontWeight.Bold, color = GreenBalance) }
                            Column(modifier = Modifier.weight(1f)) { Text("Profit/Loss", fontSize = 11.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", todayProfit), fontWeight = FontWeight.Bold, color = if (todayProfit >= 0) GreenBalance else RedAccent) }
                        }
                    }
                }
            }
            item { Text("Today's Transactions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
            items(todaySales + todayPurchases) { invoice ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(invoice.invoiceType.replace("_", " ").uppercase(), fontSize = 10.sp, color = if (invoice.invoiceType == "sales") Primary else GreenBalance)
                        }
                        Text(String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount), fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
            item { if (todaySales.isEmpty() && todayPurchases.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Text("No transactions today", color = TextSecondary) } } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
