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
fun ExpenseCategoryReportScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val expenses by viewModel.getExpenses().collectAsState(initial = emptyList())
    var dateRangeText by remember { mutableStateOf("All Time") }

    val totalExpenses = expenses.sumOf { it.amount }
    val categories = expenses.groupBy { it.category }
        .mapValues { (_, catExpenses) -> catExpenses.sumOf { it.amount } }
        .entries.sortedByDescending { it.value }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Category Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("Expense Category Report")
                            appendLine("Date Range: $dateRangeText")
                            appendLine("Total Expenses: ${String.format(Locale.US, "\u20B9%,.2f", totalExpenses)}")
                            appendLine("Categories: ${categories.size}")
                            appendLine("---")
                            categories.forEach { (cat, amt) ->
                                val pct = if (totalExpenses > 0) (amt / totalExpenses * 100) else 0.0
                                appendLine("$cat: ${String.format(Locale.US, "\u20B9%,.2f", amt)} (${String.format(Locale.US, "%.1f", pct)}%)")
                            }
                        }
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Expense Category Report", text)
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
                    colors = CardDefaults.cardColors(containerColor = VyaparRed)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Total Expenses", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            String.format(Locale.US, "\u20B9%,.2f", totalExpenses),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${categories.size} categories | ${expenses.size} transactions",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (categories.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No expenses recorded", color = VyaparTextSecondary)
                    }
                }
            }

            items(categories) { (category, amount) ->
                val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100) else 0.0
                val catExpenses = expenses.filter { it.category == category }

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
                            Text(category, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", amount), fontWeight = FontWeight.Bold, color = VyaparRed)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (percentage / 100).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = VyaparRed,
                            trackColor = VyaparRed.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${catExpenses.size} transactions | ${String.format(Locale.US, "%.1f", percentage)}% of total",
                            fontSize = 12.sp,
                            color = VyaparTextSecondary
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL", fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                        Text(String.format(Locale.US, "\u20B9%,.2f", totalExpenses), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VyaparRed)
                    }
                }
            }
        }
    }
}
