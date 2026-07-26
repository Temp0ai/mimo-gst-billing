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
fun PartyGroupSalePurchaseReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val parties by viewModel.getParties().collectAsState(initial = emptyList())
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    var selectedGroup by remember { mutableStateOf("All") }
    var dateRangeText by remember { mutableStateOf("All Time") }

    val groups = remember(parties) {
        listOf("All") + parties.map { it.partyType }.distinct().filter { it.isNotEmpty() }.sorted()
    }

    val filteredParties = if (selectedGroup == "All") parties else parties.filter { it.partyType == selectedGroup }

    val groupData = remember(filteredParties, invoices) {
        filteredParties.groupBy { it.partyType.ifEmpty { "Ungrouped" } }.mapValues { (_, groupParties) ->
            val partyIds = groupParties.map { it.id }
            val groupInvoices = invoices.filter { it.partyId in partyIds }
            val sales = groupInvoices.filter { it.invoiceType == "sales" }.sumOf { it.totalAmount }
            val purchases = groupInvoices.filter { it.invoiceType == "purchase" }.sumOf { it.totalAmount }
            mapOf("sales" to sales, "purchases" to purchases, "count" to groupParties.size)
        }
    }

    val totalSales = groupData.values.sumOf { (it["sales"] as? Double) ?: 0.0 }
    val totalPurchases = groupData.values.sumOf { (it["purchases"] as? Double) ?: 0.0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sale/Purchase by Party Group", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Sale/Purchase by Party Group Report")
                            appendLine("Date Range: $dateRangeText")
                            appendLine("Total Sales: ${String.format(Locale.US, "\u20B9%,.2f", totalSales)}")
                            appendLine("Total Purchases: ${String.format(Locale.US, "\u20B9%,.2f", totalPurchases)}")
                            appendLine("---")
                            groupData.forEach { (group, data) ->
                                appendLine("$group | Parties: ${data["count"]} | Sales: ${String.format(Locale.US, "\u20B9%,.2f", data["sales"])} | Purchases: ${String.format(Locale.US, "\u20B9%,.2f", data["purchases"])}")
                            }
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Party Group Sale Purchase Report", text)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Group", fontSize = 12.sp, color = VyaparTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            groups.take(4).forEach { group ->
                                val isSelected = selectedGroup == group
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedGroup = group },
                                    label = { Text(group, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = VyaparFilterChipBackground,
                                        selectedContainerColor = VyaparFilterChipSelected,
                                        labelColor = VyaparFilterChipText,
                                        selectedLabelColor = VyaparFilterChipSelectedText
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = VyaparFilterChipBorder,
                                        selectedBorderColor = VyaparFilterChipSelectedBorder,
                                        enabled = true,
                                        selected = isSelected
                                    )
                                )
                            }
                        }
                    }
                }
            }

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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparGreen)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Sales", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format(Locale.US, "\u20B9%,.0f", totalSales), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparRed)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Purchases", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format(Locale.US, "\u20B9%,.0f", totalPurchases), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            items(groupData.entries.toList()) { (groupName, data) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(groupName, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                Text("${data["count"]} parties", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sales", fontSize = 13.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", data["sales"]), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Purchases", fontSize = 13.sp, color = VyaparTextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", data["purchases"]), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparRed)
                        }
                    }
                }
            }

            if (groupData.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No party groups found", color = VyaparTextSecondary)
                    }
                }
            }
        }
    }
}
