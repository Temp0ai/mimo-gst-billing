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
import com.mimo.gstbilling.ui.viewmodel.ItemViewModel
import java.util.*
import com.mimo.gstbilling.ui.viewmodel.ExpenseViewModel
import com.mimo.gstbilling.ui.viewmodel.ItemViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenericReportScreen(
    navController: NavController,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyReportByItemsScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    GenericReportScreen(navController, "Party Report by Items", "Items purchased by party", Icons.Filled.Group) {
        val partyItems = invoices.flatMap { invoice ->
            invoice.items?.map { item -> invoice.customerName to item.itemName } ?: emptyList()
        }.groupBy({ it.first }, { it.second })
        if (partyItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data available", color = TextSecondary) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                partyItems.forEach { (party, items) ->
                    item {
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(party, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                items.distinct().forEach { item ->
                                    Text("\u2022 $item", fontSize = 14.sp, color = TextSecondary)
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
fun SalePurchaseByPartyScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    GenericReportScreen(navController, "Sale/Purchase by Party", "Party-wise totals", Icons.Filled.Group) {
        val partyTotals = invoices.groupBy { it.customerName }.mapValues { (_, invs) ->
            mapOf("sale" to invs.filter { it.type == "sale" }.sumOf { it.totalAmount }, "purchase" to invs.filter { it.type == "purchase" }.sumOf { it.totalAmount })
        }
        if (partyTotals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data available", color = TextSecondary) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            partyTotals.forEach { (party, totals) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(party, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1f))
                                    Text("Sale: ${String.format(Locale.US, "\u20B9%,.0f", totals["sale"])}", fontSize = 13.sp, color = GreenBalance)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Purchase: ${String.format(Locale.US, "\u20B9%,.0f", totals["purchase"])}", fontSize = 13.sp, color = RedAccent)
                                }
                                HorizontalDivider(color = Color(0xFFF5F5F5))
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
fun ItemReportByPartyScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    GenericReportScreen(navController, "Item Report by Party", "Items sold per party", Icons.Filled.Inventory) {
        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data available", color = TextSecondary) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                invoices.forEach { invoice ->
                    item {
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("${invoice.customerName} - ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount), fontSize = 13.sp, color = BlueHeader)
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
fun ItemWiseProfitLossScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    GenericReportScreen(navController, "Item Wise P&L", "Profit/loss per item", Icons.Filled.TrendingUp) {
        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data available", color = TextSecondary) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val itemRevenue = invoices.filter { it.type == "sale" }.flatMap { it.items?.map { item -> item.itemName to item.totalAmount } ?: emptyList() }
                    .groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            itemRevenue.forEach { (item, revenue) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(item, color = TextPrimary, modifier = Modifier.weight(1f))
                                    Text(String.format(Locale.US, "\u20B9%,.2f", revenue), fontWeight = FontWeight.Bold, color = GreenBalance)
                                }
                                HorizontalDivider(color = Color(0xFFF5F5F5))
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
fun StockDetailReportScreen(navController: NavController, viewModel: ItemViewModel = hiltViewModel()) {
    val items by viewModel.allItems.collectAsState(initial = emptyList())
    GenericReportScreen(navController, "Stock Detail Report", "Current stock levels", Icons.Filled.Inventory) {
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No items found", color = TextSecondary) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) { Text(item.name, fontWeight = FontWeight.Medium, color = TextPrimary); Text("HSN: ${item.hsnCode ?: "N/A"}", fontSize = 12.sp, color = TextSecondary) }
                                    Column(horizontalAlignment = Alignment.End) { Text("${item.openingStock.toInt()} ${item.unit}", fontWeight = FontWeight.Bold, color = if (item.openingStock > 10) GreenBalance else RedAccent); if (item.openingStock <= 10) Text("Low Stock", fontSize = 11.sp, color = RedAccent) }
                                }
                                HorizontalDivider(color = Color(0xFFF5F5F5))
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
fun ItemWiseDiscountScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    GenericReportScreen(navController, "Item Wise Discount", "Discount per item", Icons.Filled.Percent) {
        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data available", color = TextSecondary) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val totalDiscount = invoices.sumOf { it.discount }
                            Text("Total Discount Given", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalDiscount), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RedAccent)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    GenericReportScreen(navController, "Discount Report", "All discounts", Icons.Filled.Percent) {
        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data available", color = TextSecondary) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(invoices.filter { it.discount > 0 }) { invoice ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary); Text(invoice.customerName, fontSize = 12.sp, color = TextSecondary) }
                            Text(String.format(Locale.US, "-\u20B9%,.2f", invoice.discount), fontWeight = FontWeight.Bold, color = RedAccent)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    GenericReportScreen(navController, "Tax Report", "GST tax summary", Icons.Filled.Percent) {
        val totalCgst = invoices.sumOf { it.cgstAmount }
        val totalSgst = invoices.sumOf { it.sgstAmount }
        val totalIgst = invoices.sumOf { it.igstAmount }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total CGST", color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalCgst), fontWeight = FontWeight.Bold, color = TextPrimary) }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total SGST", color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalSgst), fontWeight = FontWeight.Bold, color = TextPrimary) }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total IGST", color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalIgst), fontWeight = FontWeight.Bold, color = TextPrimary) }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Tax", fontWeight = FontWeight.Bold, color = TextPrimary); Text(String.format(Locale.US, "\u20B9%,.2f", totalCgst + totalSgst + totalIgst), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlueHeader) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItemReportScreen(navController: NavController, viewModel: ExpenseViewModel = hiltViewModel()) {
    val expenses by viewModel.expenses.collectAsState()
    GenericReportScreen(navController, "Expense Item Report", "Expenses by category", Icons.Filled.Note) {
        if (expenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No expenses found", color = TextSecondary) }
        } else {
            val categoryTotals = expenses.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            categoryTotals.forEach { (cat, total) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(cat, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1f))
                                    Text(String.format(Locale.US, "\u20B9%,.2f", total), fontWeight = FontWeight.Bold, color = RedAccent)
                                }
                                HorizontalDivider(color = Color(0xFFF5F5F5))
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
fun OrderItemReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    GenericReportScreen(navController, "Order Item Report", "Items in orders", Icons.Filled.ShoppingCart) {
        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No orders found", color = TextSecondary) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                invoices.forEach { invoice ->
                    item {
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("${invoice.customerName} - ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount), fontSize = 13.sp, color = BlueHeader)
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
fun LoanStatementScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Loan Statement", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Loan Statement", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Loan tracking coming soon", fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}
