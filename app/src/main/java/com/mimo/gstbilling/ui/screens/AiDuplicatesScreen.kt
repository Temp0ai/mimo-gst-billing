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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.AiDuplicatesViewModel
import com.mimo.gstbilling.utils.DuplicateGroup
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDuplicatesScreen(
    navController: NavController,
    viewModel: AiDuplicatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filters = listOf("All", "Invoices", "Parties", "Expenses")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Duplicate Detector",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightBlueBg)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(VyaparBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = VyaparBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Duplicate Summary",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${uiState.totalDuplicates} duplicate groups found",
                                fontSize = 12.sp,
                                color = VyaparTextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryItem(
                            count = uiState.invoiceCount,
                            label = "Invoices",
                            color = VyaparBlue
                        )
                        SummaryItem(
                            count = uiState.partyCount,
                            label = "Parties",
                            color = VyaparGreen
                        )
                        SummaryItem(
                            count = uiState.expenseCount,
                            label = "Expenses",
                            color = VyaparRed
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = when (filter) {
                        "All" -> uiState.selectedFilter == "all"
                        "Invoices" -> uiState.selectedFilter == "invoice"
                        "Parties" -> uiState.selectedFilter == "party"
                        "Expenses" -> uiState.selectedFilter == "expense"
                        else -> false
                    }
                    val chipColor = when (filter) {
                        "Invoices" -> VyaparBlue
                        "Parties" -> VyaparGreen
                        "Expenses" -> VyaparRed
                        else -> TextPrimary
                    }
                    Surface(
                        modifier = Modifier
                            .clickable {
                                val filterKey = when (filter) {
                                    "Invoices" -> "invoice"
                                    "Parties" -> "party"
                                    "Expenses" -> "expense"
                                    else -> "all"
                                }
                                viewModel.setFilter(filterKey)
                            },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) chipColor.copy(alpha = 0.1f) else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.dp, chipColor) else BorderStroke(1.dp, VyaparDivider)
                    ) {
                        Text(
                            text = filter,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) chipColor else VyaparTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.isScanning) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = VyaparBlue
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Scanning for duplicates...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Analyzing your data for potential duplicates",
                                    fontSize = 12.sp,
                                    color = VyaparTextSecondary
                                )
                            }
                        }
                    }
                } else if (!uiState.scanComplete) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = VyaparBlue.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Find Duplicate Entries",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "AI-powered duplicate detection for invoices, parties, and expenses",
                                    fontSize = 13.sp,
                                    color = VyaparTextSecondary
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { viewModel.scanForDuplicates() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VyaparBlue
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scan for Duplicates",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (uiState.filteredGroups.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = VyaparGreen,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Duplicates Found",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your data looks clean! No duplicate entries detected.",
                                    fontSize = 13.sp,
                                    color = VyaparTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.filteredGroups) { group ->
                        DuplicateGroupCard(group = group)
                    }
                }

                if (uiState.scanComplete && uiState.filteredGroups.isNotEmpty()) {
                    item {
                        Button(
                            onClick = { viewModel.scanForDuplicates() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VyaparBlue
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rescan for Duplicates",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = VyaparTextSecondary
        )
    }
}

@Composable
private fun DuplicateGroupCard(group: DuplicateGroup) {
    val typeColor = when (group.type) {
        "invoice" -> VyaparBlue
        "party" -> VyaparGreen
        "expense" -> VyaparRed
        else -> VyaparTextSecondary
    }
    val typeLabel = when (group.type) {
        "invoice" -> "Invoices"
        "party" -> "Parties"
        "expense" -> "Expenses"
        else -> "Unknown"
    }
    val typeIcon = when (group.type) {
        "invoice" -> Icons.Default.Receipt
        "party" -> Icons.Default.People
        "expense" -> Icons.Default.Paid
        else -> Icons.Default.HelpOutline
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = typeColor.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = typeLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = typeColor
                            )
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (group.similarity >= 0.8f) VyaparRed.copy(alpha = 0.1f) else VyaparOrange.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${String.format("%.0f", group.similarity * 100)}% Match",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (group.similarity >= 0.8f) VyaparRed else VyaparOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = VyaparDivider)

            Spacer(modifier = Modifier.height(12.dp))

            group.entries.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(typeColor, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Row {
                            if (entry.amount > 0) {
                                Text(
                                    text = "₹${String.format("%.2f", entry.amount)}",
                                    fontSize = 12.sp,
                                    color = VyaparTextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (entry.date > 0) {
                                Text(
                                    text = dateFormat.format(Date(entry.date)),
                                    fontSize = 12.sp,
                                    color = VyaparTextSecondary
                                )
                            }
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = typeColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${String.format("%.0f", group.similarity * 100)}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = typeColor
                        )
                    }
                }
                if (index < group.entries.lastIndex) {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}
