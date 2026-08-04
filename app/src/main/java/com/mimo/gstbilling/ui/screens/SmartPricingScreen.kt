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
import com.mimo.gstbilling.ui.viewmodel.SmartPricingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPricingScreen(navController: NavController, viewModel: SmartPricingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Smart Pricing Advisor", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)) }) { padding ->
        if (uiState.isLoading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else if (uiState.suggestions.isEmpty()) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.AttachMoney, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)); Spacer(Modifier.height(12.dp)); Text("No pricing suggestions", fontSize = 16.sp, color = TextSecondary) } } }
        else { LazyColumn(Modifier.fillMaxSize().padding(padding).background(VyaparBackground)) {
            item { Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Revenue Potential", fontSize = 14.sp, color = TextSecondary); Text("₹${String.format("%,.0f", uiState.suggestions.sumOf { it.potentialRevenue })}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VyaparGreen); Text("${uiState.suggestions.size} suggestions", fontSize = 12.sp, color = TextSecondary) } } }
            items(uiState.suggestions) { s -> Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).background(VyaparBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.TrendingUp, null, tint = VyaparBlue, modifier = Modifier.size(20.dp)) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(s.itemName, fontWeight = FontWeight.Medium, fontSize = 14.sp); Text(s.reason, fontSize = 12.sp, color = TextSecondary); Text("Confidence: ${s.confidence}%", fontSize = 11.sp, color = VyaparBlue) }; Column(horizontalAlignment = Alignment.End) { if (s.currentPrice > 0) Text("₹${String.format("%.0f", s.currentPrice)}", fontSize = 12.sp, color = TextSecondary); if (s.suggestedPrice > 0) Text("₹${String.format("%.0f", s.suggestedPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparGreen); Text("+₹${String.format("%.0f", s.potentialRevenue)}", fontSize = 11.sp, color = VyaparGreen) } } } }
            item { Spacer(Modifier.height(16.dp)) }
        } }
    }
}
