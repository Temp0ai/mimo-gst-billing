package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

data class TcsRate(
    val id: Long,
    val section: String,
    val ratePercent: Double,
    val minAmount: Double,
    val isActive: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TcsManagementScreen(navController: NavController) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTcs by remember { mutableStateOf<TcsRate?>(null) }

    val tcsRates = remember {
        listOf(
            TcsRate(1, "Section 206C(1H) - Sale of Goods", 0.1, 500000.0, true),
            TcsRate(2, "Section 206C(1G) - Remittance", 5.0, 700000.0, true),
            TcsRate(3, "Section 206C(1) - Timber", 2.5, 0.0, false),
            TcsRate(4, "Section 206C(1F) - Motor Vehicle", 1.0, 1000000.0, true)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TCS Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.SettingsDetail.createRoute("Add TCS Rate")) },
                containerColor = VyaparFABBackground,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add TCS Rate", tint = Color.White)
            }
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
            items(tcsRates) { tcs ->
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
                            Text(tcs.section, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (tcs.isActive) VyaparSuccessBackground else VyaparErrorBackground,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (tcs.isActive) "Active" else "Inactive",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (tcs.isActive) VyaparSuccessText else VyaparErrorText
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Rate", fontSize = 11.sp, color = TextSecondary)
                                Text("${tcs.ratePercent}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Primary)
                            }
                            Column {
                                Text("Min Amount", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    if (tcs.minAmount > 0) "\u20B9${String.format("%,.0f", tcs.minAmount)}" else "No limit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { navController.navigate(Screen.SettingsDetail.createRoute("Edit TCS Rate")) }) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { selectedTcs = tcs; showDeleteDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = RedAccent)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", fontSize = 13.sp, color = RedAccent)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete TCS Rate") },
            text = { Text("Are you sure you want to delete this TCS rate configuration?") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
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
