package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val sales by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val purchases by viewModel.getInvoices("purchase").collectAsState(initial = emptyList())
    val totalSales = sales.sumOf { it.totalAmount }
    val totalPurchases = purchases.sumOf { it.totalAmount }
    val profit = totalSales - totalPurchases

    Scaffold(topBar = { TopAppBar(title = { Text("Analytics", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsMiniCard("Revenue", String.format(java.util.Locale.US, "\u20B9%,.0f", totalSales), GreenBalance, Modifier.weight(1f))
                    AnalyticsMiniCard("Expenses", String.format(java.util.Locale.US, "\u20B9%,.0f", totalPurchases), RedAccent, Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsMiniCard("Profit", String.format(java.util.Locale.US, "\u20B9%,.0f", profit), if (profit >= 0) GreenBalance else RedAccent, Modifier.weight(1f))
                    AnalyticsMiniCard("Invoices", "${sales.size + purchases.size}", Primary, Modifier.weight(1f))
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Quick Insights", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary); Spacer(modifier = Modifier.height(12.dp)); InsightRow("Avg Invoice Value", String.format(java.util.Locale.US, "\u20B9%,.0f", if (sales.isNotEmpty()) totalSales / sales.size else 0.0)); InsightRow("Total Parties", "${sales.map { it.partyId }.distinct().size}"); InsightRow("Sales to Purchase Ratio", if (totalPurchases > 0) String.format("%.1fx", totalSales / totalPurchases) else "N/A"); InsightRow("Profit Margin", if (totalSales > 0) String.format("%.1f%%", profit * 100 / totalSales) else "0%") } } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun AnalyticsMiniCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) { Text(label, fontSize = 12.sp, color = TextSecondary); Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color) }
    }
}

@Composable
fun InsightRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, fontSize = 13.sp, color = TextSecondary); Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }
}
