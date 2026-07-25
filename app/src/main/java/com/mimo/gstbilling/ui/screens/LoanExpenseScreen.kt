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

data class LoanExpense(val id: Long, val type: String, val amount: Double, val date: Long, val description: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanExpenseScreen(navController: NavController) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val expenses = remember {
        listOf(
            LoanExpense(1, "Processing Fee", 12500.0, System.currentTimeMillis() - 86400000 * 30, "Loan processing charge"),
            LoanExpense(2, "Legal Charges", 5000.0, System.currentTimeMillis() - 86400000 * 25, "Documentation & legal"),
            LoanExpense(3, "Late Payment Penalty", 1500.0, System.currentTimeMillis() - 86400000 * 10, "EMI delayed by 5 days")
        )
    }
    val totalExpenses = expenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Expenses", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = VyaparFABBackground
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Expense", tint = VyaparFABIcon)
            }
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
                    colors = CardDefaults.cardColors(containerColor = VyaparRed),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Total Loan Expenses", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format(Locale.US, "\u20B9%,.2f", totalExpenses), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${expenses.size} expense(s) recorded", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            item {
                Text("Expense List", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }

            items(expenses) { expense ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(VyaparRed.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Receipt, contentDescription = null, tint = VyaparRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.type, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, fontSize = 14.sp)
                            Text(expense.description, fontSize = 12.sp, color = VyaparTextSecondary)
                            Text(dateFormat.format(Date(expense.date)), fontSize = 11.sp, color = VyaparTextSecondary)
                        }
                        Text(String.format(Locale.US, "\u20B9%,.2f", expense.amount), fontWeight = FontWeight.Bold, color = VyaparRed, fontSize = 14.sp)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
