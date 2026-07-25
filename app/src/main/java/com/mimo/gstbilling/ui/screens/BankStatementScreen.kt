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
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.mimo.gstbilling.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class BankTxn(
    val date: Long,
    val description: String,
    val debit: Double?,
    val credit: Double?,
    val balance: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankStatementScreen(navController: NavController) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    var dateRange by remember { mutableStateOf("This Month") }
    val transactions = remember {
        listOf(
            BankTxn(System.currentTimeMillis(), "Opening Balance", null, null, 125000.0),
            BankTxn(System.currentTimeMillis() - 86400000, "Cash Deposit", null, 15000.0, 140000.0),
            BankTxn(System.currentTimeMillis() - 172800000, "Cheque to Reliance", 8500.0, null, 131500.0),
            BankTxn(System.currentTimeMillis() - 259200000, "UPI from Ravi Traders", null, 22000.0, 153500.0),
            BankTxn(System.currentTimeMillis() - 345600000, "EMI Payment", 12500.0, null, 141000.0)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Statement", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val csv = buildString {
                            append("Date,Description,Debit,Credit,Balance\n")
                            transactions.forEach { txn ->
                                append("\"${dateFormat.format(Date(txn.date))}\",\"${txn.description}\",${txn.debit ?: ""},${txn.credit ?: ""},${txn.balance}\n")
                            }
                        }
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(android.content.Intent.EXTRA_TEXT, csv)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Export Bank Statement"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export", tint = VyaparBlue)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("  $dateRange", fontSize = 14.sp, color = VyaparTextPrimary, modifier = Modifier.padding(12.dp))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Filter", tint = VyaparTextSecondary, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Opening Balance", fontSize = 12.sp, color = VyaparTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format(Locale.US, "\u20B9%,.2f", 125000.0), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Closing Balance", fontSize = 12.sp, color = VyaparTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format(Locale.US, "\u20B9%,.2f", 141000.0), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                        }
                    }
                }
            }

            item {
                Text(
                    "Transactions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VyaparTextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            items(transactions) { txn ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(txn.description, fontWeight = FontWeight.Medium, color = VyaparTextPrimary, fontSize = 14.sp)
                            Text(dateFormat.format(Date(txn.date)), fontSize = 12.sp, color = VyaparTextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            txn.debit?.let {
                                Text("-${String.format(Locale.US, "\u20B9%,.2f", it)}", color = VyaparRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            txn.credit?.let {
                                Text("+${String.format(Locale.US, "\u20B9%,.2f", it)}", color = VyaparGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text("Bal: ${String.format(Locale.US, "\u20B9%,.2f", txn.balance)}", fontSize = 11.sp, color = VyaparTextSecondary)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
