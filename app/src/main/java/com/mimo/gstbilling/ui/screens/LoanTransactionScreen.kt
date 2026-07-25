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
fun LoanTransactionScreen(navController: NavController) {
    var amount by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("emi") }
    var notes by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val paymentTypes = listOf("emi", "prepayment", "penalty")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Payment", fontWeight = FontWeight.Bold) },
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Home Loan - SBI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Outstanding", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.2f", 1800000.0), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) { Text("EMI", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f)); Text(String.format(Locale.US, "\u20B9%,.2f", 22500.0), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White) }
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
                    Text("Amount *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        placeholder = { Text("Enter payment amount", color = VyaparInputHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = VyaparDivider, focusedBorderColor = VyaparBlue),
                        leadingIcon = { Text("\u20B9", fontSize = 16.sp, color = VyaparTextPrimary, modifier = Modifier.padding(start = 8.dp)) }
                    )

                    Text("Payment Type *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    Box {
                        OutlinedTextField(
                            value = paymentType.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { IconButton(onClick = { showTypeDropdown = true }) { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = VyaparTextSecondary) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = VyaparDivider, focusedBorderColor = VyaparBlue)
                        )
                        DropdownMenu(expanded = showTypeDropdown, onDismissRequest = { showTypeDropdown = false }) {
                            paymentTypes.forEach { t ->
                                DropdownMenuItem(text = { Text(t.replaceFirstChar { it.uppercase() }) }, onClick = { paymentType = t; showTypeDropdown = false })
                            }
                        }
                    }

                    Text("Date *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    OutlinedTextField(
                        value = dateFormat.format(Date(selectedDate)),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Pick date", tint = VyaparBlue) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = VyaparDivider, focusedBorderColor = VyaparBlue)
                    )

                    Text("Notes", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Add notes", color = VyaparInputHint) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = VyaparDivider, focusedBorderColor = VyaparBlue)
                    )
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
            ) { Text("Save Payment", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { selectedDate = it }; showDatePicker = false }) { Text("OK", color = VyaparBlue) } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}
