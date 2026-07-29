package com.mimo.gstbilling.ui.screens

import android.content.Intent
import android.os.Environment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private enum class Gstr1SaleTab(val label: String) {
    SALE("Sale"),
    SALE_RETURN("Sale Return")
}

private data class Gstr1Row(
    val partyName: String,
    val gstin: String,
    val invoiceNo: String,
    val invoiceDate: Long,
    val taxableValue: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double,
    val totalValue: Double,
    val isSaleReturn: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr1ReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    val parties by viewModel.getParties().collectAsState(initial = emptyList())
    val allInvoiceItems by viewModel.getAllInvoiceItems().collectAsState(initial = emptyList())

    val months = listOf("January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December")

    val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    var startMonth by remember { mutableIntStateOf(currentMonth) }
    var startYear by remember { mutableIntStateOf(currentYear) }
    var endMonth by remember { mutableIntStateOf(currentMonth) }
    var endYear by remember { mutableIntStateOf(currentYear) }
    var considerNonTaxExempted by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(Gstr1SaleTab.SALE) }
    var showStartMonthDropdown by remember { mutableStateOf(false) }
    var showStartYearDropdown by remember { mutableStateOf(false) }
    var showEndMonthDropdown by remember { mutableStateOf(false) }
    var showEndYearDropdown by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val partyMap = remember(parties) { parties.associateBy { it.id } }
    val partyGstinMap = remember(parties) { parties.associateBy({ it.id }, { it.gstin ?: "" }) }

    val allRows = remember(invoices, partyMap, startMonth, startYear, endMonth, endYear, allInvoiceItems) {
        val partyItemMap = allInvoiceItems.groupBy { it.invoiceId }
        invoices.filter { inv ->
            val cal = Calendar.getInstance().apply { timeInMillis = inv.invoiceDate }
            val invMonth = cal.get(Calendar.MONTH)
            val invYear = cal.get(Calendar.YEAR)
            val inRange = when {
                startYear == endYear -> invYear == startYear && invMonth in startMonth..endMonth
                invYear == startYear -> invMonth >= startMonth
                invYear == endYear -> invMonth <= endMonth
                invYear in (startYear + 1) until endYear -> true
                else -> false
            }
            inRange && (inv.invoiceType == "sales" || inv.invoiceType == "sale_return" || inv.invoiceType == "credit_note")
        }.map { inv ->
            val party = partyMap[inv.partyId]
            val items = partyItemMap[inv.id] ?: emptyList()
            val taxable = items.sumOf { it.taxableAmount }
            val cgst = items.sumOf { it.cgstAmount }
            val sgst = items.sumOf { it.sgstAmount }
            val igst = items.sumOf { it.igstAmount }
            val isReturn = inv.invoiceType == "sale_return" || inv.invoiceType == "credit_note"
            Gstr1Row(
                partyName = party?.name ?: inv.partyName ?: "Walk-in",
                gstin = partyGstinMap[inv.partyId] ?: "",
                invoiceNo = inv.invoiceNumber,
                invoiceDate = inv.invoiceDate,
                taxableValue = taxable,
                cgst = cgst, sgst = sgst, igst = igst,
                totalValue = inv.totalAmount,
                isSaleReturn = isReturn
            )
        }.sortedBy { it.invoiceDate }
    }

    val saleRows = remember(allRows) { allRows.filter { !it.isSaleReturn } }
    val saleReturnRows = remember(allRows) { allRows.filter { it.isSaleReturn } }
    val displayRows = if (selectedTab == Gstr1SaleTab.SALE) saleRows else saleReturnRows

    val saleTotals = remember(saleRows) {
        Triple(saleRows.sumOf { it.taxableValue }, saleRows.sumOf { it.cgst + it.sgst }, saleRows.sumOf { it.igst })
    }
    val returnTotals = remember(saleReturnRows) {
        Triple(saleReturnRows.sumOf { it.taxableValue }, saleReturnRows.sumOf { it.cgst + it.sgst }, saleReturnRows.sumOf { it.igst })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GSTR1 Report", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val file = generateGstr1Pdf(context, saleRows, saleReturnRows, months[selectedMonth(startMonth, startYear, endMonth, endYear)])
                            shareFile(context, file, "application/pdf", "Share GSTR-1 PDF")
                        }
                    }) {
                        Box(modifier = Modifier.background(Color(0xFFE53935), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 3.dp)) {
                            Text("Pdf", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = {
                        scope.launch {
                            val file = generateGstr1Excel(context, saleRows, saleReturnRows, months, startMonth, startYear, endMonth, endYear)
                            shareFile(context, file, "application/vnd.ms-excel", "Share GSTR-1 Excel")
                        }
                    }) {
                        Box(modifier = Modifier.background(Color(0xFF4CAF50), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 3.dp)) {
                            Text("xls", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = VyaparTextPrimary)
                        }
                        DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Share with CA") },
                                onClick = {
                                    showMoreMenu = false
                                    scope.launch {
                                        val file = generateGstr1Excel(context, saleRows, saleReturnRows, months, startMonth, startYear, endMonth, endYear)
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "${context.packageName}.provider", file))
                                            putExtra(Intent.EXTRA_TEXT, "GSTR-1 Report ${months[startMonth]} $startYear to ${months[endMonth]} $endYear")
                                            type = "application/vnd.ms-excel"
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share with CA"))
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Download as JSON") },
                                onClick = {
                                    showMoreMenu = false
                                    scope.launch {
                                        val file = generateGstr1Json(context, saleRows, saleReturnRows)
                                        shareFile(context, file, "application/json", "Download GSTR-1 JSON")
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A),
                    navigationIconContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F6F6))
        ) {
            Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${months[startMonth]} $startYear", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A),
                        modifier = Modifier.clickable { showStartMonthDropdown = true })
                    Text("  To  ", fontSize = 14.sp, color = Color(0xFF888888))
                    Text("${months[endMonth]} $endYear", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A),
                        modifier = Modifier.clickable { showEndMonthDropdown = true })

                    DropdownMenu(expanded = showStartMonthDropdown, onDismissRequest = { showStartMonthDropdown = false }) {
                        months.forEachIndexed { idx, m ->
                            DropdownMenuItem(text = { Text(m) }, onClick = { startMonth = idx; showStartMonthDropdown = false })
                        }
                    }
                    DropdownMenu(expanded = showEndMonthDropdown, onDismissRequest = { showEndMonthDropdown = false }) {
                        months.forEachIndexed { idx, m ->
                            DropdownMenuItem(text = { Text(m) }, onClick = { endMonth = idx; showEndMonthDropdown = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { considerNonTaxExempted = !considerNonTaxExempted }) {
                    Checkbox(checked = considerNonTaxExempted, onCheckedChange = { considerNonTaxExempted = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFE53935)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Consider non tax transactions as exempted", fontSize = 13.sp, color = Color(0xFF555555))
                }
            }

            TabRow(
                selectedTabIndex = Gstr1SaleTab.entries.indexOf(selectedTab),
                containerColor = Color.White,
                contentColor = Color(0xFFE53935),
                edgePadding = 0.dp,
                divider = { HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp) },
                indicator = { tabPositions ->
                    if (Gstr1SaleTab.entries.indexOf(selectedTab) < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[Gstr1SaleTab.entries.indexOf(selectedTab)]),
                            height = 3.dp,
                            color = Color(0xFFE53935)
                        )
                    }
                }
            ) {
                Gstr1SaleTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                tab.label,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == tab) Color(0xFFE53935) else Color(0xFF888888),
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F0F0)).padding(horizontal = 0.dp, vertical = 10.dp)) {
                        Text("GSTIN/UIN No.", modifier = Modifier.weight(0.30f).padding(start = 12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                        Text("Party Name", modifier = Modifier.weight(0.40f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                        Text("No.", modifier = Modifier.weight(0.30f).padding(end = 12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555), textAlign = TextAlign.End)
                    }
                }

                if (displayRows.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Text("No invoices found for this period", fontSize = 14.sp, color = Color(0xFF999999))
                        }
                    }
                } else {
                    itemsIndexed(displayRows) { index, row ->
                        val bgColor = if (index % 2 == 0) Color.White else Color(0xFFF8F9FA)
                        Row(modifier = Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 0.dp, vertical = 12.dp)) {
                            Text(row.gstin.ifBlank { "" }, modifier = Modifier.weight(0.30f).padding(start = 12.dp), fontSize = 12.sp, color = Color(0xFF333333), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(row.partyName, modifier = Modifier.weight(0.40f), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(row.invoiceNo, modifier = Modifier.weight(0.30f).padding(end = 12.dp), fontSize = 12.sp, color = Color(0xFF333333), textAlign = TextAlign.End)
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE8F5E9)).padding(horizontal = 0.dp, vertical = 12.dp)) {
                        Text("Totals", modifier = Modifier.weight(0.30f).padding(start = 12.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                        Text("", modifier = Modifier.weight(0.40f))
                        Column(modifier = Modifier.weight(0.30f).padding(end = 12.dp), horizontalAlignment = Alignment.End) {
                            if (selectedTab == Gstr1SaleTab.SALE) {
                                Text(String.format(Locale.US, "\u20B9%,.2f", saleTotals.first), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                Text("CGST+SGST: ${String.format(Locale.US, "\u20B9%,.2f", saleTotals.second)}", fontSize = 10.sp, color = Color(0xFF555555))
                                Text("IGST: ${String.format(Locale.US, "\u20B9%,.2f", saleTotals.third)}", fontSize = 10.sp, color = Color(0xFF555555))
                            } else {
                                Text(String.format(Locale.US, "\u20B9%,.2f", returnTotals.first), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                Text("CGST+SGST: ${String.format(Locale.US, "\u20B9%,.2f", returnTotals.second)}", fontSize = 10.sp, color = Color(0xFF555555))
                                Text("IGST: ${String.format(Locale.US, "\u20B9%,.2f", returnTotals.third)}", fontSize = 10.sp, color = Color(0xFF555555))
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

private fun selectedMonth(sm: Int, sy: Int, em: Int, ey: Int): String {
    return ""
}

private fun generateGstr1Pdf(context: android.content.Context, saleRows: List<Gstr1Row>, returnRows: List<Gstr1Row>, period: String): File {
    val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GSTR1")
    dir.mkdirs()
    val file = File(dir, "GSTR1_Report.pdf")
    val doc = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = doc.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint().apply { textSize = 12f; color = android.graphics.Color.BLACK }
    val boldPaint = android.graphics.Paint().apply { textSize = 14f; color = android.graphics.Color.BLACK; isFakeBoldText = true }
    var y = 40f

    canvas.drawText("GSTR-1 Report", 40f, y, boldPaint.apply { textSize = 18f })
    y += 30f
    canvas.drawText("Sale Invoices: ${saleRows.size}  |  Sale Returns: ${returnRows.size}", 40f, y, paint)
    y += 25f
    canvas.drawLine(40f, y, 555f, y, paint.apply { strokeWidth = 1f; color = android.graphics.Color.GRAY })
    y += 20f

    paint.textSize = 10f
    canvas.drawText("GSTIN/UIN No.", 40f, y, paint.apply { isFakeBoldText = true })
    canvas.drawText("Party Name", 200f, y, paint)
    canvas.drawText("No.", 380f, y, paint)
    canvas.drawText("Taxable", 440f, y, paint)
    canvas.drawText("Total", 510f, y, paint)
    y += 15f
    canvas.drawLine(40f, y, 555f, y, paint.apply { color = android.graphics.Color.LTGRAY })
    y += 12f

    fun drawRows(rows: List<Gstr1Row>) {
        paint.isFakeBoldText = false
        paint.textSize = 9f
        for (row in rows) {
            if (y > 780f) return
            canvas.drawText(row.gstin.take(16), 40f, y, paint)
            canvas.drawText(row.partyName.take(20), 200f, y, paint)
            canvas.drawText(row.invoiceNo, 380f, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", row.taxableValue), 440f, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", row.totalValue), 510f, y, paint)
            y += 14f
        }
    }

    if (saleRows.isNotEmpty()) {
        paint.textSize = 12f; paint.isFakeBoldText = true; paint.color = android.graphics.Color.parseColor("#E53935")
        canvas.drawText("SALE INVOICES", 40f, y, paint); y += 16f
        paint.color = android.graphics.Color.BLACK
        drawRows(saleRows)
    }
    if (returnRows.isNotEmpty()) {
        y += 10f
        paint.textSize = 12f; paint.isFakeBoldText = true; paint.color = android.graphics.Color.parseColor("#E53935")
        canvas.drawText("SALE RETURNS", 40f, y, paint); y += 16f
        paint.color = android.graphics.Color.BLACK
        drawRows(returnRows)
    }

    doc.finishPage(page)
    doc.writeTo(file.outputStream())
    doc.close()
    return file
}

private fun generateGstr1Excel(context: android.content.Context, saleRows: List<Gstr1Row>, returnRows: List<Gstr1Row>, months: List<String>, sm: Int, sy: Int, em: Int, ey: Int): File {
    val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GSTR1")
    dir.mkdirs()
    val file = File(dir, "GSTR1_Report.xls")
    val sb = StringBuilder()
    sb.appendLine("GSTR-1 Report - ${months[sm]} $sy to ${months[em]} $ey")
    sb.appendLine()
    sb.appendLine("=== SALE INVOICES ===")
    sb.appendLine("GSTIN/UIN No.,Party Name,Invoice No,Date,Taxable Value,CGST,SGST,IGST,Total")
    for (row in saleRows) {
        sb.appendLine("${row.gstin},${row.partyName},${row.invoiceNo},${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(row.invoiceDate))},${String.format(Locale.US, "%.2f", row.taxableValue)},${String.format(Locale.US, "%.2f", row.cgst)},${String.format(Locale.US, "%.2f", row.sgst)},${String.format(Locale.US, "%.2f", row.igst)},${String.format(Locale.US, "%.2f", row.totalValue)}")
    }
    sb.appendLine("TOTAL,,,,${String.format(Locale.US, "%.2f", saleRows.sumOf { it.taxableValue })},${String.format(Locale.US, "%.2f", saleRows.sumOf { it.cgst })},${String.format(Locale.US, "%.2f", saleRows.sumOf { it.sgst })},${String.format(Locale.US, "%.2f", saleRows.sumOf { it.igst })},${String.format(Locale.US, "%.2f", saleRows.sumOf { it.totalValue })}")
    sb.appendLine()
    sb.appendLine("=== SALE RETURNS ===")
    sb.appendLine("GSTIN/UIN No.,Party Name,Invoice No,Date,Taxable Value,CGST,SGST,IGST,Total")
    for (row in returnRows) {
        sb.appendLine("${row.gstin},${row.partyName},${row.invoiceNo},${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(row.invoiceDate))},${String.format(Locale.US, "%.2f", row.taxableValue)},${String.format(Locale.US, "%.2f", row.cgst)},${String.format(Locale.US, "%.2f", row.sgst)},${String.format(Locale.US, "%.2f", row.igst)},${String.format(Locale.US, "%.2f", row.totalValue)}")
    }
    sb.appendLine("TOTAL,,,,${String.format(Locale.US, "%.2f", returnRows.sumOf { it.taxableValue })},${String.format(Locale.US, "%.2f", returnRows.sumOf { it.cgst })},${String.format(Locale.US, "%.2f", returnRows.sumOf { it.sgst })},${String.format(Locale.US, "%.2f", returnRows.sumOf { it.igst })},${String.format(Locale.US, "%.2f", returnRows.sumOf { it.totalValue })}")
    file.writeText(sb.toString())
    return file
}

private fun generateGstr1Json(context: android.content.Context, saleRows: List<Gstr1Row>, returnRows: List<Gstr1Row>): File {
    val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GSTR1")
    dir.mkdirs()
    val file = File(dir, "GSTR1_Upload.json")
    val root = JSONObject()

    val b2b = JSONArray()
    val grouped = saleRows.filter { it.gstin.isNotBlank() }.groupBy { it.gstin }
    for ((gstin, rows) in grouped) {
        val invList = JSONArray()
        for (row in rows) {
            val inv = JSONObject()
            inv.put("inum", row.invoiceNo)
            inv.put("idt", SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(row.invoiceDate)))
            inv.put("val", String.format(Locale.US, "%.2f", row.totalValue))
            inv.put("pos", "")
            inv.put("typ", "R")
            val itms = JSONArray()
            val item = JSONObject()
            item.put("num", 1)
            item.put("itm_det", JSONObject().apply {
                put("rt", 18.0)
                put("qty", "1.00")
                put("txval", String.format(Locale.US, "%.2f", row.taxableValue))
                put("iamt", String.format(Locale.US, "%.2f", row.igst))
                put("camt", String.format(Locale.US, "%.2f", row.cgst))
                put("samt", String.format(Locale.US, "%.2f", row.sgst))
                put("csamt", 0)
            })
            itms.put(item)
            inv.put("itms", itms)
            invList.put(inv)
        }
        val b2bEntry = JSONObject()
        b2bEntry.put("gstin", gstin)
        b2bEntry.put("b2b", invList)
        b2b.put(b2bEntry)
    }
    root.put("b2b", b2b)

    val b2cs = JSONArray()
    for (row in saleRows.filter { it.gstin.isBlank() }) {
        val entry = JSONObject()
        entry.put("typ", "OE")
        entry.put("pos", "")
        entry.put("txval", String.format(Locale.US, "%.2f", row.taxableValue))
        entry.put("iamt", String.format(Locale.US, "%.2f", row.igst))
        entry.put("camt", String.format(Locale.US, "%.2f", row.cgst))
        entry.put("samt", String.format(Locale.US, "%.2f", row.sgst))
        entry.put("csamt", 0)
        b2cs.put(entry)
    }
    root.put("b2cs", b2cs)

    val cdn = JSONArray()
    for (row in returnRows) {
        val note = JSONObject()
        note.put("nt_num", row.invoiceNo)
        note.put("nt_dt", SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(row.invoiceDate)))
        note.put("ntty", "C")
        note.put("val", String.format(Locale.US, "%.2f", row.totalValue))
        val itms = JSONArray()
        val item = JSONObject()
        item.put("num", 1)
        item.put("itm_det", JSONObject().apply {
            put("rt", 18.0)
            put("txval", String.format(Locale.US, "%.2f", row.taxableValue))
            put("iamt", String.format(Locale.US, "%.2f", row.igst))
            put("camt", String.format(Locale.US, "%.2f", row.cgst))
            put("samt", String.format(Locale.US, "%.2f", row.sgst))
            put("csamt", 0)
        })
        itms.put(item)
        note.put("itms", itms)
        cdn.put(note)
    }
    root.put("cdn", cdn)

    file.writeText(root.toString(2))
    return file
}

private fun shareFile(context: android.content.Context, file: File, mimeType: String, title: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, uri)
        type = mimeType
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(sendIntent, title))
}
