package com.mimo.gstbilling.ui.screens

import android.content.Intent
import android.os.Environment
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

private enum class Gstr1Tab(val label: String) {
    B2B("B2B"),
    B2C("B2C"),
    HSN("HSN"),
    CREDIT_NOTES("Credit Notes"),
    SUMMARY("Summary")
}

private data class B2BInvoice(
    val invoice: InvoiceEntity,
    val party: PartyEntity?,
    val items: List<InvoiceItemEntity>
)

private data class B2CInvoice(
    val invoice: InvoiceEntity,
    val items: List<InvoiceItemEntity>
)

private data class HsnSummary(
    val hsnCode: String,
    val description: String,
    val uqc: String,
    val totalQuantity: Double,
    val taxableValue: Double,
    val igstAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val cessAmount: Double
)

private data class Gstr1ReportData(
    val b2bInvoices: List<B2BInvoice>,
    val b2cInvoices: List<B2CInvoice>,
    val hsnSummaries: List<HsnSummary>,
    val totalInvoices: Int,
    val totalTaxableValue: Double,
    val totalCgst: Double,
    val totalSgst: Double,
    val totalIgst: Double,
    val totalTax: Double,
    val b2bCount: Int,
    val b2cCount: Int
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
    val company by produceState<com.mimo.gstbilling.data.local.entity.CompanyEntity?>(null) {
        value = viewModel.getCompanyById(1L)
    }

    val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedYear by remember { mutableIntStateOf(currentYear) }

    val months = remember {
        listOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
    }
    val years = remember {
        val cy = Calendar.getInstance().get(Calendar.YEAR)
        (cy downTo cy - 5).toList()
    }

    var selectedTab by remember { mutableStateOf(Gstr1Tab.B2B) }
    var showMonthDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }

    val reportData = remember(invoices, parties, allInvoiceItems, selectedMonth, selectedYear) {
        generateGstr1Data(invoices, parties, allInvoiceItems, selectedMonth, selectedYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GSTR-1 Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VyaparWhite,
                    titleContentColor = VyaparTextPrimary,
                    navigationIconContentColor = VyaparTextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
        ) {
            // Period Selector
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Period", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = VyaparTextPrimary)

                    // Month Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            onClick = { showMonthDropdown = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = VyaparWhite),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(months[selectedMonth], fontSize = 13.sp, color = VyaparTextPrimary)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = VyaparTextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                        DropdownMenu(expanded = showMonthDropdown, onDismissRequest = { showMonthDropdown = false }) {
                            months.forEachIndexed { index, month ->
                                DropdownMenuItem(
                                    text = { Text(month, fontSize = 13.sp) },
                                    onClick = {
                                        selectedMonth = index
                                        showMonthDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Year Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            onClick = { showYearDropdown = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = VyaparWhite),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$selectedYear", fontSize = 13.sp, color = VyaparTextPrimary)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = VyaparTextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                        DropdownMenu(expanded = showYearDropdown, onDismissRequest = { showYearDropdown = false }) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text("$year", fontSize = 13.sp) },
                                    onClick = {
                                        selectedYear = year
                                        showYearDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMiniCard(
                    title = "Total Invoices",
                    value = "${reportData.totalInvoices}",
                    color = VyaparBlue,
                    modifier = Modifier.weight(1f)
                )
                SummaryMiniCard(
                    title = "B2B",
                    value = "${reportData.b2bCount}",
                    color = VyaparGreen,
                    modifier = Modifier.weight(1f)
                )
                SummaryMiniCard(
                    title = "B2C",
                    value = "${reportData.b2cCount}",
                    color = VyaparOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tax Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparBlue)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Taxable Value", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text(
                            String.format(Locale.US, "\u20B9%,.2f", reportData.totalTaxableValue),
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparGreen)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Tax", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text(
                            String.format(Locale.US, "\u20B9%,.2f", reportData.totalTax),
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CGST + SGST + IGST row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatCard("CGST", reportData.totalCgst, Modifier.weight(1f))
                MiniStatCard("SGST", reportData.totalSgst, Modifier.weight(1f))
                MiniStatCard("IGST", reportData.totalIgst, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section Tabs
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = Gstr1Tab.entries.indexOf(selectedTab),
                    containerColor = Color.Transparent,
                    contentColor = VyaparBlue,
                    edgePadding = 4.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        if (Gstr1Tab.entries.indexOf(selectedTab) < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[Gstr1Tab.entries.indexOf(selectedTab)]),
                                height = 3.dp,
                                color = VyaparBlue
                            )
                        }
                    }
                ) {
                    Gstr1Tab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    tab.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == tab) VyaparBlue else VyaparTextSecondary
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            when (selectedTab) {
                Gstr1Tab.B2B -> B2BContent(reportData.b2bInvoices)
                Gstr1Tab.B2C -> B2CContent(reportData.b2cInvoices)
                Gstr1Tab.HSN -> HSNContent(reportData.hsnSummaries)
                Gstr1Tab.CREDIT_NOTES -> CreditNotesContent()
                Gstr1Tab.SUMMARY -> SummaryContent(reportData)
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val file = generateGstr1Json(context, reportData, company, selectedMonth, selectedYear)
                            shareFile(context, file, "application/json", "Share GSTR-1 JSON")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download JSON", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        scope.launch {
                            val file = generateGstr1Excel(context, reportData, selectedMonth, selectedYear)
                            shareFile(context, file, "application/vnd.ms-excel", "Share GSTR-1 Excel")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparGreen)
                ) {
                    Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Excel", fontWeight = FontWeight.Medium)
                }

                OutlinedButton(
                    onClick = {
                        navController.navigate("eway_bill")
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparBlue)
                ) {
                    Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate E-Way Bill", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SummaryMiniCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = color.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun MiniStatCard(label: String, amount: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = VyaparWhite)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = VyaparTextSecondary)
            Text(
                String.format(Locale.US, "\u20B9%,.2f", amount),
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary
            )
        }
    }
}

@Composable
private fun B2BContent(b2bInvoices: List<B2BInvoice>) {
    if (b2bInvoices.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No B2B invoices for this period", color = VyaparTextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("GSTIN", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(1.2f))
                Text("Inv No", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(1f))
                Text("Taxable", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(1f))
                Text("CGST", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(0.8f))
                Text("SGST", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(0.8f))
                Text("IGST", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(0.8f))
            }
        }

        items(b2bInvoices) { b2b ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            b2b.party?.gstin ?: "N/A",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = VyaparTextPrimary,
                            modifier = Modifier.weight(1.2f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(b2b.invoice.invoiceNumber, fontSize = 12.sp, color = VyaparTextPrimary, modifier = Modifier.weight(1f))
                        Text(
                            String.format(Locale.US, "%.2f", b2b.invoice.taxableAmount),
                            fontSize = 12.sp, color = VyaparTextPrimary, modifier = Modifier.weight(1f)
                        )
                        Text(
                            String.format(Locale.US, "%.2f", b2b.invoice.cgstTotal),
                            fontSize = 12.sp, color = VyaparTextPrimary, modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            String.format(Locale.US, "%.2f", b2b.invoice.sgstTotal),
                            fontSize = 12.sp, color = VyaparTextPrimary, modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            String.format(Locale.US, "%.2f", b2b.invoice.igstTotal),
                            fontSize = 12.sp, color = VyaparTextPrimary, modifier = Modifier.weight(0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        b2b.party?.name ?: "Unknown Party",
                        fontSize = 11.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        "Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(b2b.invoice.invoiceDate))} | Items: ${b2b.items.size}",
                        fontSize = 11.sp,
                        color = VyaparTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun B2CContent(b2cInvoices: List<B2CInvoice>) {
    if (b2cInvoices.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No B2C invoices for this period", color = VyaparTextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(b2cInvoices) { b2c ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(b2c.invoice.invoiceNumber, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = VyaparTextPrimary)
                        Text(
                            String.format(Locale.US, "\u20B9%,.2f", b2c.invoice.totalAmount),
                            fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyaparBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(b2c.invoice.invoiceDate))}",
                        fontSize = 12.sp, color = VyaparTextSecondary
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Taxable: ${String.format(Locale.US, "%.2f", b2c.invoice.taxableAmount)}", fontSize = 11.sp, color = VyaparTextSecondary)
                        Text("CGST: ${String.format(Locale.US, "%.2f", b2c.invoice.cgstTotal)}", fontSize = 11.sp, color = VyaparTextSecondary)
                        Text("SGST: ${String.format(Locale.US, "%.2f", b2c.invoice.sgstTotal)}", fontSize = 11.sp, color = VyaparTextSecondary)
                    }
                    if (b2c.invoice.igstTotal > 0) {
                        Text("IGST: ${String.format(Locale.US, "%.2f", b2c.invoice.igstTotal)}", fontSize = 11.sp, color = VyaparTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun HSNContent(hsnSummaries: List<HsnSummary>) {
    if (hsnSummaries.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No HSN data for this period", color = VyaparTextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("HSN", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(1f))
                Text("Qty", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(0.7f))
                Text("Taxable", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(1f))
                Text("CGST", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(0.8f))
                Text("SGST", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(0.8f))
                Text("IGST", fontSize = 10.sp, color = VyaparTextSecondary, modifier = Modifier.weight(0.8f))
            }
        }

        items(hsnSummaries) { hsn ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(hsn.hsnCode, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = VyaparTextPrimary)
                            Text(hsn.description, fontSize = 11.sp, color = VyaparTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(String.format(Locale.US, "%.1f", hsn.totalQuantity), fontSize = 12.sp, color = VyaparTextPrimary, modifier = Modifier.weight(0.7f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(String.format(Locale.US, "%.2f", hsn.taxableValue), fontSize = 12.sp, color = VyaparTextPrimary, modifier = Modifier.weight(1f))
                        Text(String.format(Locale.US, "%.2f", hsn.cgstAmount), fontSize = 12.sp, color = VyaparTextPrimary, modifier = Modifier.weight(0.8f))
                        Text(String.format(Locale.US, "%.2f", hsn.sgstAmount), fontSize = 12.sp, color = VyaparTextPrimary, modifier = Modifier.weight(0.8f))
                        Text(String.format(Locale.US, "%.2f", hsn.igstAmount), fontSize = 12.sp, color = VyaparTextPrimary, modifier = Modifier.weight(0.8f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditNotesContent() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Receipt,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = VyaparTextSecondary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Credit/Debit Notes", color = VyaparTextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("No credit or debit notes for this period", color = VyaparTextSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun SummaryContent(reportData: Gstr1ReportData) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparBlue)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("GSTR-1 Summary", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    SummaryRow("Total Sales Invoices", "${reportData.totalInvoices}")
                    SummaryRow("B2B Invoices", "${reportData.b2bCount}")
                    SummaryRow("B2C Invoices", "${reportData.b2cCount}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))
                    SummaryRow("Total Taxable Value", String.format(Locale.US, "\u20B9%,.2f", reportData.totalTaxableValue))
                    SummaryRow("CGST", String.format(Locale.US, "\u20B9%,.2f", reportData.totalCgst))
                    SummaryRow("SGST", String.format(Locale.US, "\u20B9%,.2f", reportData.totalSgst))
                    SummaryRow("IGST", String.format(Locale.US, "\u20B9%,.2f", reportData.totalIgst))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))
                    SummaryRow("Total Tax", String.format(Locale.US, "\u20B9%,.2f", reportData.totalTax))
                    SummaryRow(
                        "Invoice Value",
                        String.format(Locale.US, "\u20B9%,.2f", reportData.totalTaxableValue + reportData.totalTax),
                        bold = true
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("HSN Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total HSN Codes: ${reportData.hsnSummaries.size}", fontSize = 13.sp, color = VyaparTextSecondary)
                    Text("Unique items with different HSN codes reported in GSTR-1", fontSize = 12.sp, color = VyaparTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        Text(
            value,
            color = Color.White,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            fontSize = if (bold) 16.sp else 13.sp
        )
    }
}

private fun generateGstr1Data(
    invoices: List<InvoiceEntity>,
    parties: List<PartyEntity>,
    allInvoiceItems: List<InvoiceItemEntity>,
    selectedMonth: Int,
    selectedYear: Int
): Gstr1ReportData {
    val partyMap = parties.associateBy { it.id }
    val calendar = Calendar.getInstance()

    val filteredInvoices = invoices.filter { invoice ->
        calendar.timeInMillis = invoice.invoiceDate
        calendar.get(Calendar.MONTH) == selectedMonth &&
            calendar.get(Calendar.YEAR) == selectedYear &&
            invoice.invoiceType == "sales"
    }

    val b2bList = mutableListOf<B2BInvoice>()
    val b2cList = mutableListOf<B2CInvoice>()

    filteredInvoices.forEach { invoice ->
        val party = partyMap[invoice.partyId]
        val items = allInvoiceItems.filter { it.invoiceId == invoice.id }
        if (!party?.gstin.isNullOrBlank()) {
            b2bList.add(B2BInvoice(invoice, party, items))
        } else {
            b2cList.add(B2CInvoice(invoice, items))
        }
    }

    val allItems = filteredInvoices.flatMap { inv ->
        allInvoiceItems.filter { it.invoiceId == inv.id }
    }

    val hsnMap = mutableMapOf<String, HsnSummary>()
    allItems.forEach { item ->
        val code = item.hsnCode ?: "0000"
        val existing = hsnMap[code]
        if (existing == null) {
            hsnMap[code] = HsnSummary(
                hsnCode = code,
                description = item.itemName,
                uqc = item.unit,
                totalQuantity = item.quantity,
                taxableValue = item.taxableAmount,
                igstAmount = item.igstAmount,
                cgstAmount = item.cgstAmount,
                sgstAmount = item.sgstAmount,
                cessAmount = 0.0
            )
        } else {
            hsnMap[code] = existing.copy(
                totalQuantity = existing.totalQuantity + item.quantity,
                taxableValue = existing.taxableValue + item.taxableAmount,
                igstAmount = existing.igstAmount + item.igstAmount,
                cgstAmount = existing.cgstAmount + item.cgstAmount,
                sgstAmount = existing.sgstAmount + item.sgstAmount
            )
        }
    }

    return Gstr1ReportData(
        b2bInvoices = b2bList,
        b2cInvoices = b2cList,
        hsnSummaries = hsnMap.values.sortedByDescending { it.taxableValue },
        totalInvoices = filteredInvoices.size,
        totalTaxableValue = filteredInvoices.sumOf { it.taxableAmount },
        totalCgst = filteredInvoices.sumOf { it.cgstTotal },
        totalSgst = filteredInvoices.sumOf { it.sgstTotal },
        totalIgst = filteredInvoices.sumOf { it.igstTotal },
        totalTax = filteredInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal },
        b2bCount = b2bList.size,
        b2cCount = b2cList.size
    )
}

private fun generateGstr1Json(
    context: android.content.Context,
    data: Gstr1ReportData,
    company: com.mimo.gstbilling.data.local.entity.CompanyEntity?,
    month: Int,
    year: Int
): File {
    val fp = String.format("%02d%04d", month + 1, year)
    val root = JSONObject()
    root.put("gstin", company?.gstin ?: "")
    root.put("fp", fp)

    val b2bArray = JSONArray()
    data.b2bInvoices.forEach { b2b ->
        val invObj = JSONObject()
        invObj.put("inum", b2b.invoice.invoiceNumber)
        invObj.put("dt", SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(b2b.invoice.invoiceDate)))
        invObj.put("val", b2b.invoice.totalAmount)
        invObj.put("pos", company?.stateCode ?: "")
        invObj.put("rev", "N")
        invObj.put("typ", "R")

        val itemsArray = JSONArray()
        b2b.items.forEach { item ->
            val itemObj = JSONObject()
            itemObj.put("num", 1)
            itemObj.put("prdgslcd", item.hsnCode ?: "")
            itemObj.put("itm", item.itemName)
            itemObj.put("qty", item.quantity)
            itemObj.put("uqc", item.unit)
            itemObj.put("txval", item.taxableAmount)
            itemObj.put("camt", item.cgstAmount)
            itemObj.put("samt", item.sgstAmount)
            itemObj.put("iamt", item.igstAmount)
            itemObj.put("csamt", 0.0)
            itemsArray.put(itemObj)
        }
        invObj.put("itms", itemsArray)

        val docObj = JSONObject()
        docObj.put("gstin", b2b.party?.gstin ?: "")
        docObj.put("inv", JSONArray().put(invObj))
        b2bArray.put(docObj)
    }
    root.put("b2b", b2bArray)

    val b2csArray = JSONArray()
    data.b2cInvoices.forEach { b2c ->
        b2c.items.forEach { item ->
            val b2csObj = JSONObject()
            b2csObj.put("ty", "OE")
            b2csObj.put("txval", item.taxableAmount)
            b2csObj.put("camt", item.cgstAmount)
            b2csObj.put("samt", item.sgstAmount)
            b2csObj.put("iamt", item.igstAmount)
            b2csObj.put("csamt", 0.0)
            b2csObj.put("dt", SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(b2c.invoice.invoiceDate)))
            b2csArray.put(b2csObj)
        }
    }
    root.put("b2cs", b2csArray)

    val hsnArray = JSONArray()
    data.hsnSummaries.forEach { hsn ->
        val hsnObj = JSONObject()
        hsnObj.put("hsn_cd", hsn.hsnCode)
        hsnObj.put("desc", hsn.description)
        hsnObj.put("uqc", hsn.uqc)
        hsnObj.put("qty", hsn.totalQuantity)
        hsnObj.put("txval", hsn.taxableValue)
        hsnObj.put("iamt", hsn.igstAmount)
        hsnObj.put("camt", hsn.cgstAmount)
        hsnObj.put("samt", hsn.sgstAmount)
        hsnObj.put("csamt", hsn.cessAmount)
        hsnArray.put(hsnObj)
    }
    root.put("hsn", hsnArray)

    root.put("cdn", JSONArray())

    val fileName = "GSTR1_${company?.gstin ?: "data"}_${String.format("%02d%04d", month + 1, year)}.json"
    val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GSTR1")
    dir.mkdirs()
    val file = File(dir, fileName)
    file.writeText(root.toString(2))
    return file
}

private fun generateGstr1Excel(
    context: android.content.Context,
    data: Gstr1ReportData,
    month: Int,
    year: Int
): File {
    val sb = StringBuilder()
    sb.appendLine("GSTR-1 Report - ${String.format("%02d/%04d", month + 1, year)}")
    sb.appendLine()

    sb.appendLine("=== B2B INVOICES ===")
    sb.appendLine("GSTIN,Invoice No,Date,Taxable Value,CGST,SGST,IGST,Total")
    data.b2bInvoices.forEach { b2b ->
        sb.appendLine(
            "${b2b.party?.gstin ?: "N/A"}," +
            "${b2b.invoice.invoiceNumber}," +
            "${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(b2b.invoice.invoiceDate))}," +
            "${String.format(Locale.US, "%.2f", b2b.invoice.taxableAmount)}," +
            "${String.format(Locale.US, "%.2f", b2b.invoice.cgstTotal)}," +
            "${String.format(Locale.US, "%.2f", b2b.invoice.sgstTotal)}," +
            "${String.format(Locale.US, "%.2f", b2b.invoice.igstTotal)}," +
            "${String.format(Locale.US, "%.2f", b2b.invoice.totalAmount)}"
        )
    }
    sb.appendLine()

    sb.appendLine("=== B2C INVOICES ===")
    sb.appendLine("Invoice No,Date,Taxable Value,CGST,SGST,IGST,Total")
    data.b2cInvoices.forEach { b2c ->
        sb.appendLine(
            "${b2c.invoice.invoiceNumber}," +
            "${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(b2c.invoice.invoiceDate))}," +
            "${String.format(Locale.US, "%.2f", b2c.invoice.taxableAmount)}," +
            "${String.format(Locale.US, "%.2f", b2c.invoice.cgstTotal)}," +
            "${String.format(Locale.US, "%.2f", b2c.invoice.sgstTotal)}," +
            "${String.format(Locale.US, "%.2f", b2c.invoice.igstTotal)}," +
            "${String.format(Locale.US, "%.2f", b2c.invoice.totalAmount)}"
        )
    }
    sb.appendLine()

    sb.appendLine("=== HSN SUMMARY ===")
    sb.appendLine("HSN Code,Description,UQC,Quantity,Taxable Value,CGST,SGST,IGST")
    data.hsnSummaries.forEach { hsn ->
        sb.appendLine(
            "${hsn.hsnCode}," +
            "${hsn.description}," +
            "${hsn.uqc}," +
            "${String.format(Locale.US, "%.1f", hsn.totalQuantity)}," +
            "${String.format(Locale.US, "%.2f", hsn.taxableValue)}," +
            "${String.format(Locale.US, "%.2f", hsn.cgstAmount)}," +
            "${String.format(Locale.US, "%.2f", hsn.sgstAmount)}," +
            "${String.format(Locale.US, "%.2f", hsn.igstAmount)}"
        )
    }
    sb.appendLine()

    sb.appendLine("=== SUMMARY ===")
    sb.appendLine("Total Invoices,${data.totalInvoices}")
    sb.appendLine("B2B Count,${data.b2bCount}")
    sb.appendLine("B2C Count,${data.b2cCount}")
    sb.appendLine("Total Taxable Value,${String.format(Locale.US, "%.2f", data.totalTaxableValue)}")
    sb.appendLine("CGST,${String.format(Locale.US, "%.2f", data.totalCgst)}")
    sb.appendLine("SGST,${String.format(Locale.US, "%.2f", data.totalSgst)}")
    sb.appendLine("IGST,${String.format(Locale.US, "%.2f", data.totalIgst)}")
    sb.appendLine("Total Tax,${String.format(Locale.US, "%.2f", data.totalTax)}")
    sb.appendLine("Invoice Value,${String.format(Locale.US, "%.2f", data.totalTaxableValue + data.totalTax)}")

    val fileName = "GSTR1_Report_${String.format("%02d%04d", month + 1, year)}.csv"
    val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GSTR1")
    dir.mkdirs()
    val file = File(dir, fileName)
    file.writeText(sb.toString())
    return file
}

private fun shareFile(context: android.content.Context, file: File, mimeType: String, title: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, uri)
        type = mimeType
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(sendIntent, title))
}
