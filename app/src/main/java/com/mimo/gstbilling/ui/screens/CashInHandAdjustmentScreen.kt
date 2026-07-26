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
fun CashInHandAdjustmentScreen(navController: NavController) {
    var adjustmentType by remember { mutableStateOf("in") }
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cash In/Out Adjustment", fontWeight = FontWeight.Bold) },
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
            Text("Adjustment Type", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = adjustmentType == "in",
                    onClick = { adjustmentType = "in" },
                    label = { Text("Cash In", color = if (adjustmentType == "in") VyaparGreen else VyaparTextSecondary) },
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
                        selected = adjustmentType == "in"
                    )
                )
                FilterChip(
                    selected = adjustmentType == "out",
                    onClick = { adjustmentType = "out" },
                    label = { Text("Cash Out", color = if (adjustmentType == "out") VyaparRed else VyaparTextSecondary) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VyaparRed.copy(alpha = 0.12f),
                        containerColor = VyaparWhite,
                        labelColor = VyaparTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = VyaparDivider,
                        selectedBorderColor = VyaparRed.copy(alpha = 0.4f),
                        enabled = true,
                        selected = adjustmentType == "out"
                    )
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
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

                    Text("Remarks / Notes", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text("Enter reason for adjustment", color = VyaparInputHint) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = VyaparDivider,
                            focusedBorderColor = VyaparBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparTextSecondary)
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (adjustmentType == "in") VyaparGreen else VyaparRed)
                ) { Text("Save", color = Color.White, fontWeight = FontWeight.Bold) }
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
