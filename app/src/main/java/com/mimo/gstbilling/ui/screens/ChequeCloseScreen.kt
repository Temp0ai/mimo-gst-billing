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
fun ChequeCloseScreen(navController: NavController) {
    var reason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showReasonDropdown by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val reasons = listOf("Cheque lost", "Bank error", "Duplicate entry", "Expired", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Close Cheque", fontWeight = FontWeight.Bold) },
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = VyaparOrange, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Closing a cheque will mark it as cancelled and remove it from pending list.", fontSize = 13.sp, color = VyaparTextSecondary)
                    }
                    HorizontalDivider(color = VyaparDivider)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cheque Number", fontSize = 13.sp, color = VyaparTextSecondary)
                        Text("CHQ001234", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Amount", fontSize = 13.sp, color = VyaparTextSecondary)
                        Text(String.format(Locale.US, "\u20B9%,.2f", 25000.0), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Party", fontSize = 13.sp, color = VyaparTextSecondary)
                        Text("Reliance Industries", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextPrimary)
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
                    Text("Reason for Closing *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                    Box {
                        OutlinedTextField(
                            value = reason,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Select reason", color = VyaparInputHint) },
                            modifier = Modifier.fillMaxWidth().clickable { showReasonDropdown = true },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select", tint = VyaparTextSecondary) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = VyaparDivider,
                                focusedBorderColor = VyaparBlue
                            )
                        )
                        DropdownMenu(expanded = showReasonDropdown, onDismissRequest = { showReasonDropdown = false }) {
                            reasons.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = { reason = r; showReasonDropdown = false }
                                )
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

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparRed),
                enabled = reason.isNotBlank()
            ) { Text("Confirm Close", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = VyaparRed) },
            title = { Text("Close Cheque?", fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone. The cheque will be marked as cancelled.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    navController.popBackStack()
                }) { Text("Yes, Close", color = VyaparRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
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
