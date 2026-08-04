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
import com.mimo.gstbilling.ui.viewmodel.SmartInvoiceSuggestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartInvoiceSuggestScreen(navController: NavController, viewModel: SmartInvoiceSuggestViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Smart Invoice Suggestions", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)) }) { padding ->
        if (uiState.isLoading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else if (uiState.suggestions.isEmpty()) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Receipt, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)); Spacer(Modifier.height(12.dp)); Text("No suggestions yet", fontSize = 16.sp, color = TextSecondary); Text("Create more invoices for AI suggestions", fontSize = 12.sp, color = TextSecondary) } } }
        else { LazyColumn(Modifier.fillMaxSize().padding(padding).background(VyaparBackground)) {
            items(uiState.suggestions) { s -> Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).background(VyaparBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.ShoppingCart, null, tint = VyaparBlue, modifier = Modifier.size(20.dp)) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(s.itemName, fontWeight = FontWeight.Medium, fontSize = 14.sp); Text("Ordered ${s.timesOrdered} times | Last: ${s.lastOrderedDays}d ago", fontSize = 12.sp, color = TextSecondary) }; Column(horizontalAlignment = Alignment.End) { Text("Qty: ${s.suggestedQty.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparBlue); Text("₹${String.format("%.0f", s.lastPrice)}", fontSize = 12.sp, color = TextSecondary) } } } }
            item { Spacer(Modifier.height(16.dp)) }
        } }
    }
}
