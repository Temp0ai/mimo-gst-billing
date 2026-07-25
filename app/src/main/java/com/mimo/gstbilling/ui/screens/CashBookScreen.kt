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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CashBookEntry(val description: String, val debit: Double?, val credit: Double?, val balance: Double, val date: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashBookScreen(navController: NavController) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    var dateRange by remember { mutableStateOf("This Month") }
    var showDateFilter by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val dateOptions = listOf("Today", "This Week", "This Month", "Last Month", "This Quarter", "This Year")

    val groupedEntries = remember {
        mapOf(
            "25 Jul 2026" to listOf(
                CashBookEntry("Opening Balance", null, null, 45000.0, System.currentTimeMillis()),
                CashBookEntry("Cash received - Invoice #1045", null, 12000.0, 57000.0, System.currentTimeMillis()),
                CashBookEntry("Office rent paid", 8000.0, null, 49000.0, System.currentTimeMillis())
            ),
            "24 Jul 2026" to listOf(
                CashBookEntry("Stationery purchase", 1500.0, null, 34500.0, System.currentTimeMillis() - 86400000),
                CashBookEntry("Cash sale", null, 6500.0, 36000.0, System.currentTimeMillis() - 86400000)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cash Book", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val csv = buildString {
                            append("Date,Description,Debit,Credit,Balance\n")
                            groupedEntries.forEach { (date, entries) ->
                                entries.forEach { entry ->
                                    append("\"$date\",\"${entry.description}\",${entry.debit ?: ""},${entry.credit ?: ""},${entry.balance}\n")
                                }
                            }
                        }
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(android.content.Intent.EXTRA_TEXT, csv)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Export Cash Book"))
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        modifier = Modifier.fillMaxWidth().clickable { showDateFilter = true },
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
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = VyaparWhite), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Opening Balance", fontSize = 12.sp, color = VyaparTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format(Locale.US, "\u20B9%,.2f", 45000.0), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = VyaparWhite), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Closing Balance", fontSize = 12.sp, color = VyaparTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(String.format(Locale.US, "\u20B9%,.2f", 49000.0), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                        }
                    }
                }
            }

            groupedEntries.forEach { (date, entries) ->
                item {
                    Text(date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
                items(entries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.description, fontWeight = FontWeight.Medium, color = VyaparTextPrimary, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                entry.debit?.let { Text("-${String.format(Locale.US, "\u20B9%,.2f", it)}", color = VyaparRed, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                entry.credit?.let { Text("+${String.format(Locale.US, "\u20B9%,.2f", it)}", color = VyaparGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                Text("Bal: ${String.format(Locale.US, "\u20B9%,.2f", entry.balance)}", fontSize = 10.sp, color = VyaparTextSecondary)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
