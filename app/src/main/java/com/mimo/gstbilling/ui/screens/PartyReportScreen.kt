package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
fun PartyReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val parties by viewModel.getParties().collectAsState(initial = emptyList())
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    var selectedType by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("name") }

    val filteredParties = when (selectedType) {
        "Customer" -> parties.filter { it.partyType == "customer" }
        "Supplier" -> parties.filter { it.partyType == "supplier" }
        else -> parties
    }

    val sortedParties = when (sortBy) {
        "amount" -> filteredParties.sortedByDescending { party ->
            invoices.filter { it.partyId == party.id }.sumOf { it.totalAmount }
        }
        "outstanding" -> filteredParties.sortedByDescending { it.balance }
        else -> filteredParties.sortedBy { it.name }
    }

    val totalAmount = sortedParties.sumOf { party ->
        invoices.filter { it.partyId == party.id }.sumOf { it.totalAmount }
    }
    val totalOutstanding = sortedParties.sumOf { it.balance }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Party Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Party Report")
                            appendLine("Type: $selectedType | Sort: $sortBy")
                            appendLine("Total Parties: ${sortedParties.size}")
                            appendLine("Total Amount: ${String.format(Locale.US, "\u20B9%,.2f", totalAmount)}")
                            appendLine("Total Outstanding: ${String.format(Locale.US, "\u20B9%,.2f", totalOutstanding)}")
                            appendLine("---")
                            sortedParties.forEach { party ->
                                val amt = invoices.filter { it.partyId == party.id }.sumOf { it.totalAmount }
                                appendLine("${party.name} | ${party.partyType} | Amount: ${String.format(Locale.US, "\u20B9%,.2f", amt)} | Balance: ${String.format(Locale.US, "\u20B9%,.2f", party.balance)}")
                            }
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Party Report", text)
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Customer", "Supplier").forEach { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = type },
                            label = { Text(type, fontSize = 12.sp) },
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("name" to "Name", "amount" to "Amount", "outstanding" to "Outstanding").forEach { (key, label) ->
                        val isSelected = sortBy == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { sortBy = key },
                            label = { Text(label, fontSize = 11.sp) },
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
                    colors = CardDefaults.cardColors(containerColor = VyaparBlue)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Party Overview", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Parties", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text("${sortedParties.size}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalAmount), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Outstanding", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalOutstanding), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (sortedParties.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No parties found", color = VyaparTextSecondary)
                    }
                }
            }

            items(sortedParties) { party ->
                val partyInvoices = invoices.filter { it.partyId == party.id }
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
                            Text(party.name, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                            Text(
                                "${party.partyType.replaceFirstChar { it.uppercase() }} | ${partyInvoices.size} transactions",
                                fontSize = 12.sp,
                                color = VyaparTextSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(String.format(Locale.US, "\u20B9%,.2f", partyTotal), fontWeight = FontWeight.Bold, color = VyaparBlue)
                            if (party.balance > 0) {
                                Text(
                                    "Due: ${String.format(Locale.US, "\u20B9%,.2f", party.balance)}",
                                    fontSize = 11.sp,
                                    color = VyaparRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
