package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import java.util.*

data class LoanEntry(val id: Long, val name: String, val type: String, val amount: Double, val paidAmount: Double, val date: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanStatementScreen(navController: NavController) {
    var showAddDialog by remember { mutableStateOf(false) }
    var loans by remember { mutableStateOf(mutableListOf(
        LoanEntry(1, "Business Loan - SBI", "given", 500000.0, 125000.0, System.currentTimeMillis() - 86400000L * 90),
        LoanEntry(2, "Personal Loan - Ravi", "taken", 200000.0, 50000.0, System.currentTimeMillis() - 86400000L * 60),
        LoanEntry(3, "Advance to Supplier", "given", 75000.0, 75000.0, System.currentTimeMillis() - 86400000L * 30)
    )) }

    val totalGiven = loans.filter { it.type == "given" }.sumOf { it.amount - it.paidAmount }
    val totalTaken = loans.filter { it.type == "taken" }.sumOf { it.amount - it.paidAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Statement", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Primary) {
                Icon(Icons.Filled.Add, contentDescription = "Add Loan", tint = Color.White)
            }
        }
    ) { padding ->
        if (showAddDialog) {
            var loanName by remember { mutableStateOf("") }
            var loanType by remember { mutableStateOf("given") }
            var loanAmount by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Loan", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(value = loanName, onValueChange = { loanName = it }, label = { Text("Loan Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("given" to "Given (Receivable)", "taken" to "Taken (Payable)").forEach { (type, label) ->
                                val color = if (type == "given") GreenBalance else RedAccent
                                FilterChip(
                                    selected = loanType == type,
                                    onClick = { loanType = type },
                                    label = { Text(label, fontSize = 12.sp, color = if (loanType == type) color else TextSecondary) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = color.copy(alpha = 0.12f),
                                        containerColor = Color.White,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = Color(0xFFE0E0E0),
                                        selectedBorderColor = color.copy(alpha = 0.4f),
                                        enabled = true,
                                        selected = loanType == type
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = loanAmount, onValueChange = { loanAmount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), prefix = { Text("\u20B9 ") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (loanName.isNotBlank() && loanAmount.toDoubleOrNull() != null) {
                            loans = loans.toMutableList().also { it.add(LoanEntry(System.currentTimeMillis(), loanName, loanType, loanAmount.toDouble(), 0.0, System.currentTimeMillis())) }
                            showAddDialog = false
                        }
                    }) { Text("Add") }
                },
                dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Loan Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Amount Given", fontSize = 12.sp, color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", totalGiven), fontWeight = FontWeight.Bold, color = GreenBalance)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Amount Taken", fontSize = 12.sp, color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", totalTaken), fontWeight = FontWeight.Bold, color = RedAccent)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Position", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalGiven - totalTaken), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (totalGiven >= totalTaken) GreenBalance else RedAccent)
                        }
                    }
                }
            }

            item { Text("Loan Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.padding(top = 4.dp)) }

            loans.forEach { loan ->
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (loan.type == "given") Icons.Filled.TrendingUp else Icons.Filled.TrendingDown, contentDescription = null,
                                    tint = if (loan.type == "given") GreenBalance else RedAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(loan.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(if (loan.type == "given") "Receivable" else "Payable", fontSize = 12.sp,
                                        color = if (loan.type == "given") GreenBalance else RedAccent)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total: ${String.format(Locale.US, "\u20B9%,.2f", loan.amount)}", fontSize = 13.sp, color = TextSecondary)
                                Text("Paid: ${String.format(Locale.US, "\u20B9%,.2f", loan.paidAmount)}", fontSize = 13.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val remaining = loan.amount - loan.paidAmount
                            val progress = if (loan.amount > 0) (loan.paidAmount / loan.amount).toFloat() else 0f
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = if (loan.type == "given") GreenBalance else RedAccent, trackColor = LightBlueBg, strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Remaining: ${String.format(Locale.US, "\u20B9%,.2f", remaining)}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
