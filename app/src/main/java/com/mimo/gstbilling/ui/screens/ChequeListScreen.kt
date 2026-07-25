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

data class ChequeItem(
    val id: Long,
    val partyName: String,
    val amount: Double,
    val bankName: String,
    val date: Long,
    val status: String,
    val chequeNumber: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChequeListScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.US) }

    val receivedCheques = remember {
        listOf(
            ChequeItem(1, "Reliance Industries", 25000.0, "HDFC Bank", System.currentTimeMillis(), "pending", "CHQ001"),
            ChequeItem(2, "Tata Steel", 18000.0, "ICICI Bank", System.currentTimeMillis() - 86400000, "cleared", "CHQ002"),
            ChequeItem(3, "Wipro Ltd", 42000.0, "SBI", System.currentTimeMillis() - 172800000, "bounced", "CHQ003")
        )
    }

    val issuedCheques = remember {
        listOf(
            ChequeItem(4, "Vendor A", 12000.0, "SBI", System.currentTimeMillis(), "pending", "CHQ004"),
            ChequeItem(5, "Supplier B", 35000.0, "HDFC Bank", System.currentTimeMillis() - 86400000, "cleared", "CHQ005")
        )
    }

    val displayList = if (selectedTab == 0) receivedCheques else issuedCheques
    val filteredList = displayList.filter {
        it.partyName.contains(searchQuery, ignoreCase = true) || it.chequeNumber.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cheques", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Filled.Add, contentDescription = "Add Cheque", tint = VyaparFABIcon)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search cheques...", color = VyaparInputHint) },
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
                    Text("Received", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) VyaparTabSelectedText else VyaparTabText)
                })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = {
                    Text("Issued", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) VyaparTabSelectedText else VyaparTabText)
                })
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredList) { cheque ->
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
                                    .background(VyaparBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Receipt, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cheque.partyName, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, fontSize = 14.sp)
                                Text("${cheque.bankName} \u2022 ${cheque.chequeNumber}", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text(dateFormat.format(Date(cheque.date)), fontSize = 11.sp, color = VyaparTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(String.format(Locale.US, "\u20B9%,.2f", cheque.amount), fontWeight = FontWeight.Bold, color = VyaparTextPrimary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                AssistChip(
                                    onClick = { },
                                    label = { Text(cheque.status.replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(50),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = when (cheque.status) {
                                            "cleared" -> VyaparGreen.copy(alpha = 0.12f)
                                            "bounced" -> VyaparRed.copy(alpha = 0.12f)
                                            else -> VyaparOrange.copy(alpha = 0.12f)
                                        },
                                        labelColor = when (cheque.status) {
                                            "cleared" -> VyaparGreen
                                            "bounced" -> VyaparRed
                                            else -> VyaparOrange
                                        }
                                    ),
                                    border = AssistChipDefaults.assistChipBorder(
                                        borderColor = when (cheque.status) {
                                            "cleared" -> VyaparGreen.copy(alpha = 0.4f)
                                            "bounced" -> VyaparRed.copy(alpha = 0.4f)
                                            else -> VyaparOrange.copy(alpha = 0.4f)
                                        },
                                        enabled = true
                                    )
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
