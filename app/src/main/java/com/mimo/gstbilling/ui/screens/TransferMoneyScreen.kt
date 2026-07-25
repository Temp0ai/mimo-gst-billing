package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferMoneyScreen(navController: NavController) {
    var fromAccount by remember { mutableStateOf("") }
    var toAccount by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var chequeNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val accounts = listOf("Cash in Hand", "SBI Savings", "HDFC Current", "ICICI Business")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Money", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("From Account *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    Box {
                        OutlinedTextField(
                            value = fromAccount,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Select source account", color = VyaparInputHint) },
                            modifier = Modifier.fillMaxWidth().clickable { showFromDropdown = true },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select", tint = VyaparTextSecondary) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = VyaparDivider,
                                focusedBorderColor = VyaparBlue
                            )
                        )
                        DropdownMenu(expanded = showFromDropdown, onDismissRequest = { showFromDropdown = false }) {
                            accounts.forEach { acct ->
                                DropdownMenuItem(
                                    text = { Text(acct) },
                                    onClick = { fromAccount = acct; showFromDropdown = false }
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalDivider(color = VyaparDivider)
                        Card(
                            shape = RoundedCornerShape(50),
                            colors = CardDefaults.cardColors(containerColor = VyaparBlue),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                Icons.Filled.SwapVert,
                                contentDescription = "Swap",
                                tint = Color.White,
                                modifier = Modifier.padding(6.dp).size(20.dp)
                            )
                        }
                    }

                    Text("To Account *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    Box {
                        OutlinedTextField(
                            value = toAccount,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Select destination account", color = VyaparInputHint) },
                            modifier = Modifier.fillMaxWidth().clickable { showToDropdown = true },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select", tint = VyaparTextSecondary) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = VyaparDivider,
                                focusedBorderColor = VyaparBlue
                            )
                        )
                        DropdownMenu(expanded = showToDropdown, onDismissRequest = { showToDropdown = false }) {
                            accounts.filter { it != fromAccount }.forEach { acct ->
                                DropdownMenuItem(
                                    text = { Text(acct) },
                                    onClick = { toAccount = acct; showToDropdown = false }
                                )
                            }
                        }
                    }

                    Text("Amount *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        placeholder = { Text("Enter amount", color = VyaparInputHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = VyaparDivider,
                            focusedBorderColor = VyaparBlue
                        ),
                        leadingIcon = { Text("\u20B9", fontSize = 16.sp, color = VyaparTextPrimary, modifier = Modifier.padding(start = 8.dp)) }
                    )

                    Text("Date", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    OutlinedTextField(
                        value = dateFormat.format(Date(selectedDate)),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Pick date", tint = VyaparBlue) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = VyaparDivider,
                            focusedBorderColor = VyaparBlue
                        )
                    )

                    Text("Cheque Number (Optional)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    OutlinedTextField(
                        value = chequeNumber,
                        onValueChange = { chequeNumber = it },
                        placeholder = { Text("Enter cheque number", color = VyaparInputHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = VyaparDivider,
                            focusedBorderColor = VyaparBlue
                        )
                    )

                    Text("Notes", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Add notes", color = VyaparInputHint) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = VyaparDivider,
                            focusedBorderColor = VyaparBlue
                        )
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparTextSecondary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(border = androidx.compose.foundation.BorderStroke(1.dp, VyaparDivider))
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && fromAccount.isNotBlank() && toAccount.isNotBlank()) {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
                ) { Text("Save Transfer", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK", color = VyaparBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}
