package com.mimo.gstbilling.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailScreen(navController: NavController) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val transactions = remember {
        listOf(
            Triple("Cash Deposit", 25000.0, "credit"),
            Triple("Cheque to Vendor", 8500.0, "debit"),
            Triple("UPI Credit", 12000.0, "credit"),
            Triple("ATM Withdrawal", 5000.0, "debit")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Account Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.SettingsDetail.createRoute("Edit Bank Account")) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = VyaparBlue)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = VyaparRed)
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
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("SBI Savings Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("A/C: XXXX1234", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("IFSC: SBIN0001234", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Current Balance", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        Text(String.format(Locale.US, "\u20B9%,.2f", 141000.0), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Mini Statement", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp).background(VyaparBackground, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chart placeholder", fontSize = 12.sp, color = VyaparTextSecondary)
                        }
                    }
                }
            }

            item {
                Text(
                    "Transaction History",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VyaparTextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            items(transactions) { (desc, amount, type) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (type == "credit") VyaparGreen.copy(alpha = 0.1f) else VyaparRed.copy(alpha = 0.1f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (type == "credit") Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                tint = if (type == "credit") VyaparGreen else VyaparRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(desc, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, fontSize = 14.sp)
                            Text("${dateFormat.format(Date())} \u2022 Bank Transfer", fontSize = 12.sp, color = VyaparTextSecondary)
                        }
                        Text(
                            "${if (type == "credit") "+" else "-"}${String.format(Locale.US, "\u20B9%,.2f", amount)}",
                            fontWeight = FontWeight.Bold,
                            color = if (type == "credit") VyaparGreen else VyaparRed,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Bank Account") },
            text = { Text("Are you sure you want to delete this bank account? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    navController.popBackStack()
                }) {
                    Text("Delete", color = VyaparRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = VyaparTextSecondary)
                }
            }
        )
    }
}
