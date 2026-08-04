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
import com.mimo.gstbilling.ui.viewmodel.PaymentPatternViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentPatternScreen(navController: NavController, viewModel: PaymentPatternViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Payment Patterns", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)) }) { padding ->
        if (uiState.isLoading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else if (uiState.patterns.isEmpty()) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.QueryStats, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)); Spacer(Modifier.height(12.dp)); Text("No payment pattern data", fontSize = 16.sp, color = TextSecondary) } } }
        else { LazyColumn(Modifier.fillMaxSize().padding(padding).background(VyaparBackground)) {
            items(uiState.patterns) { p -> val reliabilityColor = when { p.onTimeRate >= 80 -> VyaparGreen; p.onTimeRate >= 60 -> Color(0xFFFFC107); else -> VyaparRed }
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(p.partyName, fontWeight = FontWeight.Bold, fontSize = 15.sp); Surface(shape = RoundedCornerShape(50), color = reliabilityColor.copy(alpha = 0.1f)) { Text("${p.onTimeRate}%", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = reliabilityColor) } }; Spacer(Modifier.height(4.dp)); Text(p.prediction, fontSize = 12.sp, color = TextPrimary); Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Avg: ${p.avgDaysToPay} days", fontSize = 11.sp, color = TextSecondary); Text("Payments: ${p.totalPayments}", fontSize = 11.sp, color = TextSecondary); Text("₹${String.format("%,.0f", p.avgPaymentAmount)}", fontSize = 11.sp, color = TextSecondary) } } } }
            item { Spacer(Modifier.height(16.dp)) }
        } }
    }
}
