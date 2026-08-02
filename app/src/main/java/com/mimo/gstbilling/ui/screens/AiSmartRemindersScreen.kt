package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.AiSmartRemindersViewModel

data class Reminder(
    val id: String,
    val partyName: String,
    val invoiceNumber: String,
    val amount: Double,
    val daysOverdue: Int,
    val reminderType: String,
    val suggestedMessage: String,
    val priority: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSmartRemindersScreen(
    navController: NavController,
    viewModel: AiSmartRemindersViewModel = hiltViewModel()
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Gentle", "Firm", "Urgent", "Final")

    val reminders by viewModel.reminders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val totalOverdueAmount by viewModel.totalOverdueAmount.collectAsState()
    val reminderCounts by viewModel.reminderCounts.collectAsState()

    val filteredReminders = if (selectedFilter == "All") {
        reminders.sortedBy { it.priority }
    } else {
        reminders.filter { it.reminderType.equals(selectedFilter, ignoreCase = true) }
            .sortedBy { it.priority }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Smart Reminders",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = LightBlueBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                SummaryCard(
                    totalOverdueAmount = totalOverdueAmount,
                    reminderCounts = reminderCounts
                )
            }

            item {
                GenerateRemindersButton(
                    isLoading = isLoading,
                    onClick = { viewModel.generateReminders() }
                )
            }

            item {
                FilterChips(
                    filters = filters,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            if (filteredReminders.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(filteredReminders, key = { it.id }) { reminder ->
                    ReminderCard(reminder = reminder)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalOverdueAmount: Double,
    reminderCounts: Map<String, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Overdue",
                    fontSize = 14.sp,
                    color = VyaparTextSecondary
                )
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = VyaparRed,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹%.2f".format(totalOverdueAmount),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = VyaparRed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = LightBlueBg, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReminderCountItem(
                    count = reminderCounts["Gentle"] ?: 0,
                    label = "Gentle",
                    color = VyaparGreen
                )
                ReminderCountItem(
                    count = reminderCounts["Firm"] ?: 0,
                    label = "Firm",
                    color = Color(0xFFFFA726)
                )
                ReminderCountItem(
                    count = reminderCounts["Urgent"] ?: 0,
                    label = "Urgent",
                    color = VyaparRed
                )
                ReminderCountItem(
                    count = reminderCounts["Final"] ?: 0,
                    label = "Final",
                    color = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
private fun ReminderCountItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = VyaparTextSecondary
        )
    }
}

@Composable
private fun GenerateRemindersButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = VyaparBlue,
            disabledContainerColor = VyaparBlue.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isLoading) "Generating..." else "Generate Reminders",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun FilterChips(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            val chipColor = when (filter) {
                "Gentle" -> VyaparGreen
                "Firm" -> Color(0xFFFFA726)
                "Urgent" -> VyaparRed
                "Final" -> Color(0xFFD32F2F)
                else -> VyaparBlue
            }

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = chipColor.copy(alpha = 0.15f),
                    labelColor = VyaparTextSecondary,
                    selectedLabelColor = chipColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.LightGray,
                    selectedBorderColor = chipColor,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

@Composable
private fun ReminderCard(reminder: Reminder) {
    val typeColor = when (reminder.reminderType.lowercase()) {
        "gentle" -> VyaparGreen
        "firm" -> Color(0xFFFFA726)
        "urgent" -> VyaparRed
        "final" -> Color(0xFFD32F2F)
        else -> VyaparBlue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.partyName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Invoice: ${reminder.invoiceNumber}",
                        fontSize = 13.sp,
                        color = VyaparTextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeColor.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = reminder.reminderType.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = typeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount",
                        fontSize = 12.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        text = "₹%.2f".format(reminder.amount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VyaparRed
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Days Overdue",
                        fontSize = 12.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        text = "${reminder.daysOverdue} days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.daysOverdue > 60) Color(0xFFD32F2F) else Color(0xFFFFA726)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = LightBlueBg, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.ChatBubbleOutline,
                    contentDescription = null,
                    tint = VyaparBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = reminder.suggestedMessage,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VyaparBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Send", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VyaparGreen
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Edit", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = null,
                tint = VyaparTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No reminders found",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap 'Generate Reminders' to create AI-powered payment reminders",
                fontSize = 14.sp,
                color = VyaparTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
