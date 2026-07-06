package com.mimo.gstbilling.ui.screens

import android.content.Context
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
fun Gstr1FilingScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedMonth by remember { mutableStateOf("") }
    var showMonthPicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM yyyy", Locale.US) }
    val months = remember {
        val cal = Calendar.getInstance()
        (0 until 12).map {
            val month = dateFormat.format(cal.time)
            cal.add(Calendar.MONTH, -1)
            month
        }
    }

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
                            Icon(Icons.Filled.Description, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("GSTR-1 - Outward Supplies", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Generate GSTR-1 JSON file for GST portal upload", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Tax Period", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = selectedMonth,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Tax Period") },
                            readOnly = true,
                            trailingIcon = { IconButton(onClick = { showMonthPicker = !showMonthPicker }) { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) } },
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (showMonthPicker) {
                            months.forEach { month ->
                                TextButton(onClick = { selectedMonth = month; showMonthPicker = false }, modifier = Modifier.fillMaxWidth()) {
                                    Text(month, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Summary
            val totalInvoices = invoices.filter { it.invoiceType == "sales" }.size
            val totalTaxable = invoices.filter { it.invoiceType == "sales" }.sumOf { it.taxableAmount }
            val totalCgst = invoices.filter { it.invoiceType == "sales" }.sumOf { it.cgstTotal }
            val totalSgst = invoices.filter { it.invoiceType == "sales" }.sumOf { it.sgstTotal }
            val totalIgst = invoices.filter { it.invoiceType == "sales" }.sumOf { it.igstTotal }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("GSTR-1 Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        SummaryRow("B2B Invoices", "$totalInvoices")
                        SummaryRow("Taxable Value", String.format(Locale.US, "\u20B9%,.2f", totalTaxable))
                        SummaryRow("CGST", String.format(Locale.US, "\u20B9%,.2f", totalCgst))
                        SummaryRow("SGST", String.format(Locale.US, "\u20B9%,.2f", totalSgst))
                        SummaryRow("IGST", String.format(Locale.US, "\u20B9%,.2f", totalIgst))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow("Total Tax", String.format(Locale.US, "\u20B9%,.2f", totalCgst + totalSgst + totalIgst), isBold = true)
                    }
                }
            }

            // HSN Summary
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("HSN-wise Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("HSN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.fillMaxWidth(0.3f))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Total outward supplies will be categorized by HSN code", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val json = generateGstr1Json(invoices.filter { it.invoiceType == "sales" })
                        val file = java.io.File(context.cacheDir, "GSTR1_${selectedMonth.ifBlank { "current" }}.json")
                        file.writeText(json)
                        PdfGenerator.sharePdf(context, file)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
    var selectedMonth by remember { mutableStateOf("") }
    var showMonthPicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM yyyy", Locale.US) }
    val months = remember {
        val cal = Calendar.getInstance()
        (0 until 12).map {
            val month = dateFormat.format(cal.time)
            cal.add(Calendar.MONTH, -1)
            month
        }
    }

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
                            Icon(Icons.Filled.Description, contentDescription = null, tint = RedAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("GSTR-3B - Monthly Return", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Generate GSTR-3B JSON for GST portal upload", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Tax Period", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = selectedMonth,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Tax Period") },
                            readOnly = true,
                            trailingIcon = { IconButton(onClick = { showMonthPicker = !showMonthPicker }) { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) } },
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (showMonthPicker) {
                            months.forEach { month ->
                                TextButton(onClick = { selectedMonth = month; showMonthPicker = false }, modifier = Modifier.fillMaxWidth()) {
                                    Text(month, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // GSTR-3B Summary
            val saleInvoices = invoices.filter { it.invoiceType == "sales" }
            val purchaseInvoices = invoices.filter { it.invoiceType == "purchase" }
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
                        SummaryRow("Total Outward Value", String.format(Locale.US, "\u20B9%,.2f", totalOutward))
                        SummaryRow("Output CGST", String.format(Locale.US, "\u20B9%,.2f", saleInvoices.sumOf { it.cgstTotal }))
                        SummaryRow("Output SGST", String.format(Locale.US, "\u20B9%,.2f", saleInvoices.sumOf { it.sgstTotal }))
                        SummaryRow("Output IGST", String.format(Locale.US, "\u20B9%,.2f", saleInvoices.sumOf { it.igstTotal }))
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("3.2 Inward Supplies (Reverse Charge)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("Total Inward Value", String.format(Locale.US, "\u20B9%,.2f", totalInward))
                        SummaryRow("Input CGST", String.format(Locale.US, "\u20B9%,.2f", purchaseInvoices.sumOf { it.cgstTotal }))
                        SummaryRow("Input SGST", String.format(Locale.US, "\u20B9%,.2f", purchaseInvoices.sumOf { it.sgstTotal }))
                        SummaryRow("Input IGST", String.format(Locale.US, "\u20B9%,.2f", purchaseInvoices.sumOf { it.igstTotal }))
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (netTax >= 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("4.1 Eligible ITC", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("Total Output Tax", String.format(Locale.US, "\u20B9%,.2f", totalOutputTax))
                        SummaryRow("Total Input Tax (ITC)", String.format(Locale.US, "\u20B9%,.2f", totalInputTax))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow("Net Tax Payable", String.format(Locale.US, "\u20B9%,.2f", netTax), isBold = true)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val json = generateGstr3bJson(saleInvoices, purchaseInvoices)
                        val file = java.io.File(context.cacheDir, "GSTR3B_${selectedMonth.ifBlank { "current" }}.json")
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
private fun SummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = if (isBold) 14.sp else 13.sp, color = if (isBold) TextPrimary else TextSecondary, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontSize = if (isBold) 14.sp else 13.sp, color = if (isBold) BlueHeader else TextPrimary, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium)
    }
}

private fun generateGstr1Json(salesInvoices: List<com.mimo.gstbilling.data.local.entity.InvoiceEntity>): String {
    val root = JSONObject()
    val dt = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    root.put("gstin", "")
    root.put("fp", SimpleDateFormat("MMyyyy", Locale.US).format(Date()))
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
    purchaseInvoices: List<com.mimo.gstbilling.data.local.entity.InvoiceEntity>
): String {
    val root = JSONObject()
    root.put("gstin", "")
    root.put("fp", SimpleDateFormat("MMyyyy", Locale.US).format(Date()))
    root.put("gstrVersion", "3.0")

    // 3.1 Outward supplies
    val outer = JSONObject()
    outer.put("opde", " taxable_outward")
    outer.put("txval", salesInvoices.sumOf { it.taxableAmount })
    outer.put("iamt", salesInvoices.sumOf { it.igstTotal })
    outer.put("camt", salesInvoices.sumOf { it.cgstTotal })
    outer.put("samt", salesInvoices.sumOf { it.sgstTotal })
    outer.put("csamt", salesInvoices.sumOf { it.cessTotal })
    root.put("sup_details", JSONObject().put("osup_zero_gst", outer))

    // 4. ITC
    val itc = JSONObject()
    itc.put("itc_elg", JSONObject().put("iamt", purchaseInvoices.sumOf { it.igstTotal }).put("camt", purchaseInvoices.sumOf { it.cgstTotal }).put("samt", purchaseInvoices.sumOf { it.sgstTotal }).put("csamt", 0.0))
    root.put("itc_details", itc)

    // 6. Payment of tax
    val taxPayable = salesInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal } - purchaseInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
    root.put("taxpayable", JSONObject().put("total", taxPayable))

    return root.toString(2)
}
