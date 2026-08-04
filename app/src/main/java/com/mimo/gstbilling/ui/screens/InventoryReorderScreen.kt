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
import com.mimo.gstbilling.ui.viewmodel.InventoryReorderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryReorderScreen(navController: NavController, viewModel: InventoryReorderViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Inventory Reorder", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)) }) { padding ->
        if (uiState.isLoading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else if (uiState.alerts.isEmpty()) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Inventory, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)); Spacer(Modifier.height(12.dp)); Text("No reorder needed", fontSize = 16.sp, color = TextSecondary) } } }
        else { LazyColumn(Modifier.fillMaxSize().padding(padding).background(VyaparBackground)) {
            item { Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Items Needing Reorder", fontSize = 14.sp, color = TextSecondary); Text("${uiState.alerts.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VyaparRed); Text("Total reorder value: ₹${String.format("%,.0f", uiState.alerts.sumOf { it.reorderValue })}", fontSize = 12.sp, color = TextSecondary) } } }
            items(uiState.alerts) { a -> val urgencyColor = when(a.urgency) { "CRITICAL" -> VyaparRed; "HIGH" -> Color(0xFFFF8A00); "MEDIUM" -> Color(0xFFFFC107); else -> VyaparGreen }
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).background(urgencyColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text(a.urgency.first().toString(), fontWeight = FontWeight.Bold, color = urgencyColor, fontSize = 14.sp) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(a.itemName, fontWeight = FontWeight.Medium, fontSize = 14.sp); Text("Stock: ${a.currentStock.toInt()} | Sales: ${a.avgDailySales}/day", fontSize = 12.sp, color = TextSecondary); Text("Days until stockout: ${a.daysUntilStockout}", fontSize = 12.sp, color = if (a.daysUntilStockout <= 7) VyaparRed else TextSecondary) }; Column(horizontalAlignment = Alignment.End) { Text("Reorder: ${a.suggestedReorderQty.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparBlue); Text("₹${String.format("%,.0f", a.reorderValue)}", fontSize = 11.sp, color = TextSecondary) } } } }
            item { Spacer(Modifier.height(16.dp)) }
        } }
    }
}
