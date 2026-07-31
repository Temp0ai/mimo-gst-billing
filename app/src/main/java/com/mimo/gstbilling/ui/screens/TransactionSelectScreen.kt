package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import com.mimo.gstbilling.ui.navigation.Screen
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSelectScreen(
    navController: NavController
) {
    var isSale by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var filterMinAmount by remember { mutableStateOf("") }
    var filterMaxAmount by remember { mutableStateOf("") }
    var filterDateFrom by remember { mutableStateOf("") }
    var filterDateTo by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    data class Transaction(val type: String, val party: String, val amount: Double, val date: String)

    val transactions = listOf(
        Transaction("Sale", "Rajesh Traders", 53100.0, "15 Jul 2026"),
        Transaction("Sale", "Priya Enterprises", 15930.0, "20 Jul 2026"),
        Transaction("Purchase", "Amit Hardware", 33040.0, "18 Jul 2026"),
        Transaction("Sale", "Suresh & Sons", 9440.0, "22 Jul 2026"),
        Transaction("Purchase", "Deepak Steel", 64900.0, "28 Jul 2026"),
        Transaction("Sale", "Vijay Electronics", 41300.0, "25 Jul 2026")
    ).let { list ->
        val filtered = list.filter {
            it.party.contains(searchQuery, ignoreCase = true)
        }
        if (isSale) filtered.filter { it.type == "Sale" } else filtered.filter { it.type == "Purchase" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateInvoice.route) },
                containerColor = Primary,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Transaction", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = isSale, onClick = { isSale = true },
                            label = { Text("Sale", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary, selectedLabelColor = Color.White,
                                containerColor = Color.White, labelColor = TextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(borderColor = Divider, selectedBorderColor = Primary, enabled = true, selected = isSale),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isSale, onClick = { isSale = false },
                            label = { Text("Purchase", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1B5E20), selectedLabelColor = Color.White,
                                containerColor = Color.White, labelColor = TextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(borderColor = Divider, selectedBorderColor = Color(0xFF1B5E20), enabled = true, selected = !isSale),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery, onValueChange = { searchQuery = it },
                        label = { Text("Search transactions") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider),
                        singleLine = true
                    )
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recent Transactions (${transactions.size})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (transactions.isEmpty()) {
                        Text("No transactions found", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                    }
                    transactions.forEach { txn ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(
                                if (txn.type == "Sale") Primary.copy(alpha = 0.1f) else Color(0xFF1B5E20).copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp)
                            ), contentAlignment = Alignment.Center) {
                                Icon(
                                    if (txn.type == "Sale") Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (txn.type == "Sale") Primary else Color(0xFF1B5E20),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(txn.party, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                Text("${txn.type} | ${txn.date}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Text("₹${String.format("%.0f", txn.amount)}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = Divider)
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Transactions", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = filterMinAmount, onValueChange = { filterMinAmount = it },
                        label = { Text("Min Amount") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider)
                    )
                    OutlinedTextField(
                        value = filterMaxAmount, onValueChange = { filterMaxAmount = it },
                        label = { Text("Max Amount") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider)
                    )
                    OutlinedTextField(
                        value = filterDateFrom, onValueChange = { filterDateFrom = it },
                        label = { Text("Date From (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider)
                    )
                    OutlinedTextField(
                        value = filterDateTo, onValueChange = { filterDateTo = it },
                        label = { Text("Date To (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showFilterDialog = false
                    scope.launch { snackbarHostState.showSnackbar("Filters applied") }
                }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    filterMinAmount = ""; filterMaxAmount = ""; filterDateFrom = ""; filterDateTo = ""
                    showFilterDialog = false
                }) { Text("Clear") }
            }
        )
    }
}
