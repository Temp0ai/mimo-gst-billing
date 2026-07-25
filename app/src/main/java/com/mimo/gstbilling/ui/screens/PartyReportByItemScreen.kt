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
fun PartyReportByItemScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    val parties by viewModel.getParties().collectAsState(initial = emptyList())
    var selectedToggle by remember { mutableIntStateOf(0) }
    var dateRangeText by remember { mutableStateOf("All Time") }

    val partyItemsMap = remember(invoices) {
        invoices.groupBy { it.partyId }
    }

    val partyNames = remember(parties) {
        parties.associate { it.id to it.name }
    }

    val totalAmount = invoices.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Party Report by Items", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Party Report by Items")
                            appendLine("Date Range: $dateRangeText")
                            appendLine("Total Amount: ${String.format(Locale.US, "\u20B9%,.2f", totalAmount)}")
                            appendLine("---")
                            partyItemsMap.forEach { (partyId, partyInvoices) ->
                                val name = partyNames[partyId] ?: "Party #$partyId"
                                appendLine("$name | ${partyInvoices.size} invoices | ${String.format(Locale.US, "\u20B9%,.2f", partyInvoices.sumOf { it.totalAmount })}")
                            }
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Party Report by Items", text)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("By Party", "By Item").forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedToggle == index,
                            onClick = { selectedToggle = index },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = VyaparFilterChipBackground,
                                selectedContainerColor = VyaparFilterChipSelected,
                                labelColor = VyaparFilterChipText,
                                selectedLabelColor = VyaparFilterChipSelectedText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = VyaparFilterChipBorder,
                                selectedBorderColor = VyaparFilterChipSelectedBorder
                            )
                        )
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparBlue)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text(String.format(Locale.US, "\u20B9%,.2f", totalAmount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            if (partyItemsMap.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No data available", color = VyaparTextSecondary)
                    }
                }
            }

            if (selectedToggle == 0) {
                items(partyItemsMap.entries.toList()) { (partyId, partyInvoices) ->
                    val partyName = partyNames[partyId] ?: "Party #$partyId"
                    val partyTotal = partyInvoices.sumOf { it.totalAmount }

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
                                    Text(partyName, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                    Text("${partyInvoices.size} invoices", fontSize = 12.sp, color = VyaparTextSecondary)
                                }
                                Text(String.format(Locale.US, "\u20B9%,.2f", partyTotal), fontWeight = FontWeight.Bold, color = VyaparBlue)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = VyaparDivider)
                            partyInvoices.take(3).forEach { inv ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(inv.invoiceNumber, fontSize = 12.sp, color = VyaparTextSecondary)
                                    Text(String.format(Locale.US, "\u20B9%,.2f", inv.totalAmount), fontSize = 12.sp, color = VyaparTextPrimary)
                                }
                            }
                            if (partyInvoices.size > 3) {
                                Text("+${partyInvoices.size - 3} more", fontSize = 11.sp, color = VyaparBlue, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            } else {
                val itemMap = remember(invoices) {
                    invoices.flatMap { inv ->
                        inv.items.map { item -> inv.invoiceNumber to item }
                    }.groupBy { it.second.name }
                }

                items(itemMap.entries.toList()) { (itemName, itemEntries) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(itemName, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                Text("${itemEntries.size} transactions", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
