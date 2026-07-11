package com.mimo.gstbilling.ui.screens

import android.content.Context
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
import androidx.compose.ui.text.style.TextAlign
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

internal val gstFilingMonths = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private fun getFinancialYear(): String {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    return if (month >= Calendar.MARCH) "$year-${year + 1}" else "${year - 1}-$year"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr1FilingScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GSTR-1 Filing", fontWeight = FontWeight.Bold) },
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
                            Icon(Icons.Filled.Note, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("GSTR-1 - Outward Supplies", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Generate GSTR-1 JSON file for GST portal upload", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            // Tax Period Selector
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Tax Period", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Financial Year Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Financial Year:", fontSize = 13.sp, color = TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showYearPicker = !showYearPicker }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Change Year", tint = Primary, modifier = Modifier.size(18.dp))
                                }
                                Text(
                                    "${selectedYear - 1}-$selectedYear",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.clickable { showYearPicker = !showYearPicker }
                                )
                            }
                        }

                        if (showYearPicker) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val years = (2017..selectedYear + 1).map { it }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                years.takeLast(5).forEach { year ->
                                    val isSelected = year == selectedYear
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) Primary else Color(0xFFF0F0F0),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedYear = year; showYearPicker = false }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$year",
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Month Selector
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
                                        .background(
                                            when {
                                                isSelected -> Primary
                                                isFuture -> Color(0xFFF5F5F5)
                                                else -> Color(0xFFF0F0F0)
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable(enabled = !isFuture) {
                                            selectedMonth = monthIndex
                                            showMonthPicker = false
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        month.take(3),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            isSelected -> Color.White
                                            isFuture -> Color(0xFFBDBDBD)
                                            else -> TextPrimary
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Selected: ${gstFilingMonths[selectedMonth]} $selectedYear",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }

            // Filter invoices by selected month/year
            val filteredInvoices = invoices.filter { inv ->
                if (inv.invoiceType != "sales") return@filter false
                val cal = Calendar.getInstance().apply { timeInMillis = inv.invoiceDate }
                cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
            }

            // Summary
            val totalInvoices = filteredInvoices.size
            val totalTaxable = filteredInvoices.sumOf { it.taxableAmount }
            val totalCgst = filteredInvoices.sumOf { it.cgstTotal }
            val totalSgst = filteredInvoices.sumOf { it.sgstTotal }
            val totalIgst = filteredInvoices.sumOf { it.igstTotal }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("GSTR-1 Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${gstFilingMonths[selectedMonth]} $selectedYear", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("B2B Invoices", "$totalInvoices")
                        SummaryRow("Taxable Value", String.format(java.util.Locale.US, "\u20B9%,.2f", totalTaxable))
                        SummaryRow("CGST", String.format(java.util.Locale.US, "\u20B9%,.2f", totalCgst))
                        SummaryRow("SGST", String.format(java.util.Locale.US, "\u20B9%,.2f", totalSgst))
                        SummaryRow("IGST", String.format(java.util.Locale.US, "\u20B9%,.2f", totalIgst))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow("Total Tax", String.format(java.util.Locale.US, "\u20B9%,.2f", totalCgst + totalSgst + totalIgst), isBold = true)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val json = generateGstr1Json(filteredInvoices, selectedMonth, selectedYear)
                        val monthStr = gstFilingMonths[selectedMonth].uppercase().take(3)
                        val file = java.io.File(context.cacheDir, "GSTR1_${monthStr}_${selectedYear}.json")
                        file.writeText(json)
                        PdfGenerator.sharePdf(context, file)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = filteredInvoices.isNotEmpty()
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Share GSTR-1 JSON")
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("How to file GSTR-1:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF8D6E00))
                        Spacer(modifier = Modifier.height(4.dp))
                        listOf(
                            "1. Generate JSON using button above",
                            "2. Go to GST Portal (gst.gov.in)",
                            "3. Login and navigate to GSTR-1",
                            "4. Click 'Import JSON' to upload",
                            "5. Review and submit"
                        ).forEach { Text(it, fontSize = 12.sp, color = Color(0xFF8D6E00)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr3bFilingScreen(
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
                title = { Text("GSTR-3B Filing", fontWeight = FontWeight.Bold) },
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
                            Icon(Icons.Filled.Note, contentDescription = null, tint = RedAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("GSTR-3B - Monthly Return", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Generate GSTR-3B JSON for GST portal upload", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            // Tax Period Selector (same as GSTR-1)
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Tax Period", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Financial Year:", fontSize = 13.sp, color = TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showYearPicker = !showYearPicker }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Change Year", tint = Primary, modifier = Modifier.size(18.dp))
                                }
                                Text(
                                    "${selectedYear - 1}-$selectedYear",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.clickable { showYearPicker = !showYearPicker }
                                )
                            }
                        }

                        if (showYearPicker) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val years = (2017..selectedYear + 1).map { it }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                years.takeLast(5).forEach { year ->
                                    val isSelected = year == selectedYear
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isSelected) Primary else Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                                            .clickable { selectedYear = year; showYearPicker = false }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$year", fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else TextPrimary)
                                    }
                                }
                            }
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
                                        .background(
                                            when {
                                                isSelected -> Primary
                                                isFuture -> Color(0xFFF5F5F5)
                                                else -> Color(0xFFF0F0F0)
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable(enabled = !isFuture) { selectedMonth = monthIndex }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        month.take(3),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            isSelected -> Color.White
                                            isFuture -> Color(0xFFBDBDBD)
                                            else -> TextPrimary
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Selected: ${gstFilingMonths[selectedMonth]} $selectedYear",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }

            // Filter invoices
            val saleInvoices = invoices.filter { inv ->
                if (inv.invoiceType != "sales") return@filter false
                val cal = Calendar.getInstance().apply { timeInMillis = inv.invoiceDate }
                cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
            }
            val purchaseInvoices = invoices.filter { inv ->
                if (inv.invoiceType != "purchase") return@filter false
                val cal = Calendar.getInstance().apply { timeInMillis = inv.invoiceDate }
                cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
            }

            val totalOutward = saleInvoices.sumOf { it.totalAmount }
            val totalInward = purchaseInvoices.sumOf { it.totalAmount }
            val totalOutputTax = saleInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
            val totalInputTax = purchaseInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
            val netTax = totalOutputTax - totalInputTax

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("3.1 Outward Supplies (Taxable)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("Total Outward Value", String.format(java.util.Locale.US, "\u20B9%,.2f", totalOutward))
                        SummaryRow("Output CGST", String.format(java.util.Locale.US, "\u20B9%,.2f", saleInvoices.sumOf { it.cgstTotal }))
                        SummaryRow("Output SGST", String.format(java.util.Locale.US, "\u20B9%,.2f", saleInvoices.sumOf { it.sgstTotal }))
                        SummaryRow("Output IGST", String.format(java.util.Locale.US, "\u20B9%,.2f", saleInvoices.sumOf { it.igstTotal }))
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("3.2 Inward Supplies (Reverse Charge)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("Total Inward Value", String.format(java.util.Locale.US, "\u20B9%,.2f", totalInward))
                        SummaryRow("Input CGST", String.format(java.util.Locale.US, "\u20B9%,.2f", purchaseInvoices.sumOf { it.cgstTotal }))
                        SummaryRow("Input SGST", String.format(java.util.Locale.US, "\u20B9%,.2f", purchaseInvoices.sumOf { it.sgstTotal }))
                        SummaryRow("Input IGST", String.format(java.util.Locale.US, "\u20B9%,.2f", purchaseInvoices.sumOf { it.igstTotal }))
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (netTax >= 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("4.1 Eligible ITC", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("Total Output Tax", String.format(java.util.Locale.US, "\u20B9%,.2f", totalOutputTax))
                        SummaryRow("Total Input Tax (ITC)", String.format(java.util.Locale.US, "\u20B9%,.2f", totalInputTax))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow("Net Tax Payable", String.format(java.util.Locale.US, "\u20B9%,.2f", netTax), isBold = true)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val json = generateGstr3bJson(saleInvoices, purchaseInvoices, selectedMonth, selectedYear)
                        val monthStr = gstFilingMonths[selectedMonth].uppercase().take(3)
                        val file = java.io.File(context.cacheDir, "GSTR3B_${monthStr}_${selectedYear}.json")
                        file.writeText(json)
                        PdfGenerator.sharePdf(context, file)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Share GSTR-3B JSON")
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("How to file GSTR-3B:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF8D6E00))
                        Spacer(modifier = Modifier.height(4.dp))
                        listOf(
                            "1. Generate JSON using button above",
                            "2. Go to GST Portal (gst.gov.in)",
                            "3. Login and navigate to GSTR-3B",
                            "4. Click 'Import JSON' to upload",
                            "5. Review outward/inward details",
                            "6. Calculate tax and pay online"
                        ).forEach { Text(it, fontSize = 12.sp, color = Color(0xFF8D6E00)) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = if (isBold) 14.sp else 13.sp, color = if (isBold) TextPrimary else TextSecondary, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontSize = if (isBold) 14.sp else 13.sp, color = if (isBold) BlueHeader else TextPrimary, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium)
    }
}

private fun generateGstr1Json(salesInvoices: List<com.mimo.gstbilling.data.local.entity.InvoiceEntity>, month: Int, year: Int): String {
    val root = JSONObject()
    val dt = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    val monthStr = String.format(Locale.US, "%02d", month + 1)
    root.put("gstin", "")
    root.put("fp", "${monthStr}${year}")
    root.put("gstrVersion", "1.0")

    val b2b = JSONArray()
    salesInvoices.forEach { inv ->
        val invJson = JSONObject()
        invJson.put("ctin", "")
        invJson.put("cfs", "N")
        val invList = JSONArray()
        val invObj = JSONObject()
        invObj.put("inum", inv.invoiceNumber)
        invObj.put("idt", dt.format(Date(inv.invoiceDate)))
        invObj.put("val", inv.totalAmount)
        invObj.put("pos", "")
        invObj.put("rev", "N")
        invObj.put("itcavl", "N")
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
    root.put("b2c", JSONArray())
    root.put("hsn", JSONObject())
    return root.toString(2)
}

private fun generateGstr3bJson(
    salesInvoices: List<com.mimo.gstbilling.data.local.entity.InvoiceEntity>,
    purchaseInvoices: List<com.mimo.gstbilling.data.local.entity.InvoiceEntity>,
    month: Int,
    year: Int
): String {
    val root = JSONObject()
    val monthStr = String.format(Locale.US, "%02d", month + 1)
    root.put("gstin", "")
    root.put("fp", "${monthStr}${year}")
    root.put("gstrVersion", "3.0")

    val outer = JSONObject()
    outer.put("opde", "taxable_outward")
    outer.put("txval", salesInvoices.sumOf { it.taxableAmount })
    outer.put("iamt", salesInvoices.sumOf { it.igstTotal })
    outer.put("camt", salesInvoices.sumOf { it.cgstTotal })
    outer.put("samt", salesInvoices.sumOf { it.sgstTotal })
    outer.put("csamt", salesInvoices.sumOf { it.cessTotal })
    root.put("sup_details", JSONObject().put("osup_zero_gst", outer))

    val itc = JSONObject()
    itc.put("itc_elg", JSONObject().put("iamt", purchaseInvoices.sumOf { it.igstTotal }).put("camt", purchaseInvoices.sumOf { it.cgstTotal }).put("samt", purchaseInvoices.sumOf { it.sgstTotal }).put("csamt", 0.0))
    root.put("itc_details", itc)

    val taxPayable = salesInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal } - purchaseInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
    root.put("taxpayable", JSONObject().put("total", taxPayable))

    return root.toString(2)
}
