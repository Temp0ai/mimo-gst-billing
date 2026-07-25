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
import java.util.*

data class LoanAccount(
    val id: Long,
    val name: String,
    val type: String,
    val principal: Double,
    val outstanding: Double,
    val emi: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanAccountsListScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val loans = remember {
        listOf(
            LoanAccount(1, "Home Loan - SBI", "received", 2500000.0, 1800000.0, 22500.0),
            LoanAccount(2, "Personal Loan to Ravi", "given", 300000.0, 180000.0, 12000.0),
            LoanAccount(3, "Business Loan - HDFC", "received", 500000.0, 420000.0, 18000.0),
            LoanAccount(4, "Loan to Supplier", "given", 150000.0, 75000.0, 8000.0)
        )
    }
    val filteredLoans = loans.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val totalOutstanding = loans.sumOf { it.outstanding }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Accounts", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Filled.Add, contentDescription = "Add Loan", tint = VyaparFABIcon)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search loans...", color = VyaparInputHint) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = VyaparIconDefault) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = VyaparSearchBackground,
                            focusedContainerColor = VyaparSearchBackground
                        )
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparRed),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Total Outstanding", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            String.format(Locale.US, "\u20B9%,.2f", totalOutstanding),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            items(filteredLoans) { loan ->
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
                                .background(
                                    if (loan.type == "given") VyaparBlue.copy(alpha = 0.1f) else VyaparOrange.copy(alpha = 0.1f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (loan.type == "given") Icons.Filled.TrendingDown else Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (loan.type == "given") VyaparBlue else VyaparOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(loan.name, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, fontSize = 14.sp)
                            Text(
                                "${loan.type.replaceFirstChar { it.uppercase() }} \u2022 EMI: ${String.format(Locale.US, "\u20B9%,.2f", loan.emi)}",
                                fontSize = 12.sp,
                                color = VyaparTextSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(String.format(Locale.US, "\u20B9%,.2f", loan.outstanding), fontWeight = FontWeight.Bold, color = VyaparTextPrimary, fontSize = 14.sp)
                            Text("of ${String.format(Locale.US, "\u20B9%,.2f", loan.principal)}", fontSize = 11.sp, color = VyaparTextSecondary)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
