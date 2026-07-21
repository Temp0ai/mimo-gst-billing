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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillWiseProfitLossScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill-wise P&L", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No invoice data available", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Create invoices to see bill-wise P&L", fontSize = 14.sp, color = TextSecondary)
                }
            }
        } else {
            val saleInvoices = invoices.filter { it.invoiceType == "sales" }
            val purchaseInvoices = invoices.filter { it.invoiceType == "purchase" }

            val totalRevenue = saleInvoices.sumOf { it.taxableAmount }
            val totalCost = purchaseInvoices.sumOf { it.taxableAmount }
            val totalProfit = totalRevenue - totalCost
            val profitMargin = if (totalRevenue > 0) (totalProfit / totalRevenue * 100) else 0.0

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Overall Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Sales", color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalRevenue), color = GreenBalance, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Purchases", color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalCost), color = RedAccent, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Profit", fontWeight = FontWeight.Bold, color = TextPrimary); Text(String.format(Locale.US, "\u20B9%,.2f", totalProfit), color = if (totalProfit >= 0) GreenBalance else RedAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Profit Margin", fontSize = 13.sp, color = TextSecondary); Text(String.format(Locale.US, "%.1f%%", profitMargin), color = Primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text("Bill-wise Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                saleInvoices.forEach { invoice ->
                    item {
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(String.format(Locale.US, "\u20B9%,.2f", invoice.taxableAmount), color = GreenBalance, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Party #${invoice.partyId}", fontSize = 12.sp, color = TextSecondary)
                                    Text("Tax: ${String.format(Locale.US, "\u20B9%,.2f", invoice.cgstTotal + invoice.sgstTotal + invoice.igstTotal)}", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyWiseProfitLossScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Party-wise P&L", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Group, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No data available", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Create invoices to see party-wise P&L", fontSize = 14.sp, color = TextSecondary)
                }
            }
        } else {
            val partyData = invoices
                .filter { it.invoiceType == "sales" }
                .groupBy { it.partyId }
                .mapValues { (_, partyInvoices) ->
                    val totalSales = partyInvoices.sumOf { it.taxableAmount }
                    totalSales
                }.let { data ->
                    data.toSortedMap(compareByDescending { data[it] ?: 0.0 })
                }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Party-wise Sales Contribution", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Top parties by taxable sales", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }

                partyData.forEach { (partyId, amount) ->
                    item {
                        val totalSales = invoices.filter { it.invoiceType == "sales" }.sumOf { it.taxableAmount }
                        val percentage = if (totalSales > 0) amount / totalSales * 100 else 0.0
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Party #$partyId", fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(String.format(Locale.US, "\u20B9%,.2f", amount), color = GreenBalance, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (percentage / 100).toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = Primary,
                                    trackColor = LightBlueBg,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${String.format(Locale.US, "%.1f", percentage)}% of total sales", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
