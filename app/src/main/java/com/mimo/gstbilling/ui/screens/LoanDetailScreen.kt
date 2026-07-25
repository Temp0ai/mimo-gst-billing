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

data class EmiEntry(val month: Int, val amount: Double, val principal: Double, val interest: Double, val balance: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(navController: NavController) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val emiSchedule = remember {
        listOf(
            EmiEntry(1, 22500.0, 15000.0, 7500.0, 1785000.0),
            EmiEntry(2, 22500.0, 15125.0, 7375.0, 1769875.0),
            EmiEntry(3, 22500.0, 15251.0, 7249.0, 1754624.0),
            EmiEntry(4, 22500.0, 15378.0, 7122.0, 1739246.0),
            EmiEntry(5, 22500.0, 15506.0, 6994.0, 1723740.0)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Detail", fontWeight = FontWeight.Bold) },
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
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Home Loan - SBI", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                        Text("Received from SBI Bank", fontSize = 13.sp, color = VyaparTextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Principal", fontSize = 12.sp, color = VyaparTextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", 2500000.0), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("EMI", fontSize = 12.sp, color = VyaparTextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", 22500.0), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparBlue) }
                            Column(horizontalAlignment = Alignment.End) { Text("Outstanding", fontSize = 12.sp, color = VyaparTextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", 1800000.0), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparRed) }
                        }
                    }
                }
            }

            item {
                Text("EMI Schedule", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }

            items(emiSchedule) { emi ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("EMI #${emi.month}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", emi.amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparBlue)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Principal: ${String.format(Locale.US, "\u20B9%,.2f", emi.principal)}", fontSize = 11.sp, color = VyaparTextSecondary)
                            Text("Interest: ${String.format(Locale.US, "\u20B9%,.2f", emi.interest)}", fontSize = 11.sp, color = VyaparTextSecondary)
                        }
                        Text("Balance: ${String.format(Locale.US, "\u20B9%,.2f", emi.balance)}", fontSize = 11.sp, color = VyaparTextSecondary)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
                ) { Text("Record Payment", color = Color.White, fontWeight = FontWeight.Bold) }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
