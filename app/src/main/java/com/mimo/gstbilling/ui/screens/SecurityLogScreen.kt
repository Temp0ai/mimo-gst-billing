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
import androidx.compose.ui.platform.LocalContext
import com.mimo.gstbilling.ui.theme.*

data class AuditLogEntry(
    val timestamp: String,
    val user: String,
    val action: String,
    val entity: String,
    val details: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityLogScreen(navController: NavController) {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf("01 Jul 2026") }
    var endDate by remember { mutableStateOf("31 Jul 2026") }
    var selectedActionFilter by remember { mutableStateOf("All") }

    val actionFilters = listOf("All", "Create", "Update", "Delete")

    val auditLogs = remember {
        listOf(
            AuditLogEntry("25 Jul 2026, 10:30 AM", "Admin", "Create", "Invoice", "Created invoice INV-005 for Rahul Enterprises"),
            AuditLogEntry("25 Jul 2026, 09:15 AM", "Admin", "Update", "Party", "Updated phone number for Priya Traders"),
            AuditLogEntry("24 Jul 2026, 04:45 PM", "Staff - Amit", "Create", "Payment", "Recorded payment of \u20B95,000 from Neha Distributors"),
            AuditLogEntry("24 Jul 2026, 02:00 PM", "Admin", "Delete", "Item", "Deleted item 'Old Widget' from inventory"),
            AuditLogEntry("23 Jul 2026, 11:30 AM", "Admin", "Update", "Settings", "Updated invoice prefix to 'INV-'"),
            AuditLogEntry("23 Jul 2026, 10:00 AM", "Staff - Amit", "Create", "Purchase", "Created purchase PUR-003 from Amit & Sons"),
            AuditLogEntry("22 Jul 2026, 03:30 PM", "Admin", "Update", "Tax", "Updated GST rate for category 'Electronics' to 18%"),
            AuditLogEntry("22 Jul 2026, 09:00 AM", "Admin", "Create", "User", "Added new staff member 'Sonia' with view-only access"),
            AuditLogEntry("21 Jul 2026, 05:15 PM", "Staff - Amit", "Update", "Stock", "Adjusted stock for 'Wireless Mouse': 50 -> 45 units"),
            AuditLogEntry("21 Jul 2026, 11:00 AM", "Admin", "Delete", "Invoice", "Cancelled invoice INV-003 (duplicate entry)")
        )
    }

    val filteredLogs = auditLogs.filter {
        selectedActionFilter == "All" || it.action.equals(selectedActionFilter, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Log", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val csv = buildString {
                            append("Timestamp,User,Action,Entity,Details\n")
                            filteredLogs.forEach { log ->
                                append("\"${log.timestamp}\",\"${log.user}\",\"${log.action}\",\"${log.entity}\",\"${log.details}\"\n")
                            }
                        }
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(android.content.Intent.EXTRA_TEXT, csv)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Export Security Log"))
                    }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Export", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Date Range", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("From") },
                                trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("To") },
                                trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actionFilters.forEach { filter ->
                        FilterChip(
                            selected = selectedActionFilter == filter,
                            onClick = { selectedActionFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyaparLightBlue,
                                selectedLabelColor = Primary
                            )
                        )
                    }
                }
            }

            items(filteredLogs) { log ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            when (log.action) {
                                                "Create" -> VyaparSuccessBackground
                                                "Update" -> VyaparInfoBackground
                                                "Delete" -> VyaparErrorBackground
                                                else -> VyaparUnselectedBg
                                            },
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        when (log.action) {
                                            "Create" -> Icons.Filled.Add
                                            "Update" -> Icons.Filled.Edit
                                            "Delete" -> Icons.Filled.Delete
                                            else -> Icons.Filled.Info
                                        },
                                        contentDescription = null,
                                        tint = when (log.action) {
                                            "Create" -> VyaparSuccessText
                                            "Update" -> VyaparInfoText
                                            "Delete" -> VyaparErrorText
                                            else -> TextSecondary
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(log.action, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text("${log.entity} - ${log.user}", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                            Text(log.timestamp, fontSize = 11.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(log.details, fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
