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
import com.mimo.gstbilling.ui.theme.*

data class TcsTransaction(
    val party: String,
    val invoiceNo: String,
    val taxableAmount: Double,
    val tcsAmount: Double,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TcsReportScreen(navController: NavController) {
    var startDate by remember { mutableStateOf("01 Jul 2026") }
    var endDate by remember { mutableStateOf("31 Jul 2026") }

    val transactions = remember {
        listOf(
            TcsTransaction("Rahul Enterprises", "INV-001", 650000.0, 650.0, "Collected"),
            TcsTransaction("Priya Traders", "INV-002", 720000.0, 720.0, "Collected"),
            TcsTransaction("Amit & Sons", "INV-003", 500000.0, 500.0, "Pending"),
            TcsTransaction("Neha Distributors", "INV-004", 850000.0, 850.0, "Collected"),
            TcsTransaction("Vikram Supply Co.", "INV-005", 450000.0, 450.0, "Pending")
        )
    }

    val totalCollected = transactions.filter { it.status == "Collected" }.sumOf { it.tcsAmount }
    val totalPending = transactions.filter { it.status == "Pending" }.sumOf { it.tcsAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TCS Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Export", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Date Range", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("From") },
                                trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("To") },
                                trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenBalance, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Collected", fontSize = 13.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "\u20B9${String.format("%,.0f", totalCollected)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenBalance
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Pending, contentDescription = null, tint = VyaparStatusOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pending", fontSize = 13.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "\u20B9${String.format("%,.0f", totalPending)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = VyaparStatusOrange
                            )
                        }
                    }
                }
            }

            item {
                Text("Transactions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            }

            items(transactions) { txn ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(txn.party, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (txn.status == "Collected") VyaparSuccessBackground else VyaparWarningBackground,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    txn.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (txn.status == "Collected") VyaparSuccessText else VyaparWarningText
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(txn.invoiceNo, fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Taxable", fontSize = 11.sp, color = TextSecondary)
                                Text("\u20B9${String.format("%,.0f", txn.taxableAmount)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Column {
                                Text("TCS", fontSize = 11.sp, color = TextSecondary)
                                Text("\u20B9${String.format("%,.0f", txn.tcsAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
