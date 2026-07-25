package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalePurchaseAmountReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }
    var dateRangeText by remember { mutableStateOf("All Time") }

    val salesInvoices = invoices.filter { it.invoiceType == "sales" }
    val purchaseInvoices = invoices.filter { it.invoiceType == "purchase" }

    val totalSales = salesInvoices.sumOf { it.totalAmount }
    val totalPurchases = purchaseInvoices.sumOf { it.totalAmount }
    val totalSalesTax = salesInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }
    val totalPurchaseTax = purchaseInvoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal }

    val activeInvoices = if (selectedTab == 0) salesInvoices else purchaseInvoices
    val activeTotal = if (selectedTab == 0) totalSales else totalPurchases
    val activeTax = if (selectedTab == 0) totalSalesTax else totalPurchaseTax

    val groupedByParty = activeInvoices.groupBy { it.partyId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sale/Purchase Amount Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Sale/Purchase Amount Report")
                            appendLine("Date Range: $dateRangeText")
                            appendLine("Total Sales: ${String.format(Locale.US, "\u20B9%,.2f", totalSales)}")
                            appendLine("Total Purchases: ${String.format(Locale.US, "\u20B9%,.2f", totalPurchases)}")
                            appendLine("Net: ${String.format(Locale.US, "\u20B9%,.2f", totalSales - totalPurchases)}")
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Sale/Purchase Amount Report", text)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Report exported to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export", tint = Color.White)
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = VyaparWhite,
                contentColor = VyaparTextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = VyaparBlue
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Sale", modifier = Modifier.padding(12.dp), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 0) VyaparBlue else VyaparTextSecondary)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Purchase", modifier = Modifier.padding(12.dp), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 1) VyaparBlue else VyaparTextSecondary)
                }
            }

            LazyColumn(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Date Range", fontSize = 14.sp, color = VyaparTextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DateRange, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(dateRangeText, fontSize = 13.sp, color = VyaparBlue, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparBlue)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Total Amount", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format(Locale.US, "\u20B9%,.2f", activeTotal), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Tax", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text(String.format(Locale.US, "\u20B9%,.2f", activeTax), color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Transactions", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text("${activeInvoices.size}", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Text("Party-wise Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparTextPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
                }

                if (groupedByParty.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No ${if (selectedTab == 0) "sales" else "purchases"} found", color = VyaparTextSecondary)
                        }
                    }
                }

                items(groupedByParty.entries.toList()) { (partyId, partyInvoices) ->
                    val partyTotal = partyInvoices.sumOf { it.totalAmount }
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Party #$partyId", fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                Text("${partyInvoices.size} invoices", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Text(
                                String.format(Locale.US, "\u20B9%,.2f", partyTotal),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (selectedTab == 0) VyaparGreen else VyaparRed
                            )
                        }
                    }
                }
            }
        }
    }
}
