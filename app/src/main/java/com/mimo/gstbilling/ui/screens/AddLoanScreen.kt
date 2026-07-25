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
fun AddLoanScreen(navController: NavController) {
    var accountName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var partyName by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf("received") }
    var principalAmount by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var interestType by remember { mutableStateOf("reducing") }
    var tenure by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showInterestDropdown by remember { mutableStateOf(false) }
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Loan", fontWeight = FontWeight.Bold) },
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
                    Text("Loan Type", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = loanType == "received",
                            onClick = { loanType = "received" },
                            label = { Text("Loan Received", color = if (loanType == "received") VyaparGreen else VyaparTextSecondary) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyaparGreen.copy(alpha = 0.12f),
                                containerColor = VyaparWhite,
                                labelColor = VyaparTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = VyaparDivider,
                                selectedBorderColor = VyaparGreen.copy(alpha = 0.4f),
                                enabled = true,
                                selected = loanType == "received"
                            )
                        )
                        FilterChip(
                            selected = loanType == "given",
                            onClick = { loanType = "given" },
                            label = { Text("Loan Given", color = if (loanType == "given") VyaparBlue else VyaparTextSecondary) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyaparBlue.copy(alpha = 0.12f),
                                containerColor = VyaparWhite,
                                labelColor = VyaparTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = VyaparDivider,
                                selectedBorderColor = VyaparBlue.copy(alpha = 0.4f),
                                enabled = true,
                                selected = loanType == "given"
                            )
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    FormField("Account Name *", accountName, { accountName = it }, "e.g. SBI Home Loan")
                    FormField("Account Number", accountNumber, { accountNumber = it }, "Loan account number")
                    FormField("Lender/Borrower Name *", partyName, { partyName = it }, if (loanType == "received") "Lender name" else "Borrower name")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    FormField("Principal Amount *", principalAmount, { principalAmount = it }, "Loan amount", prefix = "\u20B9")
                    FormField("Interest Rate (%)", interestRate, { interestRate = it }, "e.g. 8.5")

                    Text("Interest Type", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    Box {
                        OutlinedTextField(
                            value = interestType.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { IconButton(onClick = { showInterestDropdown = true }) { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = VyaparTextSecondary) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = VyaparDivider, focusedBorderColor = VyaparBlue)
                        )
                        DropdownMenu(expanded = showInterestDropdown, onDismissRequest = { showInterestDropdown = false }) {
                            listOf("fixed", "reducing").forEach { t ->
                                DropdownMenuItem(text = { Text(t.replaceFirstChar { it.uppercase() }) }, onClick = { interestType = t; showInterestDropdown = false })
                            }
                        }
                    }

                    FormField("Tenure (Months)", tenure, { tenure = it }, "Loan tenure in months")

                    Text("Start Date", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    OutlinedTextField(
                        value = dateFormat.format(Date(startDate)),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true },
                        trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Pick date", tint = VyaparBlue) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = VyaparDivider, focusedBorderColor = VyaparBlue)
                    )

                    FormField("Notes", notes, { notes = it }, "Additional notes", minLines = 2)
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
            ) { Text("Save Loan", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { startDate = it }; showStartDatePicker = false }) { Text("OK", color = VyaparBlue) } },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, prefix: String? = null, minLines: Int = 1) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = VyaparInputHint) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = minLines == 1,
            minLines = minLines,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = VyaparDivider, focusedBorderColor = VyaparBlue),
            leadingIcon = prefix?.let { { Text(it, fontSize = 16.sp, color = VyaparTextPrimary, modifier = Modifier.padding(start = 8.dp)) } }
        )
    }
}
