package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
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
fun ChequeDetailScreen(navController: NavController) {
    var status by remember { mutableStateOf("pending") }
    var showStatusDropdown by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val statuses = listOf("pending", "cleared", "bounced", "cancelled")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cheque Details", fontWeight = FontWeight.Bold) },
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
                colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Reliance Industries", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                            Text("Received Cheque", fontSize = 13.sp, color = VyaparTextSecondary)
                        }
                        AssistChip(
                            onClick = { showStatusDropdown = true },
                            label = { Text(status.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                            shape = RoundedCornerShape(50),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when (status) {
                                    "cleared" -> VyaparGreen.copy(alpha = 0.12f)
                                    "bounced" -> VyaparRed.copy(alpha = 0.12f)
                                    "cancelled" -> VyaparTextSecondary.copy(alpha = 0.12f)
                                    else -> VyaparOrange.copy(alpha = 0.12f)
                                },
                                labelColor = when (status) {
                                    "cleared" -> VyaparGreen
                                    "bounced" -> VyaparRed
                                    "cancelled" -> VyaparTextSecondary
                                    else -> VyaparOrange
                                }
                            )
                        )
                    }
                    HorizontalDivider(color = VyaparDivider)
                    InfoRow("Cheque Number", "CHQ001234")
                    InfoRow("Bank Name", "HDFC Bank")
                    InfoRow("Cheque Date", dateFormat.format(Date()))
                    InfoRow("Amount", String.format(Locale.US, "\u20B9%,.2f", 25000.0))
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Update Status", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                    Box {
                        OutlinedTextField(
                            value = status.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showStatusDropdown = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select", tint = VyaparTextSecondary)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = VyaparDivider,
                                focusedBorderColor = VyaparBlue
                            )
                        )
                        DropdownMenu(expanded = showStatusDropdown, onDismissRequest = { showStatusDropdown = false }) {
                            statuses.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.replaceFirstChar { it.uppercase() }) },
                                    onClick = { status = s; showStatusDropdown = false }
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Linked Transactions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).background(VyaparGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VyaparGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Payment Received", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextPrimary)
                            Text("Auto-adjusted against Invoice #1023", fontSize = 11.sp, color = VyaparTextSecondary)
                        }
                        Text(String.format(Locale.US, "\u20B9%,.2f", 25000.0), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                    }
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
            ) { Text("Save Status", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = VyaparTextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextPrimary)
    }
}
