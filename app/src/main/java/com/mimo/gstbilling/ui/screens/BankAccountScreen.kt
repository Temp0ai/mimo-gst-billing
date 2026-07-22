package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

data class BankAccount(
    val id: Long,
    val bankName: String,
    val accountNumber: String,
    val ifscCode: String,
    val branch: String,
    val balance: Double,
    val type: String = "Savings"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountScreen(navController: NavController) {
    var showAddDialog by remember { mutableStateOf(false) }
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }

    val accounts = remember {
        mutableStateListOf(
            BankAccount(1, "State Bank of India", "1234567890", "SBIN0001234", "Main Branch", 125000.0),
            BankAccount(2, "HDFC Bank", "0987654321", "HDFC0005678", "City Branch", 85000.0)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Accounts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Account", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RedAccent
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Bank Balance", fontSize = 14.sp, color = TextSecondary)
                        Text(
                            "\u20B9${String.format(java.util.Locale.US, "%,.2f", accounts.sumOf { it.balance })}",
                            fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GreenBalance
                        )
                    }
                }
            }

            items(accounts) { account ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(CircleShape).background(Primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(account.bankName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("A/c: ${account.accountNumber}", fontSize = 13.sp, color = TextSecondary)
                                Text("IFSC: ${account.ifscCode} | ${account.branch}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Divider)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Balance", fontSize = 12.sp, color = TextSecondary)
                                Text("\u20B9${String.format(java.util.Locale.US, "%,.2f", account.balance)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenBalance)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Transfer", fontSize = 12.sp)
                                }
                                OutlinedButton(onClick = { }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                                    Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Statement", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Bank Account", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = bankName, onValueChange = { bankName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Bank Name") }, shape = RoundedCornerShape(16.dp), singleLine = true)
                    OutlinedTextField(value = accountNumber, onValueChange = { accountNumber = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Account Number") }, shape = RoundedCornerShape(16.dp), singleLine = true)
                    OutlinedTextField(value = ifscCode, onValueChange = { ifscCode = it }, modifier = Modifier.fillMaxWidth(), label = { Text("IFSC Code") }, shape = RoundedCornerShape(16.dp), singleLine = true)
                    OutlinedTextField(value = branch, onValueChange = { branch = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Branch") }, shape = RoundedCornerShape(16.dp), singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (bankName.isNotBlank() && accountNumber.isNotBlank()) {
                        accounts.add(BankAccount(System.currentTimeMillis(), bankName, accountNumber, ifscCode, branch, 0.0))
                        bankName = ""; accountNumber = ""; ifscCode = ""; branch = ""
                        showAddDialog = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}
