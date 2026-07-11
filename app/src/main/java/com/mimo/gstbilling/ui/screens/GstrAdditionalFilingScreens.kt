package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.utils.PdfGenerator
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr2FilingScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var showYearPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GSTR-2 Filing", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Note, contentDescription = null, tint = GreenBalance, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("GSTR-2 - Inward Supplies", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Generate GSTR-2 JSON for inward supply (purchase) filing", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Tax Period", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Financial Year:", fontSize = 13.sp, color = TextSecondary)
                            Text("${selectedYear - 1}-$selectedYear", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Primary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Select Month:", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(gstFilingMonths) { month ->
                                val monthIndex = gstFilingMonths.indexOf(month)
                                val isSelected = monthIndex == selectedMonth
                                val isFuture = selectedYear > Calendar.getInstance().get(Calendar.YEAR) ||
                                        (selectedYear == Calendar.getInstance().get(Calendar.YEAR) && monthIndex > Calendar.getInstance().get(Calendar.MONTH))
                                Box(
                                    modifier = Modifier
                                        .background(if (isSelected) Primary else if (isFuture) Color(0xFFF5F5F5) else Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                                        .clickable(enabled = !isFuture) { selectedMonth = monthIndex }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(month.take(3), fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else if (isFuture) Color(0xFFBDBDBD) else TextPrimary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Selected: ${gstFilingMonths[selectedMonth]} $selectedYear", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }
                }
            }

            val purchaseInvoices = invoices.filter { inv ->
                inv.invoiceType == "purchase" &&
                Calendar.getInstance().apply { timeInMillis = inv.invoiceDate }.let {
                    it.get(Calendar.MONTH) == selectedMonth && it.get(Calendar.YEAR) == selectedYear
                }
            }

            val totalInward = purchaseInvoices.size
            val totalTaxable = purchaseInvoices.sumOf { it.taxableAmount }
            val totalIgst = purchaseInvoices.sumOf { it.igstTotal }
            val totalCgst = purchaseInvoices.sumOf { it.cgstTotal }
            val totalSgst = purchaseInvoices.sumOf { it.sgstTotal }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("GSTR-2 Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("B2B Inward Supplies", "$totalInward")
                        SummaryRow("Taxable Value", String.format(Locale.US, "\u20B9%,.2f", totalTaxable))
                        SummaryRow("IGST Paid", String.format(Locale.US, "\u20B9%,.2f", totalIgst))
                        SummaryRow("CGST Paid", String.format(Locale.US, "\u20B9%,.2f", totalCgst))
                        SummaryRow("SGST Paid", String.format(Locale.US, "\u20B9%,.2f", totalSgst))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow("Total ITC Available", String.format(Locale.US, "\u20B9%,.2f", totalIgst + totalCgst + totalSgst), isBold = true)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val json = generateGstr2Json(purchaseInvoices, selectedMonth, selectedYear)
                        val monthStr = gstFilingMonths[selectedMonth].uppercase().take(3)
                        val file = java.io.File(context.cacheDir, "GSTR2_${monthStr}_${selectedYear}.json")
                        file.writeText(json)
                        PdfGenerator.sharePdf(context, file)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = purchaseInvoices.isNotEmpty()
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Share GSTR-2 JSON")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr4FilingScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GSTR-4 Filing", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Receipt, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("GSTR-4 - Composition Taxpayer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("For composition scheme dealers - quarterly filing", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Tax Period", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        val quarters = listOf("Q1 (Apr-Jun)", "Q2 (Jul-Sep)", "Q3 (Oct-Dec)", "Q4 (Jan-Mar)")
                        quarters.forEachIndexed { index, quarter ->
                            val isSelected = index == (selectedMonth / 3)
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedMonth = index * 3 },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else Color(0xFFF5F5F5)),
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder(enabled = true) else null
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Info, contentDescription = null, tint = if (isSelected) Primary else TextSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(quarter, fontSize = 14.sp, color = if (isSelected) Primary else TextPrimary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            val purchaseInvoices = invoices.filter { inv ->
                inv.invoiceType == "purchase" &&
                Calendar.getInstance().apply { timeInMillis = inv.invoiceDate }.let {
                    it.get(Calendar.MONTH) / 3 == selectedMonth / 3 && it.get(Calendar.YEAR) == selectedYear
                }
            }
            val totalTurnover = purchaseInvoices.sumOf { it.totalAmount }
            val totalTax = purchaseInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Composition Tax Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("Total Turnover", String.format(Locale.US, "\u20B9%,.2f", totalTurnover))
                        SummaryRow("Tax @ 1% (Composite)", String.format(Locale.US, "\u20B9%,.2f", totalTurnover * 0.01))
                        SummaryRow("Number of Invoices", "${purchaseInvoices.size}")
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val json = generateGstr4Json(purchaseInvoices, selectedMonth, selectedYear)
                        val monthStr = gstFilingMonths[selectedMonth].uppercase().take(3)
                        val file = java.io.File(context.cacheDir, "GSTR4_${monthStr}_${selectedYear}.json")
                        file.writeText(json)
                        PdfGenerator.sharePdf(context, file)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Share GSTR-4 JSON")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr9aFilingScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    val context = LocalContext.current
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GSTR-9A Filing", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Description, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("GSTR-9A - Annual Return", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Annual return for composition taxpayers", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            val yearInvoices = invoices.filter { inv ->
                Calendar.getInstance().apply { timeInMillis = inv.invoiceDate }.get(Calendar.YEAR) == selectedYear
            }
            val totalSales = yearInvoices.filter { it.invoiceType == "sales" }.sumOf { it.totalAmount }
            val totalPurchases = yearInvoices.filter { it.invoiceType == "purchase" }.sumOf { it.totalAmount }
            val totalTaxPaid = yearInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Financial Year: ${selectedYear - 1}-$selectedYear", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        SummaryRow("Total Outward Supplies", String.format(Locale.US, "\u20B9%,.2f", totalSales))
                        SummaryRow("Total Inward Supplies", String.format(Locale.US, "\u20B9%,.2f", totalPurchases))
                        SummaryRow("Total Tax Paid", String.format(Locale.US, "\u20B9%,.2f", totalTaxPaid))
                        SummaryRow("Net Turnover", String.format(Locale.US, "\u20B9%,.2f", totalSales - totalPurchases))
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val json = generateGstr9aJson(yearInvoices, selectedYear)
                        val file = java.io.File(context.cacheDir, "GSTR9A_${selectedYear - 1}-${selectedYear}.json")
                        file.writeText(json)
                        PdfGenerator.sharePdf(context, file)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Share GSTR-9A JSON")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HsnSummaryScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HSN Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Inventory, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No HSN data available", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Add items with HSN codes to view summary", fontSize = 14.sp, color = TextSecondary)
                }
            }
        } else {
            val hsnData = invoices.groupBy { it.invoiceNumber }.flatMap { (_, inv) ->
                inv.map { it.copy() }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("HSN-wise Summary of Outward Supplies", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("HSN code wise summary of invoices", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }

                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("HSN", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                                Text("Qty", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(0.5f))
                                Text("Taxable", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                                Text("Tax", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            hsnData.forEach { inv ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Text("N/A", fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                                    Text("${inv.quantity}", fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(0.5f))
                                    Text(String.format(Locale.US, "\u20B9%,.2f", inv.taxableAmount), fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                                    Text(String.format(Locale.US, "\u20B9%,.2f", inv.cgstTotal + inv.sgstTotal + inv.igstTotal), fontSize = 13.sp, color = BlueHeader, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateGstr2Json(purchaseInvoices: List<com.mimo.gstbilling.data.local.entity.InvoiceEntity>, month: Int, year: Int): String {
    val root = JSONObject()
    val dt = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    val monthStr = String.format(Locale.US, "%02d", month + 1)
    root.put("gstin", "")
    root.put("fp", "${monthStr}${year}")

    val b2b = JSONArray()
    purchaseInvoices.forEach { inv ->
        val invJson = JSONObject()
        invJson.put("ctin", "")
        val invList = JSONArray()
        val invObj = JSONObject()
        invObj.put("inum", inv.invoiceNumber)
        invObj.put("idt", dt.format(Date(inv.invoiceDate)))
        invObj.put("val", inv.totalAmount)
        invObj.put("rev", "N")
        invObj.put("typ", "R")
        val items = JSONArray()
        val itemObj = JSONObject()
        itemObj.put("rt", 18)
        itemObj.put("txval", inv.taxableAmount)
        itemObj.put("iamt", inv.igstTotal)
        itemObj.put("camt", inv.cgstTotal)
        itemObj.put("samt", inv.sgstTotal)
        itemObj.put("csamt", inv.cessTotal)
        items.put(itemObj)
        invObj.put("itms", items)
        invList.put(invObj)
        invJson.put("inv", invList)
        b2b.put(invJson)
    }
    root.put("b2b", b2b)
    return root.toString(2)
}

private fun generateGstr4Json(purchaseInvoices: List<com.mimo.gstbilling.data.local.entity.InvoiceEntity>, month: Int, year: Int): String {
    val root = JSONObject()
    val monthStr = String.format(Locale.US, "%02d", month + 1)
    root.put("gstin", "")
    root.put("fp", "${monthStr}${year}")
    root.put("turnover_details", JSONObject().put("turnover", purchaseInvoices.sumOf { it.totalAmount }).put("tax_rate", 1))
    val purchases = JSONArray()
    purchaseInvoices.forEach { inv ->
        purchases.put(JSONObject().put("inum", inv.invoiceNumber).put("val", inv.totalAmount).put("txval", inv.taxableAmount))
    }
    root.put("purchases", purchases)
    return root.toString(2)
}

private fun generateGstr9aJson(invoices: List<com.mimo.gstbilling.data.local.entity.InvoiceEntity>, year: Int): String {
    val root = JSONObject()
    root.put("gstin", "")
    root.put("fy", "${year - 1}-$year")
    val sales = invoices.filter { it.invoiceType == "sales" }
    val purchases = invoices.filter { it.invoiceType == "purchase" }
    root.put("total_sales", JSONObject().put("total", sales.sumOf { it.totalAmount }).put("taxable", sales.sumOf { it.taxableAmount }))
    root.put("total_purchases", JSONObject().put("total", purchases.sumOf { it.totalAmount }).put("taxable", purchases.sumOf { it.taxableAmount }))
    root.put("total_tax_paid", JSONObject().put("cgst", sales.sumOf { it.cgstTotal }).put("sgst", sales.sumOf { it.sgstTotal }).put("igst", sales.sumOf { it.igstTotal }))
    return root.toString(2)
}
