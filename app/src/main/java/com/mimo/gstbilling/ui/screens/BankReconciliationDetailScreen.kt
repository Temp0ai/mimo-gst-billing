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
import kotlinx.coroutines.launch

data class ReconciliationTxn(val description: String, val amount: Double, val date: Long, val isMatched: Boolean, val type: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankReconciliationDetailScreen(navController: NavController) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val matchedTxns = remember {
        listOf(
            ReconciliationTxn("Cash Deposit", 15000.0, System.currentTimeMillis(), true, "credit"),
            ReconciliationTxn("Cheque #CHQ001", 8500.0, System.currentTimeMillis() - 86400000, true, "debit"),
            ReconciliationTxn("UPI Payment", 12000.0, System.currentTimeMillis() - 172800000, true, "credit")
        )
    }

    val unmatchedTxns = remember {
        listOf(
            ReconciliationTxn("ATM Withdrawal", 5000.0, System.currentTimeMillis(), false, "debit"),
            ReconciliationTxn("Bank Charges", 350.0, System.currentTimeMillis() - 86400000, false, "debit")
        )
    }

    val displayList = if (selectedTab == 0) matchedTxns else unmatchedTxns
    val matchedCount = matchedTxns.size
    val unmatchedCount = unmatchedTxns.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Reconciliation", fontWeight = FontWeight.Bold) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SBI Savings Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                    Text("A/C: XXXX1234", fontSize = 13.sp, color = VyaparTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = VyaparDivider)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Matched", fontSize = 12.sp, color = VyaparTextSecondary)
                            Text("$matchedCount transactions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Unmatched", fontSize = 12.sp, color = VyaparTextSecondary)
                            Text("$unmatchedCount transactions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparRed)
                        }
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = VyaparWhite,
                contentColor = VyaparTabSelectedText,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = VyaparRed
                    )
                },
                divider = { HorizontalDivider(color = VyaparDivider) }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = {
                    Text("Matched ($matchedCount)", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) VyaparTabSelectedText else VyaparTabText)
                })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = {
                    Text("Unmatched ($unmatchedCount)", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) VyaparTabSelectedText else VyaparTabText)
                })
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(displayList) { txn ->
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
                                        if (txn.isMatched) VyaparGreen.copy(alpha = 0.1f) else VyaparOrange.copy(alpha = 0.1f),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (txn.isMatched) Icons.Filled.CheckCircle else Icons.Filled.HelpOutline,
                                    contentDescription = null,
                                    tint = if (txn.isMatched) VyaparGreen else VyaparOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(txn.description, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, fontSize = 14.sp)
                                Text(dateFormat.format(Date(txn.date)), fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${if (txn.type == "credit") "+" else "-"}${String.format(Locale.US, "\u20B9%,.2f", txn.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (txn.type == "credit") VyaparGreen else VyaparRed,
                                    fontSize = 14.sp
                                )
                                if (!txn.isMatched) {
                                    TextButton(onClick = { scope.launch { snackbarHostState.showSnackbar("Reconciled") } }, contentPadding = PaddingValues(0.dp)) {
                                        Text("Reconcile", fontSize = 11.sp, color = VyaparBlue)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedTab == 1 && unmatchedTxns.isNotEmpty()) {
                Button(
                    onClick = { scope.launch { snackbarHostState.showSnackbar("All reconciled") } },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
                ) { Text("Reconcile All Matched", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
