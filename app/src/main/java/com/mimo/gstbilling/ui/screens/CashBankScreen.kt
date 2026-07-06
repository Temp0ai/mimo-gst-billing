package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.CashBankViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashBankScreen(navController: NavController, viewModel: CashBankViewModel = hiltViewModel()) {
    val transactions by viewModel.transactions.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    var showAddDialog by remember { mutableStateOf(false) }
    var txnType by remember { mutableStateOf("credit") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var txnMode by remember { mutableStateOf("Cash") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cash & Bank", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Transaction", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Cash In", fontSize = 12.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", transactions.filter { it.type == "credit" }.sumOf { it.amount }), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenBalance) }
                            Column(horizontalAlignment = Alignment.End) { Text("Cash Out", fontSize = 12.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", transactions.filter { it.type == "debit" }.sumOf { it.amount }), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedAccent) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Balance", fontSize = 13.sp, color = TextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", transactions.filter { it.type == "credit" }.sumOf { it.amount } - transactions.filter { it.type == "debit" }.sumOf { it.amount }), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BlueHeader)
                        }
                    }
                }
            }
            if (transactions.isNotEmpty()) {
                item {
                    Text("Recent Transactions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
            }
            items(transactions) { txn ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(if (txn.type == "credit") GreenBalance.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(if (txn.type == "credit") Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward, contentDescription = null, tint = if (txn.type == "credit") GreenBalance else RedAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(txn.description ?: txn.mode, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("${txn.mode} \u2022 ${dateFormat.format(Date(txn.date))}", fontSize = 12.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${if (txn.type == "credit") "+" else "-"}${String.format(Locale.US, "\u20B9%,.2f", txn.amount)}", fontWeight = FontWeight.Bold, color = if (txn.type == "credit") GreenBalance else RedAccent, fontSize = 14.sp)
                        }
                    }
                }
            }
            item { if (transactions.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)); Spacer(modifier = Modifier.height(12.dp)); Text("No transactions yet", fontSize = 16.sp, color = TextSecondary, fontWeight = FontWeight.Medium); Spacer(modifier = Modifier.height(4.dp)); Text("Tap + to add a cash transaction", fontSize = 13.sp, color = TextSecondary) } } } }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Transaction", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = txnType == "credit", onClick = { txnType = "credit" }, label = { Text("Cash In") }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenBalance.copy(alpha = 0.15f), selectedLabelColor = GreenBalance))
                        FilterChip(selected = txnType == "debit", onClick = { txnType = "debit" }, label = { Text("Cash Out") }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedAccent.copy(alpha = 0.15f), selectedLabelColor = RedAccent))
                    }
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Cash", "Bank", "UPI", "Cheque").forEach { mode ->
                            FilterChip(selected = txnMode == mode, onClick = { txnMode = mode }, label = { Text(mode, fontSize = 12.sp) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        viewModel.addTransaction(0L, amt, txnType, txnMode, description.ifBlank { null })
                        showAddDialog = false
                        amount = ""
                        description = ""
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}
