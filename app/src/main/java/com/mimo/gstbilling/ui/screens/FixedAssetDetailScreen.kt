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
import com.mimo.gstbilling.ui.theme.*

data class DepreciationEntry(
    val year: String,
    val openingValue: Double,
    val depreciation: Double,
    val closingValue: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedAssetDetailScreen(navController: NavController, assetId: Long) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val assetName = when (assetId) {
        1L -> "Office Laptop"
        2L -> "Air Conditioner"
        3L -> "Printer"
        4L -> "Office Desk"
        else -> "Asset #$assetId"
    }
    val category = when (assetId) {
        1L, 3L -> "Electronics"
        2L -> "Appliances"
        4L -> "Furniture"
        else -> "General"
    }

    val depreciationSchedule = remember {
        listOf(
            DepreciationEntry("Year 1", 65000.0, 13000.0, 52000.0),
            DepreciationEntry("Year 2", 52000.0, 10400.0, 41600.0),
            DepreciationEntry("Year 3", 41600.0, 8320.0, 33280.0),
            DepreciationEntry("Year 4", 33280.0, 6656.0, 26624.0),
            DepreciationEntry("Year 5", 26624.0, 5324.8, 21299.2)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asset Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Primary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(LightBlueBg, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Inventory2, contentDescription = null, tint = Primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(assetName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                Text(category, fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Purchase Date", fontSize = 12.sp, color = TextSecondary)
                                Text("15 Jan 2023", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                            Column {
                                Text("Purchase Price", fontSize = 12.sp, color = TextSecondary)
                                Text("\u20B965,000", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Depreciation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Method", fontSize = 12.sp, color = TextSecondary)
                                Text("SLM", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                            Column {
                                Text("Useful Life", fontSize = 12.sp, color = TextSecondary)
                                Text("5 Years", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                            Column {
                                Text("Salvage Value", fontSize = 12.sp, color = TextSecondary)
                                Text("\u20B95,000", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Current Value", fontSize = 12.sp, color = TextSecondary)
                                Text("\u20B948,750", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenBalance)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Depreciation", fontSize = 12.sp, color = TextSecondary)
                                Text("\u20B916,250", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RedAccent)
                            }
                        }
                    }
                }
            }

            item {
                Text("Depreciation Schedule", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            }

            items(depreciationSchedule) { entry ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(entry.year, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Opening", fontSize = 11.sp, color = TextSecondary)
                                Text("\u20B9${String.format("%,.0f", entry.openingValue)}", fontSize = 13.sp, color = TextPrimary)
                            }
                            Column {
                                Text("Depreciation", fontSize = 11.sp, color = TextSecondary)
                                Text("\u20B9${String.format("%,.0f", entry.depreciation)}", fontSize = 13.sp, color = RedAccent)
                            }
                            Column {
                                Text("Closing", fontSize = 11.sp, color = TextSecondary)
                                Text("\u20B9${String.format("%,.0f", entry.closingValue)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GreenBalance)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Asset") },
            text = { Text("Are you sure you want to delete this asset? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    navController.popBackStack()
                }) {
                    Text("Delete", color = RedAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
