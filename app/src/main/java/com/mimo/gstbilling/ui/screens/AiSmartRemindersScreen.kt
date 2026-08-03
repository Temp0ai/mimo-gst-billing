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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.AiSmartRemindersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSmartRemindersScreen(navController: NavController, viewModel: AiSmartRemindersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Overdue Amount", fontSize = 14.sp, color = VyaparTextSecondary)
                        Text(
                            "₹${String.format("%,.2f", uiState.totalOverdueAmount)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = VyaparRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("${uiState.reminderCounts["gentle"] ?: 0}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                                Text("Gentle", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Column {
                                Text("${uiState.reminderCounts["firm"] ?: 0}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA726))
                                Text("Firm", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Column {
                                Text("${uiState.reminderCounts["urgent"] ?: 0}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VyaparRed)
                                Text("Urgent", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Column {
                                Text("${uiState.reminderCounts["final"] ?: 0}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                Text("Final", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.generateReminders() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Reminders", fontWeight = FontWeight.Bold)
                }
            }

            items(uiState.reminders) { reminder ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(reminder.partyName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = when (reminder.reminderType) {
                                    "gentle" -> VyaparGreen.copy(alpha = 0.1f)
                                    "firm" -> Color(0xFFFFA726).copy(alpha = 0.1f)
                                    "urgent" -> VyaparRed.copy(alpha = 0.1f)
                                    "final" -> Color(0xFFD32F2F).copy(alpha = 0.1f)
                                    else -> VyaparBlue.copy(alpha = 0.1f)
                                }
                            ) {
                                Text(
                                    reminder.reminderType.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (reminder.reminderType) {
                                        "gentle" -> VyaparGreen
                                        "firm" -> Color(0xFFFFA726)
                                        "urgent" -> VyaparRed
                                        "final" -> Color(0xFFD32F2F)
                                        else -> VyaparBlue
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Invoice: ${reminder.invoiceNumber}", fontSize = 12.sp, color = VyaparTextSecondary)
                        Text("Amount: ₹${String.format("%,.2f", reminder.amount)} | Overdue: ${reminder.daysOverdue} days", fontSize = 12.sp, color = VyaparTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(reminder.suggestedMessage, fontSize = 13.sp, color = TextPrimary)
                    }
                }
            }

            if (uiState.reminders.isEmpty() && !uiState.isLoading) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VyaparGreen, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No overdue invoices!", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("All payments are on track", fontSize = 14.sp, color = VyaparTextSecondary)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
