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
fun StockSummaryReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val items by viewModel.getItems().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Stock Summary Report", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        val goods = items.filter { !it.isService }
        val totalStock = goods.sumOf { it.stockQuantity }
        val totalValue = goods.sumOf { it.stockQuantity * it.salePrice }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Stock Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Items", color = TextSecondary); Text("$totalStock", fontWeight = FontWeight.Bold, color = Primary) }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Stock Value", color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalValue), fontWeight = FontWeight.Bold, color = GreenBalance) }
                    }
                }
            }
            if (goods.isEmpty()) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No stock items found", color = TextSecondary) } }
            }
            goods.forEach { item ->
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("HSN: ${item.hsnCode ?: "N/A"}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${item.stockQuantity.toInt()} ${item.unit}", fontWeight = FontWeight.Bold, color = Primary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", item.stockQuantity * item.salePrice), fontSize = 12.sp, color = GreenBalance)
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
fun LowStockReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val items by viewModel.getItems().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Low Stock Report", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        val lowStockItems = items.filter { !it.isService && it.stockQuantity <= 10 }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = RedAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("${lowStockItems.size} items running low on stock", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        }
                    }
                }
            }
            if (lowStockItems.isEmpty()) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("All items are well-stocked!", color = GreenBalance, fontSize = 16.sp) } }
            }
            lowStockItems.forEach { item ->
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = if (item.stockQuantity <= 0) RedAccent else Color(0xFFFF9800), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("HSN: ${item.hsnCode ?: "N/A"}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${item.stockQuantity.toInt()} left", fontWeight = FontWeight.Bold, color = if (item.stockQuantity <= 0) RedAccent else Color(0xFFFF9800))
                                Text(String.format(Locale.US, "\u20B9%,.2f/item", item.salePrice), fontSize = 12.sp, color = TextSecondary)
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
fun CategoryStockReportScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Stock by Category", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Category, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Assign categories to items in Items screen", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate(com.mimo.gstbilling.ui.navigation.Screen.Items.route) }) { Text("Go to Items") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySalePurchaseScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sale/Purchase by Category", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Category, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Assign categories to items first", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate(com.mimo.gstbilling.ui.navigation.Screen.Items.route) }) { Text("Go to Items") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerialReportScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Item Serial Report", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Enable serial number tracking in Item Settings", fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankStatementReportScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bank Statement", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Add bank accounts in Bank Accounts screen", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate(com.mimo.gstbilling.ui.navigation.Screen.BankAccounts.route) }) { Text("Go to Bank Accounts") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstrSummaryScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GST Summary", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        val totalSales = invoices.filter { it.invoiceType == "sales" }.sumOf { it.totalAmount }
        val totalPurchases = invoices.filter { it.invoiceType == "purchase" }.sumOf { it.totalAmount }
        val totalOutputTax = invoices.filter { it.invoiceType == "sales" }.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
        val totalInputTax = invoices.filter { it.invoiceType == "purchase" }.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
        val netPayable = totalOutputTax - totalInputTax

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("GST Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        listOf("Total Sales" to totalSales, "Total Purchases" to totalPurchases, "Output Tax (CGST+SGST+IGST)" to totalOutputTax, "Input Tax (ITC)" to totalInputTax).forEach { (label, value) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(label, fontSize = 13.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", value), fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net GST Payable", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", netPayable), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (netPayable >= 0) RedAccent else GreenBalance)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Form27EqScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Form 27EQ", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.padding(32.dp)) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Form 27EQ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Statement of collection of tax at source under Section 206C of the Income Tax Act", fontSize = 13.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("TCS (Tax Collected at Source) filing form is applicable when you collect tax from buyers as per Section 206C.", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TcsReceivableScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("TCS Receivable", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        val tcsInvoices = invoices.filter { it.tcsAmount > 0 }
        val totalTcs = tcsInvoices.sumOf { it.tcsAmount }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total TCS Receivable", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format(Locale.US, "\u20B9%,.2f", totalTcs), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GreenBalance)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${tcsInvoices.size} invoices with TCS", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
            tcsInvoices.forEach { inv ->
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Party #${inv.partyId}", fontSize = 12.sp, color = TextSecondary) }
                            Column(horizontalAlignment = Alignment.End) { Text(String.format(Locale.US, "\u20B9%,.2f", inv.tcsAmount), fontWeight = FontWeight.Bold, color = GreenBalance); Text("TCS ${inv.tcsRate}%", fontSize = 12.sp, color = TextSecondary) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TdsPayableScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("TDS Payable", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        val tdsInvoices = invoices.filter { it.tdsAmount > 0 }
        val totalTds = tdsInvoices.sumOf { it.tdsAmount }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total TDS Payable", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format(Locale.US, "\u20B9%,.2f", totalTds), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RedAccent)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${tdsInvoices.size} invoices with TDS deduction", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
            tdsInvoices.forEach { inv ->
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Party #${inv.partyId}", fontSize = 12.sp, color = TextSecondary) }
                            Column(horizontalAlignment = Alignment.End) { Text(String.format(Locale.US, "\u20B9%,.2f", inv.tdsAmount), fontWeight = FontWeight.Bold, color = RedAccent); Text("TDS ${inv.tdsRate}%", fontSize = 12.sp, color = TextSecondary) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TdsReceivableScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("TDS Receivable", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.padding(32.dp)) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Receipt, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("TDS Receivable", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tax deducted by customers on your invoices. This is the TDS you can claim as credit.", fontSize = 13.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SacReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SAC Report", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        val serviceInvoices = invoices.filter { it.invoiceType == "sales" }
        val totalService = serviceInvoices.sumOf { it.taxableAmount }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SAC-wise Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Services Accounting Code summary for service invoices", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Service Value", color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalService), fontWeight = FontWeight.Bold, color = GreenBalance) }
                    }
                }
            }
            if (serviceInvoices.isEmpty()) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No service invoices found", color = TextSecondary) } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("All Transactions", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        val totalSales = invoices.filter { it.invoiceType == "sales" }.sumOf { it.totalAmount }
        val totalPurchases = invoices.filter { it.invoiceType == "purchase" }.sumOf { it.totalAmount }
        val totalTax = invoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Transaction Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Invoices", color = TextSecondary); Text("${invoices.size}", fontWeight = FontWeight.Bold, color = Primary) }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Sales", color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalSales), fontWeight = FontWeight.Bold, color = GreenBalance) }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Purchases", color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalPurchases), fontWeight = FontWeight.Bold, color = RedAccent) }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Tax", color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", totalTax), fontWeight = FontWeight.Bold, color = Primary) }
                    }
                }
            }
            invoices.take(20).forEach { inv ->
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(inv.invoiceType.uppercase(), fontSize = 12.sp, color = if (inv.invoiceType == "sales") GreenBalance else RedAccent)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(String.format(Locale.US, "\u20B9%,.2f", inv.totalAmount), fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(inv.paymentStatus, fontSize = 12.sp, color = if (inv.paymentStatus == "paid") GreenBalance else Color(0xFFFF9800))
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
fun ExpenseTransactionReportScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Expense Transaction Report", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Receipt, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No expense transactions yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Add expenses from the Expenses screen", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate(com.mimo.gstbilling.ui.navigation.Screen.Expenses.route) }) { Text("Go to Expenses") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllPartiesReportScreen(navController: NavController, viewModel: InvoiceViewModel = hiltViewModel()) {
    val parties by viewModel.getParties().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("All Parties Report", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Parties: ${parties.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Customers: ${parties.count { it.partyType == "customer" }} | Suppliers: ${parties.count { it.partyType == "supplier" }}", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
            parties.forEach { party ->
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(party.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(party.partyType.uppercase(), fontSize = 12.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(String.format(Locale.US, "\u20B9%,.2f", party.balance), fontWeight = FontWeight.Bold, color = if (party.balance >= 0) GreenBalance else RedAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}
