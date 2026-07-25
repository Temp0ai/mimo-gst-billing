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
import java.text.SimpleDateFormat
import java.util.*

data class CashTransaction(
    val id: Long,
    val type: String,
    val amount: Double,
    val date: Long,
    val reason: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashInHandDetailScreen(navController: NavController) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val transactions = remember {
        listOf(
            CashTransaction(1, "in", 5000.0, System.currentTimeMillis(), "Opening balance"),
            CashTransaction(2, "out", 1200.0, System.currentTimeMillis() - 86400000, "Office supplies"),
            CashTransaction(3, "in", 3500.0, System.currentTimeMillis() - 172800000, "Cash sale"),
            CashTransaction(4, "out", 800.0, System.currentTimeMillis() - 259200000, "Petty expenses")
        )
    }
    val totalIn = transactions.filter { it.type == "in" }.sumOf { it.amount }
    val totalOut = transactions.filter { it.type == "out" }.sumOf { it.amount }
    val balance = totalIn - totalOut

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cash In Hand", fontWeight = FontWeight.Bold) },
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
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Current Cash Balance", fontSize = 13.sp, color = VyaparTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            String.format(Locale.US, "\u20B9%,.2f", balance),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) VyaparGreen else VyaparRed
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Inward", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text(
                                    String.format(Locale.US, "\u20B9%,.2f", totalIn),
                                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VyaparGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Outward", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text(
                                    String.format(Locale.US, "\u20B9%,.2f", totalOut),
                                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VyaparRed
                                )
                            }
                        }
                    }
                }
            }

            if (transactions.isNotEmpty()) {
                item {
                    Text(
                        "Transactions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = VyaparTextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }

            items(transactions) { txn ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (txn.type == "in") VyaparGreen.copy(alpha = 0.1f) else VyaparRed.copy(alpha = 0.1f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (txn.type == "in") Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                tint = if (txn.type == "in") VyaparGreen else VyaparRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(txn.reason, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, fontSize = 14.sp)
                            Text(
                                "Cash ${if (txn.type == "in") "In" else "Out"} \u2022 ${dateFormat.format(Date(txn.date))}",
                                fontSize = 12.sp,
                                color = VyaparTextSecondary
                            )
                        }
                        Text(
                            "${if (txn.type == "in") "+" else "-"}${String.format(Locale.US, "\u20B9%,.2f", txn.amount)}",
                            fontWeight = FontWeight.Bold,
                            color = if (txn.type == "in") VyaparGreen else VyaparRed,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = VyaparEmptyStateIcon,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No cash transactions yet", fontSize = 16.sp, color = VyaparTextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Transactions will appear here", fontSize = 13.sp, color = VyaparTextSecondary)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
