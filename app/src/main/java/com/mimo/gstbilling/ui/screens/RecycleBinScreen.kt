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
import com.mimo.gstbilling.data.local.entity.DeletedItemEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.RecycleBinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(navController: NavController, viewModel: RecycleBinViewModel = hiltViewModel()) {
    val deletedItems by viewModel.deletedItems.collectAsState()
    val deletedCount by viewModel.deletedCount.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showPermanentDeleteDialog by remember { mutableStateOf<DeletedItemEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Empty Recycle Bin?", fontWeight = FontWeight.Bold) },
            text = { Text("Permanently delete all $deletedCount items? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.emptyAll(); showDeleteAllDialog = false }) { Text("Delete All", color = RedAccent, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDeleteAllDialog = false }) { Text("Cancel") } }
        )
    }

    showPermanentDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showPermanentDeleteDialog = null },
            title = { Text("Permanently Delete?", fontWeight = FontWeight.Bold) },
            text = { Text("${item.entityName} will be permanently deleted. This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.permanentlyDelete(item); showPermanentDeleteDialog = null }) { Text("Delete", color = RedAccent, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showPermanentDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recycle Bin", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        if (deletedItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recycle Bin is empty", fontSize = 16.sp, color = TextSecondary)
                    Text("Deleted items will appear here for 30 days", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("$deletedCount items", fontSize = 14.sp, color = TextSecondary)
                        TextButton(onClick = { showDeleteAllDialog = true }) {
                            Text("Empty All", color = RedAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                items(deletedItems, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(
                                when (item.entityType) { "Invoice" -> Primary.copy(alpha = 0.1f); "Party" -> GreenBalance.copy(alpha = 0.1f); "Order" -> VyaparBlue.copy(alpha = 0.1f); else -> RedAccent.copy(alpha = 0.1f) },
                                RoundedCornerShape(8.dp)
                            ), contentAlignment = Alignment.Center) {
                                Icon(
                                    when (item.entityType) { "Invoice" -> Icons.Filled.Receipt; "Party" -> Icons.Filled.Business; "Order" -> Icons.Filled.ShoppingCart; "Expense" -> Icons.Filled.Receipt; else -> Icons.Filled.Inventory },
                                    contentDescription = null,
                                    tint = when (item.entityType) { "Invoice" -> Primary; "Party" -> GreenBalance; "Order" -> VyaparBlue; "Expense" -> RedAccent; else -> VyaparTextSecondary },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.entityName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("${item.entityType} \u2022 ${dateFormat.format(Date(item.deletedAt))}", fontSize = 11.sp, color = TextSecondary)
                            }
                            if (item.amount > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(String.format(java.util.Locale.US, "\u20B9%,.0f", item.amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            IconButton(onClick = { viewModel.restore(item) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Restore, contentDescription = "Restore", tint = GreenBalance, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { showPermanentDeleteDialog = item }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.DeleteForever, contentDescription = "Permanently Delete", tint = RedAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
